"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/login/login.js";
  "./pages/diary/diary.js";
  "./pages/diary/edit.js";
  "./pages/diary/detail.js";
  "./pages/capsule/capsule.js";
  "./pages/capsule/create.js";
  "./pages/mysterybox/mysterybox.js";
  "./pages/profile/profile.js";
  "./pages/anonymous/anonymous.js";
  "./pages/anonymous/post.js";
  "./pages/anonymous/detail.js";
  "./pages/emotion/emotion.js";
  "./pages/emotion/visualization.js";
  "./pages/emotion/cv-analysis.js";
  "./pages/emotion/audio-analysis.js";
  "./pages/ai/ai.js";
  "./pages/quotes/quotes.js";
  "./pages/recommendations/recommendations.js";
  "./pages/quotes/edit.js";
  "./pages/quotes/create.js";
  "./pages/quotes/detail.js";
  "./pages/capsule/detail.js";
  "./pages/knowledge/knowledge.js";
  "./pages/knowledge/detail.js";
}
const _sfc_main = {
  __name: "App",
  setup(__props) {
    common_vendor.onLaunch(() => {
      common_vendor.index.__f__("log", "at App.vue:9", "App Launch");
      checkLoginStatus();
    });
    common_vendor.onShow(() => {
      common_vendor.index.__f__("log", "at App.vue:14", "App Show");
    });
    common_vendor.onHide(() => {
      common_vendor.index.__f__("log", "at App.vue:18", "App Hide");
    });
    const checkLoginStatus = () => {
      const userInfo = common_vendor.index.getStorageSync("userInfo");
      if (userInfo && userInfo.uid) {
        common_vendor.index.switchTab({
          url: "/pages/diary/diary"
        });
      } else {
        common_vendor.index.redirectTo({
          url: "/pages/login/login"
        });
      }
    };
    return (_ctx, _cache) => {
      return {};
    };
  }
};
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
