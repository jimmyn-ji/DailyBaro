from typing import Dict, List
import os
import time
import logging
import numpy as np
import io

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

try:
    import torch
    from transformers import Wav2Vec2ForSequenceClassification, Wav2Vec2FeatureExtractor, AutoConfig
    import librosa
    TORCH_AUDIO = True
except Exception as e:
    logger.warning(f"音频分析依赖不可用: {e}")
    TORCH_AUDIO = False

# EmoDB数据集标准情绪映射
EMODB_EMOTIONS = {
    "calm": "平静",
    "angry": "愤怒",
    "sad": "悲伤",
    "happy": "开心",
    "fear": "恐惧",
    "disgust": "厌恶",
    "surprise": "惊讶",
    "neutral": "中性"
}

# 情绪强度映射
EMOTION_INTENSITY = {
    "愤怒": 8.5, "恐惧": 9.0, "惊讶": 7.5, "开心": 7.0,
    "悲伤": 6.5, "厌恶": 8.0, "中性": 5.0, "平静": 4.0
}

def _top(scores: Dict[str, float], k: int = 3) -> List[Dict]:
    """获取Top K情绪"""
    items = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:k]
    return [{"emotion": EMODB_EMOTIONS.get(k, k), "score": float(v)} for k, v in items]

class AudioEmotionAnalyzer:
    def __init__(self):
        self.model = None
        self.feat = None
        self.config = None

        if TORCH_AUDIO:
            try:
                model_id = os.environ.get("AUDIO_MODEL_ID", "superb/wav2vec2-base-superb-er")
                logger.info(f"加载音频模型: {model_id}")

                self.model = Wav2Vec2ForSequenceClassification.from_pretrained(model_id)
                self.feat = Wav2Vec2FeatureExtractor.from_pretrained(model_id)
                self.config = AutoConfig.from_pretrained(model_id)

                logger.info("音频模型加载成功")
            except Exception as e:
                logger.error(f"音频模型加载失败: {e}")
                self.model = None
                self.feat = None
                self.config = None

    def analyze(self, audio_bytes: bytes) -> Dict:
        """分析音频情绪"""
        start_time = time.time()

        try:
            if self.model is None or self.feat is None:
                return self._fallback_analysis()

            # 加载音频
            y, sr = librosa.load(io.BytesIO(audio_bytes), sr=16000, mono=True)

            # 音频特征提取
            audio_features = self._extract_audio_features(y, sr)

            # 模型推理
            inputs = self.feat(y, sampling_rate=16000, return_tensors="pt")

            with torch.no_grad():
                logits = self.model(**inputs).logits
                probs = torch.softmax(logits, dim=-1)[0].cpu().numpy()

            # 获取情绪标签
            id2label = getattr(self.config, "id2label", {}) or {}
            scores = {id2label.get(i, str(i)).lower(): float(probs[i]) for i in range(len(probs))}

            # 映射到EmoDB情绪
            mapped_scores = {}
            for k, v in scores.items():
                if k in EMODB_EMOTIONS:
                    mapped_scores[k] = mapped_scores.get(k, 0.0) + v

            # 如果没有映射到标准情绪，使用最高概率的情绪
            if not mapped_scores:
                max_idx = np.argmax(probs)
                max_emotion = id2label.get(max_idx, "neutral").lower()
                mapped_scores[max_emotion] = float(np.max(probs))

            # 找到主要情绪
            primary_emotion = max(mapped_scores.items(), key=lambda x: x[1])
            emotion = primary_emotion[0]
            confidence = primary_emotion[1]

            # 计算情绪强度
            intensity = self._calculate_intensity(confidence, emotion)

            # 获取Top情绪
            top_emotions = _top(mapped_scores)

            # 获取所有情绪分布
            all_emotions = {EMODB_EMOTIONS.get(k, k): round(v, 4) for k, v in mapped_scores.items()}

            processing_time = int((time.time() - start_time) * 1000)

            return {
                "emotion": EMODB_EMOTIONS.get(emotion, emotion),
                "confidence": round(confidence, 4),
                "intensity": round(intensity, 1),
                "all_emotions": all_emotions,
                "top_emotions": top_emotions,
                "audio_features": audio_features,
                "processing_time_ms": processing_time,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
            }

        except Exception as e:
            logger.error(f"音频分析失败: {e}")
            return self._fallback_analysis()

    def _extract_audio_features(self, y: np.ndarray, sr: int) -> Dict:
        """提取音频特征"""
        try:
            # 基本特征
            duration = len(y) / sr
            energy = np.mean(librosa.feature.rms(y=y)[0])

            # 频谱特征
            spectral_centroids = librosa.feature.spectral_centroid(y=y, sr=sr)[0]
            spectral_rolloff = librosa.feature.spectral_rolloff(y=y, sr=sr)[0]
            spectral_bandwidth = librosa.feature.spectral_bandwidth(y=y, sr=sr)[0]

            # MFCC特征
            mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=13)
            mfcc_mean = np.mean(mfccs, axis=1)
            mfcc_std = np.std(mfccs, axis=1)

            # 音调特征
            pitches, magnitudes = librosa.piptrack(y=y, sr=sr)
            pitch_mean = np.mean(pitches[magnitudes > 0.1])

            # 节奏特征
            tempo, _ = librosa.beat.beat_track(y=y, sr=sr)

            # 零交叉率
            zero_crossing_rate = np.mean(librosa.feature.zero_crossing_rate(y)[0])

            # 色度特征
            chroma = librosa.feature.chroma_stft(y=y, sr=sr)
            chroma_mean = np.mean(chroma, axis=1)

            return {
                "duration": round(duration, 2),
                "sample_rate": sr,
                "energy": round(float(energy), 4),
                "pitch": round(float(pitch_mean), 1) if not np.isnan(pitch_mean) else 0.0,
                "tempo": round(float(tempo), 1),
                "zero_crossing_rate": round(float(zero_crossing_rate), 4),
                "spectral_centroid_mean": round(float(np.mean(spectral_centroids)), 2),
                "spectral_rolloff_mean": round(float(np.mean(spectral_rolloff)), 2),
                "spectral_bandwidth_mean": round(float(np.mean(spectral_bandwidth)), 2),
                "mfcc_mean": [round(float(x), 4) for x in mfcc_mean],
                "mfcc_std": [round(float(x), 4) for x in mfcc_std],
                "chroma_mean": [round(float(x), 4) for x in chroma_mean]
            }

        except Exception as e:
            logger.error(f"音频特征提取失败: {e}")
            return {
                "duration": 0.0,
                "sample_rate": sr,
                "energy": 0.0,
                "pitch": 0.0,
                "tempo": 0.0,
                "zero_crossing_rate": 0.0,
                "spectral_centroid_mean": 0.0,
                "spectral_rolloff_mean": 0.0,
                "spectral_bandwidth_mean": 0.0,
                "mfcc_mean": [0.0] * 13,
                "mfcc_std": [0.0] * 13,
                "chroma_mean": [0.0] * 12
            }

    def _calculate_intensity(self, confidence: float, emotion: str) -> float:
        """计算情绪强度"""
        # 基础强度
        base_intensity = confidence * 10

        # 根据情绪类型调整
        emotion_intensity = EMOTION_INTENSITY.get(EMODB_EMOTIONS.get(emotion, emotion), 5.0)

        # 综合计算
        intensity = (base_intensity + emotion_intensity) / 2

        return min(max(intensity, 1.0), 10.0)

    def _fallback_analysis(self) -> Dict:
        """降级分析"""
        return {
            "emotion": "平静",
            "confidence": 0.5,
            "intensity": 5.0,
            "all_emotions": {"平静": 0.4, "中性": 0.3, "开心": 0.2, "其他": 0.1},
            "top_emotions": [
                {"emotion": "平静", "score": 0.4},
                {"emotion": "中性", "score": 0.3},
                {"emotion": "开心", "score": 0.2}
            ],
            "audio_features": {
                "duration": 0.0,
                "sample_rate": 16000,
                "energy": 0.0,
                "pitch": 0.0,
                "tempo": 0.0,
                "zero_crossing_rate": 0.0,
                "spectral_centroid_mean": 0.0,
                "spectral_rolloff_mean": 0.0,
                "spectral_bandwidth_mean": 0.0,
                "mfcc_mean": [0.0] * 13,
                "mfcc_std": [0.0] * 13,
                "chroma_mean": [0.0] * 12
            },
            "processing_time_ms": 10,
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }


