#!/usr/bin/env python3
"""
Flight Log Integrity Verifier

Standalone CLI tool to verify flight log data integrity using keccak256 hashes
and optional on-chain verification via a FlightLogHash contract.

Dependencies (install via):
- web3.py
- eth-abi
- eth-utils

Note: This script uses a best-effort approach for on-chain verification since
the exact contract ABI/signature is user-supplied. If web3 is unavailable, the
on-chain path is gracefully skipped.
"""

from __future__ import annotations

import argparse
import json
from dataclasses import dataclass
from typing import List, Dict, Any, Optional

try:
    from eth_utils import keccak
    from eth_abi import encode_abi
except Exception:  # pragma: no cover - optional at runtime
    keccak = None  # type: ignore
    encode_abi = None  # type: ignore


@dataclass
class WaypointEntry:
    waypointData: Dict[str, Any]
    timestamp: int


def _to_bytes32(input_str: str) -> bytes:
    """Convert a string representation to a 32-byte value.

    Rules:
    - If the string starts with 0x and is at least 64 hex chars, take the first 32 bytes.
    - If the string is hex-like but shorter, pad to 32 bytes on the left.
    - If not hex, fallback to keccak256 of the utf-8 encoding (deterministic).
    """
    if input_str is None:
        raise ValueError("order_id must be a non-empty string")
    s = input_str
    if s.startswith("0x") or s.startswith("0X"):
        hexpart = s[2:]
        if len(hexpart) >= 64:
            b = bytes.fromhex(hexpart[:64])
        else:
            b = bytes.fromhex(hexpart)  # shorter hex; may raise
        if len(b) < 32:
            b = b.rjust(32, b"\x00")
        return b[:32]
    else:
        # Non-hex string: hash to 32 bytes for deterministic mapping
        if keccak is None:
            raise RuntimeError("keccak function is not available; install eth-utils")
        return keccak(s.encode("utf-8"))


def _hex(b: bytes) -> str:
    return "0x" + b.hex()


def compute_log_hash(order_id: bytes, waypoint_data: Dict[str, Any], timestamp: int) -> bytes:
    """Compute a single log hash: keccak256(abi.encode(orderId, keccak256(waypointData_json_string), timestamp))."""
    if keccak is None or encode_abi is None:
        raise RuntimeError("Prerequisites not installed: eth_utils, eth_abi")
    waypoint_json = json.dumps(waypoint_data, sort_keys=True, separators=(",", ":"))
    wp_hash = keccak(waypoint_json.encode("utf-8"))
    encoded = encode_abi(["bytes32", "bytes32", "uint256"], [order_id, wp_hash, int(timestamp)])
    return keccak(encoded)


def compute_batch_hashes(order_id: bytes, entries: List[Dict[str, Any]]) -> List[bytes]:
    """Compute per-entry hashes for a flight log batch."""
    hashes: List[bytes] = []
    for e in entries:
        wp = e.get("waypointData")
        ts = e.get("timestamp")
        if wp is None or ts is None:
            raise ValueError("Each entry must contain 'waypointData' and 'timestamp'")
        h = compute_log_hash(order_id, wp, ts)
        hashes.append(h)
    return hashes


def verify_local(log_file: str, order_id: str, reference_hash: str) -> Dict[str, Any]:
    """Local verification against a single reference hash.

    Returns a dict with per-entry hashes and a final verdict: VALID / TAMPERED / MISSING.
    """
    if keccak is None:
        raise RuntimeError("keccak function is not available; install eth-utils")
    with open(log_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, dict) or "orderId" not in data or "entries" not in data:
        raise ValueError("Log file must contain 'orderId' and 'entries' fields")
    entries = data["entries"]
    order_id_bytes = _to_bytes32(order_id)
    per_entry_hashes = compute_batch_hashes(order_id_bytes, entries)

    # Simple root hash by concatenating per-entry hashes
    root_input = b"".join(per_entry_hashes)
    root_hash = keccak(root_input) if per_entry_hashes else None

    # Normalize reference hash
    ref = _to_bytes32(reference_hash) if reference_hash.startswith("0x") or reference_hash.startswith("0X") else _to_bytes32(reference_hash)

    verdict = "MISSING" if not entries else ("VALID" if (root_hash == ref) else "TAMPERED")

    # Print per-entry results and final verdict
    for idx, h in enumerate(per_entry_hashes, start=1):
        print(f"Entry {idx}: { _hex(h) }", flush=True)
        print(f"  Result: {'VALID' if verdict == 'VALID' else 'TAMPERED'}", flush=True)

    print(f"VERDICT: {verdict}")
    return {
        "orderId": order_id,
        "entry_hashes": [ _hex(h) for h in per_entry_hashes ],
        "root_hash": _hex(root_hash) if root_hash is not None else None,
        "reference_hash": reference_hash,
        "verdict": verdict,
    }


