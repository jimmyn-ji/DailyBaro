"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      capsule: {},
      capsuleId: null
    };
  },
  onLoad(options) {
    if (options.id) {
      this.capsuleId = options.id;
      this.loadCapsuleDetail();
    }
  },
  methods: {
    // 加载胶囊详情
    async loadCapsuleDetail() {
      try {
        common_vendor.index.showLoading({ title: "加载中..." });
        await new Promise((resolve) => setTimeout(resolve, 1e3));
        this.capsule = {
          id: this.capsuleId,
          title: "我的第一个胶囊",
          content: "这是一个充满回忆的胶囊，记录了我当时的心情和想法...",
          status: "opened",
          // opened, sealed
          createTime: (/* @__PURE__ */ new Date()).getTime() - 7 * 24 * 60 * 60 * 1e3,
          openTime: (/* @__PURE__ */ new Date()).getTime(),
          emotions: ["开心", "激动", "期待"],
          attachments: [
            "https://via.placeholder.com/300x200/FF6B6B/FFFFFF?text=Photo1",
            "https://via.placeholder.com/300x200/4ECDC4/FFFFFF?text=Photo2"
          ]
        };
        common_vendor.index.hideLoading();
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({ title: "加载失败", icon: "none" });
        common_vendor.index.__f__("error", "at pages/capsule/detail.vue:114", "加载胶囊详情失败:", error);
      }
    },
    // 返回
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 编辑胶囊
    editCapsule() {
      common_vendor.index.navigateTo({
        url: `/pages/capsule/edit?id=${this.capsuleId}`
      });
    },
    // 获取状态样式
    getStatusClass(status) {
      return status === "opened" ? "status-opened" : "status-sealed";
    },
    // 获取状态文本
    getStatusText(status) {
      return status === "opened" ? "已开启" : "未开启";
    },
    // 格式化日期
    formatDate(timestamp) {
      if (!timestamp) return "";
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")} ${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
    },
    // 判断是否为图片
    isImage(url) {
      return url && (url.includes(".jpg") || url.includes(".jpeg") || url.includes(".png") || url.includes(".gif"));
    },
    // 预览图片
    previewImage(url) {
      common_vendor.index.previewImage({
        urls: this.capsule.attachments.filter((att) => this.isImage(att)),
        current: url
      });
    },
    // 下载文件
    downloadFile(url) {
      common_vendor.index.downloadFile({
        url,
        success: (res) => {
          common_vendor.index.showToast({ title: "下载成功", icon: "success" });
        },
        fail: () => {
          common_vendor.index.showToast({ title: "下载失败", icon: "none" });
        }
      });
    },
    // 获取文件名
    getFileName(url) {
      return url.split("/").pop() || "未知文件";
    },
    // 分享胶囊
    shareCapsule() {
      common_vendor.index.showToast({ title: "分享功能开发中", icon: "none" });
    },
    // 删除胶囊
    deleteCapsule() {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这个胶囊吗？删除后无法恢复。",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.showToast({ title: "删除成功", icon: "success" });
            setTimeout(() => {
              common_vendor.index.navigateBack();
            }, 1500);
          }
        }
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "4f"),
    b: common_vendor.o((...args) => $options.editCapsule && $options.editCapsule(...args), "26"),
    c: common_vendor.t($data.capsule.title || "未命名胶囊"),
    d: common_vendor.t($options.getStatusText($data.capsule.status)),
    e: common_vendor.n($options.getStatusClass($data.capsule.status)),
    f: common_vendor.t($options.formatDate($data.capsule.createTime)),
    g: $data.capsule.openTime
  }, $data.capsule.openTime ? {
    h: common_vendor.t($options.formatDate($data.capsule.openTime))
  } : {}, {
    i: common_vendor.t($data.capsule.content || "暂无内容"),
    j: $data.capsule.emotions && $data.capsule.emotions.length > 0
  }, $data.capsule.emotions && $data.capsule.emotions.length > 0 ? {
    k: common_vendor.f($data.capsule.emotions, (emotion, k0, i0) => {
      return {
        a: common_vendor.t(emotion),
        b: emotion
      };
    })
  } : {}, {
    l: $data.capsule.attachments && $data.capsule.attachments.length > 0
  }, $data.capsule.attachments && $data.capsule.attachments.length > 0 ? {
    m: common_vendor.f($data.capsule.attachments, (attachment, index, i0) => {
      return common_vendor.e({
        a: $options.isImage(attachment)
      }, $options.isImage(attachment) ? {
        b: attachment,
        c: common_vendor.o(($event) => $options.previewImage(attachment), index)
      } : {
        d: common_vendor.t($options.getFileName(attachment)),
        e: common_vendor.o(($event) => $options.downloadFile(attachment), index)
      }, {
        f: index
      });
    })
  } : {}, {
    n: common_vendor.o((...args) => $options.shareCapsule && $options.shareCapsule(...args), "ff"),
    o: common_vendor.o((...args) => $options.deleteCapsule && $options.deleteCapsule(...args), "28")
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-5f048ea8"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/capsule/detail.js.map
