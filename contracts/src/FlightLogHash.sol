// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/access/AccessControl.sol";

/// @title FlightLogHash
/// @notice Stores flight log hashes on-chain with batch submission and anti-replay protections.
contract FlightLogHash is AccessControl {
    bytes32 public constant SUBMITTER_ROLE = keccak256("SUBMITTER_ROLE");

    struct LogEntry {
        bytes32 logHash;
        uint256 timestamp;
    }

    // orderId => list of log entries
    mapping(bytes32 => LogEntry[]) private logsByOrder;
    // Anti-replay: unique combination of (orderId, logHash, timestamp) hashed to a blob and marked used
    mapping(bytes32 => bool) private usedEntries;

    /// @notice Emitted for every log hash stored in a batch submission
    /// @param orderId The order identifier this log belongs to
    /// @param timestamp The timestamp of the log entry
    /// @param logHash The hash of the log data
    event FlightLogStored(bytes32 indexed orderId, uint256 timestamp, bytes32 logHash);

    /// @notice Constructor granting admin and submitter roles
    constructor() {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(SUBMITTER_ROLE, msg.sender);
    }

    /// @notice Submit a batch of flight log hashes for a given orderId
    /// The function enforces anti-replay by ensuring each (orderId, logHash, timestamp) triple is unique
    /// @param orderId The associated order identifier
    /// @param logHashes Array of log hashes
    /// @param timestamps Corresponding timestamps for each log hash
    function submitLogHash(bytes32 orderId, bytes32[] calldata logHashes, uint256[] calldata timestamps) external onlyRole(SUBMITTER_ROLE) {
        require(logHashes.length == timestamps.length, "Length mismatch");
        for (uint256 i = 0; i < logHashes.length; i++) {
            bytes32 entryHash = keccak256(abi.encode(orderId, logHashes[i], timestamps[i]));
            require(!usedEntries[entryHash], "Replay detected");
            usedEntries[entryHash] = true;
            logsByOrder[orderId].push(LogEntry({logHash: logHashes[i], timestamp: timestamps[i]}));
            emit FlightLogStored(orderId, timestamps[i], logHashes[i]);
        }
    }

    /// @notice Verify integrity by comparing an external hash against stored log entries for an order
    /// @param orderId The order identifier to verify
    /// @param externalHash The keccak256 hash of (orderId, logHash, timestamp) to verify
    /// @return true if a matching log entry exists, false otherwise
    function verifyLogIntegrity(bytes32 orderId, bytes32 externalHash) external view returns (bool) {
        LogEntry[] storage entries = logsByOrder[orderId];
        for (uint256 i = 0; i < entries.length; i++) {
            bytes32 candidate = keccak256(abi.encode(orderId, entries[i].logHash, entries[i].timestamp));
            if (candidate == externalHash) {
                return true;
            }
        }
        return false;
    }
}
