"use strict";
const common_vendor = require("../common/vendor.js");
const utils_config = require("./config.js");
let BASE_URL = utils_config.config.BASE_URL;
const NLP_SERVICE_URL = utils_config.config.NLP_SERVICE_URL;
const API_PREFIX = "";
const initGateway = async () => {
  try {
    BASE_URL = await utils_config.checkGatewayHealth();
    common_vendor.index.__f__("log", "at utils/api.js:12", "✅ 初始化网关地址:", BASE_URL);
    common_vendor.index.__f__("log", "at utils/api.js:13", "✅ 配置的BASE_URL:", utils_config.config.BASE_URL);
    if (BASE_URL !== utils_config.config.BASE_URL) {
      common_vendor.index.__f__("warn", "at utils/api.js:15", "⚠️ 网关地址与配置不一致，使用:", BASE_URL);
    }
  } catch (error) {
    common_vendor.index.__f__("error", "at utils/api.js:18", "❌ 网关初始化失败:", error);
    BASE_URL = utils_config.config.BASE_URL;
    common_vendor.index.__f__("log", "at utils/api.js:21", "✅ 使用默认配置:", BASE_URL);
  }
};
const request = (options) => {
  return new Promise(async (resolve, reject) => {
    if (!BASE_URL || BASE_URL === utils_config.config.BASE_URL) {
      await initGateway();
    }
    const userInfo = common_vendor.index.getStorageSync("userInfo");
    const uid = userInfo ? userInfo.uid || userInfo.id || userInfo.userId : null;
    const method = (options.method || "GET").toUpperCase();
    let finalData = options.data;
    if (method === "POST" || method === "PUT" || method === "PATCH") {
      if (finalData === void 0 || finalData === null) {
        finalData = {};
        common_vendor.index.__f__("warn", "at utils/api.js:54", "POST请求data为空，使用空对象");
      } else {
        if (typeof finalData !== "object" || Array.isArray(finalData)) {
          common_vendor.index.__f__("warn", "at utils/api.js:58", "POST请求data类型异常:", typeof finalData);
          if (typeof finalData === "string") {
            try {
              finalData = JSON.parse(finalData);
            } catch (e) {
              common_vendor.index.__f__("error", "at utils/api.js:64", "无法解析data字符串:", e);
              finalData = {};
            }
          } else {
            finalData = {};
          }
        } else {
          common_vendor.index.__f__("log", "at utils/api.js:71", "POST请求data:", JSON.stringify(finalData));
          common_vendor.index.__f__("log", "at utils/api.js:72", "POST请求data keys:", Object.keys(finalData));
          common_vendor.index.__f__("log", "at utils/api.js:73", "POST请求data.code:", finalData.code);
          common_vendor.index.__f__("log", "at utils/api.js:74", "POST请求data.code 类型:", typeof finalData.code);
          common_vendor.index.__f__("log", "at utils/api.js:75", "POST请求data.code 值:", finalData.code);
          if (finalData.code === void 0 || finalData.code === null || finalData.code === "") {
            common_vendor.index.__f__("error", "at utils/api.js:78", "警告：finalData.code 为空或未定义！");
          }
        }
      }
    }
    common_vendor.index.__f__("log", "at utils/api.js:84", "发送请求:", {
      url: BASE_URL + API_PREFIX + options.url,
      method,
      data: finalData,
      dataType: typeof finalData,
      dataKeys: finalData ? Object.keys(finalData) : [],
      dataString: JSON.stringify(finalData),
      hasCode: finalData && finalData.code ? "YES: " + finalData.code : "NO"
    });
    const requestConfig = {
      url: BASE_URL + API_PREFIX + options.url,
      method,
      header: {
        "Content-Type": "application/json",
        "uid": uid || "",
        ...options.header
      },
      dataType: "json",
      timeout: 1e4
    };
    if (method === "POST" || method === "PUT" || method === "PATCH") {
      if (!requestConfig.header["Content-Type"]) {
        requestConfig.header["Content-Type"] = "application/json";
      }
    }
    if (method === "GET") {
      requestConfig.data = finalData || {};
    } else {
      if (finalData && typeof finalData === "object" && !Array.isArray(finalData)) {
        requestConfig.data = finalData;
      } else {
        requestConfig.data = finalData || {};
      }
    }
    common_vendor.index.__f__("log", "at utils/api.js:128", "uni.request 最终配置:", {
      url: requestConfig.url,
      method: requestConfig.method,
      data: requestConfig.data,
      dataType: typeof requestConfig.data,
      dataKeys: requestConfig.data ? Object.keys(requestConfig.data) : [],
      dataString: JSON.stringify(requestConfig.data),
      hasCode: requestConfig.data && requestConfig.data.code ? "YES: " + requestConfig.data.code : "NO",
      header: requestConfig.header
    });
    let requestData = requestConfig.data;
    if ((method === "POST" || method === "PUT" || method === "PATCH") && requestData) {
      if (typeof requestData === "string") {
        try {
          requestData = JSON.parse(requestData);
        } catch (e) {
          common_vendor.index.__f__("warn", "at utils/api.js:150", "data 是字符串但无法解析为JSON:", e);
        }
      }
    }
    common_vendor.index.__f__("log", "at utils/api.js:155", "最终调用 uni.request，data:", requestData);
    common_vendor.index.__f__("log", "at utils/api.js:156", "data 序列化:", JSON.stringify(requestData));
    common_vendor.index.__f__("log", "at utils/api.js:157", "data.code 值:", requestData && requestData.code ? requestData.code : "NO CODE");
    common_vendor.index.__f__("log", "at utils/api.js:158", "data.code 类型:", requestData && requestData.code ? typeof requestData.code : "NO CODE");
    common_vendor.index.__f__("log", "at utils/api.js:159", "Content-Type:", requestConfig.header["Content-Type"]);
    common_vendor.index.__f__("log", "at utils/api.js:160", "完整 header:", requestConfig.header);
    const finalHeaders = { ...requestConfig.header };
    if ((method === "POST" || method === "PUT" || method === "PATCH") && requestData) {
      finalHeaders["Content-Type"] = "application/json";
      if (typeof requestData === "string") {
        try {
          requestData = JSON.parse(requestData);
        } catch (e) {
          common_vendor.index.__f__("warn", "at utils/api.js:173", "data 是字符串但无法解析为JSON:", e);
        }
      }
      if (requestData && typeof requestData === "object" && Object.keys(requestData).length === 0) {
        common_vendor.index.__f__("warn", "at utils/api.js:178", "警告：requestData 是空对象，可能导致后端接收不到数据");
      }
    }
    common_vendor.index.__f__("log", "at utils/api.js:182", "最终发送的 data:", requestData);
    common_vendor.index.__f__("log", "at utils/api.js:183", "最终发送的 data 类型:", typeof requestData);
    common_vendor.index.__f__("log", "at utils/api.js:184", "最终发送的 data 键:", requestData ? Object.keys(requestData) : []);
    common_vendor.index.__f__("log", "at utils/api.js:185", "最终发送的 data JSON:", JSON.stringify(requestData));
    common_vendor.index.__f__("log", "at utils/api.js:186", "最终发送的 header:", finalHeaders);
    let requestDataFinal = requestData !== void 0 && requestData !== null ? requestData : {};
    if ((method === "POST" || method === "PUT" || method === "PATCH") && requestDataFinal) {
      if (typeof requestDataFinal === "string") {
        try {
          requestDataFinal = JSON.parse(requestDataFinal);
        } catch (e) {
          common_vendor.index.__f__("warn", "at utils/api.js:199", "requestDataFinal 是字符串但无法解析为JSON:", e);
        }
      }
      if (typeof requestDataFinal !== "object" || Array.isArray(requestDataFinal)) {
        common_vendor.index.__f__("warn", "at utils/api.js:204", "requestDataFinal 类型异常，转换为对象:", typeof requestDataFinal);
        requestDataFinal = {};
      }
    }
    common_vendor.index.__f__("log", "at utils/api.js:209", "最终传递给 uni.request 的 data:", requestDataFinal);
    common_vendor.index.__f__("log", "at utils/api.js:210", "最终传递给 uni.request 的 data JSON:", JSON.stringify(requestDataFinal));
    common_vendor.index.__f__("log", "at utils/api.js:211", "最终传递给 uni.request 的 data 类型:", typeof requestDataFinal);
    common_vendor.index.__f__("log", "at utils/api.js:212", "最终传递给 uni.request 的 data 键数量:", requestDataFinal ? Object.keys(requestDataFinal).length : 0);
    common_vendor.index.__f__("log", "at utils/api.js:213", "最终传递给 uni.request 的完整配置:", {
      url: requestConfig.url,
      method: requestConfig.method,
      data: requestDataFinal,
      header: finalHeaders
    });
    common_vendor.index.request({
      url: requestConfig.url,
      method: requestConfig.method,
      data: requestDataFinal,
      header: finalHeaders,
      dataType: requestConfig.dataType,
      timeout: requestConfig.timeout,
      success: (res) => {
        var _a;
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          common_vendor.index.removeStorageSync("userInfo");
          common_vendor.index.showToast({
            title: "登录已过期，请重新登录",
            icon: "none"
          });
          setTimeout(() => {
            common_vendor.index.reLaunch({
              url: "/pages/login/login"
            });
          }, 1500);
          reject(new Error("登录已过期"));
        } else if (res.statusCode === 404) {
          common_vendor.index.__f__("error", "at utils/api.js:253", "404错误，尝试切换网关");
          reject(new Error(`接口不存在: ${options.url}，请检查网关配置`));
        } else {
          reject(new Error(((_a = res.data) == null ? void 0 : _a.message) || `请求失败: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        common_vendor.index.__f__("error", "at utils/api.js:260", "API请求失败:", {
          url: options.url,
          error: err,
          gateway: BASE_URL
        });
        if (err.errMsg && err.errMsg.includes("fail")) {
          common_vendor.index.__f__("log", "at utils/api.js:268", "🔄 尝试切换网关...");
          initGateway().then(() => {
            request(options).then(resolve).catch(reject);
          }).catch(() => {
            reject(new Error("网关切换失败，请检查网络连接"));
          });
        } else {
          reject(new Error(err.errMsg || "网络请求失败"));
        }
      }
    });
  });
};
const nlpRequest = (options) => {
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      url: NLP_SERVICE_URL + options.url,
      method: options.method || "POST",
      data: options.data || {},
      header: {
        "Content-Type": "application/json",
        ...options.header
      },
      success: (res) => {
        var _a;
        if (options.url === "/api/health") {
          if (res.statusCode === 200 || res.statusCode === 426) {
            resolve({ available: res.statusCode === 200, status: res.statusCode });
          } else {
            resolve({ available: false, status: res.statusCode });
          }
        } else if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(new Error(((_a = res.data) == null ? void 0 : _a.message) || `NLP服务请求失败: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        if (options.url === "/api/health") {
          resolve({ available: false, error: err.errMsg });
        } else {
          reject(new Error(err.errMsg || "NLP服务连接失败"));
        }
      }
    });
  });
};
const userApi = {
  // 用户注册
  register: (data) => {
    return request({
      url: "/api/login/doRegister",
      method: "POST",
      data
    });
  },
  // 用户登录
  login: (data) => {
    return request({
      url: "/api/login/doLogin",
      method: "POST",
      data
    });
  },
  // 微信登录（通过 app-service 转发到后端 /login/wxLogin）
  wechatLogin: (data) => {
    return request({
      url: "/app/login/wxLogin",
      method: "POST",
      data
    });
  },
  // 获取用户信息
  getUserInfo: (uid) => {
    return request({
      url: `/app/users/getMyInfo/${uid}`,
      method: "GET"
    });
  },
  // 更新用户信息
  updateUserInfo: (uid, data) => {
    return request({
      url: `/app/users/updateUserInfo`,
      method: "POST",
      data: { ...data, uid }
    });
  },
  // 删除用户
  deleteUser: (uid) => {
    return request({
      url: `/api/user/delete/${uid}`,
      method: "DELETE"
    });
  },
  // 获取推荐列表（使用 knowledge-service 的推荐接口）
  getRecommendations: () => {
    return request({
      url: "/api/knowledge/recommendation/by-emotion?days=7",
      method: "GET"
    });
  },
  // 执行活动
  performActivity: (uid, activityId) => {
    return request({
      url: "/app/activities/perform",
      method: "POST",
      data: {
        uid,
        activityId
      }
    });
  }
};
const diaryApi = {
  // 获取日记列表
  getDiaries: (params = {}) => {
    let queryParams = {};
    if (typeof params === "string" || typeof params === "number") {
      const uid = params;
      const status = arguments[1] || "all";
      queryParams = { uid, status };
    } else {
      queryParams = params;
    }
    return request({
      url: `/api/diary`,
      method: "GET",
      data: queryParams
    });
  },
  // 获取日记详情
  getDiaryDetail: (id) => {
    return request({
      url: `/app/diary/detail/${id}`,
      method: "GET"
    });
  },
  // 创建日记
  createDiary: (data) => {
    return request({
      url: "/api/diary",
      method: "POST",
      data
    });
  },
  // 更新日记
  updateDiary: (id, data) => {
    return request({
      url: `/api/diary/${id}`,
      method: "PUT",
      data
    });
  },
  // 删除日记
  deleteDiary: (id) => {
    return request({
      url: `/api/diary/${id}`,
      method: "DELETE"
    });
  },
  // 按标签查询日记
  searchByTag: (uid, tag) => {
    return request({
      url: `/api/diary/search`,
      method: "GET",
      data: { uid, tag }
    });
  }
};
const anonymousApi = {
  // 获取动态列表
  getPosts: (page = 1, size = 20) => {
    return request({
      url: `/app/anonymous-posts`,
      method: "GET",
      data: { page, size }
    });
  },
  // 发布动态
  createPost: (data) => {
    return request({
      url: "/app/anonymous-posts",
      method: "POST",
      data
    });
  },
  // 点赞动态
  likePost: (postId) => {
    return request({
      url: `/app/anonymous-posts/${postId}/like`,
      method: "POST"
    });
  },
  // 评论动态
  commentPost: (postId, data) => {
    return request({
      url: `/app/anonymous-posts/${postId}/comment`,
      method: "POST",
      data
    });
  }
};
const emotionApi = {
  // 分析情绪
  analyzeEmotion: (data) => {
    return request({
      url: "/app/emotion/analysis",
      method: "POST",
      data
    });
  },
  // 基于日记附件分析情绪
  analyzeFromDiary: (diaryId, type) => {
    return request({
      url: "/app/emotion/analysis/from-diary",
      method: "POST",
      data: { diaryId, type }
    });
  },
  // 获取分析历史
  getAnalysisHistory: (uid) => {
    return request({
      url: `/app/analysis/result`,
      method: "GET",
      data: { uid }
    }).catch(() => {
      return request({
        url: `/api/analysis/result`,
        method: "GET",
        data: { uid }
      });
    });
  },
  // 获取情绪统计
  getEmotionStats: (uid, period = "week") => {
    return request({
      url: `/api/emotion/visualization`,
      method: "GET",
      data: { uid, period }
    });
  },
  // 导出分析报告
  exportReport: (uid, format = "pdf") => {
    return request({
      url: `/api/emotion/export`,
      method: "POST",
      data: { uid, format }
    });
  }
};
const nlpEmotionApi = {
  // 智能情绪分析
  analyzeEmotion: (text) => {
    return nlpRequest({
      url: "/api/nlp/emotion/analyze",
      method: "POST",
      data: { text }
    });
  },
  // 批量情绪分析
  batchAnalyzeEmotion: (texts) => {
    return nlpRequest({
      url: "/api/nlp/emotion/batch-analyze",
      method: "POST",
      data: { texts }
    });
  },
  // 情绪分类
  classifyEmotion: (text, topK = 3) => {
    return nlpRequest({
      url: "/api/nlp/emotion/classify",
      method: "POST",
      data: { text, top_k: topK }
    });
  },
  // 获取服务信息
  getServiceInfo: () => {
    return nlpRequest({
      url: "/api/nlp/emotion/info",
      method: "GET"
    });
  },
  // 健康检查
  healthCheck: () => {
    return nlpRequest({
      url: "/api/health",
      method: "GET"
    });
  }
};
const capsuleApi = {
  // 获取胶囊列表
  getCapsules: (uid, status = "all") => {
    return request({
      url: `/app/capsules`,
      method: "GET",
      data: { uid, status }
    });
  },
  // 创建胶囊
  createCapsule: (data) => {
    return request({
      url: "/app/capsules/json",
      method: "POST",
      data
    });
  },
  // 开启胶囊
  openCapsule: (id) => {
    return request({
      url: `/app/capsules/${id}/open`,
      method: "POST"
    });
  },
  // 删除胶囊
  deleteCapsule: (id) => {
    return request({
      url: `/app/capsules/${id}`,
      method: "DELETE"
    });
  },
  // 获取未读提醒
  getUnreadReminders: () => {
    return request({
      url: "/app/capsules/reminders/unread",
      method: "GET"
    });
  },
  // 标记提醒为已读
  markReminderRead: (id) => {
    return request({
      url: `/app/capsules/reminders/${id}/read`,
      method: "POST"
    });
  }
};
const mysteryBoxApi = {
  // 获取用户能量值
  getUserEnergy: (uid) => {
    return request({
      url: `/api/mystery-box/energy`,
      method: "GET",
      data: { uid }
    });
  },
  // 抽取盲盒
  drawBox: (uid) => {
    return request({
      url: "/api/mystery-box/draw",
      method: "POST",
      data: { uid }
    });
  },
  // 完成任务
  completeTask: (taskId) => {
    return request({
      url: `/api/mystery-box/complete/${taskId}`,
      method: "POST"
    });
  },
  // 获取抽取历史
  getDrawHistory: (uid) => {
    return request({
      url: `/api/mystery-box/records`,
      method: "GET",
      data: { uid }
    });
  }
};
const quoteApi = {
  // 获取今日日签
  getDailyQuote: () => {
    return request({
      url: "/api/quotes/custom",
      method: "GET"
    });
  },
  // 获取用户日签
  getUserQuotes: (uid) => {
    return request({
      url: `/api/quotes/random/user`,
      method: "GET",
      data: { uid }
    });
  },
  // 创建日签
  createQuote: (data) => {
    return request({
      url: "/api/quotes/custom",
      method: "POST",
      data
    });
  },
  // 更新日签
  updateQuote: (id, data) => {
    return request({
      url: `/api/quotes/custom/${id}`,
      method: "PUT",
      data
    });
  },
  // 删除日签
  deleteQuote: (id) => {
    return request({
      url: `/api/quotes/custom/${id}`,
      method: "DELETE"
    });
  },
  // 获取日签历史
  getQuoteHistory: (uid) => {
    return request({
      url: "/api/quotes/history",
      method: "GET",
      data: { uid }
    });
  }
};
const aiApi = {
  // 智能对话
  chat: (data) => {
    return request({
      url: "/app/ai/query",
      method: "POST",
      data: {
        message: data.message || data.question || "",
        timestamp: data.timestamp || Date.now()
      }
    });
  },
  // 基于日记的智能分析
  diaryAnalysis: (diaryContent) => {
    return request({
      url: "/app/ai/query",
      method: "POST",
      data: {
        message: `请分析以下日记内容并给出情绪建议：${diaryContent}`,
        timestamp: Date.now()
      }
    });
  },
  // 获取常见问题
  getCommonQuestions: () => {
    return request({
      url: "/app/ai/query",
      method: "POST",
      data: {
        message: "请给我一些常见的心理健康问题建议",
        timestamp: Date.now()
      }
    });
  }
};
const recommendationApi = {
  get: () => {
    return request({
      url: "/api/knowledge/recommendation/by-emotion?days=7",
      method: "GET"
    });
  }
};
const fileApi = {
  // 上传文件
  uploadFile: (filePath, type = "image") => {
    return new Promise((resolve, reject) => {
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      const uid = userInfo ? userInfo.uid : null;
      common_vendor.index.uploadFile({
        url: BASE_URL + "/api/uploads/media",
        filePath,
        name: "file",
        header: {
          "uid": uid || ""
        },
        formData: {
          type
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data);
            } else {
              reject(new Error(data.message || "上传失败"));
            }
          } catch (e) {
            reject(new Error("上传失败"));
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || "上传失败"));
        }
      });
    });
  }
};
exports.aiApi = aiApi;
exports.anonymousApi = anonymousApi;
exports.capsuleApi = capsuleApi;
exports.diaryApi = diaryApi;
exports.emotionApi = emotionApi;
exports.fileApi = fileApi;
exports.mysteryBoxApi = mysteryBoxApi;
exports.nlpEmotionApi = nlpEmotionApi;
exports.quoteApi = quoteApi;
exports.recommendationApi = recommendationApi;
exports.userApi = userApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/api.js.map
