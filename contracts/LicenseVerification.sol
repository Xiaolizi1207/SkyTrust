// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/access/AccessControl.sol";

/// @title LicenseVerification
/// @notice Stores hash of a license on-chain with role-based access control.
/// The actual license number is never stored in plaintext. Hashes are computed using abi.encode
/// to avoid collisions that could arise from abi.encodePacked.
contract LicenseVerification is AccessControl {
    // Role that can administer license hashes
    bytes32 public constant LICENSE_ADMIN_ROLE = keccak256("LICENSE_ADMIN_ROLE");

    // userDID => licenseHash
    mapping(bytes32 => bytes32) private licenseHashByUser;
    // orderId => licenseHash
    mapping(bytes32 => bytes32) private orderToLicenseHash;

    // Rental orders storage
    struct RentalOrder {
        bytes32 userDID;
        bytes32 licenseHash;
        bytes32 signature; // keccak256(user signed hash)
        uint256 timestamp;
        bool exists;
    }

    // orderId => RentalOrder
    mapping(bytes32 => RentalOrder) public orders;
    // list of orderIds for enumeration
    bytes32[] public orderIds;

    /// @notice Emitted when a license hash is stored for a user
    /// @param userDID The Decentralized Identifier of the user
    /// @param licenseHash The on-chain hash for the license (not plaintext)
    /// @param timestamp Block timestamp when stored
    event LicenseStored(bytes32 indexed userDID, bytes32 licenseHash, uint256 timestamp);

    /// @notice Emitted when an order is linked to a license hash
    /// @param orderId The derived order identifier
    /// @param licenseHash The on-chain license hash associated with the order
    event OrderLicensed(bytes32 indexed orderId, bytes32 licenseHash);

    /// @notice Emitted when a rental order is created
    /// @param orderId The derived rental order identifier
    /// @param userDID The user DID associated with the rental
    /// @param licenseHash The on-chain license hash linked to the rental
    /// @param signature The user-signed hash that was provided off-chain
    /// @param timestamp Block timestamp when the rental order was created
    event RentalOrderCreated(bytes32 indexed orderId, bytes32 userDID, bytes32 licenseHash, bytes32 signature, uint256 timestamp);

    /// @notice Constructor granting admin roles to deployer
    constructor() {
        _setupRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _setupRole(LICENSE_ADMIN_ROLE, msg.sender);
    }

    /// @notice Stores a license hash computed from the provided licenseHashInput, userDID and timestamp.
    /// The hash is computed using abi.encode to avoid collisions and protect plaintext license data.
    /// @param userDID The user DID to associate with the license
    /// @param licenseHashInput The input data representing the license (not stored on-chain)
    /// @return The stored license hash (on-chain)
    function verifyAndStoreLicenseHash(bytes32 userDID, bytes32 licenseHashInput) public onlyRole(LICENSE_ADMIN_ROLE) returns (bytes32) {
        bytes32 computedHash = keccak256(abi.encode(licenseHashInput, block.timestamp, userDID));
        licenseHashByUser[userDID] = computedHash;
        emit LicenseStored(userDID, computedHash, block.timestamp);

        // Derive an orderId for linkage between orders and licenses
        bytes32 orderId = keccak256(abi.encode(userDID, computedHash, block.timestamp));
        orderToLicenseHash[orderId] = computedHash;
        emit OrderLicensed(orderId, computedHash);
        return computedHash;
    }

    /// @notice Retrieve the license hash associated with a given orderId
    /// @param orderId The derived order identifier
    /// @return The on-chain license hash linked to the order
    function getLicenseHashByOrder(bytes32 orderId) public view returns (bytes32) {
        return orderToLicenseHash[orderId];
    }

    /// @notice Optional helper to view the latest license hash for a userDID
    /// @param userDID The user DID
    /// @return The on-chain license hash for the user (latest)
    function getLicenseHashByUser(bytes32 userDID) public view returns (bytes32) {
        return licenseHashByUser[userDID];
    }

    /// @notice Creates a rental order for a user with a given license hash and user signature
    /// @dev Only callable by accounts with LICENSE_ADMIN_ROLE
    /// @param userDID The user Decentralized Identifier to associate with the rental
    /// @param licenseHashInput The input data representing the license to hash on-chain
    /// @param signature The keccak256 hash signed by the user off-chain
    function createRentalOrder(bytes32 userDID, bytes32 licenseHashInput, bytes32 signature) public onlyRole(LICENSE_ADMIN_ROLE) {
        uint256 ts = block.timestamp;
        bytes32 orderId = keccak256(abi.encode(userDID, licenseHashInput, signature, ts));

        // Compute and store the license hash internally (avoid altering existing verify flow)
        bytes32 computedHash = keccak256(abi.encode(licenseHashInput, ts, userDID));
        licenseHashByUser[userDID] = computedHash;
        // Link the derived license hash to the rental order as well
        orderToLicenseHash[orderId] = computedHash;

        RentalOrder memory ro = RentalOrder({
            userDID: userDID,
            licenseHash: computedHash,
            signature: signature,
            timestamp: ts,
            exists: true
        });

        orders[orderId] = ro;
        orderIds.push(orderId);

        emit RentalOrderCreated(orderId, userDID, computedHash, signature, ts);
    }

    /// @notice Retrieve a rental order by its orderId
    /// @param orderId The derived rental order identifier
    /// @return RentalOrder memory struct containing order details
    function getOrder(bytes32 orderId) public view returns (RentalOrder memory) {
        return orders[orderId];
    }

    /// @notice Returns the total number of rental orders stored
    /// @return The count of rental orders
    function getOrderCount() public view returns (uint256) {
        return orderIds.length;
    }
}
