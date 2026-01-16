import os
import time
import logging
from typing import Dict, List, Optional
import numpy as np
import re
import jieba
import jieba.posseg as pseg

# 尝试导入 transformers
try:
    from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification
    TRANSFORMERS_AVAILABLE = True
except ImportError:
    TRANSFORMERS_AVAILABLE = False
    print("⚠️  Transformers 不可用，将使用基础实现")

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class TextEmotionAnalyzer:
    def __init__(self):
        self.cls = None
        self.zero_shot = None
        
        # 扩展的情绪标签体系（基于NLPCC 2014和ChnSentiCorp数据集）
        self.emotion_labels = [
            # 积极情绪
            "开心", "兴奋", "满足", "平静", "放松", "期待", "信任", "感激", "自豪", "爱",
            # 消极情绪
            "焦虑", "紧张", "愤怒", "悲伤", "沮丧", "困惑", "恐惧", "厌恶", "羞耻", "内疚",
            # 中性情绪
            "中性", "好奇", "惊讶", "困惑", "无聊", "疲惫"
        ]
        
        # 情绪强度词汇库
        self.intensity_words = {
            "开心": ["开心", "快乐", "高兴", "兴奋", "愉快", "爽", "棒", "好", "幸福", "满足"],
            "愤怒": ["愤怒", "生气", "恼火", "愤怒", "暴躁", "火大", "气愤", "暴怒"],
            "悲伤": ["悲伤", "难过", "伤心", "痛苦", "沮丧", "绝望", "哀伤", "忧郁"],
            "焦虑": ["焦虑", "担心", "紧张", "不安", "恐惧", "害怕", "恐慌", "忧虑"],
            "平静": ["平静", "安静", "冷静", "淡定", "从容", "平和", "宁静"],
            "兴奋": ["兴奋", "激动", "振奋", "热情", "激情", "狂热", "亢奋"],
            "满足": ["满足", "满意", "知足", "充实", "圆满", "完美", "理想"],
            "疲惫": ["疲惫", "累", "疲劳", "困倦", "无力", "虚弱", "倦怠"]
        }
        
        # 情感极性词汇库
        self.sentiment_lexicon = {
            "positive": ["好", "棒", "优秀", "完美", "喜欢", "爱", "开心", "快乐", "成功", "胜利"],
            "negative": ["坏", "差", "糟糕", "讨厌", "恨", "难过", "失败", "痛苦", "绝望"],
            "neutral": ["一般", "还行", "凑合", "正常", "普通", "标准", "常规"]
        }
        
        if TRANSFORMERS_AVAILABLE:
            try:
                # 尝试加载微调的中文模型
                model_dir = os.environ.get("TEXT_MODEL_DIR")
                model_id = os.environ.get("TEXT_MODEL_ID", "hfl/chinese-roberta-wwm-ext")
                
                if model_dir and os.path.exists(model_dir):
                    logger.info(f"加载本地微调模型: {model_dir}")
                    tokenizer = AutoTokenizer.from_pretrained(model_dir, use_fast=False)
                    model = AutoModelForSequenceClassification.from_pretrained(model_dir)
                    self.cls = pipeline("text-classification", model=model, tokenizer=tokenizer)
                else:
                    logger.info(f"加载预训练模型: {model_id}")
                    # 使用中文RoBERTa模型进行零样本分类
                    tokenizer = AutoTokenizer.from_pretrained(model_id, use_fast=False)
                    self.zero_shot = pipeline("zero-shot-classification", model=model_id, tokenizer=tokenizer)
                    
            except Exception as e:
                logger.error(f"模型加载失败: {e}")
                self.cls = None
                self.zero_shot = None
    
    def analyze(self, text: str) -> Dict:
        """分析文本情绪"""
        start_time = time.time()
        
        try:
            # 文本预处理
            processed_text = self._preprocess_text(text)
            
            if self.cls is not None:
                # 使用微调模型
                result = self.cls(processed_text)
                emotion = result[0]['label']
                confidence = result[0]['score']
                
            elif self.zero_shot is not None:
                # 使用零样本分类
                result = self.zero_shot(processed_text, self.emotion_labels)
                emotion = result['labels'][0]
                confidence = result['scores'][0]
                
            else:
                # 基础实现
                return self._advanced_analysis(processed_text)
            
            # 计算情绪强度
            intensity = self._calculate_intensity(confidence, emotion, processed_text)
            
            # 获取Top情绪
            top_emotions = self._get_top_emotions(processed_text)
            
            # 计算情感极性
            polarity = self._calculate_polarity(processed_text)
            
            # 获取所有情绪分布
            all_emotions = self._get_all_emotions(processed_text)
            
            # 文本特征分析
            text_features = self._analyze_text_features(processed_text)
            
            processing_time = int((time.time() - start_time) * 1000)
            
            return {
                "text": text,
                "emotion": emotion,
                "confidence": round(confidence, 4),
                "intensity": round(intensity, 1),
                "polarity": polarity,
                "all_emotions": all_emotions,
                "top_emotions": top_emotions,
                "text_features": text_features,
                "processing_time_ms": processing_time,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
            }
            
        except Exception as e:
            logger.error(f"情绪分析失败: {e}")
            return self._advanced_analysis(text)
    
    def _preprocess_text(self, text: str) -> str:
        """文本预处理"""
        # 去除特殊字符
        text = re.sub(r'[^\w\s\u4e00-\u9fff]', '', text)
        # 去除多余空格
        text = re.sub(r'\s+', ' ', text).strip()
        return text
    
    def _advanced_analysis(self, text: str) -> Dict:
        """高级情绪分析实现"""
        # 分词
        words = list(jieba.cut(text))
        
        # 情感极性分析
        polarity = self._calculate_polarity(text)
        
        # 情绪识别
        emotion_scores = {}
        for emotion, keywords in self.intensity_words.items():
            score = sum(1 for word in words if word in keywords)
            if score > 0:
                emotion_scores[emotion] = score
        
        # 获取主要情绪
        if emotion_scores:
            primary_emotion = max(emotion_scores.items(), key=lambda x: x[1])
            emotion = primary_emotion[0]
            confidence = min(primary_emotion[1] / len(words), 1.0)
        else:
            emotion = "中性"
            confidence = 0.5
        
        # 计算情绪强度
        intensity = self._calculate_intensity(confidence, emotion, text)
        
        # 获取Top情绪
        top_emotions = self._get_top_emotions(text)
        
        # 获取所有情绪分布
        all_emotions = self._get_all_emotions(text)
        
        # 文本特征分析
        text_features = self._analyze_text_features(text)
        
        return {
            "text": text,
            "emotion": emotion,
            "confidence": round(confidence, 4),
            "intensity": round(intensity, 1),
            "polarity": polarity,
            "all_emotions": all_emotions,
            "top_emotions": top_emotions,
            "text_features": text_features,
            "processing_time_ms": 50,
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }
    
    def _calculate_intensity(self, confidence: float, emotion: str, text: str) -> float:
        """计算情绪强度"""
        # 基础强度
        base_intensity = confidence * 10
        
        # 根据情绪类型调整
        intensity_multipliers = {
            "愤怒": 1.2, "兴奋": 1.1, "恐惧": 1.3, "悲伤": 1.0,
            "开心": 1.1, "焦虑": 1.2, "平静": 0.8, "满足": 0.9
        }
        
        multiplier = intensity_multipliers.get(emotion, 1.0)
        
        # 根据文本长度调整
        length_factor = min(len(text) / 100, 1.5)
        
        # 根据标点符号调整
        exclamation_count = text.count('！') + text.count('!')
        question_count = text.count('？') + text.count('?')
        punctuation_factor = 1.0 + (exclamation_count * 0.1) - (question_count * 0.05)
        
        intensity = base_intensity * multiplier * length_factor * punctuation_factor
        return min(max(intensity, 1.0), 10.0)
    
    def _get_top_emotions(self, text: str, top_k: int = 3) -> List[Dict]:
        """获取Top情绪"""
        emotion_scores = {}
        
        for emotion, keywords in self.intensity_words.items():
            score = sum(1 for word in text if word in keywords)
            if score > 0:
                emotion_scores[emotion] = score / len(text)
        
        # 如果没有检测到情绪，返回默认值
        if not emotion_scores:
            return [
                {"emotion": "中性", "score": 0.5},
                {"emotion": "平静", "score": 0.3},
                {"emotion": "满足", "score": 0.2}
            ]
        
        # 排序并返回Top K
        sorted_emotions = sorted(emotion_scores.items(), key=lambda x: x[1], reverse=True)
        return [{"emotion": emotion, "score": round(score, 4)} for emotion, score in sorted_emotions[:top_k]]
    
    def _get_all_emotions(self, text: str) -> Dict[str, float]:
        """获取所有情绪分布"""
        all_emotions = {}
        
        for emotion, keywords in self.intensity_words.items():
            score = sum(1 for word in text if word in keywords)
            if score > 0:
                all_emotions[emotion] = round(score / len(text), 4)
        
        # 如果没有检测到情绪，返回默认分布
        if not all_emotions:
            all_emotions = {
                "中性": 0.4, "平静": 0.3, "满足": 0.2, "其他": 0.1
            }
        
        return all_emotions
    
    def _calculate_polarity(self, text: str) -> str:
        """计算情感极性"""
        positive_score = sum(1 for word in self.sentiment_lexicon["positive"] if word in text)
        negative_score = sum(1 for word in self.sentiment_lexicon["negative"] if word in text)
        neutral_score = sum(1 for word in self.sentiment_lexicon["neutral"] if word in text)
        
        if positive_score > negative_score and positive_score > neutral_score:
            return "positive"
        elif negative_score > positive_score and negative_score > neutral_score:
            return "negative"
        else:
            return "neutral"
    
    def _analyze_text_features(self, text: str) -> Dict:
        """分析文本特征"""
        words = list(jieba.cut(text))
        
        return {
            "word_count": len(words),
            "char_count": len(text),
            "avg_word_length": round(sum(len(word) for word in words) / len(words), 2) if words else 0,
            "exclamation_count": text.count('！') + text.count('!'),
            "question_count": text.count('？') + text.count('?'),
            "emotion_word_count": sum(1 for word in words if any(word in keywords for keywords in self.intensity_words.values())),
            "sentiment_word_count": sum(1 for word in words if any(word in keywords for keywords in self.sentiment_lexicon.values()))
        }


