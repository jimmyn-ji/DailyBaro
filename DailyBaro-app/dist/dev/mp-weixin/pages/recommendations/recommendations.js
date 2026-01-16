"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return { loading: false, data: null };
  },
  onLoad() {
    this.load();
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    async load() {
      this.loading = true;
      try {
        const res = await utils_api.recommendationApi.get();
        if (res && res.code === 200) {
          this.data = res.data || null;
        } else {
          common_vendor.index.showToast({ title: (res == null ? void 0 : res.message) || "加载失败", icon: "none" });
        }
      } catch (e) {
        common_vendor.index.__f__("error", "at pages/recommendations/recommendations.vue:52", "加载推荐失败", e);
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "ea"),
    b: common_vendor.t($data.loading ? "加载中..." : "刷新推荐"),
    c: common_vendor.o((...args) => $options.load && $options.load(...args), "a7"),
    d: $data.loading,
    e: $data.data
  }, $data.data ? common_vendor.e({
    f: common_vendor.t($data.data.dominantEmotion),
    g: $data.data.advice
  }, $data.data.advice ? {
    h: common_vendor.t($data.data.advice.tip)
  } : {}, {
    i: common_vendor.f($data.data.items, (item, idx, i0) => {
      return {
        a: common_vendor.t(item.title),
        b: common_vendor.t(item.energyRequired),
        c: idx
      };
    })
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1bca7cae"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/recommendations/recommendations.js.map
