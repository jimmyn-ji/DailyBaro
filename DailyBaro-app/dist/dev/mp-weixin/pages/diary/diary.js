"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_dateUtils = require("../../utils/dateUtils.js");
const _sfc_main = {
  __name: "diary",
  setup(__props) {
    const diaries = common_vendor.ref([]);
    const currentStatus = common_vendor.ref("all");
    const loading = common_vendor.ref(false);
    const filteredDiaries = common_vendor.computed(() => {
      if (currentStatus.value === "all") {
        return diaries.value;
      }
      return diaries.value.filter((diary) => diary.status === currentStatus.value);
    });
    common_vendor.onMounted(() => {
      loadDiaries();
    });
    const loadDiaries = async () => {
      try {
        loading.value = true;
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo && (userInfo.uid || userInfo.id || userInfo.userId);
        if (!uid) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        let queryParams = {};
        if (currentStatus.value !== "all") {
          queryParams.status = currentStatus.value;
        }
        const response = await utils_api.diaryApi.getDiaries(queryParams);
        common_vendor.index.__f__("log", "at pages/diary/diary.vue:142", "日记列表响应:", response);
        if (response && response.code === 200) {
          diaries.value = response.data || [];
          common_vendor.index.__f__("log", "at pages/diary/diary.vue:145", "日记数据:", diaries.value);
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "获取日记失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/diary.vue:153", "加载日记失败:", error);
        common_vendor.index.showToast({
          title: "加载失败，请重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
      }
    };
    const goToAnonymous = () => {
      common_vendor.index.navigateTo({
        url: "/pages/anonymous/anonymous"
      });
    };
    const createDiary = () => {
      common_vendor.index.navigateTo({
        url: "/pages/diary/edit"
      });
    };
    const viewDiary = (id) => {
      common_vendor.index.__f__("log", "at pages/diary/diary.vue:176", "查看日记，ID:", id);
      common_vendor.index.navigateTo({
        url: `/pages/diary/detail?id=${id}`
      });
    };
    const editDiary = (id) => {
      common_vendor.index.__f__("log", "at pages/diary/diary.vue:183", "编辑日记，ID:", id);
      common_vendor.index.navigateTo({
        url: `/pages/diary/edit?id=${id}`
      });
    };
    const deleteDiary = async (id) => {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这篇日记吗？删除后无法恢复。",
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await utils_api.diaryApi.deleteDiary(id);
              if (response && response.code === 200) {
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
                loadDiaries();
              } else {
                common_vendor.index.showToast({
                  title: (response == null ? void 0 : response.message) || "删除失败",
                  icon: "none"
                });
              }
            } catch (error) {
              common_vendor.index.__f__("error", "at pages/diary/diary.vue:211", "删除日记失败:", error);
              common_vendor.index.showToast({
                title: "删除失败，请重试",
                icon: "none"
              });
            }
          }
        }
      });
    };
    const filterByStatus = (status) => {
      currentStatus.value = status;
      loadDiaries();
    };
    const formatDate = (dateString) => {
      return utils_dateUtils.formatDateShort(dateString);
    };
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goToAnonymous, "dc"),
        b: currentStatus.value === "all" ? 1 : "",
        c: common_vendor.o(($event) => filterByStatus("all"), "d0"),
        d: currentStatus.value === "published" ? 1 : "",
        e: common_vendor.o(($event) => filterByStatus("published"), "3d"),
        f: currentStatus.value === "draft" ? 1 : "",
        g: common_vendor.o(($event) => filterByStatus("draft"), "e8"),
        h: common_vendor.o(createDiary, "a0"),
        i: loading.value
      }, loading.value ? {} : filteredDiaries.value.length > 0 ? {
        k: common_vendor.f(filteredDiaries.value, (diary, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(formatDate(diary.createTime)),
            b: common_vendor.t(diary.status === "published" ? "已发布" : "草稿"),
            c: common_vendor.n(diary.status),
            d: common_vendor.o(($event) => editDiary(diary.diaryId), diary.diaryId),
            e: common_vendor.o(($event) => deleteDiary(diary.diaryId), diary.diaryId),
            f: common_vendor.t(diary.content),
            g: diary.emotion
          }, diary.emotion ? {
            h: common_vendor.t(diary.emotion)
          } : {}, {
            i: diary.media && diary.media.length > 0
          }, diary.media && diary.media.length > 0 ? {
            j: common_vendor.t(diary.media.length)
          } : {}, {
            k: diary.diaryId,
            l: common_vendor.o(($event) => viewDiary(diary.diaryId), diary.diaryId)
          });
        })
      } : {
        l: common_vendor.o(createDiary, "c5")
      }, {
        j: filteredDiaries.value.length > 0
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ebf65a3f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/diary/diary.js.map
