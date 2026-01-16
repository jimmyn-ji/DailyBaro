"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      form: {
        content: "",
        visibility: "public"
      }
    };
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    async publishPost() {
      if (!this.form.content.trim()) {
        common_vendor.index.showToast({
          title: "请输入动态内容",
          icon: "none"
        });
        return;
      }
      if (this.form.content.length < 5) {
        common_vendor.index.showToast({
          title: "内容至少5个字符",
          icon: "none"
        });
        return;
      }
      try {
        const res = await utils_api.anonymousApi.createPost(this.form);
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "发布成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "发布失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "发布失败",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "fe"),
    b: common_vendor.o((...args) => $options.publishPost && $options.publishPost(...args), "86"),
    c: $data.form.content,
    d: common_vendor.o(($event) => $data.form.content = $event.detail.value, "46"),
    e: common_vendor.t($data.form.content.length),
    f: common_vendor.t($data.form.visibility === "public" ? "✓" : ""),
    g: $data.form.visibility === "public" ? 1 : "",
    h: common_vendor.o(($event) => $data.form.visibility = "public", "7f"),
    i: common_vendor.t($data.form.visibility === "private" ? "✓" : ""),
    j: $data.form.visibility === "private" ? 1 : "",
    k: common_vendor.o(($event) => $data.form.visibility = "private", "55")
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1bd4a8e4"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/anonymous/post.js.map
