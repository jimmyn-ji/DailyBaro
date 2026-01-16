"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_config = require("../../utils/config.js");
const _sfc_main = {
  data() {
    return {
      audioFile: null,
      analysisResult: null,
      analyzing: false,
      errorMessage: ""
    };
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 获取基础URL
    getBaseUrl() {
      return utils_config.config.BASE_URL;
    },
    chooseAudio() {
      if (common_vendor.index.chooseAudio && typeof common_vendor.index.chooseAudio === "function") {
        common_vendor.index.chooseAudio({
          count: 1,
          success: (res) => {
            this.audioFile = {
              tempFilePath: res.tempFilePath,
              name: res.tempFilePath.split("/").pop() || "audio.mp3",
              size: res.size || 0
            };
            this.clearError();
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/emotion/audio-analysis.vue:148", "chooseAudio 选择音频失败:", err);
            if (err.errMsg && err.errMsg.includes("cancel")) {
              return;
            }
            this.chooseAudioWithChooseMedia();
          }
        });
      } else {
        this.chooseAudioWithChooseMedia();
      }
    },
    // 使用 chooseMedia 选择音频（备选方案）
    chooseAudioWithChooseMedia() {
      if (common_vendor.index.chooseMedia && typeof common_vendor.index.chooseMedia === "function") {
        common_vendor.index.chooseMedia({
          count: 1,
          mediaType: ["audio"],
          sourceType: ["album", "camera"],
          success: (res) => {
            common_vendor.index.__f__("log", "at pages/emotion/audio-analysis.vue:171", "chooseMedia 选择音频成功:", res);
            if (res.tempFiles && res.tempFiles.length > 0) {
              const file = res.tempFiles[0];
              this.audioFile = {
                tempFilePath: file.tempFilePath,
                name: file.name || file.tempFilePath.split("/").pop() || "audio.mp3",
                size: file.size || 0
              };
              this.clearError();
            } else {
              this.errorMessage = "未选择音频文件";
              common_vendor.index.showToast({
                title: "未选择音频文件",
                icon: "none"
              });
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/emotion/audio-analysis.vue:189", "chooseMedia 选择音频失败:", err);
            if (err.errMsg && err.errMsg.includes("cancel")) {
              return;
            }
            this.errorMessage = "选择音频失败，请重试";
            common_vendor.index.showToast({
              title: "选择音频失败",
              icon: "none"
            });
          }
        });
      } else {
        common_vendor.index.showToast({
          title: "当前环境不支持音频选择",
          icon: "none",
          duration: 2e3
        });
      }
    },
    removeAudio() {
      this.audioFile = null;
      this.analysisResult = null;
      this.clearError();
    },
    formatFileSize(size) {
      if (size === 0) return "0 Bytes";
      const k = 1024;
      const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
      const i = Math.floor(Math.log(size) / Math.log(k));
      return parseFloat((size / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    },
    async analyzeAudio() {
      if (!this.audioFile) {
        this.errorMessage = "请先选择音频文件";
        return;
      }
      this.analyzing = true;
      this.clearError();
      try {
        const baseUrl = this.getBaseUrl();
        const response = await common_vendor.index.uploadFile({
          url: baseUrl + "/api/nlp/emotion/analyze/audio",
          filePath: this.audioFile.tempFilePath,
          name: "audio",
          success: (res) => {
            try {
              const data = JSON.parse(res.data);
              if (data && data.code === 200) {
                this.analysisResult = data.data;
                common_vendor.index.showToast({
                  title: "分析完成",
                  icon: "success"
                });
              } else {
                this.errorMessage = (data == null ? void 0 : data.message) || "分析失败，请重试";
              }
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/emotion/audio-analysis.vue:252", "响应解析失败:", e);
              this.errorMessage = "响应解析失败，请重试";
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/emotion/audio-analysis.vue:257", "音频上传失败:", err);
            this.errorMessage = "网络错误，请检查连接后重试";
          }
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/emotion/audio-analysis.vue:262", "音频分析失败:", error);
        this.errorMessage = "网络错误，请检查连接后重试";
      } finally {
        this.analyzing = false;
      }
    },
    clearError() {
      this.errorMessage = "";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "88"),
    b: !$data.audioFile
  }, !$data.audioFile ? {
    c: common_vendor.o((...args) => $options.chooseAudio && $options.chooseAudio(...args), "83")
  } : {}, {
    d: $data.audioFile
  }, $data.audioFile ? {
    e: common_vendor.t($data.audioFile.name),
    f: common_vendor.t($options.formatFileSize($data.audioFile.size)),
    g: common_vendor.o((...args) => $options.removeAudio && $options.removeAudio(...args), "aa"),
    h: common_vendor.t($data.analyzing ? "分析中..." : "开始分析"),
    i: common_vendor.o((...args) => $options.analyzeAudio && $options.analyzeAudio(...args), "2f"),
    j: $data.analyzing
  } : {}, {
    k: $data.analysisResult
  }, $data.analysisResult ? common_vendor.e({
    l: common_vendor.t($data.analysisResult.timestamp),
    m: common_vendor.t($data.analysisResult.emotion),
    n: $data.analysisResult.confidence * 100 + "%",
    o: common_vendor.t(($data.analysisResult.confidence * 100).toFixed(1)),
    p: common_vendor.f(10, (i, k0, i0) => {
      return {
        a: i,
        b: i <= $data.analysisResult.intensity ? 1 : ""
      };
    }),
    q: common_vendor.t($data.analysisResult.intensity.toFixed(1)),
    r: common_vendor.f($data.analysisResult.top_emotions, (emotion, k0, i0) => {
      return {
        a: common_vendor.t(emotion.emotion),
        b: emotion.score * 100 + "%",
        c: common_vendor.t((emotion.score * 100).toFixed(1)),
        d: emotion.emotion
      };
    }),
    s: $data.analysisResult.audio_features
  }, $data.analysisResult.audio_features ? {
    t: common_vendor.t($data.analysisResult.audio_features.duration.toFixed(2)),
    v: common_vendor.t($data.analysisResult.audio_features.sample_rate),
    w: common_vendor.t($data.analysisResult.audio_features.energy.toFixed(4)),
    x: common_vendor.t($data.analysisResult.audio_features.pitch.toFixed(1))
  } : {}) : {}, {
    y: $data.errorMessage
  }, $data.errorMessage ? {
    z: common_vendor.t($data.errorMessage),
    A: common_vendor.o((...args) => $options.clearError && $options.clearError(...args), "74")
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e25d4696"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/emotion/audio-analysis.js.map
