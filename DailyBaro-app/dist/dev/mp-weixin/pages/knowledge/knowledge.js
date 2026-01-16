"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      searchKeyword: "",
      selectedCategory: null,
      selectedSubcategory: null,
      searching: false,
      searchResults: null,
      categories: [
        { label: "全部", value: null },
        { label: "情绪管理", value: "情绪管理" },
        { label: "心理排忧", value: "心理排忧" },
        { label: "成长指南", value: "成长指南" },
        { label: "案例分享", value: "案例分享" }
      ],
      subcategories: {
        "情绪管理": [
          { label: "全部", value: null },
          { label: "焦虑", value: "焦虑" },
          { label: "抑郁", value: "抑郁" },
          { label: "压力", value: "压力" },
          { label: "愤怒", value: "愤怒" }
        ],
        "心理排忧": [
          { label: "全部", value: null },
          { label: "常见问题", value: "常见问题" },
          { label: "自我调节", value: "自我调节" }
        ],
        "成长指南": [
          { label: "全部", value: null },
          { label: "自我认知", value: "自我认知" },
          { label: "人际关系", value: "人际关系" }
        ]
      }
    };
  },
  onLoad() {
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    onSearchInput(e) {
      this.searchKeyword = e.detail.value;
    },
    selectCategory(category) {
      this.selectedCategory = category;
      this.selectedSubcategory = null;
      this.loadKnowledgeByCategory();
    },
    selectSubcategory(subcategory) {
      this.selectedSubcategory = subcategory;
      this.loadKnowledgeBySubcategory();
    },
    async performSearch() {
      var _a;
      if (!this.searchKeyword || this.searchKeyword.trim().length === 0) {
        common_vendor.index.showToast({
          title: "请输入搜索关键词",
          icon: "none"
        });
        return;
      }
      this.searching = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        const response = await common_vendor.index.request({
          url: baseUrl + "/app/knowledge/search",
          method: "POST",
          header: {
            "Content-Type": "application/json",
            "uid": uid
          },
          data: {
            query: this.searchKeyword,
            useRAG: true,
            // 使用RAG检索
            page: 1,
            size: 20
          }
        });
        if (response.data && response.data.code === 200) {
          this.searchResults = response.data.data;
        } else {
          common_vendor.index.showToast({
            title: ((_a = response.data) == null ? void 0 : _a.message) || "搜索失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/knowledge/knowledge.vue:216", "搜索失败:", error);
        common_vendor.index.showToast({
          title: "搜索失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.searching = false;
      }
    },
    async loadKnowledgeByCategory() {
      if (!this.selectedCategory) {
        this.searchResults = null;
        return;
      }
      this.searching = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        const response = await common_vendor.index.request({
          url: baseUrl + "/app/knowledge/category/" + encodeURIComponent(this.selectedCategory),
          method: "GET",
          header: {
            "uid": uid
          }
        });
        if (response.data && response.data.code === 200) {
          this.searchResults = {
            knowledgeList: response.data.data,
            total: response.data.data.length
          };
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/knowledge/knowledge.vue:253", "加载分类知识失败:", error);
      } finally {
        this.searching = false;
      }
    },
    async loadKnowledgeBySubcategory() {
      if (!this.selectedCategory || !this.selectedSubcategory) {
        return;
      }
      this.searching = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        const response = await common_vendor.index.request({
          url: baseUrl + "/app/knowledge/category/" + encodeURIComponent(this.selectedCategory) + "/subcategory/" + encodeURIComponent(this.selectedSubcategory),
          method: "GET",
          header: {
            "uid": uid
          }
        });
        if (response.data && response.data.code === 200) {
          this.searchResults = {
            knowledgeList: response.data.data,
            total: response.data.data.length
          };
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/knowledge/knowledge.vue:286", "加载子分类知识失败:", error);
      } finally {
        this.searching = false;
      }
    },
    viewKnowledgeDetail(id) {
      common_vendor.index.navigateTo({
        url: "/pages/knowledge/detail?id=" + id
      });
    },
    getBaseUrl() {
      const config = require("@/utils/config.js");
      return config.baseUrl || "https://dailybaro.cn";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "9e"),
    b: common_vendor.o((...args) => $options.performSearch && $options.performSearch(...args), "76"),
    c: common_vendor.o([($event) => $data.searchKeyword = $event.detail.value, (...args) => $options.onSearchInput && $options.onSearchInput(...args)], "42"),
    d: $data.searchKeyword,
    e: common_vendor.o((...args) => $options.performSearch && $options.performSearch(...args), "cf"),
    f: common_vendor.f($data.categories, (category, k0, i0) => {
      return {
        a: common_vendor.t(category.label),
        b: $data.selectedCategory === category.value ? 1 : "",
        c: category.value,
        d: common_vendor.o(($event) => $options.selectCategory(category.value), category.value)
      };
    }),
    g: $data.selectedCategory && $data.subcategories[$data.selectedCategory]
  }, $data.selectedCategory && $data.subcategories[$data.selectedCategory] ? {
    h: common_vendor.f($data.subcategories[$data.selectedCategory], (sub, k0, i0) => {
      return {
        a: common_vendor.t(sub.label),
        b: $data.selectedSubcategory === sub.value ? 1 : "",
        c: sub.value,
        d: common_vendor.o(($event) => $options.selectSubcategory(sub.value), sub.value)
      };
    })
  } : {}, {
    i: $data.searching
  }, $data.searching ? {} : $data.searchResults && $data.searchResults.knowledgeList && $data.searchResults.knowledgeList.length > 0 ? common_vendor.e({
    k: $data.searchResults.aiAnswer
  }, $data.searchResults.aiAnswer ? common_vendor.e({
    l: common_vendor.t($data.searchResults.aiAnswer),
    m: $data.searchResults.sources && $data.searchResults.sources.length > 0
  }, $data.searchResults.sources && $data.searchResults.sources.length > 0 ? {
    n: common_vendor.f($data.searchResults.sources, (source, index, i0) => {
      return {
        a: common_vendor.t(source),
        b: index
      };
    })
  } : {}) : {}, {
    o: common_vendor.f($data.searchResults.knowledgeList, (item, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(item.title),
        b: common_vendor.t(item.category),
        c: common_vendor.t(item.summary || item.content),
        d: item.tags
      }, item.tags ? {
        e: common_vendor.t(item.tags)
      } : {}, {
        f: common_vendor.t(item.viewCount || 0),
        g: item.id,
        h: common_vendor.o(($event) => $options.viewKnowledgeDetail(item.id), item.id)
      });
    })
  }) : !$data.searchKeyword && !$data.selectedCategory ? {} : {}, {
    j: $data.searchResults && $data.searchResults.knowledgeList && $data.searchResults.knowledgeList.length > 0,
    p: !$data.searchKeyword && !$data.selectedCategory
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-3d5fb6e9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/knowledge/knowledge.js.map
