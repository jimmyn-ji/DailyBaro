#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from flask import Flask, request, jsonify
from flask_cors import CORS
import jieba
import snownlp
import numpy as np
from datetime import datetime
import logging

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

class SimpleEmotionAnalyzer:
    def __init__(self):
        self.positive_words = {
            '开心', '快乐', '高兴', '兴奋', '激动', '满意', '满足', '幸福', '愉快', '欢乐',
            '喜欢', '爱', '好', '棒', '优秀', '完美', '成功', '胜利', '希望', '期待',
            '感谢', '谢谢', '感恩', '温暖', '美好', '精彩', '有趣', '好玩', '舒服', '放松'
        }
        
        self.negative_words = {
            '难过', '伤心', '痛苦', '悲伤', '失望', '沮丧', '焦虑', '担心', '害怕', '恐惧',
            '愤怒', '生气', '恼火', '烦躁', '郁闷', '压抑', '孤独', '寂寞', '无聊', '疲惫',
            '讨厌', '恨', '坏', '糟糕', '失败', '绝望', '痛苦', '难受', '不舒服', '紧张'
        }
        
        self.neutral_words = {
            '一般', '普通', '正常', '平常', '日常', '工作', '学习', '生活', '时间', '天气',
            '吃饭', '睡觉', '走路', '说话', '思考', '计划', '安排', '准备', '完成', '开始'
        }
    
    def analyze_emotion(self, text):
        """分析文本情绪"""
        if not text or len(text.strip()) == 0:
            return {
                'emotion': 'neutral',
                'confidence': 0.5,
                'score': 0.0,
                'details': '文本为空'
            }
        
        # 使用jieba分词
        words = list(jieba.cut(text))
        
        # 计算情感分数
        positive_count = sum(1 for word in words if word in self.positive_words)
        negative_count = sum(1 for word in words if word in self.negative_words)
        neutral_count = sum(1 for word in words if word in self.neutral_words)
        
        # 使用snownlp进行情感分析
        try:
            snownlp_score = snownlp.SnowNLP(text).sentiments
        except:
            snownlp_score = 0.5
        
        # 综合评分
        total_words = len(words)
        if total_words == 0:
            score = 0.5
        else:
            # 结合词典和snownlp的结果
            dict_score = (positive_count - negative_count) / total_words
            score = (dict_score + snownlp_score) / 2
        
        # 确定情绪类型
        if score > 0.6:
            emotion = 'positive'
            confidence = min(0.9, score)
        elif score < 0.4:
            emotion = 'negative'
            confidence = min(0.9, 1 - score)
        else:
            emotion = 'neutral'
            confidence = 0.5
        
        return {
            'emotion': emotion,
            'confidence': round(confidence, 3),
            'score': round(score, 3),
            'details': {
                'positive_words': positive_count,
                'negative_words': negative_count,
                'neutral_words': neutral_count,
                'total_words': total_words,
                'snownlp_score': round(snownlp_score, 3)
            }
        }

# 初始化分析器
analyzer = SimpleEmotionAnalyzer()

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'healthy',
        'service': 'nlp-emotion-analyzer',
        'timestamp': datetime.now().isoformat(),
        'version': '1.0.0'
    })

