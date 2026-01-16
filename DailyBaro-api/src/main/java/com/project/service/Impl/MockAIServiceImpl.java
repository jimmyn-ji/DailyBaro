package com.project.service.Impl;

import com.project.service.AIService;
import com.project.util.Result;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("mockAIService") // Give it a specific name to distinguish from a real implementation later
public class MockAIServiceImpl implements AIService {

    private static final Map<String, String> DIARY_KEYWORD_RESPONSES = new HashMap<>();
    private static final Map<String, String> GENERAL_QUESTION_RESPONSES = new HashMap<>();
    private static final Map<String, String> EMOTION_ANALYSIS_RESPONSES = new HashMap<>();

    static {
        // 日记关键词自动建议
        DIARY_KEYWORD_RESPONSES.put("失眠", "检测到您提到了失眠，尝试一些放松技巧可能会有帮助，比如深呼吸、冥想或听一些舒缓的音乐。");
        DIARY_KEYWORD_RESPONSES.put("压力", "感到有压力时，记得给自己一些喘息的时间。散步、与朋友聊天或做一些你喜欢的事情都能有效缓解压力。");
        DIARY_KEYWORD_RESPONSES.put("焦虑", "焦虑是很常见的情绪。试着将注意力集中在当下，或者写下你的担忧，这有助于理清思绪，减轻焦虑感。");
        DIARY_KEYWORD_RESPONSES.put("抑郁", "如果你感到抑郁，可以尝试与信任的人交流，或寻求专业心理咨询师的帮助。");
        DIARY_KEYWORD_RESPONSES.put("孤独", "孤独时可以主动联系朋友或家人，参加一些兴趣小组，丰富自己的生活。");
        DIARY_KEYWORD_RESPONSES.put("愤怒", "愤怒时可以尝试深呼吸、短暂离开现场，等情绪平复后再处理问题。");
        DIARY_KEYWORD_RESPONSES.put("开心", "很高兴看到你有开心的时刻，继续保持积极的心态，享受生活。");
        DIARY_KEYWORD_RESPONSES.put("无助", "当你感到无助时，不妨向身边的人寻求帮助，你并不孤单。");
        DIARY_KEYWORD_RESPONSES.put("自卑", "自卑时可以回顾自己的优点和成就，尝试设立小目标并逐步实现。");
        DIARY_KEYWORD_RESPONSES.put("恐惧", "面对恐惧时，可以尝试逐步暴露法，或与信任的人交流你的感受。");

        // 高频问题
        GENERAL_QUESTION_RESPONSES.put("如何缓解压力", "缓解压力的好方法有很多，例如：进行适度的体育锻炼，如散步、瑜伽或跑步；保证充足的睡眠；与朋友或家人倾诉；或者培养一个能让你放松的爱好。");
        GENERAL_QUESTION_RESPONSES.put("我为什么会感到焦虑", "焦虑可能由多种因素引起，包括工作压力、人际关系、健康问题或对未来的不确定性。了解触发你焦虑的原因是管理它的第一步。");
        GENERAL_QUESTION_RESPONSES.put("如何改善睡眠", "改善睡眠可以从规律作息、睡前减少电子产品使用、保持舒适的睡眠环境等方面入手。");
        GENERAL_QUESTION_RESPONSES.put("怎样调节情绪", "调节情绪可以尝试深呼吸、运动、与朋友交流，或写日记记录自己的感受。");
        GENERAL_QUESTION_RESPONSES.put("压力大怎么办", "当压力大时，可以适当休息、分解任务、寻求家人朋友的支持，必要时可寻求专业帮助。");
        GENERAL_QUESTION_RESPONSES.put("如何缓解失眠", "睡前避免咖啡因、保持规律作息、可以听舒缓音乐或冥想，有助于缓解失眠。");
        GENERAL_QUESTION_RESPONSES.put("如何提升自信", "多肯定自己、设立小目标并逐步实现、回顾自己的成长和成就，有助于提升自信。");
        GENERAL_QUESTION_RESPONSES.put("如何应对孤独", "可以主动联系朋友或家人，参加兴趣小组，或培养新的兴趣爱好。");
        GENERAL_QUESTION_RESPONSES.put("如何面对抑郁情绪", "可以尝试规律作息、适度运动、与信任的人交流，必要时寻求专业心理帮助。");
        GENERAL_QUESTION_RESPONSES.put("如何管理愤怒", "愤怒时可以尝试深呼吸、短暂离开现场，等情绪平复后再处理问题。");
        GENERAL_QUESTION_RESPONSES.put("如何克服自卑", "多关注自己的优点，设立并完成小目标，逐步建立自信。");
        GENERAL_QUESTION_RESPONSES.put("如何缓解恐惧", "可以尝试逐步暴露法，或与信任的人交流你的感受，必要时寻求专业帮助。");

        // 情绪分析快速响应
        EMOTION_ANALYSIS_RESPONSES.put("积极", "您的情绪状态较为积极，建议继续保持乐观心态，适当分享快乐给身边的人。");
        EMOTION_ANALYSIS_RESPONSES.put("消极", "检测到消极情绪，建议多进行户外活动，与朋友交流，必要时寻求专业帮助。");
        EMOTION_ANALYSIS_RESPONSES.put("波动", "情绪波动较大，建议保持规律作息，学习情绪管理技巧，建立稳定的情绪调节机制。");
        EMOTION_ANALYSIS_RESPONSES.put("稳定", "情绪状态稳定，建议继续保持当前的生活方式，适当增加一些新的兴趣爱好。");
        EMOTION_ANALYSIS_RESPONSES.put("焦虑", "存在焦虑情绪，建议进行深呼吸练习，减少咖啡因摄入，保持充足睡眠。");
        EMOTION_ANALYSIS_RESPONSES.put("抑郁", "情绪偏低，建议增加户外活动，与亲友多交流，必要时寻求心理咨询。");
    }

