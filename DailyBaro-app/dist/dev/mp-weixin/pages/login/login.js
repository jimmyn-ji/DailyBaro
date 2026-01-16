"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const common_assets = require("../../common/assets.js");
const _sfc_main = {
  data() {
    return {
      form: {
        username: "",
        password: ""
      },
      loading: false,
      usernameFocused: false,
      passwordFocused: false
    };
  },
  methods: {
    onUsernameFocus() {
      this.usernameFocused = true;
    },
    onUsernameBlur() {
      this.usernameFocused = false;
    },
    onPasswordFocus() {
      this.passwordFocused = true;
    },
    onPasswordBlur() {
      this.passwordFocused = false;
    },
    async login() {
      if (!this.form.username || !this.form.password) {
        common_vendor.index.showToast({
          title: "请输入用户名和密码",
          icon: "none"
        });
        return;
      }
      if (this.loading) return;
      this.loading = true;
      try {
        common_vendor.index.showLoading({ title: "登录中..." });
        common_vendor.index.__f__("log", "at pages/login/login.vue:104", "登录请求参数:", {
          account: this.form.username,
          password: this.form.password
        });
        const response = await utils_api.userApi.login({
          account: this.form.username,
          password: this.form.password
        });
        common_vendor.index.__f__("log", "at pages/login/login.vue:114", "登录响应:", response);
        if (response && response.code === 200) {
          common_vendor.index.setStorageSync("userInfo", response.data);
          const reward = response.data && response.data.energyReward ? response.data.energyReward : 0;
          common_vendor.index.showToast({
            title: reward > 0 ? `登录成功 · 今日能量+${reward}` : "登录成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.switchTab({
              url: "/pages/diary/diary"
            });
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "登录失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:139", "登录失败:", error);
        common_vendor.index.showToast({
          title: "登录失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
        this.loading = false;
      }
    },
    async wechatLogin() {
      if (this.loading) return;
      this.loading = true;
      try {
        common_vendor.index.showLoading({ title: "微信登录中..." });
        common_vendor.index.__f__("log", "at pages/login/login.vue:159", "开始获取微信登录凭证...");
        const loginResult = await new Promise((resolve, reject) => {
          common_vendor.index.login({
            success: (res) => {
              common_vendor.index.__f__("log", "at pages/login/login.vue:163", "uni.login success:", res);
              resolve(res);
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/login/login.vue:167", "uni.login fail:", err);
              reject(err);
            }
          });
        });
        common_vendor.index.__f__("log", "at pages/login/login.vue:173", "微信登录凭证:", loginResult);
        common_vendor.index.__f__("log", "at pages/login/login.vue:174", "loginResult.code:", loginResult == null ? void 0 : loginResult.code);
        common_vendor.index.__f__("log", "at pages/login/login.vue:175", "loginResult.code 类型:", typeof (loginResult == null ? void 0 : loginResult.code));
        if (!loginResult || !loginResult.code) {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "获取微信登录凭证失败，请重试",
            icon: "none",
            duration: 2e3
          });
          return;
        }
        const requestData = {
          code: loginResult.code
        };
        common_vendor.index.__f__("log", "at pages/login/login.vue:196", "发送微信登录请求，code:", loginResult.code);
        common_vendor.index.__f__("log", "at pages/login/login.vue:197", "请求数据:", JSON.stringify(requestData));
        common_vendor.index.__f__("log", "at pages/login/login.vue:198", "请求数据类型:", typeof requestData, Object.keys(requestData));
        common_vendor.index.__f__("log", "at pages/login/login.vue:199", "requestData.code:", requestData.code);
        common_vendor.index.__f__("log", "at pages/login/login.vue:200", "requestData.code 类型:", typeof requestData.code);
        common_vendor.index.__f__("log", "at pages/login/login.vue:201", "requestData.code 值:", requestData.code);
        if (!requestData.code) {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "微信登录code为空，请重试",
            icon: "none",
            duration: 2e3
          });
          return;
        }
        const response = await utils_api.userApi.wechatLogin(requestData);
        common_vendor.index.__f__("log", "at pages/login/login.vue:216", "微信登录响应:", response);
        common_vendor.index.hideLoading();
        if (response && response.code === 200) {
          common_vendor.index.setStorageSync("userInfo", response.data);
          const reward = response.data && response.data.energyReward ? response.data.energyReward : 0;
          common_vendor.index.showToast({
            title: reward > 0 ? `登录成功 · 今日能量+${reward}` : "登录成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.switchTab({
              url: "/pages/diary/diary"
            });
          }, 1500);
        } else {
          let errorMessage = "微信登录失败";
          if (response && response.message) {
            if (response.message === "WECHAT_LOGIN_NOT_IMPLEMENTED") {
              errorMessage = "微信登录功能暂未开放，请使用账号密码登录";
            } else if (response.message.includes("缺少授权码")) {
              errorMessage = "微信授权码获取失败，请重试";
            } else {
              errorMessage = response.message;
            }
          }
          common_vendor.index.showToast({
            title: errorMessage,
            icon: "none",
            duration: 2e3
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:255", "微信登录失败:", error);
        common_vendor.index.hideLoading();
        let errorMessage = "微信登录失败，请重试";
        if (error.message) {
          errorMessage = error.message;
        } else if (error.errMsg) {
          if (error.errMsg.includes("getUserProfile:fail")) {
            errorMessage = "需要授权获取用户信息才能登录";
          } else if (error.errMsg.includes("login:fail")) {
            errorMessage = "微信登录失败，请检查网络连接";
          }
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none",
          duration: 2e3
        });
      } finally {
        this.loading = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_assets._imports_0,
    b: common_assets._imports_1,
    c: $data.usernameFocused ? 1 : "",
    d: common_vendor.o((...args) => $options.onUsernameFocus && $options.onUsernameFocus(...args), "c1"),
    e: common_vendor.o((...args) => $options.onUsernameBlur && $options.onUsernameBlur(...args), "1b"),
    f: $data.form.username,
    g: common_vendor.o(($event) => $data.form.username = $event.detail.value, "0e"),
    h: $data.passwordFocused ? 1 : "",
    i: common_vendor.o((...args) => $options.onPasswordFocus && $options.onPasswordFocus(...args), "19"),
    j: common_vendor.o((...args) => $options.onPasswordBlur && $options.onPasswordBlur(...args), "f6"),
    k: $data.form.password,
    l: common_vendor.o(($event) => $data.form.password = $event.detail.value, "65"),
    m: common_vendor.t($data.loading ? "登录中..." : "登录"),
    n: common_vendor.o((...args) => $options.login && $options.login(...args), "df"),
    o: $data.loading,
    p: common_vendor.o((...args) => $options.wechatLogin && $options.wechatLogin(...args), "85"),
    q: $data.loading ? 1 : ""
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-cdfe2409"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/login.js.map
