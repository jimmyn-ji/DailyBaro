"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      quoteContent: "",
      quoteTags: "",
      selectedMood: "happy",
      isPublic: true,
      selectedTemplate: null,
      moods: [
        { value: "happy", label: "开心", emoji: "😊" },
        { value: "sad", label: "难过", emoji: "😢" },
        { value: "angry", label: "愤怒", emoji: "😠" },
        { value: "calm", label: "平静", emoji: "😌" },
        { value: "excited", label: "兴奋", emoji: "🤩" },
        { value: "tired", label: "疲惫", emoji: "😴" }
      ],
      templates: [
        { id: 1, content: "今天又是美好的一天", author: "生活" },
        { id: 2, content: "保持微笑，保持希望", author: "乐观" },
        { id: 3, content: "每一个当下都是礼物", author: "感恩" },
        { id: 4, content: "相信自己，你可以的", author: "鼓励" },
        { id: 5, content: "慢下来，感受生活的美好", author: "慢生活" },
        { id: 6, content: "今天也要加油哦", author: "正能量" }
      ]
    };
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
    selectTemplate(templateId) {
      this.selectedTemplate = templateId;
      const template = this.templates.find((t) => t.id === templateId);
      if (template) {
        this.quoteContent = template.content;
      }
    },
    async createQuote() {
      if (!this.quoteContent.trim()) {
        common_vendor.index.showToast({
          title: "请输入日签内容",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({ title: "创建中..." });
        const quoteData = {
          content: this.quoteContent,
          tags: this.quoteTags,
          mood: this.selectedMood,
          isPublic: this.isPublic
        };
        setTimeout(() => {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "创建成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        }, 1e3);
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "创建失败",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "4f"),
    b: common_vendor.o((...args) => $options.createQuote && $options.createQuote(...args), "3c"),
    c: $data.quoteContent,
    d: common_vendor.o(($event) => $data.quoteContent = $event.detail.value, "91"),
    e: $data.quoteTags,
    f: common_vendor.o(($event) => $data.quoteTags = $event.detail.value, "c7"),
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
    i: common_vendor.o((...args) => $options.togglePublic && $options.togglePublic(...args), "cc"),
    j: common_vendor.f($data.templates, (template, k0, i0) => {
      return {
        a: common_vendor.t(template.content),
        b: common_vendor.t(template.author),
        c: template.id,
        d: $data.selectedTemplate === template.id ? 1 : "",
        e: common_vendor.o(($event) => $options.selectTemplate(template.id), template.id)
      };
    })
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-75adcf45"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/quotes/create.js.map
