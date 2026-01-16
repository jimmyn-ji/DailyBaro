"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      quoteId: "",
      quoteContent: "",
      quoteTags: "",
      selectedMood: "happy",
      isPublic: true,
      moods: [
        { value: "happy", label: "开心", emoji: "😊" },
        { value: "sad", label: "难过", emoji: "😢" },
        { value: "angry", label: "愤怒", emoji: "😠" },
        { value: "calm", label: "平静", emoji: "😌" },
        { value: "excited", label: "兴奋", emoji: "🤩" },
        { value: "tired", label: "疲惫", emoji: "😴" }
      ]
    };
  },
  onLoad(options) {
    if (options.id && options.id !== "undefined") {
      this.quoteId = options.id;
      this.loadQuote();
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    selectMood(mood) {
      this.selectedMood = mood;
    },
    togglePublic(e) {
      this.isPublic = e.detail.value;
    },
    async loadQuote() {
      try {
        common_vendor.index.showLoading({ title: "加载中..." });
        setTimeout(() => {
          this.quoteContent = "这是一个示例日签内容";
          this.quoteTags = "生活,感悟";
          this.selectedMood = "happy";
          this.isPublic = true;
          common_vendor.index.hideLoading();
        }, 1e3);
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      }
    },
    async saveQuote() {
      if (!this.quoteContent.trim()) {
        common_vendor.index.showToast({
          title: "请输入日签内容",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({ title: "保存中..." });
        const quoteData = {
          id: this.quoteId,
          content: this.quoteContent,
          tags: this.quoteTags,
          mood: this.selectedMood,
          isPublic: this.isPublic
        };
        setTimeout(() => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "保存成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        }, 1e3);
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "保存失败",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "7c"),
    b: common_vendor.o((...args) => $options.saveQuote && $options.saveQuote(...args), "f5"),
    c: $data.quoteContent,
    d: common_vendor.o(($event) => $data.quoteContent = $event.detail.value, "92"),
    e: $data.quoteTags,
    f: common_vendor.o(($event) => $data.quoteTags = $event.detail.value, "8d"),
    g: common_vendor.f($data.moods, (mood, k0, i0) => {
      return {
        a: common_vendor.t(mood.emoji),
        b: common_vendor.t(mood.label),
        c: mood.value,
        d: $data.selectedMood === mood.value ? 1 : "",
        e: common_vendor.o(($event) => $options.selectMood(mood.value), mood.value)
      };
    }),
    h: $data.isPublic,
    i: common_vendor.o((...args) => $options.togglePublic && $options.togglePublic(...args), "a0")
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-363e46db"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/quotes/edit.js.map