@app.route('/api/emotion/analyze', methods=['POST'])
def analyze_emotion_api():
    """分析文本情绪 - API 接口（兼容 diary-service 调用）"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                'code': 400,
                'message': '请求体不能为空',
                'data': None
            }), 400
        
        # 支持 text 或 content 字段
        text = data.get('text') or data.get('content')
        if not text:
            return jsonify({
                'code': 400,
                'message': '缺少text或content参数',
                'data': None
            }), 400
        
        result = analyzer.analyze_emotion(text)
        
        # 转换为中文情绪标签
        emotion_map = {
            'positive': '积极',
            'negative': '消极',
            'neutral': '中性'
        }
        emotion_cn = emotion_map.get(result.get('emotion', 'neutral'), '中性')
        
        return jsonify({
            'code': 200,
            'message': 'success',
            'data': {
                'emotion': emotion_cn,
                'confidence': result.get('confidence', 0.5),
                'score': result.get('score', 0.5),
                'details': result.get('details', {})
            }
        })
        
    except Exception as e:
        logger.error(f"分析文本时出错: {str(e)}")
        return jsonify({
            'code': 500,
            'message': f'分析失败: {str(e)}',
            'data': None
        }), 500

@app.route('/api/emotion/analyze-diary', methods=['POST'])
def analyze_diary_emotion():
    """分析日记情绪 - 兼容 diary-service 调用"""
    # 复用 analyze_emotion_api 的逻辑
    return analyze_emotion_api()

@app.route('/api/emotion/suggest-tags', methods=['POST'])
def suggest_emotion_tags():
    """建议情绪标签 - 兼容 diary-service 调用"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                'code': 400,
                'message': '请求体不能为空',
                'data': None
            }), 400
        
        text = data.get('text') or data.get('content')
        if not text:
            return jsonify({
                'code': 400,
                'message': '缺少text或content参数',
                'data': None
            }), 400
        
        result = analyzer.analyze_emotion(text)
        emotion_map = {
            'positive': '积极',
            'negative': '消极',
            'neutral': '中性'
        }
        emotion_cn = emotion_map.get(result.get('emotion', 'neutral'), '中性')
        
        # 返回建议的标签列表
        return jsonify({
            'code': 200,
            'message': 'success',
            'data': {
                'suggested_tags': [emotion_cn],
                'confidence': result.get('confidence', 0.5),
                'score': result.get('score', 0.5)
            }
        })
        
    except Exception as e:
        logger.error(f"建议标签时出错: {str(e)}")
        return jsonify({
            'code': 500,
            'message': f'建议标签失败: {str(e)}',
            'data': None
        }), 500

@app.route('/analyze/text', methods=['POST'])
def analyze_text():
    """分析文本情绪"""
    try:
        data = request.get_json()
        if not data or 'text' not in data:
            return jsonify({
                'error': '缺少text参数',
                'status': 'error'
            }), 400
        
        text = data['text']
        result = analyzer.analyze_emotion(text)
        
        return jsonify({
            'status': 'success',
            'data': result,
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        logger.error(f"分析文本时出错: {str(e)}")
        return jsonify({
            'error': f'分析失败: {str(e)}',
            'status': 'error'
        }), 500

@app.route('/analyze/batch', methods=['POST'])
def analyze_batch():
    """批量分析文本情绪"""
    try:
        data = request.get_json()
        if not data or 'texts' not in data:
            return jsonify({
                'error': '缺少texts参数',
                'status': 'error'
            }), 400
        
        texts = data['texts']
        if not isinstance(texts, list):
            return jsonify({
                'error': 'texts必须是数组',
                'status': 'error'
            }), 400
        
        results = []
        for i, text in enumerate(texts):
            result = analyzer.analyze_emotion(text)
            results.append({
                'index': i,
                'text': text,
                'analysis': result
            })
        
        return jsonify({
            'status': 'success',
            'data': results,
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        logger.error(f"批量分析时出错: {str(e)}")
        return jsonify({
            'error': f'批量分析失败: {str(e)}',
            'status': 'error'
        }), 500

@app.route('/stats', methods=['GET'])
def get_stats():
    """获取服务统计信息"""
    return jsonify({
        'status': 'success',
        'data': {
            'service': 'nlp-emotion-analyzer',
            'version': '1.0.0',
            'features': ['text_emotion_analysis', 'batch_analysis'],
            'timestamp': datetime.now().isoformat()
        }
    })

if __name__ == '__main__':
    logger.info("启动NLP情绪分析服务...")
    app.run(host='0.0.0.0', port=5001, debug=False)
