"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      userInfo: {},
      recommendations: null,
      loadingRecommendations: false,
      dailyQuote: null
    };
  },
  onLoad() {
    this.loadUserInfo();
    this.loadRecommendations();
    this.loadDailyQuote();
  },
  onShow() {
    this.loadUserInfo();
    this.loadRecommendations();
    this.loadDailyQuote();
  },
  methods: {
    async changeAvatar() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: async (res) => {
          const tempFilePath = res.tempFilePaths[0];
          try {
            common_vendor.index.showLoading({ title: "上传中..." });
            const uploadRes = await utils_api.fileApi.uploadFile(tempFilePath, "avatar");
            common_vendor.index.__f__("log", "at pages/profile/profile.vue:144", "头像上传响应:", uploadRes);
            if (uploadRes && uploadRes.code === 200) {
              let avatarUrl = null;
              if (uploadRes.data) {
                if (typeof uploadRes.data === "string") {
                  avatarUrl = uploadRes.data;
                } else if (uploadRes.data.url) {
                  avatarUrl = uploadRes.data.url;
                } else if (uploadRes.data.fileUrl) {
                  avatarUrl = uploadRes.data.fileUrl;
                } else if (uploadRes.data.path) {
                  avatarUrl = uploadRes.data.path;
                } else if (uploadRes.data.filename) {
                  avatarUrl = `/api/file/uploads/${uploadRes.data.filename}`;
                }
              }
              if (!avatarUrl) {
                common_vendor.index.__f__("error", "at pages/profile/profile.vue:165", "无法从响应中提取头像URL:", uploadRes);
                common_vendor.index.showToast({
                  title: "上传成功但无法获取头像地址",
                  icon: "none"
                });
                return;
              }
              if (avatarUrl.startsWith("/uploads/")) {
                avatarUrl = `/api/file${avatarUrl}`;
              } else if (avatarUrl.startsWith("uploads/")) {
                avatarUrl = `/api/file/${avatarUrl}`;
              }
              common_vendor.index.__f__("log", "at pages/profile/profile.vue:180", "处理后的头像URL:", avatarUrl);
              const userInfo = common_vendor.index.getStorageSync("userInfo");
              if (userInfo && userInfo.uid) {
                const updateRes = await utils_api.userApi.updateUserInfo(userInfo.uid, { avatar: avatarUrl });
                if (updateRes && updateRes.code === 200) {
                  this.userInfo.avatar = avatarUrl;
                  userInfo.avatar = avatarUrl;
                  common_vendor.index.setStorageSync("userInfo", userInfo);
                  common_vendor.index.showToast({
                    title: "头像更新成功",
                    icon: "success"
                  });
                } else {
                  common_vendor.index.showToast({
                    title: (updateRes == null ? void 0 : updateRes.message) || "更新失败",
                    icon: "none"
                  });
                }
              }
            } else {
              const errorMsg = (uploadRes == null ? void 0 : uploadRes.message) || (uploadRes == null ? void 0 : uploadRes.msg) || "上传失败";
              common_vendor.index.__f__("error", "at pages/profile/profile.vue:202", "头像上传失败:", errorMsg, uploadRes);
              common_vendor.index.showToast({
                title: errorMsg,
                icon: "none",
                duration: 3e3
              });
            }
          } catch (error) {
            common_vendor.index.__f__("error", "at pages/profile/profile.vue:210", "上传头像失败:", error);
            const errorMsg = error.message || error.errMsg || "上传失败，请重试";
            common_vendor.index.showToast({
              title: errorMsg,
              icon: "none",
              duration: 3e3
            });
          } finally {
            common_vendor.index.hideLoading();
          }
        }
      });
    },
    async loadUserInfo() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (userInfo && userInfo.uid) {
          const response = await utils_api.userApi.getUserInfo(userInfo.uid);
          if (response && response.code === 200) {
            this.userInfo = response.data;
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/profile.vue:234", "加载用户信息失败:", error);
      }
    },
    async loadDailyQuote() {
      try {
        const response = await utils_api.quoteApi.getDailyQuote();
        if (response && response.code === 200) {
          this.dailyQuote = response.data || { content: "今天也值得被温柔对待" };
        } else {
          try {
            const userInfo = common_vendor.index.getStorageSync("userInfo");
            const uid = userInfo && (userInfo.uid || userInfo.id || userInfo.userId);
            if (uid) {
              const randomResponse = await utils_api.quoteApi.getUserQuotes(uid);
              if (randomResponse && randomResponse.code === 200) {
                this.dailyQuote = randomResponse.data || { content: "今天也值得被温柔对待" };
              } else {
                this.dailyQuote = { content: "今天也值得被温柔对待" };
              }
            } else {
              this.dailyQuote = { content: "今天也值得被温柔对待" };
            }
          } catch (error) {
            common_vendor.index.__f__("error", "at pages/profile/profile.vue:259", "加载随机日签失败:", error);
            this.dailyQuote = { content: "今天也值得被温柔对待" };
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/profile.vue:264", "加载日签失败:", error);
        this.dailyQuote = { content: "今天也值得被温柔对待" };
      }
    },
    goToEmotion() {
      common_vendor.index.navigateTo({
        url: "/pages/emotion/emotion"
      });
    },
    goToQuotes() {
      common_vendor.index.navigateTo({
        url: "/pages/quotes/quotes"
      });
    },
    goToAI() {
      common_vendor.index.navigateTo({
        url: "/pages/ai/ai"
      });
    },
    // 获取情绪样式类（用于情绪标签）
    getEmotionClass(emotion) {
      const positiveEmotions = ["开心", "兴奋", "满足", "平静", "放松", "期待", "信任"];
      const negativeEmotions = ["焦虑", "紧张", "愤怒", "悲伤", "沮丧", "困惑"];
      if (positiveEmotions.includes(emotion)) return "positive";
      if (negativeEmotions.includes(emotion)) return "negative";
      return "neutral";
    },
    showSettings() {
      common_vendor.index.showToast({
        title: "设置功能开发中",
        icon: "none"
      });
    },
    showAbout() {
      common_vendor.index.showToast({
        title: "关于功能开发中",
        icon: "none"
      });
    },
    logout() {
      common_vendor.index.showModal({
        title: "确认退出",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.clearStorageSync();
            common_vendor.index.reLaunch({
              url: "/pages/login/login"
            });
          }
        }
      });
    },
    async loadRecommendations() {
      this.loadingRecommendations = true;
      this.recommendations = null;
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (userInfo && userInfo.uid) {
          const response = await utils_api.userApi.getRecommendations();
          if (response && response.code === 200) {
            this.$set(this, "recommendations", response.data);
            common_vendor.index.showToast({
              title: "推荐已更新",
              icon: "success",
              duration: 1500
            });
          } else {
            this.recommendations = null;
            common_vendor.index.showToast({
              title: (response == null ? void 0 : response.message) || "加载失败",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: "用户未登录",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/profile.vue:356", "加载推荐失败:", error);
        this.recommendations = null;
        common_vendor.index.showToast({
          title: "加载失败，请重试",
          icon: "none"
        });
      } finally {
        this.loadingRecommendations = false;
      }
    },
    tryActivity(activity) {
      common_vendor.index.showModal({
        title: "提示",
        content: `确定要尝试活动 "${activity.title}" 吗？这将消耗 ${activity.energyRequired} 能量。`,
        success: (res) => {
          if (res.confirm) {
            this.performActivity(activity);
          }
        }
      });
    },
    async performActivity(activity) {
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (userInfo && userInfo.uid) {
        try {
          const response = await utils_api.userApi.performActivity(userInfo.uid, activity.id);
          if (response && response.code === 200) {
            common_vendor.index.showToast({
              title: "活动成功完成！",
              icon: "none"
            });
            this.loadUserInfo();
            this.loadRecommendations();
          } else {
            common_vendor.index.showToast({
              title: response.msg || "活动失败",
              icon: "none"
            });
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/profile/profile.vue:398", "执行活动失败:", error);
          common_vendor.index.showToast({
            title: "执行活动失败",
            icon: "none"
          });
        }
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.userInfo.avatar || "/static/imgs/avatar.png",
    b: common_vendor.o((...args) => $options.changeAvatar && $options.changeAvatar(...args), "4e"),
    c: common_vendor.t($data.userInfo.account || "用户"),
    d: common_vendor.t($data.userInfo.uid || "未知"),
    e: common_vendor.t($data.userInfo.energy || 0),
    f: $data.dailyQuote
  }, $data.dailyQuote ? {
    g: common_vendor.t($data.dailyQuote.content || "今天也值得被温柔对待"),
    h: common_vendor.o((...args) => $options.goToQuotes && $options.goToQuotes(...args), "8c")
  } : {}, {
    i: common_vendor.o((...args) => $options.goToEmotion && $options.goToEmotion(...args), "4e"),
    j: common_vendor.o((...args) => $options.goToAI && $options.goToAI(...args), "07"),
    k: common_vendor.t($data.loadingRecommendations ? "刷新中..." : "🔄"),
    l: common_vendor.o((...args) => $options.loadRecommendations && $options.loadRecommendations(...args), "78"),
    m: $data.loadingRecommendations,
    n: $data.recommendations
  }, $data.recommendations ? common_vendor.e({
    o: common_vendor.t($data.recommendations.dominantEmotion),
    p: common_vendor.n($options.getEmotionClass($data.recommendations.dominantEmotion)),
    q: $data.recommendations.advice
  }, $data.recommendations.advice ? {
    r: common_vendor.t($data.recommendations.advice.tip)
  } : {}, {
    s: common_vendor.f($data.recommendations.items, (item, index, i0) => {
      return {
        a: common_vendor.t(item.title),
        b: common_vendor.t(item.energyRequired),
        c: common_vendor.o(($event) => $options.tryActivity(item), index),
        d: index
      };
    })
  }) : $data.loadingRecommendations ? {} : {}, {
    t: $data.loadingRecommendations,
    v: common_vendor.o((...args) => $options.logout && $options.logout(...args), "32")
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-04d37cba"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/profile.js.map
