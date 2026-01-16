#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
情绪数据分析模块
用于分析用户情绪数据，生成统计报告和可视化图表
"""

import os
import json
import logging
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from typing import Dict, List, Optional
import matplotlib.pyplot as plt
import seaborn as sns
from collections import Counter

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei', 'Arial Unicode MS', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class EmotionDataAnalyzer:
    """情绪数据分析器"""
    
    def __init__(self, data_path: str = None):
        """
        初始化分析器
        
        Args:
            data_path: 数据文件路径（JSON格式）
        """
        self.data_path = data_path
        self.df = None
        self.load_data()
    
    def load_data(self):
        """加载数据"""
        if self.data_path and os.path.exists(self.data_path):
            logger.info(f"从 {self.data_path} 加载数据")
            with open(self.data_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            self.df = pd.DataFrame(data)
        else:
            logger.warning("未找到数据文件，使用模拟数据")
            self.df = self.generate_mock_data()
        
        # 转换时间戳
        if 'timestamp' in self.df.columns:
            self.df['timestamp'] = pd.to_datetime(self.df['timestamp'])
            self.df['date'] = self.df['timestamp'].dt.date
            self.df['hour'] = self.df['timestamp'].dt.hour
            self.df['day_of_week'] = self.df['timestamp'].dt.day_name()
    
    def generate_mock_data(self) -> pd.DataFrame:
        """生成模拟数据用于演示"""
        np.random.seed(42)
        n_samples = 1000
        
        emotions = ["开心", "焦虑", "平静", "愤怒", "悲伤", "兴奋", "疲惫", "满足"]
        polarities = ["positive", "negative", "neutral"]
        
        data = []
        base_time = datetime.now() - timedelta(days=30)
        
        for i in range(n_samples):
            emotion = np.random.choice(emotions)
            polarity = "positive" if emotion in ["开心", "兴奋", "满足", "平静"] else \
                      "negative" if emotion in ["焦虑", "愤怒", "悲伤", "疲惫"] else "neutral"
            
            data.append({
                'id': i + 1,
                'user_id': np.random.randint(1, 50),
                'emotion': emotion,
                'polarity': polarity,
                'confidence': np.random.uniform(0.6, 0.95),
                'intensity': np.random.uniform(3.0, 9.0),
                'timestamp': (base_time + timedelta(
                    days=np.random.randint(0, 30),
                    hours=np.random.randint(0, 24)
                )).isoformat(),
                'text_length': np.random.randint(10, 500)
            })
        
        return pd.DataFrame(data)
    
    def emotion_distribution(self) -> Dict:
        """情绪分布统计"""
        if self.df is None or 'emotion' not in self.df.columns:
            return {}
        
        emotion_counts = self.df['emotion'].value_counts().to_dict()
        emotion_percentages = {
            k: (v / len(self.df)) * 100 
            for k, v in emotion_counts.items()
        }
        
        return {
            'total_samples': len(self.df),
            'emotion_counts': emotion_counts,
            'emotion_percentages': emotion_percentages,
            'unique_emotions': len(emotion_counts)
        }
    
    def polarity_distribution(self) -> Dict:
        """情绪极性分布"""
        if self.df is None or 'polarity' not in self.df.columns:
            return {}
        
        polarity_counts = self.df['polarity'].value_counts().to_dict()
        polarity_percentages = {
            k: (v / len(self.df)) * 100 
            for k, v in polarity_counts.items()
        }
        
        return {
            'polarity_counts': polarity_counts,
            'polarity_percentages': polarity_percentages
        }
    
    def time_series_analysis(self) -> Dict:
        """时间序列分析"""
        if self.df is None or 'date' not in self.df.columns:
            return {}
        
        daily_emotions = self.df.groupby(['date', 'emotion']).size().unstack(fill_value=0)
        daily_polarity = self.df.groupby(['date', 'polarity']).size().unstack(fill_value=0)
        
        return {
            'daily_emotions': daily_emotions.to_dict(),
            'daily_polarity': daily_polarity.to_dict(),
            'date_range': {
                'start': str(self.df['date'].min()),
                'end': str(self.df['date'].max())
            }
        }
    
    def hourly_pattern(self) -> Dict:
        """小时模式分析"""
        if self.df is None or 'hour' not in self.df.columns:
            return {}
        
        hourly_emotions = self.df.groupby(['hour', 'emotion']).size().unstack(fill_value=0)
        hourly_polarity = self.df.groupby(['hour', 'polarity']).size().unstack(fill_value=0)
        
        return {
            'hourly_emotions': hourly_emotions.to_dict(),
            'hourly_polarity': hourly_polarity.to_dict()
        }
    
    def user_behavior_analysis(self) -> Dict:
        """用户行为分析"""
        if self.df is None or 'user_id' not in self.df.columns:
            return {}
        
        user_stats = self.df.groupby('user_id').agg({
            'emotion': ['count', lambda x: x.mode()[0] if len(x.mode()) > 0 else None],
            'polarity': lambda x: x.mode()[0] if len(x.mode()) > 0 else None,
            'confidence': 'mean',
            'intensity': 'mean'
        }).round(4)
        
        user_stats.columns = ['total_records', 'most_common_emotion', 'most_common_polarity', 
                             'avg_confidence', 'avg_intensity']
        
        return {
            'total_users': self.df['user_id'].nunique(),
            'user_statistics': user_stats.to_dict('index'),
            'active_users': len(user_stats[user_stats['total_records'] >= 10])
        }
    
    def confidence_analysis(self) -> Dict:
        """置信度分析"""
        if self.df is None or 'confidence' not in self.df.columns:
            return {}
        
        return {
            'mean_confidence': float(self.df['confidence'].mean()),
            'median_confidence': float(self.df['confidence'].median()),
            'std_confidence': float(self.df['confidence'].std()),
            'min_confidence': float(self.df['confidence'].min()),
            'max_confidence': float(self.df['confidence'].max()),
            'high_confidence_ratio': float((self.df['confidence'] >= 0.8).sum() / len(self.df))
        }
    
    def intensity_analysis(self) -> Dict:
        """情绪强度分析"""
        if self.df is None or 'intensity' not in self.df.columns:
            return {}
        
        return {
            'mean_intensity': float(self.df['intensity'].mean()),
            'median_intensity': float(self.df['intensity'].median()),
            'std_intensity': float(self.df['intensity'].std()),
            'intensity_by_emotion': self.df.groupby('emotion')['intensity'].mean().to_dict()
        }
    
    def generate_visualizations(self, output_dir: str = './analysis_output'):
        """生成可视化图表"""
        os.makedirs(output_dir, exist_ok=True)
        
        # 1. 情绪分布饼图
        self.plot_emotion_distribution(output_dir)
        
        # 2. 情绪极性分布
        self.plot_polarity_distribution(output_dir)
        
        # 3. 时间序列趋势
        self.plot_time_series(output_dir)
        
        # 4. 小时模式
        self.plot_hourly_pattern(output_dir)
        
        # 5. 置信度分布
        self.plot_confidence_distribution(output_dir)
        
        # 6. 情绪强度分布
        self.plot_intensity_distribution(output_dir)
        
        logger.info(f"所有可视化图表已保存到: {output_dir}")
    
    def plot_emotion_distribution(self, output_dir: str):
        """绘制情绪分布图"""
        emotion_counts = self.df['emotion'].value_counts()
        
        plt.figure(figsize=(12, 6))
        emotion_counts.plot(kind='bar', color='steelblue')
        plt.title('情绪分布统计', fontsize=16, fontweight='bold')
        plt.xlabel('情绪类型', fontsize=12)
        plt.ylabel('出现次数', fontsize=12)
        plt.xticks(rotation=45, ha='right')
        plt.grid(axis='y', alpha=0.3)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'emotion_distribution.png'), dpi=300)
        plt.close()
    
    def plot_polarity_distribution(self, output_dir: str):
        """绘制情绪极性分布"""
        polarity_counts = self.df['polarity'].value_counts()
        colors = {'positive': '#4CAF50', 'negative': '#F44336', 'neutral': '#FF9800'}
        
        plt.figure(figsize=(8, 8))
        plt.pie(
            polarity_counts.values,
            labels=[f'{k} ({v})' for k, v in polarity_counts.items()],
            autopct='%1.1f%%',
            colors=[colors.get(k, '#999999') for k in polarity_counts.index],
            startangle=90
        )
        plt.title('情绪极性分布', fontsize=16, fontweight='bold')
        plt.savefig(os.path.join(output_dir, 'polarity_distribution.png'), dpi=300)
        plt.close()
    
    def plot_time_series(self, output_dir: str):
        """绘制时间序列趋势"""
        daily_polarity = self.df.groupby(['date', 'polarity']).size().unstack(fill_value=0)
        
        plt.figure(figsize=(14, 6))
        for polarity in daily_polarity.columns:
            plt.plot(daily_polarity.index, daily_polarity[polarity], 
                    label=polarity, marker='o', linewidth=2)
        plt.title('情绪极性时间趋势', fontsize=16, fontweight='bold')
        plt.xlabel('日期', fontsize=12)
        plt.ylabel('数量', fontsize=12)
        plt.legend()
        plt.grid(alpha=0.3)
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'time_series.png'), dpi=300)
        plt.close()
    
    def plot_hourly_pattern(self, output_dir: str):
        """绘制小时模式"""
        hourly_polarity = self.df.groupby(['hour', 'polarity']).size().unstack(fill_value=0)
        
        plt.figure(figsize=(12, 6))
        hourly_polarity.plot(kind='bar', stacked=True, colormap='Set3')
        plt.title('情绪极性小时分布模式', fontsize=16, fontweight='bold')
        plt.xlabel('小时', fontsize=12)
        plt.ylabel('数量', fontsize=12)
        plt.legend(title='情绪极性')
        plt.xticks(rotation=0)
        plt.grid(axis='y', alpha=0.3)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'hourly_pattern.png'), dpi=300)
        plt.close()
    
    def plot_confidence_distribution(self, output_dir: str):
        """绘制置信度分布"""
        plt.figure(figsize=(10, 6))
        plt.hist(self.df['confidence'], bins=20, color='skyblue', edgecolor='black', alpha=0.7)
        plt.axvline(self.df['confidence'].mean(), color='red', linestyle='--', 
                   label=f'平均值: {self.df["confidence"].mean():.3f}')
        plt.title('置信度分布直方图', fontsize=16, fontweight='bold')
        plt.xlabel('置信度', fontsize=12)
        plt.ylabel('频数', fontsize=12)
        plt.legend()
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'confidence_distribution.png'), dpi=300)
        plt.close()
    
    def plot_intensity_distribution(self, output_dir: str):
        """绘制情绪强度分布"""
        intensity_by_emotion = self.df.groupby('emotion')['intensity'].mean().sort_values()
        
        plt.figure(figsize=(12, 6))
        intensity_by_emotion.plot(kind='barh', color='coral')
        plt.title('各情绪平均强度', fontsize=16, fontweight='bold')
        plt.xlabel('平均强度', fontsize=12)
        plt.ylabel('情绪类型', fontsize=12)
        plt.grid(axis='x', alpha=0.3)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, 'intensity_distribution.png'), dpi=300)
        plt.close()
    
    def generate_report(self, output_path: str = './analysis_output/analysis_report.json'):
        """生成分析报告"""
        report = {
            'analysis_time': datetime.now().isoformat(),
            'data_summary': {
                'total_samples': len(self.df),
                'date_range': {
                    'start': str(self.df['date'].min()) if 'date' in self.df.columns else None,
                    'end': str(self.df['date'].max()) if 'date' in self.df.columns else None
                }
            },
            'emotion_distribution': self.emotion_distribution(),
            'polarity_distribution': self.polarity_distribution(),
            'time_series_analysis': self.time_series_analysis(),
            'hourly_pattern': self.hourly_pattern(),
            'user_behavior': self.user_behavior_analysis(),
            'confidence_analysis': self.confidence_analysis(),
            'intensity_analysis': self.intensity_analysis()
        }
        
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        logger.info(f"分析报告已保存到: {output_path}")
        return report

if __name__ == '__main__':
    import argparse
    
    parser = argparse.ArgumentParser(description='情绪数据分析')
    parser.add_argument('--data', type=str, default=None, help='数据文件路径')
    parser.add_argument('--output', type=str, default='./analysis_output', help='输出目录')
    
    args = parser.parse_args()
    
    analyzer = EmotionDataAnalyzer(data_path=args.data)
    analyzer.generate_visualizations(output_dir=args.output)
    analyzer.generate_report(output_path=os.path.join(args.output, 'analysis_report.json'))
    
    print("\n数据分析完成！")
    print(f"报告和图表已保存到: {args.output}")
