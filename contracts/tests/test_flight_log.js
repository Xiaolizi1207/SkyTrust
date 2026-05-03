// FlightLogHash 合约测试 (Hardhat + ethers.js v6)
// 测试: submitLogHash (批量提交), verifyLogIntegrity, 防重放, 权限控制

const { expect } = require("chai");
const hre = require("hardhat");

describe("FlightLogHash", function () {
  let flightLog;
  let admin, submitter, user;
  let SUBMITTER_ROLE;

  before(async function () {
    [admin, submitter, user] = await hre.ethers.getSigners();
    const FlightLogHash = await hre.ethers.getContractFactory("FlightLogHash");
    flightLog = await FlightLogHash.connect(admin).deploy();
    await flightLog.waitForDeployment();

    SUBMITTER_ROLE = await flightLog.SUBMITTER_ROLE();
    await flightLog.grantRole(SUBMITTER_ROLE, submitter.address);
  });

  // ── submitLogHash (单条/批量) ──────────────────────────────

  it("should submit a single flight log hash", async function () {
    const orderId = hre.ethers.id("ORDER-001");
    const logHash = hre.ethers.id("LOG-DATA-001");
    const timestamp = Math.floor(Date.now() / 1000);

    await expect(
      flightLog
        .connect(submitter)
        .submitLogHash(orderId, [logHash], [timestamp])
    ).to.emit(flightLog, "FlightLogStored")
      .withArgs(orderId, timestamp, logHash);
  });

  it("should submit multiple flight log hashes in a batch", async function () {
    const orderId = hre.ethers.id("ORDER-002");
    const hashes = [
      hre.ethers.id("LOG-BATCH-1"),
      hre.ethers.id("LOG-BATCH-2"),
      hre.ethers.id("LOG-BATCH-3"),
    ];
    const timestamps = [1000000, 1000001, 1000002];

    const tx = await flightLog
      .connect(submitter)
      .submitLogHash(orderId, hashes, timestamps);
    const receipt = await tx.wait();

    // 应触发 3 次 FlightLogHash 事件
    const events = receipt.logs.filter(
      (log) => log.fragment?.name === "FlightLogStored"
    );
    expect(events.length).to.equal(3);
  });

  it("should revert when logHashes and timestamps arrays have different lengths", async function () {
    const orderId = hre.ethers.id("ORDER-LEN-ERR");
    await expect(
      flightLog
        .connect(submitter)
        .submitLogHash(
          orderId,
          [hre.ethers.id("A")],
          [1, 2] // timestamps 多一个
        )
    ).to.be.revertedWith("Length mismatch");
  });

  // ── verifyLogIntegrity ─────────────────────────────────────

  it("should verify a stored log entry", async function () {
    const orderId = hre.ethers.id("ORDER-VERIFY");
    const logHash = hre.ethers.id("LOG-TO-VERIFY");
    const timestamp = 5000000;

    await flightLog
      .connect(submitter)
      .submitLogHash(orderId, [logHash], [timestamp]);

    const candidate = hre.ethers.solidityPackedKeccak256(
      ["bytes32", "bytes32", "uint256"],
      [orderId, logHash, timestamp]
    );

    const result = await flightLog.verifyLogIntegrity(orderId, candidate);
    expect(result).to.equal(true);
  });

  it("should return false for non-existent log entry", async function () {
    const fakeCandidate = hre.ethers.id("FAKE-LOG-ENTRY");
    const result = await flightLog.verifyLogIntegrity(
      hre.ethers.id("ORDER-NOEXIST"),
      fakeCandidate
    );
    expect(result).to.equal(false);
  });

  it("should return false for wrong timestamp (different from stored)", async function () {
    const orderId = hre.ethers.id("ORDER-WRONG-TS");
    const logHash = hre.ethers.id("LOG-TS-CHECK");
    const timestamp = 9000000;

    await flightLog
      .connect(submitter)
      .submitLogHash(orderId, [logHash], [timestamp]);

    // 构造一个不同 timestamp 的 candidate
    const wrongCandidate = hre.ethers.solidityPackedKeccak256(
      ["bytes32", "bytes32", "uint256"],
      [orderId, logHash, timestamp + 1]
    );

    const result = await flightLog.verifyLogIntegrity(orderId, wrongCandidate);
    expect(result).to.equal(false);
  });

  // ── 防重放 ────────────────────────────────────────────────

  it("should reject duplicate (orderId, logHash, timestamp) triple", async function () {
    const orderId = hre.ethers.id("ORDER-REPLAY");
    const logHash = hre.ethers.id("LOG-REPLAY");
    const timestamp = 9999999;

    // 第一次提交成功
    await flightLog
      .connect(submitter)
      .submitLogHash(orderId, [logHash], [timestamp]);

    // 第二次相同数据应被拒绝
    await expect(
      flightLog
        .connect(submitter)
        .submitLogHash(orderId, [logHash], [timestamp])
    ).to.be.revertedWith("Replay detected");
  });

  // ── 权限控制 ──────────────────────────────────────────────

  it("should revert when non-submitter submits log", async function () {
    const orderId = hre.ethers.id("ORDER-AUTH");
    await expect(
      flightLog
        .connect(user)
        .submitLogHash(
          orderId,
          [hre.ethers.id("UN-AUTH")],
          [Math.floor(Date.now() / 1000)]
        )
    ).to.be.revertedWithCustomError(flightLog, "AccessControlUnauthorizedAccount");
  });

  it("SUBMITTER_ROLE should be correctly assigned", async function () {
    const hasRole = await flightLog.hasRole(
      SUBMITTER_ROLE,
      submitter.address
    );
    expect(hasRole).to.equal(true);
  });

  // ── 空数组 ────────────────────────────────────────────────

  it("should accept empty arrays without revert", async function () {
    const orderId = hre.ethers.id("ORDER-EMPTY");
    await expect(
      flightLog.connect(submitter).submitLogHash(orderId, [], [])
    ).to.not.be.reverted;
  });
});
