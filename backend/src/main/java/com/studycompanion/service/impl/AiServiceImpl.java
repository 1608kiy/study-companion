package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.entity.AiAnalysis;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.entity.User;
import com.studycompanion.mapper.AiAnalysisMapper;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiAnalysisMapper aiAnalysisMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final AiClient aiClient;

    @Override
    public AiAnalysisVO generateWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String dataSummary = buildDataSummary(userId, weekStart, weekEnd);
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户本周的学习数据，生成一份温暖、鼓励的学习周报。" +
                "包含学习概况、科目分布分析、学习建议。使用Markdown格式，语气温暖积极。";
        String content = aiClient.chat(systemPrompt, "请根据以下学习数据生成周报：\n\n" + dataSummary);

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
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户本月的学习数据，生成一份详细的月度学习报告。" +
                "包含学习概况、趋势分析、薄弱科目提醒、下月建议。使用Markdown格式，语气温暖鼓励。";
        String content = aiClient.chat(systemPrompt, "请根据以下学习数据生成月报：\n\n" + dataSummary);

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

        String answer = aiClient.chat(systemPrompt, request.getQuestion());

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

        String content = "根据AI分析，您的专注度情况：\n\n" + result;

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
            return sb.toString();
        }

        int totalDuration = records.stream().mapToInt(StudyRecord::getDuration).sum();
        int totalDays = (int) records.stream().map(StudyRecord::getStudyDate).distinct().count();

        sb.append("累计学习天数：").append(totalDays).append(" 天\n");
        sb.append("累计学习时长：").append(totalDuration).append(" 分钟\n");
        sb.append("日均学习时长：").append(totalDuration / Math.max(totalDays, 1)).append(" 分钟\n\n");

        sb.append("科目分布：\n");
        Map<String, Integer> subjectStats = records.stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            Subject subject = subjectMapper.selectById(r.getSubjectId());
                            return subject != null ? subject.getName() : "未知";
                        },
                        Collectors.summingInt(StudyRecord::getDuration)
                ));
        subjectStats.forEach((name, duration) ->
                sb.append("- ").append(name).append("：").append(duration).append(" 分钟\n")
        );

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

    private AiAnalysisVO convertToVO(AiAnalysis analysis) {
        AiAnalysisVO vo = new AiAnalysisVO();
        vo.setId(analysis.getId());
        vo.setAnalysisType(analysis.getAnalysisType());
        vo.setContent(analysis.getContent());
        vo.setCreateTime(analysis.getCreateTime());
        return vo;
    }
}
