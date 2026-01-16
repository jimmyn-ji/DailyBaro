"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      knowledgeId: null,
      knowledge: null,
      loading: true
    };
  },
  computed: {
    tagList() {
      if (!this.knowledge || !this.knowledge.tags) return [];
      return this.knowledge.tags.split(",").filter((t) => t.trim());
    }
  },
  onLoad(options) {
    if (options.id) {
      this.knowledgeId = options.id;
      this.loadKnowledge();
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    async loadKnowledge() {
      this.loading = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        const response = await common_vendor.index.request({
          url: baseUrl + "/app/knowledge/" + this.knowledgeId,
          method: "GET",
          header: {
            "uid": uid
          }
        });
        if (response.data && response.data.code === 200) {
          this.knowledge = response.data.data;
        } else {
          common_vendor.index.showToast({
            title: "加载失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/knowledge/detail.vue:102", "加载知识详情失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    formatTime(time) {
      if (!time) return "";
      const date = new Date(time);
      return date.toLocaleDateString("zh-CN");
    },
    getBaseUrl() {
      const config = require("@/utils/config.js");
      return config.baseUrl || "https://dailybaro.cn";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "54"),
    b: $data.loading
  }, $data.loading ? {} : $data.knowledge ? common_vendor.e({
    d: common_vendor.t($data.knowledge.title),
    e: common_vendor.t($data.knowledge.category),
    f: $data.knowledge.subcategory
  }, $data.knowledge.subcategory ? {
    g: common_vendor.t($data.knowledge.subcategory)
  } : {}, {
    h: common_vendor.t($data.knowledge.content),
    i: $data.knowledge.tags
  }, $data.knowledge.tags ? {
    j: common_vendor.f($options.tagList, (tag, k0, i0) => {
      return {
        a: common_vendor.t(tag),
        b: tag
      };
    })
  } : {}, {
    k: common_vendor.t($data.knowledge.viewCount || 0),
    l: common_vendor.t($options.formatTime($data.knowledge.updateTime))
  }) : {}, {
    c: $data.knowledge
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-42b42b3b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/knowledge/detail.js.map
