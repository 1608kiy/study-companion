package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Value("${ai.enabled}")
    private boolean aiEnabled;

    @Value("${ai.provider}")
    private String aiProvider;

    @Value("${ai.zhipu.api-key}")
    private String zhipuApiKey;

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;

    @Override
    public AiAnalysisVO generateWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        String content = generateReportContent(userId, weekStart, weekEnd, "周报");

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

        String content = generateReportContent(userId, monthStart, monthEnd, "月报");

        AiAnalysis analysis = new AiAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType("monthly_report");
        analysis.setContent(content);
        aiAnalysisMapper.insert(analysis);

        return convertToVO(analysis);
    }

    @Override
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        // TODO: 接入真实AI服务
        // 目前简单实现：返回模拟回答
        String answer = "感谢您的提问！关于「" + request.getQuestion() + "」，建议您：\n\n" +
                "1. 保持规律的学习时间\n" +
                "2. 注意劳逸结合\n" +
                "3. 复习比学习新内容更重要\n\n" +
                "加油！您一定能取得好成绩！";

        AiChatResponse response = new AiChatResponse();
        response.setAnswer(answer);
        return response;
    }

    @Override
    public AiAnalysisVO judgeFocusLevel(Long userId, String studyContext) {
        // TODO: 接入AI服务判断专注度
        // 目前简单实现：返回模拟专注度
        int focusLevel = 3; // 默认中等专注度
        if (studyContext != null) {
            if (studyContext.contains("高效") || studyContext.contains("专注")) {
                focusLevel = 5;
            } else if (studyContext.contains("分心") || studyContext.contains("中断")) {
                focusLevel = 2;
            }
        }

        String content = "根据您提供的学习情况，AI判断您的专注度为：" + focusLevel + "级（1-5级）\n\n";

        AiAnalysis analysis = new AiAnalysis();
        analysis.setUserId(userId);
        analysis.setAnalysisType("advice");
        analysis.setContent(content);
        aiAnalysisMapper.insert(analysis);

        return convertToVO(analysis);
    }

    @Override
    public ShareImageVO generateShareImage(Long userId) {
        // TODO: 生成分享图片
        // 目前返回文本内容
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
                "🎓 %s 的学习打卡\n\n" +
                "📅 本周学习 %d 天\n" +
                "⏰ 累计学习 %d 小时 %d 分钟\n" +
                "📚 学习科目：%s\n\n" +
                "💪 继续加油，每天进步一点点！",
                nickname,
                totalDays,
                totalDuration / 60,
                totalDuration % 60,
                getSubjectNames(userId)
        ));
        vo.setImageUrl("/api/v1/ai/share-image/" + userId);

        return vo;
    }

    private String generateReportContent(Long userId, LocalDate startDate, LocalDate endDate, String reportType) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .ge(StudyRecord::getStudyDate, startDate)
               .le(StudyRecord::getStudyDate, endDate);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        StringBuilder content = new StringBuilder();
        content.append("# ").append(reportType).append("\n\n");
        content.append("## 📊 学习概况\n\n");

        if (records.isEmpty()) {
            content.append("本周暂无学习记录。\n");
        } else {
            int totalDuration = records.stream().mapToInt(StudyRecord::getDuration).sum();
            int totalDays = (int) records.stream().map(StudyRecord::getStudyDate).distinct().count();

            content.append("- 累计学习天数：**").append(totalDays).append("** 天\n");
            content.append("- 累计学习时长：**").append(totalDuration).append("** 分钟\n");
            content.append("- 日均学习时长：**").append(totalDuration / Math.max(totalDays, 1)).append("** 分钟\n\n");

            content.append("## 📚 科目分布\n\n");
            Map<String, Integer> subjectStats = records.stream()
                    .collect(Collectors.groupingBy(
                            r -> {
                                Subject subject = subjectMapper.selectById(r.getSubjectId());
                                return subject != null ? subject.getName() : "未知";
                            },
                            Collectors.summingInt(StudyRecord::getDuration)
                    ));
            subjectStats.forEach((name, duration) ->
                    content.append("- ").append(name).append("：**").append(duration).append("** 分钟\n")
            );
        }

        return content.toString();
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
