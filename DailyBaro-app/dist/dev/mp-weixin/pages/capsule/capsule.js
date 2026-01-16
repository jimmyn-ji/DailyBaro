"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_dateUtils = require("../../utils/dateUtils.js");
const _sfc_main = {
  data() {
    return {
      capsules: [],
      currentStatus: "all",
      loading: false
    };
  },
  computed: {
    filteredCapsules() {
      if (this.currentStatus === "all") {
        return this.capsules;
      }
      if (this.currentStatus === "pending") {
        return this.capsules.filter((capsule) => !capsule.isOpened);
      } else if (this.currentStatus === "opened") {
        return this.capsules.filter((capsule) => capsule.isOpened);
      }
      return this.capsules;
    }
  },
  onLoad() {
    this.loadCapsules();
  },
  onShow() {
    this.loadCapsules();
  },
  methods: {
    async loadCapsules() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const response = await utils_api.capsuleApi.getCapsules(userInfo.uid);
        if (response && response.code === 200) {
          this.capsules = response.data || [];
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "获取胶囊失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/capsule/capsule.vue:116", "加载胶囊失败:", error);
        common_vendor.index.showToast({
          title: "加载失败，请重试",
          icon: "none"
        });
      }
    },
    // 创建胶囊
    createCapsule() {
      common_vendor.index.navigateTo({
        url: "/pages/capsule/create"
      });
    },
    // 查看胶囊详情
    viewCapsule(id) {
      common_vendor.index.navigateTo({
        url: `/pages/capsule/detail?id=${id}`
      });
    },
    // 删除胶囊
    async deleteCapsule(id) {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这个胶囊吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await utils_api.capsuleApi.deleteCapsule(id);
              if (response && response.code === 200) {
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
                this.loadCapsules();
              } else {
                common_vendor.index.showToast({
                  title: (response == null ? void 0 : response.message) || "删除失败",
                  icon: "none"
                });
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/capsule/capsule.vue:162", "删除胶囊失败:", error);
              common_vendor.index.showToast({
                title: "删除失败，请重试",
                icon: "none"
              });
            }
          }
        }
      });
    },
    // 按状态筛选
    filterByStatus(status) {
      this.currentStatus = status;
      common_vendor.index.showToast({
        title: `筛选: ${status}`,
        icon: "success"
      });
    },
    // 格式化日期
    formatDate(dateString) {
      return utils_dateUtils.formatDateShort(dateString);
    },
    formatDateTime(dateStr) {
      return utils_dateUtils.formatDateTime(dateStr);
    },
    getStatusText(isOpened) {
      return isOpened ? "已开启" : "待开启";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.currentStatus === "all" ? 1 : "",
    b: common_vendor.o(($event) => $options.filterByStatus("all"), "91"),
    c: $data.currentStatus === "pending" ? 1 : "",
    d: common_vendor.o(($event) => $options.filterByStatus("pending"), "99"),
    e: $data.currentStatus === "opened" ? 1 : "",
    f: common_vendor.o(($event) => $options.filterByStatus("opened"), "40"),
    g: common_vendor.f($options.filteredCapsules, (capsule, k0, i0) => {
      return {
        a: common_vendor.t($options.formatDate(capsule.openTime)),
        b: common_vendor.t($options.getStatusText(capsule.isOpened)),
        c: common_vendor.n(capsule.isOpened ? "opened" : "pending"),
        d: common_vendor.t(capsule.content || "暂无内容"),
        e: common_vendor.t($options.formatDateTime(capsule.openTime)),
        f: capsule.capsuleId,
        g: common_vendor.o(($event) => $options.viewCapsule(capsule.capsuleId), capsule.capsuleId)
      };
    }),
    h: common_vendor.o((...args) => $options.createCapsule && $options.createCapsule(...args), "a3")
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-885c935a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/capsule/capsule.js.map
