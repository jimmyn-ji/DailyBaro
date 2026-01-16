"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      postId: null,
      post: null,
      comments: [],
      loading: false,
      commentText: "",
      commentFocused: false
    };
  },
  onLoad(options) {
    if (options.id) {
      this.postId = options.id;
      this.loadPostDetail();
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    showMore() {
      common_vendor.index.showActionSheet({
        itemList: ["举报", "分享"],
        success: (res) => {
          if (res.tapIndex === 0) {
            common_vendor.index.showToast({
              title: "举报功能开发中",
              icon: "none"
            });
          } else if (res.tapIndex === 1) {
            common_vendor.index.showToast({
              title: "分享功能开发中",
              icon: "none"
            });
          }
        }
      });
    },
    async loadPostDetail() {
      this.loading = true;
      try {
        const res = await utils_api.anonymousApi.getPostDetail(this.postId);
        if (res.code === 200) {
          this.post = res.data;
          this.loadComments();
        } else {
          common_vendor.index.showToast({
            title: res.message || "加载失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    async loadComments() {
      try {
        this.comments = [];
      } catch (error) {
        common_vendor.index.__f__("log", "at pages/anonymous/detail.vue:179", "加载评论失败", error);
      }
    },
    async likePost() {
      try {
        const res = await utils_api.anonymousApi.likePost(this.post.postId);
        if (res.code === 200) {
          this.post.isLiked = !this.post.isLiked;
          this.post.likeCount = this.post.isLiked ? (this.post.likeCount || 0) + 1 : (this.post.likeCount || 1) - 1;
          common_vendor.index.showToast({
            title: this.post.isLiked ? "点赞成功" : "取消点赞",
            icon: "success"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "操作失败",
          icon: "none"
        });
      }
    },
    focusComment() {
      this.commentFocused = true;
    },
    async submitComment() {
      if (!this.commentText.trim()) return;
      try {
        const res = await utils_api.anonymousApi.commentPost(this.postId, {
          content: this.commentText
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "评论成功",
            icon: "success"
          });
          this.commentText = "";
          this.commentFocused = false;
          this.loadComments();
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "评论失败",
          icon: "none"
        });
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return "";
      const date = new Date(dateStr);
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      const minutes = Math.floor(diff / (1e3 * 60));
      const hours = Math.floor(diff / (1e3 * 60 * 60));
      const days = Math.floor(diff / (1e3 * 60 * 60 * 24));
      if (minutes < 1) return "刚刚";
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      return date.toLocaleDateString();
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "b4"),
    b: common_vendor.o((...args) => $options.showMore && $options.showMore(...args), "43"),
    c: $data.loading
  }, $data.loading ? {} : $data.post ? common_vendor.e({
    e: common_vendor.t($options.formatDate($data.post.createTime)),
    f: $data.post.visibility === "private"
  }, $data.post.visibility === "private" ? {} : {}, {
    g: common_vendor.t($data.post.content),
    h: common_vendor.t($data.post.isLiked ? "❤️" : "🤍"),
    i: common_vendor.t($data.post.likeCount || 0),
    j: common_vendor.o((...args) => $options.likePost && $options.likePost(...args), "ff"),
    k: common_vendor.t($data.post.commentCount || 0),
    l: common_vendor.o((...args) => $options.focusComment && $options.focusComment(...args), "57"),
    m: common_vendor.t($data.comments.length),
    n: $data.comments.length === 0
  }, $data.comments.length === 0 ? {} : {
    o: common_vendor.f($data.comments, (comment, k0, i0) => {
      return {
        a: common_vendor.t($options.formatDate(comment.createTime)),
        b: common_vendor.t(comment.content),
        c: comment.commentId
      };
    })
  }) : {}, {
    d: $data.post,
    p: $data.commentFocused,
    q: common_vendor.o(($event) => $data.commentFocused = true, "a4"),
    r: common_vendor.o(($event) => $data.commentFocused = false, "c8"),
    s: $data.commentText,
    t: common_vendor.o(($event) => $data.commentText = $event.detail.value, "00"),
    v: !$data.commentText.trim(),
    w: common_vendor.o((...args) => $options.submitComment && $options.submitComment(...args), "95")
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-a12d599d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/anonymous/detail.js.map
