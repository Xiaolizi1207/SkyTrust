// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/utils/Strings.sol";
import "@openzeppelin/contracts/utils/Base64.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";

/// @title DronePassportNFT
/// @notice ERC-721 token representing a drone passport with on-chain metadata and repair history
contract DronePassportNFT is ERC721, AccessControl {
    using Strings for uint256;

    bytes32 public constant MAINTENANCE_ROLE = keccak256("MAINTENANCE_ROLE");

    // MULTI-SIG for maintenance: 2-of-N approvals
    uint256 public constant REQUIRED_APPROVALS = 2;
    // proposalId => approval count
    mapping(bytes32 => uint256) private approvalCount;
    // proposalId => (approver => hasApproved)
    mapping(bytes32 => mapping(address => bool)) private hasApproved;
    // Repair proposal data structure
    struct RepairProposal {
        uint256 tokenId;
        bytes32 repairDataHash;
        bytes32 maintenanceProviderDID;
        address proposer;
        uint256 timestamp;
        bool executed;
    }
    // proposalId => RepairProposal
    mapping(bytes32 => RepairProposal) private repairProposals;

    // Nonces for each token to protect against replay in onboard-signed updates
    mapping(uint256 => uint256) public nonces;
    // Onboard signer address (admin controllable)
    address public onboardSigner;

    struct DroneAttributes {
        string manufacturer;
        string manufactureDate;
        string serialNumber;
        string firmwareVersion;
        uint256 batteryCycles;
        uint256 repairCount;
    }

    struct RepairRecord {
        bytes32 repairDataHash;
        bytes32 maintenanceProviderDID;
        uint256 timestamp;
    }

    // tokenId => attributes
    mapping(uint256 => DroneAttributes) private droneAttributes;
    // tokenId => repair history
    mapping(uint256 => RepairRecord[]) private repairHistory;

    event RepairRecordAdded(uint256 indexed tokenId, bytes32 repairDataHash, bytes32 maintenanceProviderDID, uint256 timestamp);
    event RepairProposed(bytes32 indexed proposalId, uint256 indexed tokenId, bytes32 repairDataHash, bytes32 maintenanceProviderDID, address proposer, uint256 timestamp);
    event FirmwareUpdated(uint256 indexed tokenId, string version, uint256 timestamp);
    event BatteryCyclesUpdated(uint256 indexed tokenId, uint256 cycles, uint256 nonce, address signer);

    constructor() ERC721("DronePassport", "DPASS") {
        _setupRole(DEFAULT_ADMIN_ROLE, msg.sender);
    }

    /// @notice Mint a new DronePassport NFT with initial manufacturer data
    /// @param to Recipient address
    /// @param tokenId Token identifier
    /// @param manufacturer Manufacturer name
    /// @param manufactureDate Manufacture date string
    /// @param serialNumber Serial/UDI number
    function mintDronePassport(
        address to,
        uint256 tokenId,
        string memory manufacturer,
        string memory manufactureDate,
        string memory serialNumber
    ) external onlyRole(DEFAULT_ADMIN_ROLE) {
        DroneAttributes memory attrs = DroneAttributes({
            manufacturer: manufacturer,
            manufactureDate: manufactureDate,
            serialNumber: serialNumber,
            firmwareVersion: "",
            batteryCycles: 0,
            repairCount: 0
        });
        droneAttributes[tokenId] = attrs;
        _safeMint(to, tokenId);
    }

    /// @notice Propose a repair record for a drone (MAINTENANCE_ROLE required)
    /// @param tokenId Drone token identifier
    /// @param repairDataHash Hash of repair data to attach to the record
    /// @param maintenanceProviderDID DID of the maintenance provider
    /// @return proposalId Unique identifier for the repair proposal
    function proposeRepairRecord(uint256 tokenId, bytes32 repairDataHash, bytes32 maintenanceProviderDID) external onlyRole(MAINTENANCE_ROLE) returns (bytes32) {
        require(_exists(tokenId), "DronePassportNFT: nonexistent token");
        // Create a unique proposalId
        bytes32 proposalId = keccak256(abi.encodePacked(tokenId, repairDataHash, maintenanceProviderDID, msg.sender, block.timestamp));
        RepairProposal memory p = RepairProposal({
            tokenId: tokenId,
            repairDataHash: repairDataHash,
            maintenanceProviderDID: maintenanceProviderDID,
            proposer: msg.sender,
            timestamp: block.timestamp,
            executed: false
        });
        repairProposals[proposalId] = p;
        // Proposer automatically approves their own proposal
        approvalCount[proposalId] = 1;
        hasApproved[proposalId][msg.sender] = true;
        emit RepairProposed(proposalId, tokenId, repairDataHash, maintenanceProviderDID, msg.sender, block.timestamp);
        return proposalId;
    }

    /// @notice Approve a pending repair record proposal (MAINTENANCE_ROLE required)
    /// @param proposalId The unique identifier of the repair proposal
    function approveRepairRecord(bytes32 proposalId) external onlyRole(MAINTENANCE_ROLE) {
        RepairProposal storage p = repairProposals[proposalId];
        require(p.proposer != address(0), "DronePassportNFT: invalid proposal");
        require(_exists(p.tokenId), "DronePassportNFT: nonexistent token");
        require(!p.executed, "DronePassportNFT: proposal already executed");
        require(!hasApproved[proposalId][msg.sender], "DronePassportNFT: already approved");

        // Mark approval
        hasApproved[proposalId][msg.sender] = true;
        approvalCount[proposalId] += 1;

        // If threshold reached, execute actual repair record creation
        if (approvalCount[proposalId] >= REQUIRED_APPROVALS) {
            // Create the actual repair record now
            RepairRecord memory rec = RepairRecord({
                repairDataHash: p.repairDataHash,
                maintenanceProviderDID: p.maintenanceProviderDID,
                timestamp: block.timestamp
            });
            repairHistory[p.tokenId].push(rec);
            droneAttributes[p.tokenId].repairCount += 1;
            p.executed = true;
            emit RepairRecordAdded(p.tokenId, p.repairDataHash, p.maintenanceProviderDID, block.timestamp);
        }
    }

    /// @notice Update the firmware version for a drone (MAINTENANCE_ROLE required)
    /// @param tokenId Drone token identifier
    /// @param version New firmware version string
    function updateFirmware(uint256 tokenId, string memory version) external onlyRole(MAINTENANCE_ROLE) {
        require(_exists(tokenId), "DronePassportNFT: nonexistent token");
        droneAttributes[tokenId].firmwareVersion = version;
        emit FirmwareUpdated(tokenId, version, block.timestamp);
    }

    /// @notice Update the battery cycles for a drone with onboard-signed authorization
    /// @param tokenId Drone token identifier
    /// @param cycles New battery cycle count
    /// @param signature Ethereum signed message from onboard signer
    function updateBatteryCyclesSigned(uint256 tokenId, uint256 cycles, bytes calldata signature) external {
        require(_exists(tokenId), "DronePassportNFT: nonexistent token");
        uint256 currentNonce = nonces[tokenId];
        // Build the message hash per nonce
        bytes32 messageHash = keccak256(abi.encodePacked(tokenId, cycles, currentNonce));
        bytes32 ethSignedMessageHash = MessageHashUtils.toEthSignedMessageHash(messageHash);
        address signer = ECDSA.recover(ethSignedMessageHash, signature);
        require(signer == onboardSigner, "DronePassportNFT: invalid onboard signature");
        // Replay protection: consume nonce
        nonces[tokenId] = currentNonce + 1;
        droneAttributes[tokenId].batteryCycles = cycles;
        emit BatteryCyclesUpdated(tokenId, cycles, currentNonce, signer);
    }

    /// @notice Set the onboard signer address (admin only)
    /// @param signer Address of onboard signer
    function setOnboardSigner(address signer) external onlyRole(DEFAULT_ADMIN_ROLE) {
        onboardSigner = signer;
    }

    /// @notice On-chain metadata for the NFT encoded as Base64 JSON
    function tokenURI(uint256 tokenId) public view override returns (string memory) {
        require(_exists(tokenId), "DronePassportNFT: URI query for nonexistent token");
        DroneAttributes memory a = droneAttributes[tokenId];

        string memory json = string(abi.encodePacked(
            '{',
                '"name":', '"DronePassport #', tokenId.toString(), '"', ',',
                '"description":', '"Drone passport NFT with repair and firmware history"', ',',
                '"attributes": [',
                    '{',
                        '"manufacturer":', '"', a.manufacturer, '"', ',',
                        '"manufactureDate":', '"', a.manufactureDate, '"', ',',
                        '"serialNumber":', '"', a.serialNumber, '"', ',',
                        '"firmwareVersion":', '"', a.firmwareVersion, '"', ',',
                        '"batteryCycles":', a.batteryCycles.toString(), ',',
                        '"repairCount":', a.repairCount.toString(),
                    '}',
                ']',
            '}'
        ));

        return string(abi.encodePacked("data:application/json;base64,", Base64.encode(bytes(json))));
    }

    /// @notice Get attributes for a token
    function getDroneAttributes(uint256 tokenId) external view returns (string memory manufacturer, string memory manufactureDate, string memory serialNumber, string memory firmwareVersion, uint256 batteryCycles, uint256 repairCount) {
        require(_exists(tokenId), "DronePassportNFT: nonexistent token");
        DroneAttributes memory a = droneAttributes[tokenId];
        return (a.manufacturer, a.manufactureDate, a.serialNumber, a.firmwareVersion, a.batteryCycles, a.repairCount);
    }

    /// @notice Get repair history for a token
    function getRepairHistory(uint256 tokenId) external view returns (RepairRecord[] memory) {
        require(_exists(tokenId), "DronePassportNFT: nonexistent token");
        return repairHistory[tokenId];
    }

    /// @notice Expose interface support (ERC721 + AccessControl)
    function supportsInterface(bytes4 interfaceId) public view virtual override(ERC721, AccessControl) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
}
