package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.AiResponseParser;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.entity.AiAnalysis;
import com.studycompanion.entity.MissRecord;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.entity.User;
import com.studycompanion.mapper.AiAnalysisMapper;
import com.studycompanion.mapper.MissRecordMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.mapper.UserMapper;
import com.studycompanion.service.AiService;
import com.studycompanion.vo.AiAnalysisVO;
import com.studycompanion.vo.AiChatResponse;
import com.studycompanion.vo.ShareImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiAnalysisMapper aiAnalysisMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final SubjectMapper subjectMapper;
    private final MissRecordMapper missRecordMapper;
    private final UserMapper userMapper;
    private final AiClient aiClient;

    @Override
    public AiAnalysisVO generateWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String dataSummary = buildDataSummary(userId, weekStart, weekEnd);
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户本周的学习数据，生成一份温暖、鼓励的学习周报。\n\n" +
                "要求：\n" +
                "1. 包含学习概况、科目分布分析\n" +
                "2. 如果有断签记录，要特别提醒用户，但语气要温和鼓励\n" +
                "3. 分析断签原因，给出改进建议\n" +
                "4. 如果没有断签，要表扬用户的坚持\n" +
                "5. 使用Markdown格式，语气温暖积极\n" +
                "6. 在报告末尾给出下周的学习建议";
        String content;
        try {
            content = aiClient.chat(systemPrompt, "请根据以下学习数据生成周报：\n\n" + dataSummary);
        } catch (Exception e) {
            log.warn("AI周报生成失败，使用模板生成: {}", e.getMessage());
            content = generateTemplateReport(dataSummary, "周");
        }

        AiAnalysis analysis = new AiAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType("weekly_report");
        analysis.setContent(content);
        aiAnalysisMapper.insert(analysis);

        return convertToVO(analysis);
    }

    @Override
    public AiAnalysisVO generateMonthlyReport(Long userId) {
        YearMonth thisMonth = YearMonth.now();
        LocalDate monthStart = thisMonth.atDay(1);
        LocalDate monthEnd = thisMonth.atEndOfMonth();

        String dataSummary = buildDataSummary(userId, monthStart, monthEnd);
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户本月的学习数据，生成一份详细的月度学习报告。\n\n" +
                "要求：\n" +
                "1. 包含学习概况、趋势分析、薄弱科目提醒\n" +
                "2. 如果有断签记录，要重点分析断签规律和原因\n" +
                "3. 统计断签次数，提醒用户注意学习连续性\n" +
                "4. 给出具体的改进建议，帮助用户减少断签\n" +
                "5. 如果断签次数少或没有，要给予肯定和鼓励\n" +
                "6. 使用Markdown格式，语气温暖鼓励\n" +
                "7. 在报告末尾给出下月的学习计划建议";

        String content;
        try {
            content = aiClient.chat(systemPrompt, "请根据以下学习数据生成月报：\n\n" + dataSummary);
        } catch (Exception e) {
            log.warn("AI月报生成失败，使用模板生成: {}", e.getMessage());
            content = generateTemplateReport(dataSummary, "月");
        }

        AiAnalysis analysis = new AiAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType("monthly_report");
        analysis.setContent(content);
        aiAnalysisMapper.insert(analysis);

        return convertToVO(analysis);
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        User user = userMapper.selectById(userId);
        String nickname = user != null ? user.getNickname() : "学习者";

        String systemPrompt = "你是「智学伴」，一个专门为考公考编用户设计的AI学习助手。" +
                "用户昵称是" + nickname + "。" +
                "你的回答应该简洁、实用、温暖，给出具体可执行的建议。" +
                "如果用户问学习相关问题，给出专业建议；如果是闲聊，温暖回应并引导回学习。";

        List<Map<String, String>> messages = new java.util.ArrayList<>();

        // 添加历史对话（最多保留最近10轮）
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            List<Map<String, String>> history = request.getHistory();
            int start = Math.max(0, history.size() - 20); // 最多20条消息（10轮）
            messages.addAll(history.subList(start, history.size()));
        }

        // 添加当前问题
        messages.add(Map.of("role", "user", "content", request.getQuestion()));

        String answer = aiClient.chat(systemPrompt, messages);

        AiChatResponse response = new AiChatResponse();
        response.setAnswer(answer);
        return response;
    }

    @Override
    public AiAnalysisVO judgeFocusLevel(Long userId, String studyContext) {
        String systemPrompt = "你是一个学习专注度分析助手。根据用户描述的学习情况，判断专注度等级（1-5级）。" +
                "1级=非常不专注，2级=不太专注，3级=一般，4级=比较专注，5级=非常专注。" +
                "请只返回JSON格式：{\"level\":数字,\"reason\":\"简短原因\"}";
        String userMessage = studyContext != null ? studyContext : "用户未提供具体描述";

        String result = aiClient.chat(systemPrompt, userMessage);

        // 解析 AI 返回的 JSON，格式化为可读文本
        String formattedResult;
        try {
            AiResponseParser.AiJudgmentResult judgment = AiResponseParser.parseJudgment(result);
            int level = judgment.getLevel();
            String reason = judgment.getReason().isEmpty() ? "未提供原因" : judgment.getReason();
            String[] levelDesc = {"", "非常不专注", "不太专注", "一般", "比较专注", "非常专注"};
            String levelText = level >= 1 && level <= 5 ? levelDesc[level] : "未知";
            formattedResult = String.format("专注度评级：%d/5（%s）\n\n%s", level, levelText, reason);
        } catch (Exception e) {
            log.warn("解析专注度JSON失败，使用原始结果: {}", e.getMessage());
            formattedResult = result;
        }

        String content = "根据AI分析，您的专注度情况：\n\n" + formattedResult;

        AiAnalysis analysis = new AiAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType("advice");
        analysis.setContent(content);
        aiAnalysisMapper.insert(analysis);

        return convertToVO(analysis);
    }

    @Override
    public ShareImageVO generateShareImage(Long userId) {
        User user = userMapper.selectById(userId);
        String nickname = user != null ? user.getNickname() : "学习者";

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .ge(StudyRecord::getStudyDate, weekStart)
               .le(StudyRecord::getStudyDate, today);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        int totalDuration = records.stream().mapToInt(StudyRecord::getDuration).sum();
        int totalDays = (int) records.stream().map(StudyRecord::getStudyDate).distinct().count();

        ShareImageVO vo = new ShareImageVO();
        vo.setContent(String.format(
                "%s 的学习打卡\n\n" +
                "本周学习 %d 天\n" +
                "累计学习 %d 小时 %d 分钟\n" +
                "学习科目：%s\n\n" +
                "继续加油，每天进步一点点！",
                nickname,
                totalDays,
                totalDuration / 60,
                totalDuration % 60,
                getSubjectNames(userId)
        ));
        vo.setImageUrl("/api/v1/ai/share-image/" + userId);

        return vo;
    }

    private String buildDataSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .ge(StudyRecord::getStudyDate, startDate)
               .le(StudyRecord::getStudyDate, endDate);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder();
        sb.append("统计周期：").append(startDate).append(" 至 ").append(endDate).append("\n");

        if (records.isEmpty()) {
            sb.append("暂无学习记录。\n");
        } else {
            int totalDuration = records.stream().mapToInt(StudyRecord::getDuration).sum();
            int totalDays = (int) records.stream().map(StudyRecord::getStudyDate).distinct().count();

            sb.append("累计学习天数：").append(totalDays).append(" 天\n");
            sb.append("累计学习时长：").append(totalDuration).append(" 分钟\n");
            sb.append("日均学习时长：").append(totalDuration / Math.max(totalDays, 1)).append(" 分钟\n\n");

            // 批量查询科目，避免 N+1
            Set<Long> subjectIds = records.stream()
                    .map(StudyRecord::getSubjectId)
                    .collect(Collectors.toSet());
            Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                    ? Collections.emptyMap()
                    : subjectMapper.selectBatchIds(subjectIds).stream()
                            .collect(Collectors.toMap(Subject::getId, s -> s));

            sb.append("科目分布：\n");
            Map<String, Integer> subjectStats = records.stream()
                    .collect(Collectors.groupingBy(
                            r -> {
                                Subject subject = subjectMap.get(r.getSubjectId());
                                return subject != null ? subject.getName() : "未知";
                            },
                            Collectors.summingInt(StudyRecord::getDuration)
                    ));
            subjectStats.forEach((name, duration) ->
                    sb.append("- ").append(name).append("：").append(duration).append(" 分钟\n")
            );
        }

        // 查询断签记录
        LambdaQueryWrapper<MissRecord> missWrapper = new LambdaQueryWrapper<>();
        missWrapper.eq(MissRecord::getUserId, userId)
                   .ge(MissRecord::getMissDate, startDate)
                   .le(MissRecord::getMissDate, endDate)
                   .orderByAsc(MissRecord::getMissDate);
        List<MissRecord> missRecords = missRecordMapper.selectList(missWrapper);

        if (!missRecords.isEmpty()) {
            sb.append("\n断签记录（共").append(missRecords.size()).append("天）：\n");
            for (MissRecord miss : missRecords) {
                sb.append("- ").append(miss.getMissDate()).append("：").append(miss.getReason());
                if (miss.getIsReplenished() == 1) {
                    sb.append(" [已补签]");
                } else if (miss.getAiAllowReplenish() != null && miss.getAiAllowReplenish() == 1) {
                    sb.append(" [可补签]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String getSubjectNames(Long userId) {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subject::getUserId, userId);
        List<Subject> subjects = subjectMapper.selectList(wrapper);
        return subjects.stream()
                .map(Subject::getName)
                .collect(Collectors.joining("、"));
    }

    /**
     * 模板报告（AI 不可用时的降级方案）
     */
    private String generateTemplateReport(String dataSummary, String period) {
        return "# 学习" + period + "报\n\n" +
                "## 学习数据\n\n" + dataSummary + "\n\n" +
                "## 总结\n\n" +
                "以上是您本" + period + "的学习数据汇总。请继续保持学习节奏，每天进步一点点！\n\n" +
                "## 建议\n\n" +
                "1. 保持每日学习习惯\n" +
                "2. 合理分配各科学习时间\n" +
                "3. 注意劳逸结合，保持良好状态";
    }

    private AiAnalysisVO convertToVO(AiAnalysis analysis) {
        AiAnalysisVO vo = new AiAnalysisVO();
        vo.setId(analysis.getId());
        vo.setAnalysisType(analysis.getAnalysisType());
        vo.setContent(analysis.getContent());
        vo.setCreateTime(analysis.getCreateTime());
        return vo;
    }
}
