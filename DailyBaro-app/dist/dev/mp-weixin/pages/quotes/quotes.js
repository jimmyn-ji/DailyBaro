"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      dailyQuote: null,
      myQuotes: [],
      loading: false
    };
  },
  onLoad() {
    this.loadDailyQuote();
    this.loadMyQuotes();
  },
  methods: {
    // 返回方法
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 跳转到创建日签页面
    goToCreate() {
      common_vendor.index.navigateTo({
        url: "/pages/quotes/create"
      });
    },
    // 查看日签详情
    viewQuote(id) {
      common_vendor.index.navigateTo({
        url: `/pages/quotes/detail?id=${id}`
      });
    },
    // 编辑日签
    editQuote(id) {
      common_vendor.index.navigateTo({
        url: `/pages/quotes/edit?id=${id}`
      });
    },
    // 删除日签
    deleteQuote(id) {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这个日签吗？删除后无法恢复。",
        success: async (res) => {
          if (res.confirm) {
            try {
              common_vendor.index.showLoading({ title: "删除中..." });
              const result = await utils_api.quoteApi.deleteQuote(id);
              common_vendor.index.hideLoading();
              if (result.code === 200) {
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
                this.loadMyQuotes();
              } else {
                throw new Error(result.message || "删除失败");
              }
            } catch (error) {
              common_vendor.index.hideLoading();
              common_vendor.index.showToast({
                title: error.message || "删除失败",
                icon: "none"
              });
            }
          }
        }
      });
    },
    // 加载今日日签
    async loadDailyQuote() {
      try {
        const res = await utils_api.quoteApi.getDailyQuote();
        if (res.code === 200) {
          this.dailyQuote = res.data;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/quotes/quotes.vue:168", "加载今日日签失败:", error);
      }
    },
    // 加载我的日签
    async loadMyQuotes() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          return;
        }
        const res = await utils_api.quoteApi.getUserQuotes(userInfo.uid);
        if (res.code === 200) {
          this.myQuotes = res.data || [];
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/quotes/quotes.vue:186", "加载我的日签失败:", error);
      }
    },
    // 格式化日期
    formatDate(date) {
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit"
      });
    },
    // 格式化时间
    formatTime(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      const now = /* @__PURE__ */ new Date();
      const diffTime = Math.abs(now - date);
      const diffDays = Math.ceil(diffTime / (1e3 * 60 * 60 * 24));
      if (diffDays === 1) {
        return "昨天";
      } else if (diffDays === 0) {
        return "今天";
      } else if (diffDays < 7) {
        return `${diffDays}天前`;
      } else {
        return date.toLocaleDateString("zh-CN", {
          month: "2-digit",
          day: "2-digit"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  var _a, _b;
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "84"),
    b: common_vendor.o((...args) => $options.goToCreate && $options.goToCreate(...args), "3c"),
    c: common_vendor.t(((_a = $data.dailyQuote) == null ? void 0 : _a.content) || "今天也值得被温柔对待"),
    d: common_vendor.t(((_b = $data.dailyQuote) == null ? void 0 : _b.author) || "DailyBaro"),
    e: common_vendor.t($options.formatDate(/* @__PURE__ */ new Date())),
    f: common_vendor.f($data.myQuotes, (quote, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(quote.title || "我的日签"),
        b: common_vendor.t($options.formatTime(quote.createTime)),
        c: common_vendor.t(quote.content),
        d: quote.emotion
      }, quote.emotion ? {
        e: common_vendor.t(quote.emotion)
      } : {}, {
        f: quote.category
      }, quote.category ? {
        g: common_vendor.t(quote.category)
      } : {}, {
        h: common_vendor.o(($event) => $options.editQuote(quote.id), quote.id),
        i: common_vendor.o(($event) => $options.deleteQuote(quote.id), quote.id),
        j: quote.id,
        k: common_vendor.o(($event) => $options.viewQuote(quote.id), quote.id)
      });
    }),
    g: $data.myQuotes.length === 0
  }, $data.myQuotes.length === 0 ? {
    h: common_vendor.o((...args) => $options.goToCreate && $options.goToCreate(...args), "04")
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-5d34c910"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/quotes/quotes.js.map
