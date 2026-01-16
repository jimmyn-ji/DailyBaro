"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_dateUtils = require("../../utils/dateUtils.js");
const utils_config = require("../../utils/config.js");
const _sfc_main = {
  data() {
    return {
      diaryContent: "",
      diaryDate: "",
      emotionTags: ["开心", "平静", "兴奋", "焦虑", "悲伤", "愤怒", "困惑", "满足", "放松", "紧张", "期待", "信任", "消极", "积极", "中性"],
      selectedEmotion: "等待识别...",
      // 自动识别的情绪标签
      isEdit: false,
      diaryId: null,
      analyzing: false,
      emotionResult: null,
      selectedImage: null,
      selectedAudio: null,
      selectedVideo: null,
      analyzingImage: false,
      analyzingAudio: false,
      multimodalResult: null,
      autoAnalyzeTimer: null
      // 自动分析定时器（防抖）
    };
  },
  onLoad(options) {
    common_vendor.index.__f__("log", "at pages/diary/edit.vue:186", "编辑页面参数:", options);
    this.initPage(options);
  },
  methods: {
    // 获取基础URL
    getBaseUrl() {
      return utils_config.config.BASE_URL;
    },
    // 获取完整URL（处理相对路径）
    getFullUrl(url) {
      if (!url) {
        return "";
      }
      if (url.startsWith("http://") || url.startsWith("https://")) {
        return url;
      }
      if (url.startsWith("/src/static/") || url.startsWith("src/static/")) {
        const filePath2 = url.replace(/^\/?src\/static\//, "/api/uploads/static/");
        const baseUrl2 = this.getBaseUrl();
        return baseUrl2 + filePath2;
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
      const baseUrl = this.getBaseUrl();
      const fullUrl = baseUrl + filePath;
      common_vendor.index.__f__("log", "at pages/diary/edit.vue:235", "edit.vue getFullUrl: 拼接URL:", url, "->", fullUrl);
      return fullUrl;
    },
    initPage(options) {
      const today = /* @__PURE__ */ new Date();
      this.diaryDate = today.toISOString().split("T")[0];
      if (options && options.id) {
        this.isEdit = true;
        this.diaryId = options.id;
        common_vendor.index.__f__("log", "at pages/diary/edit.vue:248", "编辑模式，日记ID:", this.diaryId);
        this.loadDiaryData();
      }
    },
    async loadDiaryData() {
      try {
        const response = await utils_api.diaryApi.getDiaryDetail(this.diaryId);
        if (response && response.code === 200) {
          const diary = response.data;
          this.diaryContent = diary.content || "";
          if (diary.createTime) {
            const dateStr = utils_dateUtils.formatDateToStandard(diary.createTime);
            if (dateStr) {
              this.diaryDate = dateStr;
            }
          }
          this.selectedEmotion = diary.emotion || "等待识别...";
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/edit.vue:273", "加载日记数据失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      }
    },
    goBack() {
      common_vendor.index.navigateBack();
    },
    onDateChange(e) {
      this.diaryDate = e.detail.value;
    },
    // 文本输入监听（自动情绪识别）
    onContentInput(e) {
      this.diaryContent = e.detail.value;
      if (this.autoAnalyzeTimer) {
        clearTimeout(this.autoAnalyzeTimer);
      }
      if (!this.diaryContent || this.diaryContent.trim().length < 10) {
        this.selectedEmotion = "等待识别...";
        return;
      }
      this.autoAnalyzeTimer = setTimeout(() => {
        this.autoAnalyzeEmotion();
      }, 1e3);
    },
    // 自动情绪识别（文本）
    async autoAnalyzeEmotion() {
      if (!this.diaryContent || this.diaryContent.trim().length < 10) {
        return;
      }
      this.analyzing = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        if (!uid) {
          return;
        }
        const response = await common_vendor.index.request({
          url: baseUrl + "/app/emotion/analysis",
          method: "POST",
          header: {
            "Content-Type": "application/json",
            "uid": uid
          },
          data: {
            text: this.diaryContent
          }
        });
        if (response.data && response.data.code === 200) {
          const data = response.data.data;
          const emotion = (data == null ? void 0 : data.emotion) || (data == null ? void 0 : data.primaryEmotion) || (data == null ? void 0 : data.label);
          if (emotion) {
            const mappedEmotion = this.mapEmotionToTag(emotion);
            this.selectedEmotion = mappedEmotion;
            this.emotionResult = data;
            common_vendor.index.__f__("log", "at pages/diary/edit.vue:346", "情绪识别成功:", { 原始: emotion, 映射后: mappedEmotion, 完整数据: data });
          } else {
            common_vendor.index.__f__("warn", "at pages/diary/edit.vue:348", "情绪识别响应中未找到情绪字段:", data);
          }
        } else {
          common_vendor.index.__f__("warn", "at pages/diary/edit.vue:351", "情绪识别失败:", response.data);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/edit.vue:354", "自动情绪识别失败:", error);
      } finally {
        this.analyzing = false;
      }
    },
    // 映射后端返回的情绪到前端标签
    mapEmotionToTag(emotion) {
      if (!emotion) return "等待识别...";
      const emotionLower = emotion.toLowerCase().trim();
      const emotionMap = {
        // 积极情绪
        "开心": "开心",
        "快乐": "开心",
        "高兴": "开心",
        "愉快": "开心",
        "positive": "开心",
        "积极": "开心",
        "happy": "开心",
        "joy": "开心",
        "joyful": "开心",
        // 中性情绪
        "平静": "平静",
        "冷静": "平静",
        "平和": "平静",
        "neutral": "平静",
        "中性": "平静",
        "calm": "平静",
        "peaceful": "平静",
        // 兴奋情绪
        "兴奋": "兴奋",
        "激动": "兴奋",
        "excited": "兴奋",
        "excitement": "兴奋",
        // 焦虑情绪
        "焦虑": "焦虑",
        "担心": "焦虑",
        "不安": "焦虑",
        "anxious": "焦虑",
        "anxiety": "焦虑",
        "worried": "焦虑",
        // 紧张情绪
        "紧张": "紧张",
        "nervous": "紧张",
        "tense": "紧张",
        // 消极情绪
        "悲伤": "悲伤",
        "难过": "悲伤",
        "伤心": "悲伤",
        "negative": "悲伤",
        "消极": "悲伤",
        "sad": "悲伤",
        "sadness": "悲伤",
        "depressed": "悲伤",
        // 愤怒情绪
        "愤怒": "愤怒",
        "生气": "愤怒",
        "angry": "愤怒",
        "anger": "愤怒",
        "mad": "愤怒",
        // 其他情绪
        "困惑": "困惑",
        "confused": "困惑",
        "confusion": "困惑",
        "满足": "满足",
        "satisfied": "满足",
        "satisfaction": "满足",
        "放松": "放松",
        "relaxed": "放松",
        "relaxation": "放松",
        "期待": "期待",
        "expectation": "期待",
        "信任": "信任",
        "trust": "信任",
        "疲惫": "疲惫",
        "tired": "疲惫",
        "exhausted": "疲惫"
      };
      const directMatch = emotionMap[emotion] || emotionMap[emotionLower];
      if (directMatch) {
        return directMatch;
      }
      for (const [key, value] of Object.entries(emotionMap)) {
        if (emotionLower.includes(key.toLowerCase()) || key.toLowerCase().includes(emotionLower)) {
          return value;
        }
      }
      if (this.emotionTags.includes(emotion)) {
        return emotion;
      }
      common_vendor.index.__f__("warn", "at pages/diary/edit.vue:463", "未识别的情绪:", emotion, "，使用默认值: 平静");
      return "平静";
    },
    // 关闭情绪分析结果
    closeEmotionResult() {
      this.emotionResult = null;
    },
    // 选择图片进行多模态分析
    chooseImageForAnalysis() {
      common_vendor.index.getSetting({
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/diary/edit.vue:477", "当前权限设置:", res.authSetting);
          const hasCameraAuth = res.authSetting["scope.camera"] !== false;
          const hasAlbumAuth = res.authSetting["scope.writePhotosAlbum"] !== false;
          if (!hasCameraAuth || !hasAlbumAuth) {
            common_vendor.index.authorize({
              scope: "scope.camera",
              success: () => {
                common_vendor.index.__f__("log", "at pages/diary/edit.vue:488", "相机权限授权成功");
                this.selectImage();
              },
              fail: (err) => {
                common_vendor.index.__f__("log", "at pages/diary/edit.vue:492", "相机权限授权失败:", err);
                common_vendor.index.showModal({
                  title: "权限提示",
                  content: "需要相机和相册权限来选择图片，请在设置中开启",
                  confirmText: "去设置",
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      common_vendor.index.openSetting({
                        success: (settingRes) => {
                          common_vendor.index.__f__("log", "at pages/diary/edit.vue:501", "设置页面返回:", settingRes);
                          if (settingRes.authSetting["scope.camera"] && settingRes.authSetting["scope.writePhotosAlbum"]) {
                            this.selectImage();
                          }
                        }
                      });
                    }
                  }
                });
              }
            });
          } else {
            this.selectImage();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:518", "获取权限设置失败:", err);
          this.selectImage();
        }
      });
    },
    // 实际选择图片的方法
    selectImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["original", "compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/diary/edit.vue:532", "选择图片成功:", res);
          this.selectedImage = res.tempFilePaths[0];
          this.analyzeImageEmotion();
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:537", "选择图片失败:", err);
          if (err.errMsg && err.errMsg.includes("cancel")) {
            common_vendor.index.showToast({
              title: "已取消选择图片",
              icon: "none"
            });
          } else if (err.errMsg && err.errMsg.includes("auth")) {
            common_vendor.index.showModal({
              title: "权限不足",
              content: "需要相机和相册权限，请在设置中开启",
              confirmText: "去设置",
              success: (modalRes) => {
                if (modalRes.confirm) {
                  common_vendor.index.openSetting();
                }
              }
            });
          } else {
            common_vendor.index.showToast({
              title: "选择图片失败，请重试",
              icon: "none"
            });
          }
        }
      });
    },
    // 选择音频进行多模态分析
    chooseAudioForAnalysis() {
      common_vendor.index.getSetting({
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/diary/edit.vue:571", "当前权限设置:", res.authSetting);
          const hasRecordAuth = res.authSetting["scope.record"] !== false;
          if (!hasRecordAuth) {
            common_vendor.index.authorize({
              scope: "scope.record",
              success: () => {
                common_vendor.index.__f__("log", "at pages/diary/edit.vue:581", "录音权限授权成功");
                this.selectAudio();
              },
              fail: (err) => {
                common_vendor.index.__f__("log", "at pages/diary/edit.vue:585", "录音权限授权失败:", err);
                common_vendor.index.showModal({
                  title: "权限提示",
                  content: "需要录音权限来选择音频，请在设置中开启",
                  confirmText: "去设置",
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      common_vendor.index.openSetting({
                        success: (settingRes) => {
                          common_vendor.index.__f__("log", "at pages/diary/edit.vue:594", "设置页面返回:", settingRes);
                          if (settingRes.authSetting["scope.record"]) {
                            this.selectAudio();
                          }
                        }
                      });
                    }
                  }
                });
              }
            });
          } else {
            this.selectAudio();
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:611", "获取权限设置失败:", err);
          this.selectAudio();
        }
      });
    },
    // 实际选择音频的方法
    selectAudio() {
      if (common_vendor.index.chooseAudio && typeof common_vendor.index.chooseAudio === "function") {
        common_vendor.index.chooseAudio({
          count: 1,
          success: (res) => {
            common_vendor.index.__f__("log", "at pages/diary/edit.vue:625", "选择音频成功:", res);
            this.selectedAudio = {
              tempFilePath: res.tempFilePath,
              name: res.tempFilePath.split("/").pop() || "audio.mp3",
              size: res.size || 0
            };
            this.analyzeAudioEmotion();
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/diary/edit.vue:634", "chooseAudio 选择音频失败:", err);
            this.selectAudioWithChooseMedia();
          }
        });
      } else {
        this.selectAudioWithChooseMedia();
      }
    },
    // 使用 chooseMedia 选择音频（备选方案）
    selectAudioWithChooseMedia() {
      if (common_vendor.index.chooseMedia && typeof common_vendor.index.chooseMedia === "function") {
        common_vendor.index.chooseMedia({
          count: 1,
          mediaType: ["audio"],
          sourceType: ["album", "camera"],
          success: (res) => {
            common_vendor.index.__f__("log", "at pages/diary/edit.vue:653", "chooseMedia 选择音频成功:", res);
            if (res.tempFiles && res.tempFiles.length > 0) {
              const file = res.tempFiles[0];
              this.selectedAudio = {
                tempFilePath: file.tempFilePath,
                name: file.name || file.tempFilePath.split("/").pop() || "audio.mp3",
                size: file.size || 0
              };
              this.analyzeAudioEmotion();
            } else {
              this.handleAudioSelectError({ errMsg: "未选择音频文件" });
            }
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/diary/edit.vue:667", "chooseMedia 选择音频失败:", err);
            this.handleAudioSelectError(err);
          }
        });
      } else {
        common_vendor.index.showToast({
          title: "当前环境不支持音频选择",
          icon: "none",
          duration: 2e3
        });
      }
    },
    // 处理音频选择错误
    handleAudioSelectError(err) {
      if (err.errMsg && err.errMsg.includes("cancel")) {
        common_vendor.index.showToast({
          title: "已取消选择音频",
          icon: "none"
        });
      } else if (err.errMsg && err.errMsg.includes("auth")) {
        common_vendor.index.showModal({
          title: "权限不足",
          content: "需要录音权限，请在设置中开启",
          confirmText: "去设置",
          success: (modalRes) => {
            if (modalRes.confirm) {
              common_vendor.index.openSetting();
            }
          }
        });
      } else {
        common_vendor.index.showToast({
          title: "选择音频失败，请重试",
          icon: "none"
        });
      }
    },
    onUnload() {
    },
    // 格式化音频时间
    formatAudioTime(seconds) {
      if (!seconds || isNaN(seconds)) return "00:00";
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
    },
    // 移除已选择的图片
    removeSelectedImage() {
      this.selectedImage = null;
      this.multimodalResult = null;
    },
    // 移除已选择的音频
    removeSelectedAudio() {
      this.selectedAudio = null;
      this.multimodalResult = null;
    },
    // 格式化文件大小
    formatFileSize(size) {
      if (size === 0) return "0 Bytes";
      const k = 1024;
      const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
      const i = Math.floor(Math.log(size) / Math.log(k));
      return parseFloat((size / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    },
    // 分析图片情绪
    async analyzeImageEmotion() {
      var _a, _b;
      if (!this.selectedImage) {
        common_vendor.index.showToast({
          title: "请先选择一张图片",
          icon: "none"
        });
        return;
      }
      this.analyzingImage = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        if (!uid) {
          common_vendor.index.showToast({
            title: "用户未登录，请重新登录",
            icon: "none"
          });
          return;
        }
        common_vendor.index.__f__("log", "at pages/diary/edit.vue:765", "开始图片情绪分析，文件:", this.selectedImage);
        const uploadRes = await new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: baseUrl + "/app/emotion/analyze/image",
            filePath: this.selectedImage,
            name: "image",
            header: { "uid": uid },
            success: (res) => {
              common_vendor.index.__f__("log", "at pages/diary/edit.vue:774", "图片上传成功:", res);
              resolve(res);
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/diary/edit.vue:778", "图片上传失败:", err);
              reject(err);
            }
          });
        });
        common_vendor.index.__f__("log", "at pages/diary/edit.vue:784", "图片分析响应:", uploadRes);
        let response;
        try {
          response = JSON.parse(uploadRes.data);
          common_vendor.index.__f__("log", "at pages/diary/edit.vue:789", "解析后的响应:", response);
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:791", "响应解析失败:", e, uploadRes.data);
          response = {};
        }
        if (response && (response.code === 200 || response.status === "ok")) {
          const data = response.data || response.result || response;
          this.multimodalResult = { ...this.multimodalResult, image: data.data || data.image || data };
          const emotion = data.emotion || ((_a = data.data) == null ? void 0 : _a.emotion) || ((_b = data.image) == null ? void 0 : _b.emotion) || data.label || data.primaryEmotion;
          if (emotion) {
            const mappedEmotion = this.mapEmotionToTag(emotion);
            this.selectedEmotion = mappedEmotion;
            common_vendor.index.__f__("log", "at pages/diary/edit.vue:804", "图像情绪识别:", { 原始: emotion, 映射后: mappedEmotion });
          }
          common_vendor.index.showToast({
            title: "图像情绪分析完成",
            icon: "success"
          });
        } else {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:812", "图片分析失败，响应:", response);
          common_vendor.index.showToast({
            title: response.message || "图像情绪分析失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/edit.vue:819", "图像情绪分析失败:", error);
        common_vendor.index.showToast({
          title: "图像情绪分析失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.analyzingImage = false;
      }
    },
    // 分析音频情绪
    async analyzeAudioEmotion() {
      var _a, _b;
      if (!this.selectedAudio) {
        common_vendor.index.showToast({
          title: "请先选择一个音频文件",
          icon: "none"
        });
        return;
      }
      this.analyzingAudio = true;
      try {
        const baseUrl = this.getBaseUrl();
        const userInfo = common_vendor.index.getStorageSync("userInfo");
        const uid = userInfo ? userInfo.uid : "";
        if (!uid) {
          common_vendor.index.showToast({
            title: "用户未登录，请重新登录",
            icon: "none"
          });
          return;
        }
        common_vendor.index.__f__("log", "at pages/diary/edit.vue:853", "开始音频情绪分析，文件:", this.selectedAudio);
        const uploadRes = await new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: baseUrl + "/api/emotion/analyze/audio",
            filePath: this.selectedAudio.tempFilePath,
            name: "audio",
            header: { "uid": uid },
            success: (res) => {
              common_vendor.index.__f__("log", "at pages/diary/edit.vue:862", "音频上传成功:", res);
              resolve(res);
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/diary/edit.vue:866", "音频上传失败:", err);
              reject(err);
            }
          });
        });
        common_vendor.index.__f__("log", "at pages/diary/edit.vue:872", "音频分析响应:", uploadRes);
        let response;
        try {
          response = JSON.parse(uploadRes.data);
          common_vendor.index.__f__("log", "at pages/diary/edit.vue:877", "解析后的响应:", response);
        } catch (e) {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:879", "响应解析失败:", e, uploadRes.data);
          response = {};
        }
        if (response && (response.code === 200 || response.status === "ok")) {
          const data = response.data || response.result || response;
          this.multimodalResult = { ...this.multimodalResult, audio: data.data || data.audio || data };
          const emotion = data.emotion || ((_a = data.data) == null ? void 0 : _a.emotion) || ((_b = data.audio) == null ? void 0 : _b.emotion) || data.label || data.primaryEmotion;
          if (emotion) {
            const mappedEmotion = this.mapEmotionToTag(emotion);
            this.selectedEmotion = mappedEmotion;
            common_vendor.index.__f__("log", "at pages/diary/edit.vue:892", "音频情绪识别:", { 原始: emotion, 映射后: mappedEmotion });
          }
          common_vendor.index.showToast({
            title: "音频情绪分析完成",
            icon: "success"
          });
        } else {
          common_vendor.index.__f__("error", "at pages/diary/edit.vue:900", "音频分析失败，响应:", response);
          common_vendor.index.showToast({
            title: response.message || "音频情绪分析失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/edit.vue:907", "音频情绪分析失败:", error);
        common_vendor.index.showToast({
          title: "音频情绪分析失败，请稍后重试",
          icon: "none"
        });
      } finally {
        this.analyzingAudio = false;
      }
    },
    // 关闭多模态分析结果
    closeMultimodalResult() {
      this.multimodalResult = null;
    },
    async saveAsDraft() {
      await this.saveDiary("draft");
    },
    async publishDiary() {
      await this.saveDiary("published");
    },
    async saveDiary(status) {
      if (!this.diaryContent.trim()) {
        common_vendor.index.showToast({
          title: "请输入日记内容",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.showLoading({ title: "保存中..." });
        const diaryData = {
          content: this.diaryContent,
          emotion: this.selectedEmotion,
          createTime: this.diaryDate,
          status
        };
        let response;
        if (this.isEdit) {
          response = await utils_api.diaryApi.updateDiary(this.diaryId, diaryData);
        } else {
          response = await utils_api.diaryApi.createDiary(diaryData);
        }
        if (response && response.code === 200) {
          common_vendor.index.showToast({
            title: status === "draft" ? "草稿保存成功" : "日记发布成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          common_vendor.index.showToast({
            title: (response == null ? void 0 : response.message) || "保存失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/diary/edit.vue:972", "保存日记失败:", error);
        common_vendor.index.showToast({
          title: "保存失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args), "a0"),
    b: common_vendor.t($data.isEdit ? "编辑日记" : "写日记"),
    c: common_vendor.o([($event) => $data.diaryContent = $event.detail.value, (...args) => $options.onContentInput && $options.onContentInput(...args)], "13"),
    d: $data.diaryContent,
    e: common_vendor.t($data.diaryContent.length),
    f: common_vendor.t($data.diaryDate),
    g: $data.diaryDate,
    h: common_vendor.o((...args) => $options.onDateChange && $options.onDateChange(...args), "7b"),
    i: common_vendor.t($data.selectedEmotion || "等待识别..."),
    j: $data.analyzing
  }, $data.analyzing ? {} : {}, {
    k: !$data.selectedImage && !$data.selectedAudio
  }, !$data.selectedImage && !$data.selectedAudio ? {
    l: common_vendor.o((...args) => $options.chooseImageForAnalysis && $options.chooseImageForAnalysis(...args), "cd"),
    m: $data.analyzing,
    n: common_vendor.o((...args) => $options.chooseAudioForAnalysis && $options.chooseAudioForAnalysis(...args), "07"),
    o: $data.analyzing
  } : {}, {
    p: $data.selectedImage
  }, $data.selectedImage ? {
    q: common_vendor.o((...args) => $options.chooseImageForAnalysis && $options.chooseImageForAnalysis(...args), "c3"),
    r: common_vendor.o((...args) => $options.removeSelectedImage && $options.removeSelectedImage(...args), "8f"),
    s: $data.selectedImage,
    t: common_vendor.t($data.analyzingImage ? "分析中..." : "分析图片情绪"),
    v: common_vendor.o((...args) => $options.analyzeImageEmotion && $options.analyzeImageEmotion(...args), "d8"),
    w: $data.analyzingImage,
    x: $data.analyzingImage
  } : {}, {
    y: $data.selectedAudio
  }, $data.selectedAudio ? {
    z: common_vendor.o((...args) => $options.chooseAudioForAnalysis && $options.chooseAudioForAnalysis(...args), "3e"),
    A: common_vendor.o((...args) => $options.removeSelectedAudio && $options.removeSelectedAudio(...args), "37"),
    B: common_vendor.t($data.selectedAudio.name),
    C: common_vendor.t($options.formatFileSize($data.selectedAudio.size)),
    D: common_vendor.t($data.analyzingAudio ? "分析中..." : "分析音频情绪"),
    E: common_vendor.o((...args) => $options.analyzeAudioEmotion && $options.analyzeAudioEmotion(...args), "bd"),
    F: $data.analyzingAudio,
    G: $data.analyzingAudio
  } : {}, {
    H: $data.multimodalResult
  }, $data.multimodalResult ? common_vendor.e({
    I: common_vendor.o((...args) => $options.closeMultimodalResult && $options.closeMultimodalResult(...args), "3d"),
    J: $data.multimodalResult.text
  }, $data.multimodalResult.text ? {
    K: common_vendor.t($data.multimodalResult.text.emotion)
  } : {}, {
    L: $data.multimodalResult.image
  }, $data.multimodalResult.image ? {
    M: common_vendor.t($data.multimodalResult.image.emotion)
  } : {}, {
    N: $data.multimodalResult.audio
  }, $data.multimodalResult.audio ? {
    O: common_vendor.t($data.multimodalResult.audio.emotion)
  } : {}, {
    P: $data.multimodalResult.fused
  }, $data.multimodalResult.fused ? {
    Q: common_vendor.t($data.multimodalResult.fused.emotion)
  } : {}) : {}, {
    R: common_vendor.o((...args) => $options.saveAsDraft && $options.saveAsDraft(...args), "4c"),
    S: common_vendor.o((...args) => $options.publishDiary && $options.publishDiary(...args), "b0")
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-48e4f991"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/diary/edit.js.map
