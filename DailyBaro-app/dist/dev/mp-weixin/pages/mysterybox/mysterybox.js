"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      todayBox: {
        id: 1,
        content: "今天的心情盲盒：保持微笑，世界会因你而美好",
        type: "quote",
        isOpened: false
      },
      isShaking: false,
      userEnergy: 0,
      historyList: [],
      showHistoryModal: false
    };
  },
  onLoad() {
    this.loadUserEnergy();
    this.loadHistory();
  },
  methods: {
    async loadUserEnergy() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const response = await utils_api.mysteryBoxApi.getUserEnergy(userInfo.uid);
        if (response && response.code === 200) {
          this.userEnergy = response.data.energy || 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mysterybox/mysterybox.vue:144", "获取能量值失败:", error);
      }
    },
    async loadHistory() {
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          return;
        }
        const response = await utils_api.mysteryBoxApi.getDrawHistory(userInfo.uid);
        if (response && response.code === 200) {
          common_vendor.index.__f__("log", "at pages/mysterybox/mysterybox.vue:157", "历史记录响应数据:", response.data);
          this.historyList = (response.data || []).map((item) => ({
            ...item,
            date: item.date || item.createTime || item.drawTime || (/* @__PURE__ */ new Date()).toISOString()
          }));
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mysterybox/mysterybox.vue:165", "获取历史记录失败:", error);
      }
    },
    async drawBox() {
      if (this.isShaking) return;
      try {
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        if (!userInfo || !userInfo.uid) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        this.isShaking = true;
        const response = await utils_api.mysteryBoxApi.drawBox(userInfo.uid);
        if (response && response.code === 200) {
          common_vendor.index.__f__("log", "at pages/mysterybox/mysterybox.vue:186", "抽取盲盒响应数据:", response.data);
          this.todayBox = {
            ...response.data,
            isOpened: true,
            date: response.data.date || response.data.createTime || response.data.drawTime || (/* @__PURE__ */ new Date()).toISOString()
          };
          this.loadUserEnergy();
          common_vendor.index.showToast({
            title: "盲盒已开启",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "抽取失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mysterybox/mysterybox.vue:207", "抽取盲盒失败:", error);
        common_vendor.index.showToast({
          title: "抽取失败，请重试",
          icon: "none"
        });
      } finally {
        this.isShaking = false;
      }
    },
    getTypeText(type) {
      switch (type) {
        case "task":
          return "任务";
        case "quote":
          return "日签";
        case "reward":
          return "奖励";
        default:
          return "盲盒";
      }
    },
    getStatusText(type) {
      if (type === "task") return "待完成";
      return "已开启";
    },
    completeTask(itemId) {
      common_vendor.index.showToast({
        title: "任务完成",
        icon: "success"
      });
    },
    showHistory() {
      this.showHistoryModal = true;
    },
    hideHistory() {
      this.showHistoryModal = false;
    },
    formatDate(dateString) {
      if (!dateString) {
        return "--";
      }
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
          common_vendor.index.__f__("warn", "at pages/mysterybox/mysterybox.vue:255", "无效的日期:", dateString);
          return "--";
        }
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${month}-${day}`;
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mysterybox/mysterybox.vue:263", "日期格式化错误:", error, dateString);
        return "--";
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.todayBox.isOpened
  }, $data.todayBox.isOpened ? common_vendor.e({
    b: common_vendor.t($options.getTypeText($data.todayBox.type)),
    c: common_vendor.t($options.getStatusText($data.todayBox.type)),
    d: common_vendor.n($data.todayBox.type),
    e: common_vendor.t($data.todayBox.content),
    f: common_vendor.t($options.formatDate($data.todayBox.date)),
    g: common_vendor.t($data.todayBox.reward || 10),
    h: $data.todayBox.type === "task" && !$data.todayBox.completed
  }, $data.todayBox.type === "task" && !$data.todayBox.completed ? {
    i: common_vendor.o(($event) => $options.completeTask($data.todayBox.id), "a6")
  } : {}) : {
    j: common_vendor.o((...args) => $options.drawBox && $options.drawBox(...args), "53")
  }, {
    k: common_vendor.o((...args) => $options.loadHistory && $options.loadHistory(...args), "6c"),
    l: $data.historyList.length === 0
  }, $data.historyList.length === 0 ? {} : {
    m: common_vendor.f($data.historyList, (record, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($options.getTypeText(record.type)),
        b: common_vendor.t($options.getStatusText(record.type)),
        c: common_vendor.n(record.type),
        d: common_vendor.t(record.content),
        e: common_vendor.t($options.formatDate(record.date)),
        f: common_vendor.t(record.reward || 10),
        g: record.type === "task" && !record.completed
      }, record.type === "task" && !record.completed ? {
        h: common_vendor.o(($event) => $options.completeTask(record.id), record.id)
      } : {}, {
        i: record.id,
        j: common_vendor.n(record.type)
      });
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-f82a62d6"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/mysterybox/mysterybox.js.map