def verify_onchain(log_file: str, order_id: str, rpc_url: str, contract_address: str, contract_abi_path: str) -> Dict[str, Any]:
    """Attempt on-chain verification by calling verifyLogIntegrity on the FlightLogHash contract.

    Returns a dict with per-entry on-chain results and a final verdict.
    If web3 or dependencies are unavailable, returns an error dict.
    """
    result: Dict[str, Any] = {
        "orderId": order_id,
        "entry_hashes": [],
        "verdict": "MISSING",
    }
    try:
        from web3 import Web3
        from web3.exceptions import BadFunctionCallOutput
    except Exception:  # pragma: no cover
        result["error"] = "web3.py not installed; on-chain verification skipped"
        return result

    with open(contract_abi_path, "r", encoding="utf-8") as f:
        abi = json.load(f)
    w3 = Web3(Web3.HTTPProvider(rpc_url))
    if not w3.isConnected():
        result["error"] = "Cannot connect to RPC endpoint"
        return result

    try:
        contract = w3.eth.contract(address=Web3.toChecksumAddress(contract_address), abi=abi)
    except Exception as e:  # pragma: no cover
        result["error"] = f"Invalid contract address or ABI: {e}"
        return result

    with open(log_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, dict) or "orderId" not in data or "entries" not in data:
        raise ValueError("Log file must contain 'orderId' and 'entries' fields")
    entries = data["entries"]
    order_id_bytes = _to_bytes32(order_id)
    per_entry_hashes = compute_batch_hashes(order_id_bytes, entries)

    results: List[str] = []
    all_valid = True
    for idx, h in enumerate(per_entry_hashes):
        # Try common signature verifyLogIntegrity(orderId, logHash)
        try:
            onchain = contract.functions.verifyLogIntegrity(order_id_bytes, h).call()
        except TypeError:
            onchain = None
        except BadFunctionCallOutput:
            onchain = None
        if isinstance(onchain, bool):
            ok = bool(onchain)
        else:
            ok = bool(onchain) if onchain is not None else False
        results.append("VALID" if ok else "TAMPERED")
        all_valid = all_valid and ok
    verdict = "VALID" if (entries and all_valid) else ("MISSING" if not entries else "TAMPERED")

    for idx, h in enumerate(per_entry_hashes, start=1):
        print(f"Entry {idx}: { _hex(h) } -> {results[idx-1]}")

    print(f"VERDICT: {verdict}")
    result.update({
        "entry_hashes": [_hex(h) for h in per_entry_hashes],
        "per_entry_results": results,
        "verdict": verdict,
    })
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description="Flight Log Integrity Verifier")
    parser.add_argument("--log-file", dest="log_file", required=True, help="JSON file with flight log entries")
    parser.add_argument("--order-id", dest="order_id", required=True, help="Order identifier (hex or string to bytes32)")

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--reference-hash", dest="reference_hash", help="Local reference hash (hex 0x...) for verification")
    group.add_argument("--rpc-url", dest="rpc_url", help="RPC URL for on-chain verification")
    parser.add_argument("--contract-address", dest="contract_address", help="FlightLogHash contract address (for on-chain verification)")
    parser.add_argument("--contract-abi", dest="contract_abi", help="Path to the FlightLogHash contract ABI JSON (for on-chain verification)")

    args = parser.parse_args()

    # Decide mode
    if args.reference_hash:
        return_code = verify_local(args.log_file, args.order_id, args.reference_hash)
        # ensure exit code 0
        return 0
    else:
        # on-chain mode: require rpc_url, contract_address, contract_abi
        if not (args.rpc_url and args.contract_address and args.contract_abi):
            print("On-chain mode requires --rpc-url, --contract-address, and --contract-abi", flush=True)
            return 2
        result = verify_onchain(args.log_file, args.order_id, args.rpc_url, args.contract_address, args.contract_abi)
        return 0 if not result.get("error") else 1


if __name__ == "__main__":
    raise SystemExit(main())
