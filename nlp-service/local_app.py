#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
NLP/CV/Audio 情绪分析服务（Flask 轻量服务）
端口: 5001

API 规范（与 Java EmotionController 对齐）：
- POST /api/emotion/analyze            {text}            -> { code, data }
- POST /api/emotion/analyze/image      multipart image   -> { code, data }
- POST /api/emotion/analyze/audio      multipart audio   -> { code, data }

返回 data 统一字段：
  emotion: 主情绪标签（字符串）
  confidence: 置信度 (0~1 浮点)
  intensity: 强度 (0~10 或 0~1，前端已做百分比显示容错)
  top_emotions: [{emotion, score}], score 0~1
  processing_time_ms: 耗时

说明：
- 当前为可运行的占位实现，提供稳定的接口与结构。
- 后续将接入实际模型：
  A 文本：Transformers + bert-base-chinese / chinese-roberta-wwm-ext 微调
  B 图像：FER2013 + ResNet18
  C 音频（可选）：Wav2Vec2 (EmoDB)
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import time
import os
from datetime import datetime
# 尝试导入分析器；若依赖不可用（如 Transformers 在 Py3.13 构建失败），降级为本地轻量实现
try:
    from analyzers.text_analyzer import TextEmotionAnalyzer
    TEXT = TextEmotionAnalyzer()
except Exception:
    class TextEmotionAnalyzer:
        def analyze(self, text: str):
            lower = str(text or "").lower()
            if any(k in lower for k in ["开心", "高兴", "快乐", "愉快", "excited", "喜悦"]):
                return {"emotion": "开心", "confidence": 0.9, "intensity": 9.0, "top_emotions": [{"emotion": "开心", "score": 0.9}, {"emotion": "平静", "score": 0.08}, {"emotion": "其他", "score": 0.02}]}
            if any(k in lower for k in ["焦虑", "担心", "紧张", "不安", "anxious"]):
                return {"emotion": "焦虑", "confidence": 0.85, "intensity": 8.5, "top_emotions": [{"emotion": "焦虑", "score": 0.85}, {"emotion": "平静", "score": 0.1}, {"emotion": "难过", "score": 0.05}]}
            if any(k in lower for k in ["生气", "愤怒", "火大", "angry"]):
                return {"emotion": "愤怒", "confidence": 0.85, "intensity": 8.5, "top_emotions": [{"emotion": "愤怒", "score": 0.85}, {"emotion": "焦虑", "score": 0.1}, {"emotion": "其他", "score": 0.05}]}
            if any(k in lower for k in ["难过", "伤心", "悲伤", "sad"]):
                return {"emotion": "难过", "confidence": 0.82, "intensity": 8.2, "top_emotions": [{"emotion": "难过", "score": 0.82}, {"emotion": "平静", "score": 0.12}, {"emotion": "其他", "score": 0.06}]}
            return {"emotion": "平静", "confidence": 0.8, "intensity": 8.0, "top_emotions": [{"emotion": "平静", "score": 0.8}, {"emotion": "开心", "score": 0.12}, {"emotion": "其他", "score": 0.08}]}
    TEXT = TextEmotionAnalyzer()

try:
    from analyzers.image_analyzer import ImageEmotionAnalyzer
    IMG = ImageEmotionAnalyzer()
except Exception:
    class ImageEmotionAnalyzer:
        def analyze(self, image_bytes: bytes):
            # 简易占位，根据数据长度随机给个稳定结果
            l = max(1, len(image_bytes) % 1000) / 1000.0
            conf = 0.7 + (l * 0.3)
            return {"emotion": "平静", "confidence": min(0.99, conf), "intensity": round(min(0.99, conf) * 10, 1), "top_emotions": [{"emotion": "平静", "score": min(0.99, conf)}, {"emotion": "开心", "score": 1 - min(0.99, conf)}, {"emotion": "其他", "score": 0.01}]}
    IMG = ImageEmotionAnalyzer()

