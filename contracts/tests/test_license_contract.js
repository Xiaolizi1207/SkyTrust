// LicenseVerification 合约测试 (Hardhat + ethers.js v6)
// 匹配实际合约 ABI:
//   verifyAndStoreLicenseHash(bytes32,bytes32) → bytes32
//   getLicenseHashByOrder(bytes32) → bytes32
//   getLicenseHashByUser(bytes32) → bytes32
//   createRentalOrder(bytes32,bytes32,bytes32)
//   getOrder(bytes32) → RentalOrder
//   getOrderCount() → uint256

const { expect } = require("chai");
const hre = require("hardhat");

describe("LicenseVerification", function () {
  let license;
  let admin, licenseAdmin, user;
  let LICENSE_ADMIN_ROLE;

  before(async function () {
    [admin, licenseAdmin, user] = await hre.ethers.getSigners();
    const License = await hre.ethers.getContractFactory("LicenseVerification");
    license = await License.connect(admin).deploy();
    await license.waitForDeployment();

    LICENSE_ADMIN_ROLE = await license.LICENSE_ADMIN_ROLE();
    await license.grantRole(LICENSE_ADMIN_ROLE, licenseAdmin.address);
  });

  // ── verifyAndStoreLicenseHash ──────────────────────────────

  it("should allow LICENSE_ADMIN_ROLE to store a license hash", async function () {
    const userDID = hre.ethers.id("did:skytrust:user:1");
    const input = hre.ethers.id("LICENSE-ABC-123");

    await expect(
      license.connect(licenseAdmin).verifyAndStoreLicenseHash(userDID, input)
    ).to.not.be.reverted;
  });

  it("should revert when non-admin tries to store a license hash", async function () {
    const userDID = hre.ethers.id("did:skytrust:user:2");
    const input = hre.ethers.id("LICENSE-XYZ-456");

    await expect(
      license.connect(user).verifyAndStoreLicenseHash(userDID, input)
    ).to.be.revertedWithCustomError(license, "AccessControlUnauthorizedAccount");
  });

  it("should return the correct license hash for a stored user", async function () {
    const userDID = hre.ethers.id("did:skytrust:user:3");
    const input = hre.ethers.id("LICENSE-MATCH-1");

    const tx = await license
      .connect(licenseAdmin)
      .verifyAndStoreLicenseHash(userDID, input);
    const receipt = await tx.wait();

    // getLicenseHashByUser should return a non-zero hash
    const stored = await license.getLicenseHashByUser(userDID);
    expect(stored).to.not.equal(hre.ethers.ZeroHash);
  });

  it("different inputs produce different hashes", async function () {
    const userA = hre.ethers.id("did:skytrust:user:a");
    const userB = hre.ethers.id("did:skytrust:user:b");
    const inputA = hre.ethers.id("INPUT-A");
    const inputB = hre.ethers.id("INPUT-B");

    await license.connect(licenseAdmin).verifyAndStoreLicenseHash(userA, inputA);
    await license.connect(licenseAdmin).verifyAndStoreLicenseHash(userB, inputB);

    const hashA = await license.getLicenseHashByUser(userA);
    const hashB = await license.getLicenseHashByUser(userB);

    expect(hashA).to.not.equal(hashB);
  });

  // ── createRentalOrder ──────────────────────────────────────

  it("should create a rental order and link license hash", async function () {
    const userDID = hre.ethers.id("did:skytrust:order:1");
    const input = hre.ethers.id("ORDER-LICENSE-001");
    const signature = hre.ethers.id("USER-SIGNATURE-001");

    await license
      .connect(licenseAdmin)
      .createRentalOrder(userDID, input, signature);

    const count = await license.getOrderCount();
    expect(count).to.equal(1n);
  });

  it("should retrieve rental order details", async function () {
    const userDID = hre.ethers.id("did:skytrust:order:2");
    const input = hre.ethers.id("ORDER-LICENSE-002");
    const signature = hre.ethers.id("USER-SIGNATURE-002");

    await license
      .connect(licenseAdmin)
      .createRentalOrder(userDID, input, signature);

    const count = await license.getOrderCount();
    // orderIds is public — we can read the last one
    const orderId = await license.orderIds(count - 1n);
    const order = await license.getOrder(orderId);

    expect(order.userDID).to.equal(userDID);
    expect(order.signature).to.equal(signature);
    expect(order.exists).to.equal(true);
  });

  it("should reject rental order from non-admin", async function () {
    const userDID = hre.ethers.id("did:skytrust:order:3");
    const input = hre.ethers.id("ORDER-LICENSE-003");
    const signature = hre.ethers.id("USER-SIGNATURE-003");

    await expect(
      license.connect(user).createRentalOrder(userDID, input, signature)
    ).to.be.revertedWithCustomError(license, "AccessControlUnauthorizedAccount");
  });

  // ── getLicenseHashByOrder ──────────────────────────────────

  it("should return the correct license hash by orderId", async function () {
    const userDID = hre.ethers.id("did:skytrust:order:4");
    const input = hre.ethers.id("ORDER-LICENSE-004");
    const signature = hre.ethers.id("USER-SIGNATURE-004");

    await license
      .connect(licenseAdmin)
      .createRentalOrder(userDID, input, signature);

    const count = await license.getOrderCount();
    const orderId = await license.orderIds(count - 1n);

    const licenseHash = await license.getLicenseHashByOrder(orderId);
    expect(licenseHash).to.not.equal(hre.ethers.ZeroHash);
  });

  // ── Access control checks ──────────────────────────────────

  it("should have set correct roles", async function () {
    const hasRole = await license.hasRole(
      LICENSE_ADMIN_ROLE,
      licenseAdmin.address
    );
    expect(hasRole).to.equal(true);
  });
});
