"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      posts: [],
      loading: false,
      page: 1,
      hasMore: true
    };
  },
  onLoad() {
    this.loadPosts();
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    goToPost() {
      common_vendor.index.navigateTo({
        url: "/pages/anonymous/post"
      });
    },
    async loadPosts() {
      if (this.loading) return;
      this.loading = true;
      try {
        const res = await utils_api.anonymousApi.getPosts();
        if (res.code === 200) {
          this.posts = res.data || [];
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
    loadMore() {
      common_vendor.index.__f__("log", "at pages/anonymous/anonymous.vue:111", "加载更多");
    },
    viewPost(postId) {
      common_vendor.index.navigateTo({
        url: `/pages/anonymous/detail?id=${postId}`
      });
    },
    async likePost(postId) {
      if (!postId) {
        common_vendor.index.showToast({ title: "无效的帖子ID", icon: "none" });
        return;
      }
      try {
        const res = await utils_api.anonymousApi.likePost(postId);
        if (res.code === 200) {
          const postIndex = this.posts.findIndex((post) => post.id === postId);
          if (postIndex !== -1) {
            this.posts[postIndex].isLiked = !this.posts[postIndex].isLiked;
            this.posts[postIndex].likeCount = this.posts[postIndex].isLiked ? (this.posts[postIndex].likeCount || 0) + 1 : (this.posts[postIndex].likeCount || 1) - 1;
          }
          common_vendor.index.showToast({
            title: this.posts[postIndex].isLiked ? "点赞成功" : "取消点赞",
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
    commentPost(postId) {
      common_vendor.index.navigateTo({
        url: `/pages/anonymous/detail?id=${postId}`
      });
    },
    formatTime(dateStr) {
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
    },
    getRandomAvatar() {
      const avatars = ["👤", "👧", "👦", "👩", "👨", "👱", "👵", "👴", "👶", "👷"];
      return avatars[Math.floor(Math.random() * avatars.length)];
    },
    getRandomNickname() {
      const nicknames = ["匿名小星星", "小星星", "匿名星", "星星", "匿名", "星"];
      return nicknames[Math.floor(Math.random() * nicknames.length)];
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goToPost && $options.goToPost(...args), "f5"),
    b: common_vendor.f($data.posts, (post, k0, i0) => {
      return {
        a: common_vendor.t($options.formatTime(post.createTime)),
        b: common_vendor.t(post.content),
        c: common_vendor.t(post.isLiked ? "❤️" : "🤍"),
        d: common_vendor.t(post.likeCount || 0),
        e: common_vendor.o(($event) => $options.likePost(post.id), post.id),
        f: common_vendor.t(post.commentCount || 0),
        g: common_vendor.o(($event) => $options.commentPost(post.id), post.id),
        h: post.id,
        i: common_vendor.o(($event) => $options.viewPost(post.id), post.id)
      };
    }),
    c: common_vendor.t($options.getRandomAvatar()),
    d: common_vendor.t($options.getRandomNickname()),
    e: $data.posts.length === 0
  }, $data.posts.length === 0 ? {
    f: common_vendor.o((...args) => $options.goToPost && $options.goToPost(...args), "b7")
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-34fd0416"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/anonymous/anonymous.js.map
