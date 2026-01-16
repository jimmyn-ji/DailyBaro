"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      quoteId: "",
      quote: {},
      recommendations: []
    };
  },
  onLoad(options) {
    if (options.id) {
      this.quoteId = options.id;
      this.loadQuoteDetail();
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    editQuote() {
      common_vendor.index.navigateTo({
        url: `/pages/quotes/edit?id=${this.quoteId}`
      });
    },
    async loadQuoteDetail() {
      try {
        common_vendor.index.showLoading({ title: "加载中..." });
        setTimeout(() => {
          this.quote = {
            id: this.quoteId,
            title: "美好的一天",
            content: "今天又是充满希望的一天，阳光明媚，心情愉悦。感谢生活中的每一个美好瞬间，让我能够感受到幸福的存在。",
            createTime: (/* @__PURE__ */ new Date()).toISOString(),
            emotion: "开心",
            category: "生活感悟",
            tags: "生活,感悟,正能量",
            views: 128,
            likes: 15,
            isLiked: false
          };
          this.recommendations = [
            {
              id: 1,
              content: "每一个当下都是礼物",
              author: "感恩"
            },
            {
              id: 2,
              content: "保持微笑，保持希望",
              author: "乐观"
            },
            {
              id: 3,
              content: "相信自己，你可以的",
              author: "鼓励"
            }
          ];
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
    shareQuote() {
      common_vendor.index.showToast({
        title: "分享功能开发中",
        icon: "none"
      });
    },
    likeQuote() {
      this.quote.isLiked = !this.quote.isLiked;
      if (this.quote.isLiked) {
        this.quote.likes++;
        common_vendor.index.showToast({
          title: "点赞成功",
          icon: "success"
        });
      } else {
        this.quote.likes--;
      }
    },
    deleteQuote() {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这个日签吗？删除后无法恢复。",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showToast({
              title: "删除成功",
              icon: "success"
            });
            setTimeout(() => {
              common_vendor.index.navigateBack();
            }, 1500);
          }
        }
      });
    },
    viewRecommendation(id) {
      common_vendor.index.showToast({
        title: "查看推荐功能开发中",
        icon: "none"
      });
    },
    formatTime(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "4f"),
    b: common_vendor.o((...args) => $options.editQuote && $options.editQuote(...args), "ed"),
    c: common_vendor.t($data.quote.title || "我的日签"),
    d: common_vendor.t($options.formatTime($data.quote.createTime)),
    e: common_vendor.t($data.quote.content),
    f: $data.quote.emotion
  }, $data.quote.emotion ? {
    g: common_vendor.t($data.quote.emotion)
  } : {}, {
    h: $data.quote.category
  }, $data.quote.category ? {
    i: common_vendor.t($data.quote.category)
  } : {}, {
    j: $data.quote.tags
  }, $data.quote.tags ? {
    k: common_vendor.t($data.quote.tags)
  } : {}, {
    l: common_vendor.t($data.quote.views || 0),
    m: common_vendor.t($data.quote.likes || 0),
    n: common_vendor.o((...args) => $options.shareQuote && $options.shareQuote(...args), "b7"),
    o: common_vendor.t($data.quote.isLiked ? "已点赞" : "点赞"),
    p: common_vendor.o((...args) => $options.likeQuote && $options.likeQuote(...args), "12"),
    q: $data.quote.isLiked ? 1 : "",
    r: common_vendor.o((...args) => $options.deleteQuote && $options.deleteQuote(...args), "a4"),
    s: $data.recommendations.length > 0
  }, $data.recommendations.length > 0 ? {
    t: common_vendor.f($data.recommendations, (item, k0, i0) => {
      return {
        a: common_vendor.t(item.content),
        b: common_vendor.t(item.author),
        c: item.id,
        d: common_vendor.o(($event) => $options.viewRecommendation(item.id), item.id)
      };
    })
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-af301bab"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/quotes/detail.js.map
