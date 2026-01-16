"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const _sfc_main = {
  data() {
    return {
      selectedRange: "week",
      timeRanges: [
        { label: "一周", value: "week" },
        { label: "一月", value: "month" },
        { label: "三月", value: "quarter" },
        { label: "半年", value: "half_year" }
      ],
      analysisReport: "",
      chartData: null
    };
  },
  onLoad() {
    this.loadVisualizationData();
  },
  methods: {
    selectTimeRange(range) {
      this.selectedRange = range;
      this.loadVisualizationData();
    },
    async loadVisualizationData() {
      try {
        common_vendor.index.showLoading({ title: "加载中..." });
        const lineRes = await utils_api.emotionApi.getVisualization({
          timeRange: this.selectedRange,
          type: "line"
        });
        const pieRes = await utils_api.emotionApi.getStatistics({
          timeRange: this.selectedRange,
          type: "pie"
        });
        common_vendor.index.hideLoading();
        if (lineRes.code === 200 && pieRes.code === 200) {
          this.chartData = {
            line: lineRes.data,
            pie: pieRes.data
          };
          this.renderCharts();
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "加载数据失败",
          icon: "none"
        });
      }
    },
    renderCharts() {
      common_vendor.index.__f__("log", "at pages/emotion/visualization.vue:140", "渲染图表数据:", this.chartData);
    },
    async generateReport() {
      try {
        common_vendor.index.showLoading({ title: "生成中..." });
        const res = await utils_api.emotionApi.exportReport({
          timeRange: this.selectedRange,
          includeCharts: true,
          includeAnalysis: true
        });
        common_vendor.index.hideLoading();
        if (res.code === 200) {
          this.analysisReport = res.data.analysis || "基于你的情绪数据，AI生成了详细的分析报告。";
          common_vendor.index.showToast({
            title: "报告生成成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "生成失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "生成失败，请稍后重试",
          icon: "none"
        });
      }
    },
    async exportPDF() {
      try {
        common_vendor.index.showLoading({ title: "导出中..." });
        const res = await utils_api.emotionApi.exportReport({
          timeRange: this.selectedRange,
          format: "pdf",
          includeCharts: true,
          includeAnalysis: true
        });
        common_vendor.index.hideLoading();
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "PDF导出成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "导出失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "导出失败，请稍后重试",
          icon: "none"
        });
      }
    },
    async exportImage() {
      try {
        common_vendor.index.showLoading({ title: "导出中..." });
        const res = await utils_api.emotionApi.exportReport({
          timeRange: this.selectedRange,
          format: "image",
          includeCharts: true,
          includeAnalysis: true
        });
        common_vendor.index.hideLoading();
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "图片导出成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "导出失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "导出失败，请稍后重试",
          icon: "none"
        });
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.f($data.timeRanges, (range, k0, i0) => {
      return {
        a: common_vendor.t(range.label),
        b: range.value,
        c: $data.selectedRange === range.value ? 1 : "",
        d: common_vendor.o(($event) => $options.selectTimeRange(range.value), range.value)
      };
    }),
    b: common_vendor.t($data.analysisReport || "点击生成按钮获取智能分析报告"),
    c: common_vendor.o((...args) => $options.generateReport && $options.generateReport(...args), "56"),
    d: common_vendor.o((...args) => $options.exportPDF && $options.exportPDF(...args), "64"),
    e: common_vendor.o((...args) => $options.exportImage && $options.exportImage(...args), "76")
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1ff9f941"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/emotion/visualization.js.map
