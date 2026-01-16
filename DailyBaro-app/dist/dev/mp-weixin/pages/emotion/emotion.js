"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      emotionOptions: ["开心", "兴奋", "满足", "平静", "放松", "焦虑", "紧张", "愤怒", "悲伤", "沮丧", "惊讶", "困惑", "期待", "信任", "其他"],
      emotionIndex: 0,
      emotionIntensity: 5,
      emotionDescription: "",
      analysisHistory: [],
      loading: false,
      // NLP相关
      nlpServiceStatus: null,
      aiAnalyzing: false,
      aiAnalysisResult: null
    };
  },
  computed: {
    selectedEmotion() {
      return this.emotionOptions[this.emotionIndex] || "请选择情绪";
    },
    canAnalyze() {
      return this.emotionDescription.trim() && (this.aiAnalysisResult || !this.aiAnalysisResult);
    }
  },
  onLoad() {
    this.checkNlpService();
    this.loadAnalysisHistory();
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    goToVisualization() {
      common_vendor.index.showToast({
        title: "Go to visualization",
        icon: "success"
      });
    },
    goToCvAnalysis() {
      common_vendor.index.navigateTo({
        url: "/pages/emotion/cv-analysis"
      });
    },
    goToAudioAnalysis() {
      common_vendor.index.navigateTo({
        url: "/pages/emotion/audio-analysis"
      });
    },
    goToRecommendations() {
      common_vendor.index.navigateTo({
        url: "/pages/recommendations/recommendations"
      });
    },
    onEmotionChange(e) {
      this.emotionIndex = e.detail.value;
    },
    onIntensityChange(e) {
      this.emotionIntensity = e.detail.value;
    },
    onAiIntensityChange(e) {
      if (this.aiAnalysisResult) {
        this.aiAnalysisResult.intensity = e.detail.value;
      }
    },
    // 检查NLP服务状态
    async checkNlpService() {
      try {
        const response = await utils_api.nlpEmotionApi.healthCheck();
        common_vendor.index.__f__("log", "at pages/emotion/emotion.vue:275", "NLP服务健康检查响应:", response);
        if (response && response.available) {
          this.nlpServiceStatus = {
            ready: true,
            message: "BERT模型已加载，可以开始智能分析"
          };
        } else {
          common_vendor.index.__f__("warn", "at pages/emotion/emotion.vue:284", "NLP服务不可用，状态码:", response == null ? void 0 : response.status);
          this.nlpServiceStatus = {
            ready: false,
            message: `NLP服务不可用 (状态码: ${(response == null ? void 0 : response.status) || "未知"})，将使用后端分析服务`
          };
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/emotion/emotion.vue:291", "NLP服务检查失败:", error);
        this.nlpServiceStatus = {
          ready: false,
          message: "NLP服务检查失败，将使用后端分析服务"
        };
      }
    },
    // AI智能情绪分析
    async aiAnalyzeEmotion() {
      if (!this.emotionDescription.trim()) {
        common_vendor.index.showToast({
          title: "请先输入文本描述",
          icon: "none"
        });
        return;
      }
      this.aiAnalyzing = true;
      try {
        const response = await utils_api.nlpEmotionApi.analyzeEmotion(this.emotionDescription);
        if (response && response.code === 200) {
          this.aiAnalysisResult = response.data;
          common_vendor.index.showToast({
            title: "AI分析完成",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "AI分析失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/emotion/emotion.vue:329", "AI情绪分析失败:", error);
        common_vendor.index.showToast({
          title: "AI分析失败，请检查服务状态",
          icon: "none"
        });
      } finally {
        this.aiAnalyzing = false;
      }
    },
    // 获取情绪样式类
    getEmotionClass(emotion) {
      const positiveEmotions = ["开心", "兴奋", "满足", "平静", "放松", "期待", "信任"];
      const negativeEmotions = ["焦虑", "紧张", "愤怒", "悲伤", "沮丧", "困惑"];
      if (positiveEmotions.includes(emotion)) return "positive";
      if (negativeEmotions.includes(emotion)) return "negative";
      return "neutral";
    },
    async analyzeEmotion() {
      if (!this.emotionDescription.trim()) {
        common_vendor.index.showToast({
          title: "请先输入情绪描述",
          icon: "none"
        });
        return;
      }
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        let analysisData = {};
        if (this.aiAnalysisResult) {
          analysisData = {
            uid: userInfo.uid,
            emotion: this.aiAnalysisResult.emotion,
            intensity: this.aiAnalysisResult.intensity,
            description: this.emotionDescription,
            aiAnalyzed: true,
            confidence: this.aiAnalysisResult.confidence,
            polarity: this.aiAnalysisResult.polarity,
            allEmotions: this.aiAnalysisResult.all_emotions
          };
        } else {
          analysisData = {
            uid: userInfo.uid,
            emotion: this.emotionOptions[this.emotionIndex],
            intensity: this.emotionIntensity,
            description: this.emotionDescription,
            aiAnalyzed: false
          };
        }
        const response = await utils_api.emotionApi.analyzeEmotion(analysisData);
        if (response && response.code === 200) {
          common_vendor.index.showToast({
            title: "分析完成",
            icon: "success"
          });
          this.emotionDescription = "";
          this.emotionIntensity = 5;
          this.aiAnalysisResult = null;
          this.loadAnalysisHistory();
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "分析失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/emotion/emotion.vue:415", "情绪分析失败:", error);
        common_vendor.index.showToast({
          title: "分析失败，请重试",
          icon: "none"
        });
      }
    },
    async loadAnalysisHistory() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !(userInfo.uid || userInfo.id || userInfo.userId)) {
          this.analysisHistory = [];
          return;
        }
        const uid = userInfo.uid || userInfo.id || userInfo.userId;
        const res = await utils_api.emotionApi.getAnalysisHistory(uid);
        if (res && res.code === 200) {
          this.analysisHistory = res.data || [];
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/emotion/emotion.vue:436", "加载情绪分析历史失败:", e);
      }
    },
    viewAnalysis(id) {
      common_vendor.index.showToast({
        title: `View analysis ${id}`,
        icon: "success"
      });
    },
    formatTime(dateString) {
      const date = new Date(dateString);
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${month}-${day}`;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "7a"),
    b: common_vendor.o((...args) => $options.goToCvAnalysis && $options.goToCvAnalysis(...args), "49"),
    c: common_vendor.o((...args) => $options.goToAudioAnalysis && $options.goToAudioAnalysis(...args), "c8"),
    d: common_vendor.o((...args) => $options.goToVisualization && $options.goToVisualization(...args), "29"),
    e: common_vendor.o((...args) => $options.goToRecommendations && $options.goToRecommendations(...args), "53"),
    f: $data.nlpServiceStatus
  }, $data.nlpServiceStatus ? {
    g: common_vendor.t($data.nlpServiceStatus.ready ? "NLP服务就绪" : "NLP服务未连接"),
    h: common_vendor.n($data.nlpServiceStatus.ready ? "ready" : "error"),
    i: common_vendor.t($data.nlpServiceStatus.message)
  } : {}, {
    j: $data.emotionDescription,
    k: common_vendor.o(($event) => $data.emotionDescription = $event.detail.value, "3d"),
    l: common_vendor.t($data.emotionDescription.length),
    m: $data.nlpServiceStatus && $data.nlpServiceStatus.ready
  }, $data.nlpServiceStatus && $data.nlpServiceStatus.ready ? {
    n: common_vendor.t($data.aiAnalyzing ? "AI分析中..." : "AI智能识别情绪"),
    o: common_vendor.o((...args) => $options.aiAnalyzeEmotion && $options.aiAnalyzeEmotion(...args), "82"),
    p: !$data.emotionDescription.trim() || $data.aiAnalyzing
  } : {}, {
    q: $data.aiAnalysisResult
  }, $data.aiAnalysisResult ? {
    r: common_vendor.t(($data.aiAnalysisResult.confidence * 100).toFixed(1)),
    s: common_vendor.t($data.aiAnalysisResult.emotion),
    t: common_vendor.n($data.aiAnalysisResult.polarity),
    v: $data.aiAnalysisResult.intensity,
    w: common_vendor.o((...args) => $options.onAiIntensityChange && $options.onAiIntensityChange(...args), "e1"),
    x: common_vendor.t($data.aiAnalysisResult.intensity),
    y: common_vendor.t($data.aiAnalysisResult.polarity === "positive" ? "正面" : "负面"),
    z: common_vendor.n($data.aiAnalysisResult.polarity),
    A: common_vendor.f($data.aiAnalysisResult.all_emotions, (score, emotion, i0) => {
      return {
        a: common_vendor.t(emotion),
        b: score * 100 + "%",
        c: common_vendor.n($options.getEmotionClass(emotion)),
        d: common_vendor.t((score * 100).toFixed(1)),
        e: emotion
      };
    })
  } : {}, {
    B: !$data.aiAnalysisResult
  }, !$data.aiAnalysisResult ? {
    C: common_vendor.t($options.selectedEmotion),
    D: $data.emotionOptions,
    E: $data.emotionIndex,
    F: common_vendor.o((...args) => $options.onEmotionChange && $options.onEmotionChange(...args), "7e"),
    G: $data.emotionIntensity,
    H: common_vendor.o((...args) => $options.onIntensityChange && $options.onIntensityChange(...args), "e8"),
    I: common_vendor.t($data.emotionIntensity)
  } : {}, {
    J: common_vendor.t($data.aiAnalysisResult ? "保存AI分析结果" : "手动分析情绪"),
    K: common_vendor.o((...args) => $options.analyzeEmotion && $options.analyzeEmotion(...args), "33"),
    L: !$options.canAnalyze,
    M: $data.analysisHistory.length > 0
  }, $data.analysisHistory.length > 0 ? {
    N: common_vendor.f($data.analysisHistory, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.emotion),
        b: common_vendor.n($options.getEmotionClass(item.emotion)),
        c: common_vendor.t($options.formatTime(item.createTime)),
        d: common_vendor.t(item.description),
        e: common_vendor.t(item.intensity),
        f: common_vendor.t(item.aiAnalyzed ? "🤖" : "✓"),
        g: common_vendor.t(item.aiAnalyzed ? "AI分析" : "手动分析"),
        h: item.id,
        i: common_vendor.o(($event) => $options.viewAnalysis(item.id), item.id)
      };
    })
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0b75ea60"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/emotion/emotion.js.map