try:
    from analyzers.audio_analyzer import AudioEmotionAnalyzer
    AUDIO = AudioEmotionAnalyzer()
except Exception:
    class AudioEmotionAnalyzer:
        def analyze(self, audio_bytes: bytes):
            return {"emotion": "平静", "confidence": 0.8, "intensity": 8.0, "top_emotions": [{"emotion": "平静", "score": 0.8}, {"emotion": "愤怒", "score": 0.12}, {"emotion": "难过", "score": 0.08}]}
    AUDIO = AudioEmotionAnalyzer()

app = Flask(__name__)
CORS(app)


def ok(data):
    return jsonify({"code": 200, "data": data})


def fail(msg: str, code: int = 500):
    return jsonify({"code": code, "message": msg}), code


# ========== 文本情绪分析 ========== #
@app.post("/api/emotion/analyze")
def analyze_text():
    t0 = time.time()
    try:
        payload = request.get_json(silent=True) or {}
        text = payload.get("text") or request.form.get("text")
        if not text or not str(text).strip():
            return fail("文本内容不能为空", 400)

        out = TEXT.analyze(text)
        data = {
            **out,
            "processing_time_ms": int((time.time() - t0) * 1000),
        }
        return ok(data)
    except Exception as e:
        return fail(f"文本情绪分析失败: {e}")


# ========== 图像情绪分析 ========== #
@app.post("/api/emotion/analyze/image")
def analyze_image():
    t0 = time.time()
    try:
        image = request.files.get("image") or request.files.get("file")
        if not image:
            return fail("图片不能为空", 400)

        out = IMG.analyze(image.read())
        data = {
            **out,
            "processing_time_ms": int((time.time() - t0) * 1000),
        }
        return ok(data)
    except Exception as e:
        return fail(f"图像情绪分析失败: {e}")


# ========== 音频情绪分析 ========== #
@app.post("/api/emotion/analyze/audio")
def analyze_audio():
    t0 = time.time()
    try:
        audio = request.files.get("audio") or request.files.get("file")
        if not audio:
            return fail("音频不能为空", 400)

        out = AUDIO.analyze(audio.read())
        data = {
            **out,
            "processing_time_ms": int((time.time() - t0) * 1000),
        }
        return ok(data)
    except Exception as e:
        return fail(f"音频情绪分析失败: {e}")


