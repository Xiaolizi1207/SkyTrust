require("@nomicfoundation/hardhat-toolbox");

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  solidity: {
    version: "0.8.24",
    settings: {
      evmVersion: "cancun",
      viaIR: true,
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },

  networks: {
    // Local Hardhat node (npx hardhat node)
    localhost: {
      url: "http://127.0.0.1:8545",
      chainId: 31337,
    },
    // Ganache (from docker-compose)
    ganache: {
      url: process.env.GANACHE_URL || "http://127.0.0.1:8545",
      chainId: 1337,
      // Ganache deterministic mnemonic
      accounts: {
        mnemonic:
          "myth like bonus scare over problem client lizard pioneer submit female collect",
      },
    },
    // Sepolia testnet (optional — set SEPOLIA_URL and SEPOLIA_PRIVATE_KEY in .env)
    sepolia: {
      url: process.env.SEPOLIA_URL || "",
      accounts: process.env.SEPOLIA_PRIVATE_KEY
        ? [process.env.SEPOLIA_PRIVATE_KEY]
        : [],
      chainId: 11155111,
    },
  },

  // Gas reporter (optional)
  gasReporter: {
    enabled: process.env.REPORT_GAS === "true",
    currency: "USD",
  },

  // Etherscan verification (optional — set ETHERSCAN_API_KEY in .env)
  etherscan: {
    apiKey: process.env.ETHERSCAN_API_KEY || "",
  },

  paths: {
    sources: "./src",
    tests: "./tests",
    cache: "./cache",
    artifacts: "./artifacts",
  },
};
