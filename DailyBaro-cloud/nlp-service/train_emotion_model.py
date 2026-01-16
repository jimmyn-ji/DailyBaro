#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
情绪分类模型训练脚本
用于训练和评估中文情绪分类模型
支持多种预训练模型：BERT、RoBERTa、DistilBERT等
"""

import os
import json
import logging
import numpy as np
from datetime import datetime
from typing import Dict, List, Tuple
import torch
from torch.utils.data import Dataset, DataLoader
from transformers import (
    AutoTokenizer, 
    AutoModelForSequenceClassification,
    TrainingArguments,
    Trainer,
    EarlyStoppingCallback
)
from sklearn.metrics import accuracy_score, precision_recall_fscore_support, confusion_matrix
import matplotlib.pyplot as plt
import seaborn as sns

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 情绪标签映射
EMOTION_LABELS = [
    "开心", "兴奋", "满足", "平静", "放松", "期待", "信任", "感激", "自豪", "爱",
    "焦虑", "紧张", "愤怒", "悲伤", "沮丧", "困惑", "恐惧", "厌恶", "羞耻", "内疚",
    "中性", "好奇", "惊讶", "无聊", "疲惫"
]

# 简化的情绪标签（用于训练）
SIMPLIFIED_EMOTIONS = {
    "positive": ["开心", "兴奋", "满足", "平静", "放松", "期待", "信任", "感激", "自豪", "爱"],
    "negative": ["焦虑", "紧张", "愤怒", "悲伤", "沮丧", "困惑", "恐惧", "厌恶", "羞耻", "内疚"],
    "neutral": ["中性", "好奇", "惊讶", "无聊", "疲惫"]
}

class EmotionDataset(Dataset):
    """情绪分类数据集"""
    
    def __init__(self, texts: List[str], labels: List[int], tokenizer, max_length=128):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length
    
    def __len__(self):
        return len(self.texts)
    
    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = self.labels[idx]
        
        encoding = self.tokenizer(
            text,
            truncation=True,
            padding='max_length',
            max_length=self.max_length,
            return_tensors='pt'
        )
        
        return {
            'input_ids': encoding['input_ids'].flatten(),
            'attention_mask': encoding['attention_mask'].flatten(),
            'labels': torch.tensor(label, dtype=torch.long)
        }

def load_training_data(data_path: str = None) -> Tuple[List[str], List[int]]:
    """
    加载训练数据
    如果没有提供数据文件，使用模拟数据（实际应用中应使用真实数据集）
    """
    if data_path and os.path.exists(data_path):
        logger.info(f"从 {data_path} 加载训练数据")
        with open(data_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        texts = [item['text'] for item in data]
        labels = [item['label'] for item in data]
        return texts, labels
    else:
        logger.warning("未找到训练数据文件，使用模拟数据")
        # 模拟训练数据（实际应使用真实数据集如NLPCC 2014、ChnSentiCorp等）
        return generate_mock_data()

def generate_mock_data() -> Tuple[List[str], List[int]]:
    """生成模拟训练数据"""
    texts = []
    labels = []
    
    # 积极情绪样本
    positive_samples = [
        "今天天气真好，心情很愉快",
        "完成了这个项目，感觉很满足",
        "和朋友一起玩得很开心",
        "收到了好消息，非常兴奋",
        "今天的努力得到了回报，很自豪",
        "感受到了家人的温暖，很感激",
        "对未来充满期待",
        "心情很平静，感觉很放松"
    ]
    
    # 消极情绪样本
    negative_samples = [
        "今天工作压力很大，感觉很焦虑",
        "遇到了困难，心情很沮丧",
        "被人误解了，很生气",
        "失去了重要的东西，很悲伤",
        "对未来感到恐惧和不安",
        "对某些事情感到厌恶",
        "做错了事情，很内疚",
        "感到困惑和迷茫"
    ]
    
    # 中性情绪样本
    neutral_samples = [
        "今天和往常一样",
        "没有什么特别的事情",
        "对这件事感到好奇",
        "有点惊讶",
        "感觉有点无聊",
        "工作了一天，有点疲惫"
    ]
    
    for text in positive_samples:
        texts.append(text)
        labels.append(0)  # positive
    
    for text in negative_samples:
        texts.append(text)
        labels.append(1)  # negative
    
    for text in neutral_samples:
        texts.append(text)
        labels.append(2)  # neutral
    
    return texts, labels

def compute_metrics(eval_pred):
    """计算评估指标"""
    predictions, labels = eval_pred
    predictions = np.argmax(predictions, axis=1)
    
    accuracy = accuracy_score(labels, predictions)
    precision, recall, f1, _ = precision_recall_fscore_support(
        labels, predictions, average='weighted', zero_division=0
    )
    
    return {
        'accuracy': accuracy,
        'precision': precision,
        'recall': recall,
        'f1': f1
    }

def train_model(
    model_name: str = "bert-base-chinese",
    output_dir: str = "./models/emotion_classifier",
    num_epochs: int = 3,
    batch_size: int = 16,
    learning_rate: float = 2e-5,
    train_data_path: str = None
):
    """
    训练情绪分类模型
    
    Args:
        model_name: 预训练模型名称
        output_dir: 模型输出目录
        num_epochs: 训练轮数
        batch_size: 批次大小
        learning_rate: 学习率
        train_data_path: 训练数据路径
    """
    logger.info(f"开始训练模型: {model_name}")
    logger.info(f"输出目录: {output_dir}")
    
    # 加载训练数据
    texts, labels = load_training_data(train_data_path)
    logger.info(f"训练样本数: {len(texts)}")
    
    # 划分训练集和验证集（80/20）
    split_idx = int(len(texts) * 0.8)
    train_texts = texts[:split_idx]
    train_labels = labels[:split_idx]
    val_texts = texts[split_idx:]
    val_labels = labels[split_idx:]
    
    logger.info(f"训练集: {len(train_texts)}, 验证集: {len(val_texts)}")
    
    # 加载tokenizer和模型
    logger.info("加载tokenizer和模型...")
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForSequenceClassification.from_pretrained(
        model_name,
        num_labels=3  # positive, negative, neutral
    )
    
    # 创建数据集
    train_dataset = EmotionDataset(train_texts, train_labels, tokenizer)
    val_dataset = EmotionDataset(val_texts, val_labels, tokenizer)
    
    # 训练参数
    training_args = TrainingArguments(
        output_dir=output_dir,
        num_train_epochs=num_epochs,
        per_device_train_batch_size=batch_size,
        per_device_eval_batch_size=batch_size,
        learning_rate=learning_rate,
        weight_decay=0.01,
        logging_dir=f'{output_dir}/logs',
        logging_steps=10,
        eval_strategy="epoch",
        save_strategy="epoch",
        load_best_model_at_end=True,
        metric_for_best_model="f1",
        greater_is_better=True,
        save_total_limit=3,
        report_to="none"  # 不使用wandb等工具
    )
    
    # 创建Trainer
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=val_dataset,
        compute_metrics=compute_metrics,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=3)]
    )
    
    # 开始训练
    logger.info("开始训练...")
    train_result = trainer.train()
    
    # 保存模型
    logger.info(f"保存模型到 {output_dir}")
    trainer.save_model()
    tokenizer.save_pretrained(output_dir)
    
    # 评估模型
    logger.info("评估模型...")
    eval_result = trainer.evaluate()
    
    # 保存训练结果
    results = {
        'model_name': model_name,
        'training_time': str(datetime.now()),
        'train_loss': train_result.training_loss,
        'eval_accuracy': eval_result['eval_accuracy'],
        'eval_precision': eval_result['eval_precision'],
        'eval_recall': eval_result['eval_recall'],
        'eval_f1': eval_result['eval_f1'],
        'num_train_samples': len(train_texts),
        'num_val_samples': len(val_texts),
        'num_epochs': num_epochs,
        'batch_size': batch_size,
        'learning_rate': learning_rate
    }
    
    results_path = os.path.join(output_dir, 'training_results.json')
    with open(results_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    logger.info("训练完成！")
    logger.info(f"验证集准确率: {eval_result['eval_accuracy']:.4f}")
    logger.info(f"验证集F1分数: {eval_result['eval_f1']:.4f}")
    logger.info(f"训练结果已保存到: {results_path}")
    
    # 生成混淆矩阵
    generate_confusion_matrix(trainer, val_dataset, output_dir)
    
    return results

def generate_confusion_matrix(trainer, val_dataset, output_dir):
    """生成混淆矩阵可视化"""
    try:
        predictions = trainer.predict(val_dataset)
        pred_labels = np.argmax(predictions.predictions, axis=1)
        true_labels = predictions.label_ids
        
        cm = confusion_matrix(true_labels, pred_labels)
        
        plt.figure(figsize=(8, 6))
        sns.heatmap(
            cm, 
            annot=True, 
            fmt='d', 
            cmap='Blues',
            xticklabels=['Positive', 'Negative', 'Neutral'],
            yticklabels=['Positive', 'Negative', 'Neutral']
        )
        plt.title('Confusion Matrix')
        plt.ylabel('True Label')
        plt.xlabel('Predicted Label')
        
        cm_path = os.path.join(output_dir, 'confusion_matrix.png')
        plt.savefig(cm_path, dpi=300, bbox_inches='tight')
        plt.close()
        
        logger.info(f"混淆矩阵已保存到: {cm_path}")
    except Exception as e:
        logger.warning(f"生成混淆矩阵失败: {e}")

def compare_models(
    model_names: List[str] = None,
    output_base_dir: str = "./models/comparison",
    num_epochs: int = 3
):
    """
    对比多个模型的性能
    
    Args:
        model_names: 要对比的模型列表
        output_base_dir: 输出基础目录
        num_epochs: 训练轮数
    """
    if model_names is None:
        model_names = [
            "bert-base-chinese",
            "hfl/chinese-roberta-wwm-ext",
            "distilbert-base-chinese"
        ]
    
    results = []
    
    for model_name in model_names:
        logger.info(f"\n{'='*50}")
        logger.info(f"训练模型: {model_name}")
        logger.info(f"{'='*50}")
        
        output_dir = os.path.join(output_base_dir, model_name.replace('/', '_'))
        os.makedirs(output_dir, exist_ok=True)
        
        try:
            result = train_model(
                model_name=model_name,
                output_dir=output_dir,
                num_epochs=num_epochs
            )
            results.append(result)
        except Exception as e:
            logger.error(f"训练模型 {model_name} 失败: {e}")
            results.append({
                'model_name': model_name,
                'error': str(e)
            })
    
    # 保存对比结果
    comparison_path = os.path.join(output_base_dir, 'model_comparison.json')
    with open(comparison_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    
    logger.info(f"\n模型对比结果已保存到: {comparison_path}")
    
    # 打印对比结果
    print("\n" + "="*60)
    print("模型性能对比")
    print("="*60)
    for result in results:
        if 'error' not in result:
            print(f"\n模型: {result['model_name']}")
            print(f"  准确率: {result['eval_accuracy']:.4f}")
            print(f"  F1分数: {result['eval_f1']:.4f}")
            print(f"  精确率: {result['eval_precision']:.4f}")
            print(f"  召回率: {result['eval_recall']:.4f}")
        else:
            print(f"\n模型: {result['model_name']} - 训练失败: {result['error']}")
    
    return results

if __name__ == '__main__':
    import argparse
    
    parser = argparse.ArgumentParser(description='训练情绪分类模型')
    parser.add_argument('--model', type=str, default='bert-base-chinese', help='预训练模型名称')
    parser.add_argument('--output', type=str, default='./models/emotion_classifier', help='输出目录')
    parser.add_argument('--epochs', type=int, default=3, help='训练轮数')
    parser.add_argument('--batch-size', type=int, default=16, help='批次大小')
    parser.add_argument('--lr', type=float, default=2e-5, help='学习率')
    parser.add_argument('--data', type=str, default=None, help='训练数据路径')
    parser.add_argument('--compare', action='store_true', help='对比多个模型')
    
    args = parser.parse_args()
    
    if args.compare:
        compare_models(num_epochs=args.epochs)
    else:
        train_model(
            model_name=args.model,
            output_dir=args.output,
            num_epochs=args.epochs,
            batch_size=args.batch_size,
            learning_rate=args.lr,
            train_data_path=args.data
        )
