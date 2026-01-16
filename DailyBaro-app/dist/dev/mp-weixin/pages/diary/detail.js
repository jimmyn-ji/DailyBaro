"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_dateUtils = require("../../utils/dateUtils.js");
const utils_config = require("../../utils/config.js");
const _sfc_main = {
  data() {
    return {
      diaryId: null,
      diary: null,
      loading: false,
      analyzing: false,
      audioPlaying: {},
      audioContexts: {},
      audioCurrentTime: {},
      audioDuration: {},
      audioProgress: {},
      emotionResult: null
    };
  },
  computed: {
    // 统一处理附件列表
    mediaList() {
      if (!this.diary) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:146", "mediaList: diary为空");
        return [];
      }
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:150", "mediaList computed: 开始处理附件");
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:151", "diary.media:", this.diary.media);
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:152", "diary.mediaUrls:", this.diary.mediaUrls);
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:153", "diary.mediaList:", this.diary.mediaList);
      if (this.diary.media && Array.isArray(this.diary.media) && this.diary.media.length > 0) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:157", "使用 diary.media，数量:", this.diary.media.length);
        return this.diary.media.map((item, index) => {
          if (typeof item === "string") {
            let mediaType = "image";
            if (item.includes(".mp4") || item.includes(".mov") || item.includes(".avi")) {
              mediaType = "video";
            } else if (item.includes(".mp3") || item.includes(".wav") || item.includes(".m4a")) {
              mediaType = "audio";
            }
            return {
              mediaId: index,
              mediaType,
              mediaUrl: item
            };
          }
          return {
            mediaId: item.mediaId || index,
            mediaType: item.mediaType || item.type || "image",
            mediaUrl: item.mediaUrl || item.url || item
          };
        });
      }
      if (this.diary.mediaUrls && Array.isArray(this.diary.mediaUrls) && this.diary.mediaUrls.length > 0) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:185", "使用 diary.mediaUrls，数量:", this.diary.mediaUrls.length);
        return this.diary.mediaUrls.map((url, index) => {
          let mediaType = "image";
          const urlLower = url.toLowerCase();
          if (urlLower.includes(".mp4") || urlLower.includes(".mov") || urlLower.includes(".avi") || urlLower.includes(".m4v")) {
            mediaType = "video";
          } else if (urlLower.includes(".mp3") || urlLower.includes(".wav") || urlLower.includes(".m4a") || urlLower.includes(".aac")) {
            mediaType = "audio";
          } else if (urlLower.includes(".jpg") || urlLower.includes(".jpeg") || urlLower.includes(".png") || urlLower.includes(".gif") || urlLower.includes(".webp")) {
            mediaType = "image";
          }
          return {
            mediaId: index,
            mediaType,
            mediaUrl: url
          };
        });
      }
      if (this.diary.mediaList && Array.isArray(this.diary.mediaList) && this.diary.mediaList.length > 0) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:207", "使用 diary.mediaList，数量:", this.diary.mediaList.length);
        return this.diary.mediaList;
      }
      common_vendor.index.__f__("warn", "at pages/diary/detail.vue:211", "mediaList: 未找到附件数据");
      return [];
    }
  },
  onLoad(options) {
    common_vendor.index.__f__("log", "at pages/diary/detail.vue:217", "日记详情页面参数:", options);
    if (options.id) {
      this.diaryId = options.id;
      this.loadDiary();
    } else {
      common_vendor.index.showToast({
        title: "参数错误",
        icon: "none"
      });
      setTimeout(() => {
        common_vendor.index.navigateBack();
      }, 1500);
    }
  },
  onUnload() {
    Object.values(this.audioContexts).forEach((audioContext) => {
      if (audioContext) {
        audioContext.stop();
        audioContext.destroy();
      }
    });
    this.audioContexts = {};
    this.audioPlaying = {};
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    async loadDiary() {
      this.loading = true;
      try {
        const res = await utils_api.diaryApi.getDiaryDetail(this.diaryId);
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:255", "========== 日记详情响应 ==========");
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:256", "完整响应:", JSON.stringify(res, null, 2));
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:257", "响应code:", res.code);
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:258", "响应data:", res.data);
        if (res.code === 200) {
          this.diary = res.data;
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:262", "========== 日记数据 ==========");
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:263", "日记对象:", this.diary);
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:264", "diary.media:", this.diary.media);
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:265", "diary.mediaUrls:", this.diary.mediaUrls);
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:266", "diary.mediaList:", this.diary.mediaList);
          this.$nextTick(() => {
            common_vendor.index.__f__("log", "at pages/diary/detail.vue:270", "========== 附件列表 ==========");
            common_vendor.index.__f__("log", "at pages/diary/detail.vue:271", "mediaList computed:", this.mediaList);
            common_vendor.index.__f__("log", "at pages/diary/detail.vue:272", "附件数量:", this.mediaList.length);
            if (this.mediaList.length > 0) {
              this.mediaList.forEach((media, index) => {
                common_vendor.index.__f__("log", "at pages/diary/detail.vue:275", `附件${index + 1}:`, {
                  type: media.mediaType,
                  url: media.mediaUrl,
                  fullUrl: this.getFullUrl(media.mediaUrl)
                });
              });
            } else {
              common_vendor.index.__f__("warn", "at pages/diary/detail.vue:282", "⚠️ 没有找到附件！");
            }
          });
        } else {
          common_vendor.index.showToast({
            title: res.message || "加载失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/detail.vue:292", "========== 加载日记失败 ==========");
        common_vendor.index.__f__("error", "at pages/diary/detail.vue:293", "错误详情:", error);
        common_vendor.index.showToast({
          title: error.message || "加载失败",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    getFullUrl(url) {
      if (!url) {
        common_vendor.index.__f__("warn", "at pages/diary/detail.vue:305", "getFullUrl: url为空");
        return "";
      }
      if (url.startsWith("http://") || url.startsWith("https://")) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:311", "getFullUrl: 完整URL，直接返回:", url);
        return url;
      }
      if (url.startsWith("/src/static/") || url.startsWith("src/static/")) {
        const filePath2 = url.replace(/^\/?src\/static\//, "/api/uploads/static/");
        const baseUrl2 = utils_config.config.BASE_URL;
        const fullUrl2 = baseUrl2 + filePath2;
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:321", "getFullUrl: 转换静态资源路径:", url, "->", fullUrl2);
        return fullUrl2;
      }
      let filePath = url;
      if (url.startsWith("/api/uploads/")) {
        filePath = url;
      } else if (url.startsWith("/uploads/")) {
        filePath = "/api/uploads/" + url.substring("/uploads/".length);
      } else if (url.startsWith("uploads/")) {
        filePath = "/api/uploads/" + url.substring("uploads/".length);
      } else if (!url.startsWith("/api/") && !url.startsWith("http") && !url.startsWith("/")) {
        filePath = "/api/uploads/" + url;
      } else if (url.startsWith("/") && !url.startsWith("/api/") && !url.startsWith("/uploads/")) {
        filePath = "/api/uploads" + url;
      }
      const baseUrl = utils_config.config.BASE_URL;
      const fullUrl = baseUrl + filePath;
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:353", "getFullUrl: 拼接URL:", url, "->", fullUrl);
      return fullUrl;
    },
    editDiary() {
      common_vendor.index.navigateTo({
        url: `/pages/diary/edit?id=${this.diaryId}`
      });
    },
    previewImage(url) {
      const urls = this.mediaList.filter((media) => media.mediaType === "image").map((media) => this.getFullUrl(media.mediaUrl));
      common_vendor.index.previewImage({
        current: url,
        urls
      });
    },
    formatDate(dateStr) {
      return utils_dateUtils.formatDateLocal(dateStr);
    },
    playAudio(url, index) {
      common_vendor.index.__f__("log", "at pages/diary/detail.vue:380", "播放音频:", url, "索引:", index);
      Object.keys(this.audioContexts).forEach((key) => {
        if (key != index && this.audioContexts[key]) {
          this.audioContexts[key].stop();
          this.$set(this.audioPlaying, key, false);
          this.$set(this.audioProgress, key, 0);
        }
      });
      if (!this.audioContexts[index]) {
        const audioContext2 = common_vendor.index.createInnerAudioContext();
        audioContext2.src = url;
        audioContext2.autoplay = false;
        audioContext2.onPlay(() => {
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:398", "音频开始播放");
          this.$set(this.audioPlaying, index, true);
        });
        audioContext2.onPause(() => {
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:403", "音频暂停");
          this.$set(this.audioPlaying, index, false);
        });
        audioContext2.onStop(() => {
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:408", "音频停止");
          this.$set(this.audioPlaying, index, false);
          this.$set(this.audioProgress, index, 0);
          this.$set(this.audioCurrentTime, index, 0);
        });
        audioContext2.onEnded(() => {
          common_vendor.index.__f__("log", "at pages/diary/detail.vue:415", "音频播放结束");
          this.$set(this.audioPlaying, index, false);
          this.$set(this.audioProgress, index, 0);
          this.$set(this.audioCurrentTime, index, 0);
        });
        audioContext2.onCanplay(() => {
          audioContext2.duration && this.$set(this.audioDuration, index, audioContext2.duration);
        });
        audioContext2.onTimeUpdate(() => {
          if (audioContext2.duration) {
            const currentTime = audioContext2.currentTime || 0;
            const duration = audioContext2.duration || 1;
            const progress = currentTime / duration * 100;
            this.$set(this.audioCurrentTime, index, currentTime);
            this.$set(this.audioDuration, index, duration);
            this.$set(this.audioProgress, index, progress);
          }
        });
        audioContext2.onError((err) => {
          common_vendor.index.__f__("error", "at pages/diary/detail.vue:439", "音频播放错误:", err);
          common_vendor.index.__f__("error", "at pages/diary/detail.vue:440", "错误详情:", JSON.stringify(err));
          common_vendor.index.__f__("error", "at pages/diary/detail.vue:441", "音频URL:", url);
          if (err.errMsg && err.errMsg.includes("404")) {
            let altUrl = null;
            if (url.includes("/api/uploads/")) {
              altUrl = url.replace("/api/uploads/", "/uploads/");
            } else if (url.includes("/uploads/")) {
              altUrl = url.replace("/uploads/", "/api/uploads/");
            }
            if (altUrl) {
              common_vendor.index.__f__("log", "at pages/diary/detail.vue:456", "尝试备用路径:", altUrl);
              audioContext2.destroy();
              this.$set(this.audioContexts, index, null);
              setTimeout(() => {
                this.playAudio(altUrl, index);
              }, 100);
              return;
            }
            common_vendor.index.showToast({
              title: "音频文件不存在，请检查文件路径",
              icon: "none",
              duration: 3e3
            });
          } else if (err.errMsg && err.errMsg.includes("decode")) {
            common_vendor.index.showToast({
              title: "音频格式不支持或文件损坏",
              icon: "none",
              duration: 3e3
            });
          } else {
            common_vendor.index.showToast({
              title: `音频播放失败: ${err.errMsg || "未知错误"}`,
              icon: "none",
              duration: 3e3
            });
          }
          this.$set(this.audioPlaying, index, false);
          if (this.audioContexts[index]) {
            this.audioContexts[index].destroy();
            this.$set(this.audioContexts, index, null);
          }
        });
        this.$set(this.audioContexts, index, audioContext2);
      }
      const audioContext = this.audioContexts[index];
      if (!audioContext) {
        common_vendor.index.__f__("error", "at pages/diary/detail.vue:500", "音频上下文不存在");
        return;
      }
      if (this.audioPlaying[index]) {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:505", "暂停音频");
        audioContext.pause();
        this.$set(this.audioPlaying, index, false);
      } else {
        common_vendor.index.__f__("log", "at pages/diary/detail.vue:509", "播放音频");
        audioContext.play();
        this.$set(this.audioPlaying, index, true);
      }
    },
    seekAudio(e, index) {
      const audioContext = this.audioContexts[index];
      if (!audioContext || !audioContext.duration) return;
      const query = common_vendor.index.createSelectorQuery().in(this);
      query.select(`.progress-bar-wrapper`).boundingClientRect((rect) => {
        if (rect) {
          const x = e.detail.x - rect.left;
          const percent = x / rect.width;
          const seekTime = percent * audioContext.duration;
          audioContext.seek(seekTime);
          this.$set(this.audioCurrentTime, index, seekTime);
          this.$set(this.audioProgress, index, percent * 100);
        }
      }).exec();
    },
    formatAudioTime(seconds) {
      if (!seconds || isNaN(seconds)) return "00:00";
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
    },
    // 分析情绪
    async analyzeEmotion() {
      var _a;
      if (this.analyzing) return;
      this.analyzing = true;
      try {
        common_vendor.index.showLoading({ title: "分析中..." });
        if (this.mediaList && this.mediaList.length > 0) {
          const audioMedia = this.mediaList.find((m) => m.mediaType === "audio");
          const videoMedia = this.mediaList.find((m) => m.mediaType === "video");
          const imageMedia = this.mediaList.find((m) => m.mediaType === "image");
          const mediaToAnalyze = audioMedia || videoMedia || imageMedia;
          if (mediaToAnalyze) {
            const type = mediaToAnalyze.mediaType;
            const res = await utils_api.emotionApi.analyzeFromDiary(this.diaryId, type);
            common_vendor.index.hideLoading();
            if (res.code === 200) {
              this.emotionResult = res.data;
              common_vendor.index.showToast({
                title: "分析完成",
                icon: "success"
              });
              common_vendor.index.__f__("log", "at pages/diary/detail.vue:567", "分析结果:", res.data);
              return;
            } else {
              common_vendor.index.showToast({
                title: res.message || "分析失败",
                icon: "none"
              });
            }
          }
        }
        if (this.diary && this.diary.content) {
          const baseUrl = utils_config.config.BASE_URL || "https://dailybaro.cn";
          const userInfo = common_vendor.index.getStorageSync("userInfo");
          const uid = userInfo ? userInfo.uid : "";
          const response = await common_vendor.index.request({
            url: baseUrl + "/app/diary/analyze-emotion",
            method: "POST",
            header: {
              "Content-Type": "application/json",
              "uid": uid
            },
            data: {
              content: this.diary.content
            }
          });
          common_vendor.index.hideLoading();
          if (response.data && response.data.code === 200) {
            this.emotionResult = response.data.data;
            common_vendor.index.showToast({
              title: "文本分析完成",
              icon: "success"
            });
            common_vendor.index.__f__("log", "at pages/diary/detail.vue:604", "文本分析结果:", response.data.data);
          } else {
            common_vendor.index.showToast({
              title: ((_a = response.data) == null ? void 0 : _a.message) || "分析失败",
              icon: "none"
            });
          }
        } else {
          common_vendor.index.hideLoading();
          common_vendor.index.showToast({
            title: "没有可分析的内容",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/diary/detail.vue:620", "分析失败:", error);
        common_vendor.index.showToast({
          title: error.message || "分析失败",
          icon: "none"
        });
      } finally {
        this.analyzing = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "b4"),
    b: common_vendor.o((...args) => $options.editDiary && $options.editDiary(...args), "1b"),
    c: $data.loading
  }, $data.loading ? {} : $data.diary ? common_vendor.e({
    e: common_vendor.t($data.diary.title || "无标题"),
    f: common_vendor.t($options.formatDate($data.diary.createTime)),
    g: $data.diary.status === "draft"
  }, $data.diary.status === "draft" ? {} : {}, {
    h: common_vendor.t($data.diary.content || "暂无内容"),
    i: $options.mediaList && $options.mediaList.length > 0
  }, $options.mediaList && $options.mediaList.length > 0 ? common_vendor.e({
    j: common_vendor.f($options.mediaList, (media, index, i0) => {
      return common_vendor.e({
        a: media.mediaType === "image"
      }, media.mediaType === "image" ? {
        b: $options.getFullUrl(media.mediaUrl),
        c: common_vendor.o(($event) => $options.previewImage($options.getFullUrl(media.mediaUrl)), index)
      } : media.mediaType === "video" ? {
        e: $options.getFullUrl(media.mediaUrl)
      } : media.mediaType === "audio" ? {
        g: common_vendor.t($data.audioPlaying[index] ? "⏸" : "▶"),
        h: common_vendor.o(($event) => $options.playAudio($options.getFullUrl(media.mediaUrl), index), index),
        i: common_vendor.t($options.formatAudioTime($data.audioCurrentTime[index] || 0)),
        j: common_vendor.t($options.formatAudioTime($data.audioDuration[index] || 0)),
        k: $data.audioProgress[index] + "%",
        l: common_vendor.o(($event) => $options.seekAudio($event, index), index)
      } : {}, {
        d: media.mediaType === "video",
        f: media.mediaType === "audio",
        m: index
      });
    }),
    k: common_vendor.t($data.analyzing ? "分析中..." : "情绪分析"),
    l: common_vendor.o((...args) => $options.analyzeEmotion && $options.analyzeEmotion(...args), "fa"),
    m: $data.analyzing,
    n: $data.emotionResult
  }, $data.emotionResult ? common_vendor.e({
    o: $data.emotionResult.emotion
  }, $data.emotionResult.emotion ? {
    p: common_vendor.t($data.emotionResult.emotion)
  } : {}, {
    q: $data.emotionResult.score !== void 0
  }, $data.emotionResult.score !== void 0 ? {
    r: common_vendor.t($data.emotionResult.score)
  } : {}, {
    s: $data.emotionResult.confidence !== void 0
  }, $data.emotionResult.confidence !== void 0 ? {
    t: common_vendor.t(($data.emotionResult.confidence * 100).toFixed(1))
  } : {}, {
    v: $data.emotionResult.description
  }, $data.emotionResult.description ? {
    w: common_vendor.t($data.emotionResult.description)
  } : {}) : {}) : {}) : {}, {
    d: $data.diary
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-fc64f144"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/diary/detail.js.map
