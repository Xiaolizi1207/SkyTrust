// SkyTrust 合约部署脚本
// 用法: npx hardhat run scripts/deploy.js --network ganache
// 部署全部 3 个合约并输出地址供 backend application.yml 配置使用

const hre = require("hardhat");

async function main() {
  const [deployer, admin, maintenance1, maintenance2, submitter] =
    await hre.ethers.getSigners();

  console.log("Deploying SkyTrust contracts with account:", deployer.address);
  console.log("");

  // ============================================================
  // 1. DronePassportNFT (ERC-721)
  // ============================================================
  const DronePassportNFT = await hre.ethers.getContractFactory(
    "DronePassportNFT"
  );
  const passport = await DronePassportNFT.deploy();
  await passport.waitForDeployment();
  const passportAddr = await passport.getAddress();
  console.log(`[1/3] DronePassportNFT deployed: ${passportAddr}`);

  // 授予维护角色
  const MAINTENANCE_ROLE = await passport.MAINTENANCE_ROLE();
  await passport.grantRole(MAINTENANCE_ROLE, maintenance1.address);
  await passport.grantRole(MAINTENANCE_ROLE, maintenance2.address);
  console.log(
    `      MAINTENANCE_ROLE granted to: ${maintenance1.address}, ${maintenance2.address}`
  );

  // 设置机载签名者（模拟机载设备地址）
  await passport.setOnboardSigner(admin.address);
  console.log(`      onboardSigner set to: ${admin.address}`);

  // ============================================================
  // 2. LicenseVerification
  // ============================================================
  const LicenseVerification = await hre.ethers.getContractFactory(
    "LicenseVerification"
  );
  const license = await LicenseVerification.deploy();
  await license.waitForDeployment();
  const licenseAddr = await license.getAddress();
  console.log(`[2/3] LicenseVerification deployed: ${licenseAddr}`);

  const LICENSE_ADMIN_ROLE = await license.LICENSE_ADMIN_ROLE();
  await license.grantRole(LICENSE_ADMIN_ROLE, admin.address);
  console.log(`      LICENSE_ADMIN_ROLE granted to: ${admin.address}`);

  // ============================================================
  // 3. FlightLogHash
  // ============================================================
  const FlightLogHash = await hre.ethers.getContractFactory("FlightLogHash");
  const flightLog = await FlightLogHash.deploy();
  await flightLog.waitForDeployment();
  const flightLogAddr = await flightLog.getAddress();
  console.log(`[3/3] FlightLogHash deployed: ${flightLogAddr}`);

  const SUBMITTER_ROLE = await flightLog.SUBMITTER_ROLE();
  await flightLog.grantRole(SUBMITTER_ROLE, submitter.address);
  console.log(`      SUBMITTER_ROLE granted to: ${submitter.address}`);

  // ============================================================
  // 部署摘要
  // ============================================================
  console.log("");
  console.log("========================================");
  console.log("  Deployment Summary");
  console.log("========================================");
  console.log(`  DronePassportNFT:    ${passportAddr}`);
  console.log(`  LicenseVerification: ${licenseAddr}`);
  console.log(`  FlightLogHash:       ${flightLogAddr}`);
  console.log("========================================");
  console.log("");
  console.log("Roles assigned:");
  console.log(`  DEFAULT_ADMIN_ROLE: ${deployer.address} (deployer)`);
  console.log(`  MAINTENANCE_ROLE:   ${maintenance1.address}`);
  console.log(`  MAINTENANCE_ROLE:   ${maintenance2.address}`);
  console.log(`  LICENSE_ADMIN_ROLE: ${admin.address}`);
  console.log(`  SUBMITTER_ROLE:     ${submitter.address}`);
  console.log(`  onboardSigner:      ${admin.address}`);

  return {
    passport: passportAddr,
    license: licenseAddr,
    flightLog: flightLogAddr,
  };
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