    @Override
    public Result<String> getGeneralResponse(String question) {
        for (Map.Entry<String, String> entry : GENERAL_QUESTION_RESPONSES.entrySet()) {
            if (question.contains(entry.getKey())) {
                return Result.success(entry.getValue());
            }
        }
        return Result.success("这是一个很好的问题，我现在还在学习中，暂时无法回答。");
    }

    @Override
    public Result<String> getResponseForDiary(String diaryContent) {
        // 检查是否是情绪分析请求
        if (diaryContent.contains("情绪数据") || diaryContent.contains("情绪波动") || diaryContent.contains("情绪分布")) {
            return getEmotionAnalysisResponse(diaryContent);
        }
        
        for (Map.Entry<String, String> entry : DIARY_KEYWORD_RESPONSES.entrySet()) {
            if (diaryContent.contains(entry.getKey())) {
                return Result.success(entry.getValue());
            }
        }
        // If no keywords are found, we can return a generic supportive message
        return Result.success("感谢你的记录，很高兴能成为你的倾听者。");
    }
    
    private Result<String> getEmotionAnalysisResponse(String emotionData) {
        // 快速分析情绪数据并给出建议
        if (emotionData.contains("积极") || emotionData.contains("开心")) {
            return Result.success("根据您的情绪数据，整体情绪状态积极向上。建议继续保持乐观心态，适当分享快乐给身边的人，同时注意情绪的持续稳定性。");
        } else if (emotionData.contains("消极") || emotionData.contains("难过") || emotionData.contains("焦虑")) {
            return Result.success("检测到您的情绪存在波动和消极倾向。建议多进行户外活动，与朋友交流，学习情绪管理技巧，必要时寻求专业帮助。");
        } else if (emotionData.contains("波动")) {
            return Result.success("您的情绪波动较大，建议保持规律作息，学习深呼吸等放松技巧，建立稳定的情绪调节机制。");
        } else {
            return Result.success("基于您的情绪数据，建议保持当前的生活方式，适当增加户外活动和社交互动，有助于情绪的稳定和提升。");
        }
    }
} 