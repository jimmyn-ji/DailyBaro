"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      inputMessage: "",
      chatMessages: [],
      quickQuestions: []
    };
  },
  onLoad() {
    this.initChat();
    this.loadCommonQuestions();
  },
  methods: {
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack({
        delta: 1
      });
    },
    async loadCommonQuestions() {
      try {
        const response = await utils_api.aiApi.getCommonQuestions();
        if (response && response.code === 200) {
          this.quickQuestions = response.data || [];
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/ai/ai.vue:119", "加载常见问题失败:", error);
      }
    },
    initChat() {
      this.addMessage("assistant", "你好！我是你的AI情绪助手，有什么可以帮助你的吗？");
    },
    askQuestion(question) {
      this.inputMessage = question;
      this.sendMessage();
    },
    async sendMessage() {
      var _a, _b, _c, _d;
      if (!this.inputMessage.trim()) return;
      const message = this.inputMessage.trim();
      this.addMessage("user", message);
      this.inputMessage = "";
      this.addMessage("assistant", "正在思考中...", true);
      try {
        const res = await utils_api.aiApi.chat({
          message,
          timestamp: Date.now()
        });
        common_vendor.index.__f__("log", "at pages/ai/ai.vue:148", "AI API 响应:", res);
        this.chatMessages.pop();
        if (res && res.code === 200) {
          const reply = ((_a = res.data) == null ? void 0 : _a.reply) || ((_b = res.data) == null ? void 0 : _b.message) || ((_c = res.data) == null ? void 0 : _c.content) || ((_d = res.data) == null ? void 0 : _d.answer) || res.message || "抱歉，我现在无法回答这个问题。";
          this.addMessage("assistant", reply);
        } else if (res && res.data) {
          const reply = res.data.reply || res.data.message || res.data.content || res.data.answer || "抱歉，我现在无法回答这个问题。";
          this.addMessage("assistant", reply);
        } else {
          common_vendor.index.__f__("error", "at pages/ai/ai.vue:162", "AI API 返回格式异常:", res);
          this.addMessage("assistant", `抱歉，我遇到了一些问题。${(res == null ? void 0 : res.message) || "请稍后再试"}`);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/ai/ai.vue:166", "AI API 调用失败:", error);
        this.chatMessages.pop();
        const errorMsg = error.message || error.errMsg || "网络连接出现问题，请检查网络后重试。";
        this.addMessage("assistant", `网络连接出现问题：${errorMsg}`);
      }
    },
    addMessage(type, content, isTyping = false) {
      this.chatMessages.push({
        type,
        content,
        time: /* @__PURE__ */ new Date(),
        isTyping
      });
      setTimeout(() => {
        common_vendor.index.pageScrollTo({
          scrollTop: 9999,
          duration: 300
        });
      }, 100);
    },
    async analyzeDiary() {
      try {
        common_vendor.index.showLoading({ title: "分析中..." });
        const res = await utils_api.aiApi.diaryAnalysis({
          action: "analyze_today",
          timestamp: Date.now()
        });
        common_vendor.index.hideLoading();
        if (res.code === 200) {
          this.addMessage("assistant", res.data.analysis || "基于你的日记内容，我为你提供了一些建议。");
        } else {
          common_vendor.index.showToast({
            title: res.message || "分析失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "分析失败，请稍后重试",
          icon: "none"
        });
      }
    },
    formatTime(date) {
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      if (diff < 6e4) return "刚刚";
      if (diff < 36e5) return `${Math.floor(diff / 6e4)}分钟前`;
      if (diff < 864e5) return `${Math.floor(diff / 36e5)}小时前`;
      return date.toLocaleDateString();
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "22"),
    b: common_vendor.f($data.quickQuestions, (question, k0, i0) => {
      return {
        a: common_vendor.t(question.text),
        b: question.id,
        c: common_vendor.o(($event) => $options.askQuestion(question.text), question.id)
      };
    }),
    c: common_vendor.f($data.chatMessages, (message, index, i0) => {
      return common_vendor.e({
        a: message.type === "user"
      }, message.type === "user" ? {} : {}, {
        b: common_vendor.t(message.content),
        c: common_vendor.t($options.formatTime(message.time)),
        d: index,
        e: common_vendor.n(message.type)
      });
    }),
    d: common_vendor.o((...args) => $options.sendMessage && $options.sendMessage(...args), "24"),
    e: $data.inputMessage,
    f: common_vendor.o(($event) => $data.inputMessage = $event.detail.value, "8f"),
    g: common_vendor.o((...args) => $options.sendMessage && $options.sendMessage(...args), "96"),
    h: !$data.inputMessage.trim(),
    i: common_vendor.o((...args) => $options.analyzeDiary && $options.analyzeDiary(...args), "34")
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1a49ba32"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/ai/ai.js.map
