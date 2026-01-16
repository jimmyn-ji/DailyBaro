"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      isEdit: false,
      capsuleId: null,
      capsuleContent: "",
      openDate: "",
      selectedHour: "09",
      selectedMinute: "00",
      enableReminder: true,
      minDate: (/* @__PURE__ */ new Date()).toISOString().split("T")[0],
      hours: Array.from({ length: 24 }, (_, i) => i),
      minutes: Array.from({ length: 60 }, (_, i) => i)
    };
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.capsuleId = options.id;
      this.loadCapsule();
    } else {
      const tomorrow = /* @__PURE__ */ new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(9, 0, 0, 0);
      this.openDate = tomorrow.toISOString().split("T")[0];
      this.selectedHour = "09";
      this.selectedMinute = "00";
    }
  },
  computed: {
    canCreate() {
      return this.capsuleContent && this.openDate && this.selectedHour && this.selectedMinute;
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    async loadCapsule() {
      try {
        const res = await utils_api.capsuleApi.getCapsuleDetail(this.capsuleId);
        if (res.code === 200) {
          this.capsuleContent = res.data.content || "";
          this.openDate = res.data.openTime ? res.data.openTime.split("T")[0] : "";
          this.selectedHour = res.data.openTime ? res.data.openTime.split("T")[1].substring(0, 2) : "09";
          this.selectedMinute = res.data.openTime ? res.data.openTime.split("T")[1].substring(3, 5) : "00";
          this.enableReminder = res.data.reminderType === "app_notification";
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "加载失败",
          icon: "none"
        });
      }
    },
    onDateChange(e) {
      this.openDate = e.detail.value;
    },
    onHourChange(e) {
      this.selectedHour = e.detail.value;
    },
    onMinuteChange(e) {
      this.selectedMinute = e.detail.value;
    },
    toggleReminder() {
      this.enableReminder = !this.enableReminder;
    },
    async createCapsule() {
      if (!this.canCreate) {
        common_vendor.index.showToast({
          title: "请完善信息",
          icon: "none"
        });
        return;
      }
      try {
        const data = {
          content: this.capsuleContent,
          openTime: this.openDate + "T" + this.selectedHour + ":" + this.selectedMinute + ":00",
          reminderType: this.enableReminder ? "app_notification" : "sms"
        };
        let res;
        if (this.isEdit) {
          res = await utils_api.capsuleApi.updateCapsule(this.capsuleId, data);
        } else {
          res = await utils_api.capsuleApi.createCapsule(data);
        }
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "保存成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: res.message || "保存失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: error.message || "保存失败",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "c1"),
    b: common_vendor.t($data.isEdit ? "编辑胶囊" : "创建胶囊"),
    c: $data.capsuleContent,
    d: common_vendor.o(($event) => $data.capsuleContent = $event.detail.value, "e0"),
    e: common_vendor.t($data.capsuleContent.length),
    f: common_vendor.t($data.openDate),
    g: $data.openDate,
    h: $data.minDate,
    i: common_vendor.o((...args) => $options.onDateChange && $options.onDateChange(...args), "13"),
    j: common_vendor.t($data.selectedHour),
    k: $data.hours,
    l: $data.selectedHour,
    m: common_vendor.o((...args) => $options.onHourChange && $options.onHourChange(...args), "e9"),
    n: common_vendor.t($data.selectedMinute),
    o: $data.minutes,
    p: $data.selectedMinute,
    q: common_vendor.o((...args) => $options.onMinuteChange && $options.onMinuteChange(...args), "2c"),
    r: common_vendor.t($data.enableReminder ? "🔔" : "🔕"),
    s: common_vendor.t($data.enableReminder ? "已开启" : "已关闭"),
    t: $data.enableReminder ? 1 : "",
    v: common_vendor.o((...args) => $options.toggleReminder && $options.toggleReminder(...args), "10"),
    w: common_vendor.t($data.isEdit ? "保存修改" : "创建胶囊"),
    x: common_vendor.o((...args) => $options.createCapsule && $options.createCapsule(...args), "43"),
    y: !$options.canCreate
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-6d28ee0e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/capsule/create.js.map
