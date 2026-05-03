// DronePassportNFT 合约测试 (Hardhat + ethers.js v6)
// 测试: mint, proposeRepairRecord(多签), approveRepairRecord, updateFirmware,
//       updateBatteryCyclesSigned, onboard signer, tokenURI, getDroneAttributes, getRepairHistory

const { expect } = require("chai");
const hre = require("hardhat");

describe("DronePassportNFT", function () {
  let passport;
  let admin, maintenance1, maintenance2, onboardSigner, user;
  let MAINTENANCE_ROLE;
  let DEFAULT_ADMIN_ROLE;

  before(async function () {
    [admin, maintenance1, maintenance2, onboardSigner, user] =
      await hre.ethers.getSigners();

    const DronePassportNFT = await hre.ethers.getContractFactory(
      "DronePassportNFT"
    );
    passport = await DronePassportNFT.connect(admin).deploy();
    await passport.waitForDeployment();

    MAINTENANCE_ROLE = await passport.MAINTENANCE_ROLE();
    DEFAULT_ADMIN_ROLE = await passport.DEFAULT_ADMIN_ROLE();

    // 授予维护角色
    await passport.grantRole(MAINTENANCE_ROLE, maintenance1.address);
    await passport.grantRole(MAINTENANCE_ROLE, maintenance2.address);

    // 设置机载签名者
    await passport.setOnboardSigner(onboardSigner.address);
  });

  // ── mintDronePassport ──────────────────────────────────────

  it("should mint a DronePassport NFT", async function () {
    const tokenId = 1;
    await passport
      .connect(admin)
      .mintDronePassport(
        admin.address,
        tokenId,
        "DJI",
        "2025-06-01",
        "SN-DJI-001"
      );

    const owner = await passport.ownerOf(tokenId);
    expect(owner).to.equal(admin.address);
  });

  it("should return correct drone attributes", async function () {
    const attrs = await passport.getDroneAttributes(1);
    expect(attrs.manufacturer).to.equal("DJI");
    expect(attrs.manufactureDate).to.equal("2025-06-01");
    expect(attrs.serialNumber).to.equal("SN-DJI-001");
    expect(attrs.batteryCycles).to.equal(0n);
    expect(attrs.repairCount).to.equal(0n);
  });

  it("should return tokenURI with base64 JSON", async function () {
    const uri = await passport.tokenURI(1);
    expect(uri).to.include("data:application/json;base64,");
  });

  it("should revert minting from non-admin", async function () {
    await expect(
      passport
        .connect(user)
        .mintDronePassport(user.address, 99, "X", "2025", "X-SN")
    ).to.be.revertedWithCustomError(passport, "AccessControlUnauthorizedAccount");
  });

  it("should revert tokenURI for non-existent token", async function () {
    await expect(passport.tokenURI(99999)).to.be.revertedWith(
      "DronePassportNFT: URI query for nonexistent token"
    );
  });

  // ── proposeRepairRecord + approveRepairRecord (多签) ──────

  it("should propose a repair record (multi-sig flow)", async function () {
    const repairHash = hre.ethers.id("REPAIR-DATA-001");
    const providerDID = hre.ethers.id("did:maintenance:provider:1");

    const tx = await passport
      .connect(maintenance1)
      .proposeRepairRecord(1, repairHash, providerDID);
    const receipt = await tx.wait();

    // 提取 ProposalId 从事件
    const event = receipt.logs.find(
      (log) => log.fragment?.name === "RepairProposed"
    );
    expect(event).to.not.be.undefined;
    this.proposalId = event.args[0]; // proposalId
  });

  it("first maintenance approval alone should NOT execute (REQUIRED_APPROVALS=2)", async function () {
    const repairCountBefore = (await passport.getDroneAttributes(1))
      .repairCount;
    expect(repairCountBefore).to.equal(0n);
  });

  it("second maintenance approval should execute the repair record", async function () {
    await passport
      .connect(maintenance2)
      .approveRepairRecord(this.proposalId);

    const attrs = await passport.getDroneAttributes(1);
    expect(attrs.repairCount).to.equal(1n);

    const history = await passport.getRepairHistory(1);
    expect(history.length).to.equal(1);
  });

  it("should revert double-approval on same proposal", async function () {
    await expect(
      passport.connect(maintenance1).approveRepairRecord(this.proposalId)
    ).to.be.revertedWith("DronePassportNFT: proposal already executed");
  });

  it("should revert non-maintenance proposing repair", async function () {
    await expect(
      passport
        .connect(user)
        .proposeRepairRecord(
          1,
          hre.ethers.id("BAD-REPAIR"),
          hre.ethers.id("did:r:1")
        )
    ).to.be.revertedWithCustomError(passport, "AccessControlUnauthorizedAccount");
  });

  // ── updateFirmware ─────────────────────────────────────────

  it("should update firmware version", async function () {
    await passport.connect(maintenance1).updateFirmware(1, "v2.1.0");
    const attrs = await passport.getDroneAttributes(1);
    expect(attrs.firmwareVersion).to.equal("v2.1.0");
  });

  it("should revert firmware update on non-existent token", async function () {
    await expect(
      passport.connect(maintenance1).updateFirmware(99999, "v3")
    ).to.be.revertedWith("DronePassportNFT: nonexistent token");
  });

  // ── updateBatteryCyclesSigned ──────────────────────────────

  it("should update battery cycles with valid onboard signature", async function () {
    const tokenId = 1;
    const cycles = 150;
    const nonce = await passport.nonces(tokenId);

    // 构造机载签名
    const messageHash = hre.ethers.solidityPackedKeccak256(
      ["uint256", "uint256", "uint256"],
      [tokenId, cycles, nonce]
    );
    const signature = await onboardSigner.signMessage(
      hre.ethers.getBytes(messageHash)
    );

    await passport
      .connect(user) // 任何人都可以调用
      .updateBatteryCyclesSigned(tokenId, cycles, signature);

    const attrs = await passport.getDroneAttributes(tokenId);
    expect(attrs.batteryCycles).to.equal(150n);
  });

  it("should reject battery update with wrong signer", async function () {
    const tokenId = 1;
    const cycles = 200;
    const nonce = await passport.nonces(tokenId);

    const messageHash = hre.ethers.solidityPackedKeccak256(
      ["uint256", "uint256", "uint256"],
      [tokenId, cycles, nonce]
    );
    // 用 user(非 onboardSigner) 签名
    const badSig = await user.signMessage(
      hre.ethers.getBytes(messageHash)
    );

    await expect(
      passport
        .connect(user)
        .updateBatteryCyclesSigned(tokenId, cycles, badSig)
    ).to.be.revertedWith("DronePassportNFT: invalid onboard signature");
  });

  it("should reject replay attack (same nonce used twice)", async function () {
    const tokenId = 1;
    const cycles = 300;
    const nonce = await passport.nonces(tokenId);

    const messageHash = hre.ethers.solidityPackedKeccak256(
      ["uint256", "uint256", "uint256"],
      [tokenId, cycles, nonce]
    );
    const signature = await onboardSigner.signMessage(
      hre.ethers.getBytes(messageHash)
    );

    // 第一次成功
    await passport
      .connect(user)
      .updateBatteryCyclesSigned(tokenId, cycles, signature);

    // nonce 已递增，用旧 nonce 重放应失败
    await expect(
      passport
        .connect(user)
        .updateBatteryCyclesSigned(tokenId, cycles, signature)
    ).to.be.revertedWith("DronePassportNFT: invalid onboard signature");
  });

  // ── supportsInterface ──────────────────────────────────────

  it("should support ERC721 and AccessControl interfaces", async function () {
    // ERC721 interface ID = 0x80ac58cd
    const erc721 = await passport.supportsInterface("0x80ac58cd");
    expect(erc721).to.equal(true);

    // AccessControl interface ID = 0x7965db0b
    const ac = await passport.supportsInterface("0x7965db0b");
    expect(ac).to.equal(true);
  });

  // ── setOnboardSigner admin only ────────────────────────────

  it("should allow admin to change onboard signer", async function () {
    await passport.connect(admin).setOnboardSigner(maintenance1.address);
    const signer = await passport.onboardSigner();
    expect(signer).to.equal(maintenance1.address);

    // 恢复
    await passport.connect(admin).setOnboardSigner(onboardSigner.address);
  });

  it("should revert non-admin changing onboard signer", async function () {
    await expect(
      passport.connect(user).setOnboardSigner(user.address)
    ).to.be.revertedWithCustomError(passport, "AccessControlUnauthorizedAccount");
  });
});
