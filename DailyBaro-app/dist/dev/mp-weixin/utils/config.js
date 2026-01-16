"use strict";
const common_vendor = require("../common/vendor.js");
const ENV = {
  // 开发环境
  development: {
    // 注意：微信小程序无法访问 localhost，需要使用本机IP地址
    // 你的本机IP是: 172.20.10.4
    // 如果要在本地测试，请确保你的服务运行在 172.20.10.4:8000
    BASE_URL: "http://172.20.10.4:8000",
    // 网关地址（使用本机IP）
    NLP_SERVICE_URL: "http://172.20.10.4:5001",
    // 本地NLP服务地址
    NLP_PYTHON_URL: "http://172.20.10.4:5001",
    // 本地Python NLP服务地址
    DEBUG: true,
    // 备用网关地址（如果本地服务不可用）
    FALLBACK_URL: "http://172.20.10.4:8000"
  },
  // 生产环境
  production: {
    BASE_URL: "https://dailybaro.cn",
    // 生产环境网关地址（HTTPS）
    NLP_SERVICE_URL: "https://dailybaro.cn/api/emotion",
    // 通过网关访问NLP服务
    NLP_PYTHON_URL: "https://dailybaro.cn:5000",
    // 生产环境Python服务地址（如果使用）
    DEBUG: false,
    FALLBACK_URL: "https://dailybaro.cn"
  }
};
const currentEnv = "production";
const config = ENV[currentEnv];
const isDev = currentEnv === "development";
const checkGatewayHealth = async () => {
  try {
    const healthUrl = config.BASE_URL + (config.BASE_URL.includes("localhost") ? "/actuator/health" : "");
    const response = await common_vendor.index.request({
      url: healthUrl || config.BASE_URL,
      method: "GET",
      timeout: 5e3,
      // 小程序端需要忽略 SSL 证书验证（仅开发环境）
      sslVerify: isDev
    });
    if (response.statusCode === 200 || response.statusCode === 404) {
      common_vendor.index.__f__("log", "at utils/config.js:69", "✅ 云服务器网关正常");
      return config.BASE_URL;
    }
  } catch (error) {
    common_vendor.index.__f__("log", "at utils/config.js:73", "❌ 云服务器网关不可用:", error.message || error);
  }
  {
    try {
      const response = await common_vendor.index.request({
        url: config.FALLBACK_URL + "/actuator/health",
        method: "GET",
        timeout: 5e3
      });
      if (response.statusCode === 200) {
        common_vendor.index.__f__("log", "at utils/config.js:87", "✅ 备用网关正常");
        return config.FALLBACK_URL;
      }
    } catch (error) {
      common_vendor.index.__f__("log", "at utils/config.js:91", "❌ 备用网关也不可用:", error);
    }
  }
  common_vendor.index.__f__("log", "at utils/config.js:95", "❌ 所有网关都不可用，使用默认配置");
  return config.BASE_URL;
};
exports.checkGatewayHealth = checkGatewayHealth;
exports.config = config;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/config.js.map