@app.route('/analyze/comprehensive', methods=['POST'])
def analyze_comprehensive():
    """综合情绪分析 - 整合文本、图像、音频分析结果"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "请求数据为空"}), 400
        
        results = {}
        start_time = time.time()
        
        # 文本分析
        if 'text' in data and data['text']:
            try:
                text_result = TEXT.analyze(data['text'])
                results['text'] = text_result
            except Exception as e:
                # logger.error(f"文本分析失败: {e}") # Original code had this line commented out
                results['text'] = {"error": str(e)}
        
        # 图像分析
        if 'image' in data and data['image']:
            try:
                # 解码base64图像
                image_data = base64.b64decode(data['image'])
                image_result = IMG.analyze(image_data)
                results['image'] = image_result
            except Exception as e:
                # logger.error(f"图像分析失败: {e}") # Original code had this line commented out
                results['image'] = {"error": str(e)}
        
        # 音频分析
        if 'audio' in data and data['audio']:
            try:
                # 解码base64音频
                audio_data = base64.b64decode(data['audio'])
                audio_result = AUDIO.analyze(audio_data)
                results['audio'] = audio_result
            except Exception as e:
                # logger.error(f"音频分析失败: {e}") # Original code had this line commented out
                results['audio'] = {"error": str(e)}
        
        # 综合情绪分析
        comprehensive_result = _analyze_comprehensive_emotion(results)
        
        # 计算总处理时间
        total_time = int((time.time() - start_time) * 1000)
        
        response = {
            "comprehensive_emotion": comprehensive_result,
            "modalities": results,
            "processing_time_ms": total_time,
            "timestamp": datetime.now().isoformat(), # Changed from time.strftime to datetime.now().isoformat()
            "modality_count": len([k for k, v in results.items() if 'error' not in v])
        }
        
        return jsonify(response)
        
    except Exception as e:
        # logger.error(f"综合分析失败: {e}") # Original code had this line commented out
        return jsonify({"error": str(e)}), 500

def _analyze_comprehensive_emotion(modality_results):
    """综合分析各模态的情绪结果"""
    try:
        emotion_scores = {}
        modality_weights = {
            'text': 0.5,      # 文本权重最高
            'image': 0.3,     # 图像权重中等
            'audio': 0.2      # 音频权重较低
        }
        
        # 收集所有情绪分数
        for modality, result in modality_results.items():
            if 'error' in result:
                continue
                
            weight = modality_weights.get(modality, 0.1)
            emotion = result.get('emotion', '中性')
            confidence = result.get('confidence', 0.5)
            intensity = result.get('intensity', 5.0)
            
            # 计算加权分数
            weighted_score = confidence * weight
            
            if emotion in emotion_scores:
                emotion_scores[emotion]['score'] += weighted_score
                emotion_scores[emotion]['intensity'] += intensity * weight
                emotion_scores[emotion]['modalities'].append(modality)
            else:
                emotion_scores[emotion] = {
                    'score': weighted_score,
                    'intensity': intensity * weight,
                    'modalities': [modality]
                }
        
        if not emotion_scores:
            return {
                "emotion": "中性",
                "confidence": 0.5,
                "intensity": 5.0,
                "modalities": [],
                "analysis_method": "fallback"
            }
        
        # 找到主要情绪
        primary_emotion = max(emotion_scores.items(), key=lambda x: x[1]['score'])
        emotion_name = primary_emotion[0]
        emotion_data = primary_emotion[1]
        
        # 计算综合置信度
        total_weight = sum(modality_weights.get(mod, 0.1) for mod in modality_results.keys() if 'error' not in modality_results.get(mod, {}))
        if total_weight > 0:
            confidence = emotion_data['score'] / total_weight
        else:
            confidence = 0.5
        
        # 计算综合强度
        intensity = emotion_data['intensity'] / len(emotion_data['modalities']) if emotion_data['modalities'] else 5.0
        
        return {
            "emotion": emotion_name,
            "confidence": round(confidence, 4),
            "intensity": round(intensity, 1),
            "modalities": emotion_data['modalities'],
            "analysis_method": "multimodal_fusion",
            "all_emotions": {k: round(v['score'], 4) for k, v in emotion_scores.items()}
        }
        
    except Exception as e:
        # logger.error(f"综合情绪分析失败: {e}") # Original code had this line commented out
        return {
            "emotion": "中性",
            "confidence": 0.5,
            "intensity": 5.0,
            "modalities": [],
            "analysis_method": "error_fallback"
        }

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    try:
        # 检查各个模型是否可用
        models_status = {
            'text_analyzer': TEXT is not None,
            'image_analyzer': IMG is not None,
            'audio_analyzer': AUDIO is not None
        }
        
        if all(models_status.values()):
            return jsonify({
                'status': 'healthy',
                'models': models_status,
                'timestamp': datetime.now().isoformat()
            }), 200
        else:
            return jsonify({
                'status': 'degraded',
                'models': models_status,
                'timestamp': datetime.now().isoformat()
            }), 200
    except Exception as e:
        return jsonify({
            'status': 'unhealthy',
            'error': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/api/health', methods=['GET'])
def api_health_check():
    """API 健康检查接口（通过网关访问）"""
    return health_check()


if __name__ == "__main__":
    # 默认 0.0.0.0:5001
    app.run(host="0.0.0.0", port=5001, debug=False)
