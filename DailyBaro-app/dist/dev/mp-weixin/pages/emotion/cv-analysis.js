"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_config = require("../../utils/config.js");
const _sfc_main = {
  data() {
    return {
      imageUrl: "",
      analysisResult: null,
      isAnalyzing: false
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
    chooseImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          this.imageUrl = res.tempFilePaths[0];
          this.analysisResult = null;
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/emotion/cv-analysis.vue:109", "选择图片失败:", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    },
    async analyzeImage() {
      if (!this.imageUrl) {
        common_vendor.index.showToast({
          title: "请先选择图片",
          icon: "none"
        });
        return;
      }
      this.isAnalyzing = true;
      try {
        const baseUrl = this.getBaseUrl();
        const response = await common_vendor.index.uploadFile({
          url: baseUrl + "/api/nlp/emotion/analyze/image",
          filePath: this.imageUrl,
          name: "image",
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
                common_vendor.index.showToast({
                  title: (data == null ? void 0 : data.message) || "分析失败",
                  icon: "none"
                });
              }
            } catch (e) {
              common_vendor.index.__f__("error", "at pages/emotion/cv-analysis.vue:149", "响应解析失败:", e);
              common_vendor.index.showToast({
                title: "响应解析失败",
                icon: "none"
              });
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/emotion/cv-analysis.vue:157", "图片上传失败:", err);
            common_vendor.index.showToast({
              title: "网络错误，请重试",
              icon: "none"
            });
          }
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/emotion/cv-analysis.vue:165", "图片分析失败:", error);
        common_vendor.index.showToast({
          title: "分析失败，请重试",
          icon: "none"
        });
      } finally {
        this.isAnalyzing = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "31"),
    b: !$data.imageUrl
  }, !$data.imageUrl ? {
    c: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args), "91")
  } : {}, {
    d: $data.imageUrl
  }, $data.imageUrl ? {
    e: $data.imageUrl,
    f: common_vendor.o((...args) => $options.chooseImage && $options.chooseImage(...args), "13"),
    g: common_vendor.o((...args) => $options.analyzeImage && $options.analyzeImage(...args), "f2")
  } : {}, {
    h: $data.analysisResult
  }, $data.analysisResult ? common_vendor.e({
    i: common_vendor.t($data.analysisResult.primaryEmotion),
    j: common_vendor.t($data.analysisResult.confidence),
    k: common_vendor.t($data.analysisResult.intensity),
    l: $data.analysisResult.details
  }, $data.analysisResult.details ? {
    m: common_vendor.f($data.analysisResult.details, (detail, index, i0) => {
      return {
        a: common_vendor.t(detail.emotion),
        b: common_vendor.t(detail.probability),
        c: index
      };
    })
  } : {}) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e88778a4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/emotion/cv-analysis.js.map
