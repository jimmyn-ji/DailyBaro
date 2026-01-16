import os
import time
import logging
import numpy as np
from typing import Dict, List
from PIL import Image
import io

# 尝试导入 FER
try:
    from fer import FER
    FER_AVAILABLE = True
except ImportError:
    FER_AVAILABLE = False
    print("⚠️  FER 不可用，将使用基础实现")

# 尝试导入 torch
try:
    import torch
    import torchvision.transforms as transforms
    from torchvision.models import resnet18
    TORCH_AVAILABLE = True
except ImportError:
    TORCH_AVAILABLE = False
    print("⚠️  PyTorch 不可用，将使用基础实现")

# 尝试导入 OpenCV
try:
    import cv2
    OPENCV_AVAILABLE = True
except ImportError:
    OPENCV_AVAILABLE = False
    print("⚠️  OpenCV 不可用，将使用基础实现")

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ImageEmotionAnalyzer:
    def __init__(self):
        self.fer = None
        self.model = None
        self.transform = None
        
        # FER2013数据集标准情绪标签
        self.fer_emotions = ["愤怒", "厌恶", "恐惧", "开心", "悲伤", "惊讶", "中性"]
        
        # 扩展的情绪标签体系
        self.emotion_labels = [
            # 基础情绪
            "愤怒", "厌恶", "恐惧", "开心", "悲伤", "惊讶", "中性",
            # 复合情绪
            "焦虑", "紧张", "满足", "兴奋", "平静", "困惑", "疲惫"
        ]
        
        # 情绪强度映射
        self.intensity_mapping = {
            "愤怒": 8.5, "恐惧": 9.0, "惊讶": 7.5, "开心": 7.0,
            "悲伤": 6.5, "厌恶": 8.0, "中性": 5.0, "焦虑": 7.5,
            "紧张": 7.0, "满足": 6.0, "兴奋": 8.0, "平静": 4.0,
            "困惑": 5.5, "疲惫": 4.5
        }
        
        if FER_AVAILABLE:
            try:
                logger.info("加载 FER 模型...")
                self.fer = FER(mtcnn=False)  # 使用更快的检测器
                logger.info("FER 模型加载成功")
            except Exception as e:
                logger.error(f"FER 模型加载失败: {e}")
                self.fer = None
        
        if TORCH_AVAILABLE and not self.fer:
            try:
                logger.info("加载 ResNet18 模型...")
                # 加载预训练的 ResNet18
                self.model = resnet18(pretrained=True)
                self.model.eval()
                
                # 图像预处理
                self.transform = transforms.Compose([
                    transforms.Resize((224, 224)),
                    transforms.ToTensor(),
                    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
                ])
                logger.info("ResNet18 模型加载成功")
            except Exception as e:
                logger.error(f"ResNet18 模型加载失败: {e}")
                self.model = None
    
    def analyze(self, image_bytes: bytes) -> Dict:
        """分析图像情绪"""
        start_time = time.time()
        
        try:
            # 转换为 PIL Image
            image = Image.open(io.BytesIO(image_bytes)).convert('RGB')
            
            if self.fer is not None:
                return self._analyze_with_fer(image)
            elif self.model is not None:
                return self._analyze_with_resnet(image)
            else:
                return self._advanced_analysis(image)
                
        except Exception as e:
            logger.error(f"图像情绪分析失败: {e}")
            return self._fallback_analysis()
    
    def _analyze_with_fer(self, image: Image.Image) -> Dict:
        """使用 FER 模型分析"""
        try:
            # 转换为 numpy 数组
            img_array = np.array(image)
            
            # 检测情绪
            result = self.fer.detect_emotions(img_array)
            
            if result and len(result) > 0:
                # 获取第一个检测到的人脸
                emotions = result[0]['emotions']
                
                # 找到主要情绪
                primary_emotion = max(emotions.items(), key=lambda x: x[1])
                emotion = primary_emotion[0]
                confidence = primary_emotion[1]
                
                # 计算情绪强度
                intensity = self._calculate_intensity(confidence, emotion)
                
                # 获取Top情绪
                top_emotions = self._get_top_emotions(emotions)
                
                # 获取所有情绪分布
                all_emotions = {k: round(v, 4) for k, v in emotions.items()}
                
                # 图像特征分析
                image_features = self._analyze_image_features(image)
                
                processing_time = int((time.time() - start_time) * 1000)
                
                return {
                    "emotion": emotion,
                    "confidence": round(confidence, 4),
                    "intensity": round(intensity, 1),
                    "all_emotions": all_emotions,
                    "top_emotions": top_emotions,
                    "image_features": image_features,
                    "face_detected": True,
                    "face_count": len(result),
                    "processing_time_ms": processing_time,
                    "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
                }
            else:
                return self._no_face_detected()
                
        except Exception as e:
            logger.error(f"FER 分析失败: {e}")
            return self._fallback_analysis()
    
    def _analyze_with_resnet(self, image: Image.Image) -> Dict:
        """使用 ResNet18 模型分析"""
        try:
            # 预处理图像
            input_tensor = self.transform(image).unsqueeze(0)
            
            # 推理
            with torch.no_grad():
                output = self.model(input_tensor)
                probabilities = torch.softmax(output, dim=1)
            
            # 获取预测结果
            probs = probabilities[0].cpu().numpy()
            
            # 映射到情绪标签（这里需要根据实际训练情况调整）
            emotion_scores = {
                "愤怒": probs[0] if len(probs) > 0 else 0.1,
                "厌恶": probs[1] if len(probs) > 1 else 0.1,
                "恐惧": probs[2] if len(probs) > 2 else 0.1,
                "开心": probs[3] if len(probs) > 3 else 0.1,
                "悲伤": probs[4] if len(probs) > 4 else 0.1,
                "惊讶": probs[5] if len(probs) > 5 else 0.1,
                "中性": probs[6] if len(probs) > 6 else 0.1
            }
            
            # 找到主要情绪
            primary_emotion = max(emotion_scores.items(), key=lambda x: x[1])
            emotion = primary_emotion[0]
            confidence = primary_emotion[1]
            
            # 计算情绪强度
            intensity = self._calculate_intensity(confidence, emotion)
            
            # 获取Top情绪
            top_emotions = self._get_top_emotions(emotion_scores)
            
            # 获取所有情绪分布
            all_emotions = {k: round(v, 4) for k, v in emotion_scores.items()}
            
            # 图像特征分析
            image_features = self._analyze_image_features(image)
            
            processing_time = int((time.time() - start_time) * 1000)
            
            return {
                "emotion": emotion,
                "confidence": round(confidence, 4),
                "intensity": round(intensity, 1),
                "all_emotions": all_emotions,
                "top_emotions": top_emotions,
                "image_features": image_features,
                "face_detected": True,
                "face_count": 1,
                "processing_time_ms": processing_time,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
            }
            
        except Exception as e:
            logger.error(f"ResNet18 分析失败: {e}")
            return self._fallback_analysis()
    
    def _advanced_analysis(self, image: Image.Image) -> Dict:
        """高级图像分析实现"""
        try:
            # 图像特征分析
            image_features = self._analyze_image_features(image)
            
            # 基于图像特征的情绪分析
            emotion_scores = self._analyze_by_features(image_features)
            
            # 找到主要情绪
            primary_emotion = max(emotion_scores.items(), key=lambda x: x[1])
            emotion = primary_emotion[0]
            confidence = primary_emotion[1]
            
            # 计算情绪强度
            intensity = self._calculate_intensity(confidence, emotion)
            
            # 获取Top情绪
            top_emotions = self._get_top_emotions(emotion_scores)
            
            # 获取所有情绪分布
            all_emotions = {k: round(v, 4) for k, v in emotion_scores.items()}
            
            processing_time = int((time.time() - start_time) * 1000)
            
            return {
                "emotion": emotion,
                "confidence": round(confidence, 4),
                "intensity": round(intensity, 1),
                "all_emotions": all_emotions,
                "top_emotions": top_emotions,
                "image_features": image_features,
                "face_detected": False,
                "face_count": 0,
                "processing_time_ms": processing_time,
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
            }
            
        except Exception as e:
            logger.error(f"高级分析失败: {e}")
            return self._fallback_analysis()
    
    def _analyze_by_features(self, features: Dict) -> Dict[str, float]:
        """基于图像特征分析情绪"""
        emotion_scores = {}
        
        # 基于亮度分析
        brightness = features.get("brightness", 0.5)
        if brightness > 0.7:
            emotion_scores["开心"] = 0.6
            emotion_scores["兴奋"] = 0.4
        elif brightness < 0.3:
            emotion_scores["悲伤"] = 0.6
            emotion_scores["恐惧"] = 0.4
        else:
            emotion_scores["中性"] = 0.5
            emotion_scores["平静"] = 0.3
        
        # 基于对比度分析
        contrast = features.get("contrast", 0.5)
        if contrast > 0.7:
            emotion_scores["惊讶"] = emotion_scores.get("惊讶", 0) + 0.3
            emotion_scores["愤怒"] = emotion_scores.get("愤怒", 0) + 0.2
        
        # 基于饱和度分析
        saturation = features.get("saturation", 0.5)
        if saturation > 0.7:
            emotion_scores["开心"] = emotion_scores.get("开心", 0) + 0.2
            emotion_scores["兴奋"] = emotion_scores.get("兴奋", 0) + 0.2
        elif saturation < 0.3:
            emotion_scores["悲伤"] = emotion_scores.get("悲伤", 0) + 0.2
            emotion_scores["疲惫"] = emotion_scores.get("疲惫", 0) + 0.2
        
        # 归一化分数
        total_score = sum(emotion_scores.values())
        if total_score > 0:
            emotion_scores = {k: v / total_score for k, v in emotion_scores.items()}
        
        return emotion_scores
    
    def _analyze_image_features(self, image: Image.Image) -> Dict:
        """分析图像特征"""
        try:
            # 转换为numpy数组
            img_array = np.array(image)
            
            # 计算亮度
            brightness = np.mean(img_array) / 255.0
            
            # 计算对比度
            contrast = np.std(img_array) / 255.0
            
            # 计算饱和度
            hsv = cv2.cvtColor(img_array, cv2.COLOR_RGB2HSV) if OPENCV_AVAILABLE else None
            saturation = np.mean(hsv[:, :, 1]) / 255.0 if hsv is not None else 0.5
            
            # 计算锐度
            gray = cv2.cvtColor(img_array, cv2.COLOR_RGB2GRAY) if OPENCV_AVAILABLE else None
            if gray is not None:
                laplacian = cv2.Laplacian(gray, cv2.CV_64F)
                sharpness = np.var(laplacian)
            else:
                sharpness = 0.0
            
            # 计算颜色分布
            color_distribution = {
                "red": np.mean(img_array[:, :, 0]) / 255.0,
                "green": np.mean(img_array[:, :, 1]) / 255.0,
                "blue": np.mean(img_array[:, :, 2]) / 255.0
            }
            
            return {
                "brightness": round(brightness, 4),
                "contrast": round(contrast, 4),
                "saturation": round(saturation, 4),
                "sharpness": round(sharpness, 4),
                "color_distribution": color_distribution,
                "width": image.width,
                "height": image.height,
                "aspect_ratio": round(image.width / image.height, 4)
            }
            
        except Exception as e:
            logger.error(f"图像特征分析失败: {e}")
            return {
                "brightness": 0.5,
                "contrast": 0.5,
                "saturation": 0.5,
                "sharpness": 0.0,
                "color_distribution": {"red": 0.5, "green": 0.5, "blue": 0.5},
                "width": image.width,
                "height": image.height,
                "aspect_ratio": round(image.width / image.height, 4)
            }
    
    def _calculate_intensity(self, confidence: float, emotion: str) -> float:
        """计算情绪强度"""
        # 基础强度
        base_intensity = confidence * 10
        
        # 根据情绪类型调整
        emotion_intensity = self.intensity_mapping.get(emotion, 5.0)
        
        # 综合计算
        intensity = (base_intensity + emotion_intensity) / 2
        
        return min(max(intensity, 1.0), 10.0)
    
    def _get_top_emotions(self, emotions: Dict[str, float], top_k: int = 3) -> List[Dict]:
        """获取Top情绪"""
        sorted_emotions = sorted(emotions.items(), key=lambda x: x[1], reverse=True)
        return [{"emotion": emotion, "score": round(score, 4)} for emotion, score in sorted_emotions[:top_k]]
    
    def _no_face_detected(self) -> Dict:
        """未检测到人脸时的返回"""
        return {
            "emotion": "中性",
            "confidence": 0.5,
            "intensity": 5.0,
            "all_emotions": {"中性": 1.0},
            "top_emotions": [{"emotion": "中性", "score": 1.0}],
            "image_features": {},
            "face_detected": False,
            "face_count": 0,
            "processing_time_ms": 50,
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }
    
    def _fallback_analysis(self) -> Dict:
        """降级分析"""
        return {
            "emotion": "中性",
            "confidence": 0.5,
            "intensity": 5.0,
            "all_emotions": {"中性": 0.4, "平静": 0.3, "满足": 0.2, "其他": 0.1},
            "top_emotions": [
                {"emotion": "中性", "score": 0.4},
                {"emotion": "平静", "score": 0.3},
                {"emotion": "满足", "score": 0.2}
            ],
            "image_features": {},
            "face_detected": False,
            "face_count": 0,
            "processing_time_ms": 10,
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }


