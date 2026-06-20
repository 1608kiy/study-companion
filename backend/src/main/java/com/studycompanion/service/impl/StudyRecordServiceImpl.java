package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.StartTimerRequest;
import com.studycompanion.dto.UpdateStudyRecordRequest;
import com.studycompanion.entity.CheckIn;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.CheckInMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.StudyRecordService;
import com.studycompanion.vo.PageResponse;
import com.studycompanion.vo.StudyRecordVO;
import com.studycompanion.vo.StudyStatsVO;
import com.studycompanion.vo.TimerStateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl implements StudyRecordService {

    private final StudyRecordMapper studyRecordMapper;
    private final SubjectMapper subjectMapper;
    private final CheckInMapper checkInMapper;
    private final AiClient aiClient;
    private final StringRedisTemplate redisTemplate;

    private static final String TIMER_PREFIX = "timer:";
    private static final String TIMER_RUNNING_SUFFIX = ":running";
    private static final String TIMER_SUBJECT_SUFFIX = ":subject";
    private static final String TIMER_START_SUFFIX = ":start";
    private static final String TIMER_PAUSED_SUFFIX = ":paused";
    private static final String TIMER_ELAPSED_SUFFIX = ":elapsed";

    @Override
    public TimerStateVO startTimer(Long userId, StartTimerRequest request) {
        // 检查是否有正在运行的计时器
        if (isTimerRunning(userId)) {
            throw new BusinessException(ErrorCode.CHECKIN_ALREADY_COMPLETED);
        }

        // 验证科目是否存在
        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null || !subject.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        String key = TIMER_PREFIX + userId;
        redisTemplate.opsForValue().set(key + TIMER_RUNNING_SUFFIX, "true", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_SUBJECT_SUFFIX, String.valueOf(request.getSubjectId()), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_START_SUFFIX, String.valueOf(System.currentTimeMillis()), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_PAUSED_SUFFIX, "false", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_ELAPSED_SUFFIX, "0", 24, TimeUnit.HOURS);

        TimerStateVO state = new TimerStateVO();
        state.setIsRunning(true);
        state.setSubjectId(request.getSubjectId());
        state.setSubjectName(subject.getName());
        state.setStartTime(LocalDateTime.now());
        state.setElapsedSeconds(0L);

        return state;
    }

    @Override
    public TimerStateVO pauseTimer(Long userId) {
        if (!isTimerRunning(userId)) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_COMPLETED);
        }

        String key = TIMER_PREFIX + userId;
        long startTime = Long.parseLong(redisTemplate.opsForValue().get(key + TIMER_START_SUFFIX));
        long elapsed = Long.parseLong(redisTemplate.opsForValue().get(key + TIMER_ELAPSED_SUFFIX));
        elapsed += (System.currentTimeMillis() - startTime) / 1000;

        redisTemplate.opsForValue().set(key + TIMER_RUNNING_SUFFIX, "false", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_PAUSED_SUFFIX, "true", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_ELAPSED_SUFFIX, String.valueOf(elapsed), 24, TimeUnit.HOURS);

        return getTimerState(userId);
    }

    @Override
    public TimerStateVO resumeTimer(Long userId) {
        String key = TIMER_PREFIX + userId;
        if (!"true".equals(redisTemplate.opsForValue().get(key + TIMER_PAUSED_SUFFIX))) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_COMPLETED);
        }

        redisTemplate.opsForValue().set(key + TIMER_RUNNING_SUFFIX, "true", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_PAUSED_SUFFIX, "false", 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(key + TIMER_START_SUFFIX, String.valueOf(System.currentTimeMillis()), 24, TimeUnit.HOURS);

        return getTimerState(userId);
    }

    @Override
    public StudyRecordVO stopTimer(Long userId) {
        if (!isTimerRunning(userId) && !"true".equals(redisTemplate.opsForValue().get(TIMER_PREFIX + userId + TIMER_PAUSED_SUFFIX))) {
            throw new BusinessException(ErrorCode.CHECKIN_NOT_COMPLETED);
        }

        String key = TIMER_PREFIX + userId;
        Long subjectId = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_SUBJECT_SUFFIX)));
        long elapsed = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_ELAPSED_SUFFIX)));

        if ("true".equals(redisTemplate.opsForValue().get(key + TIMER_RUNNING_SUFFIX))) {
            long startTime = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_START_SUFFIX)));
            elapsed += (System.currentTimeMillis() - startTime) / 1000;
        }

        int duration = (int) (elapsed / 60);
        if (duration < 1) {
            duration = 1;
        }

        StudyRecord record = new StudyRecord();
        record.setUserId(userId);
        record.setSubjectId(subjectId);
        record.setStudyDate(LocalDate.now());
        record.setStartTime(LocalDateTime.now().minusSeconds(elapsed));
        record.setEndTime(LocalDateTime.now());
        record.setDuration(duration);
        studyRecordMapper.insert(record);

        // 清除计时器状态
        redisTemplate.delete(Arrays.asList(
                key + TIMER_RUNNING_SUFFIX,
                key + TIMER_SUBJECT_SUFFIX,
                key + TIMER_START_SUFFIX,
                key + TIMER_PAUSED_SUFFIX,
                key + TIMER_ELAPSED_SUFFIX
        ));

        return convertToVO(record);
    }

    @Override
    public TimerStateVO getTimerState(Long userId) {
        String key = TIMER_PREFIX + userId;
        String isRunning = redisTemplate.opsForValue().get(key + TIMER_RUNNING_SUFFIX);

        TimerStateVO state = new TimerStateVO();
        if (!"true".equals(isRunning) && !"true".equals(redisTemplate.opsForValue().get(key + TIMER_PAUSED_SUFFIX))) {
            state.setIsRunning(false);
            return state;
        }

        state.setIsRunning("true".equals(isRunning));
        Long subjectId = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_SUBJECT_SUFFIX)));
        state.setSubjectId(subjectId);

        Subject subject = subjectMapper.selectById(subjectId);
        if (subject != null) {
            state.setSubjectName(subject.getName());
        }

        long elapsed = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_ELAPSED_SUFFIX)));
        if ("true".equals(isRunning)) {
            long startTime = Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(key + TIMER_START_SUFFIX)));
            elapsed += (System.currentTimeMillis() - startTime) / 1000;
            state.setStartTime(LocalDateTime.now().minusSeconds(elapsed));
        }
        state.setElapsedSeconds(elapsed);

        return state;
    }

    @Override
    public List<StudyRecordVO> getStudyRecords(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);

        if (startDate != null) {
            wrapper.ge(StudyRecord::getStudyDate, LocalDate.parse(startDate));
        }
        if (endDate != null) {
            wrapper.le(StudyRecord::getStudyDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(StudyRecord::getStudyDate)
               .orderByDesc(StudyRecord::getStartTime);

        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        Set<Long> subjectIds = records.stream()
                .map(StudyRecord::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, s -> s));

        return records.stream()
                .map(r -> convertToVO(r, subjectMap))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<StudyRecordVO> getStudyRecordsPaged(Long userId, String startDate, String endDate, int page, int size) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);

        if (startDate != null) {
            wrapper.ge(StudyRecord::getStudyDate, LocalDate.parse(startDate));
        }
        if (endDate != null) {
            wrapper.le(StudyRecord::getStudyDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(StudyRecord::getStudyDate)
               .orderByDesc(StudyRecord::getStartTime);

        // 获取总数
        long total = studyRecordMapper.selectCount(wrapper);

        // 分页查询
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        Set<Long> subjectIds = records.stream()
                .map(StudyRecord::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, s -> s));

        List<StudyRecordVO> voList = records.stream()
                .map(r -> convertToVO(r, subjectMap))
                .collect(Collectors.toList());

        return new PageResponse<>(voList, total, page, size);
    }

    @Override
    public StudyRecordVO getStudyRecordById(Long userId, Long recordId) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }
        return convertToVO(record);
    }

    private static final String AI_APPROVE_PREFIX = "ai_approve:";
    private static final long AI_APPROVE_EXPIRE_MINUTES = 10;

    @Override
    public Map<String, Object> aiJudgeModify(Long userId, Long recordId, String reason) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }

        Subject subject = subjectMapper.selectById(record.getSubjectId());
        String subjectName = subject != null ? subject.getName() : "未知科目";

        String systemPrompt = "你是一个考公学习陪伴助手。根据用户描述的修改原因，判断是否允许修改学习记录。\n\n" +
                "判断规则：\n" +
                "- 记录有误、科目选错、时间记录错误 → 允许修改\n" +
                "- 补充备注、添加心情 → 允许修改\n" +
                "- 想删除记录、想改短时间、觉得学太多 → 不允许修改\n" +
                "- 没有正当理由 → 不允许修改\n\n" +
                "学习记录信息：\n" +
                "- 科目：" + subjectName + "\n" +
                "- 时长：" + record.getDuration() + "分钟\n" +
                "- 日期：" + record.getStudyDate() + "\n\n" +
                "请只返回JSON格式：{\"allow\":true/false,\"reason\":\"简短原因\"}";

        String userMessage = "修改原因：" + reason;

        try {
            String result = aiClient.chat(systemPrompt, userMessage);
            
            String trimmed = result.trim();
            if (trimmed.startsWith("```")) {
                trimmed = trimmed.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> jsonMap = mapper.readValue(trimmed, Map.class);
            boolean allow = jsonMap.containsKey("allow") ? (Boolean) jsonMap.get("allow") : false;
            String aiReason = jsonMap.containsKey("reason") ? (String) jsonMap.get("reason") : "";
            
            log.info("AI修改判断: userId={}, recordId={}, allow={}, reason={}", userId, recordId, allow, aiReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("allow", allow);
            response.put("reason", aiReason);
            
            // 如果AI允许，生成审批token存入Redis
            if (allow) {
                String token = UUID.randomUUID().toString().replace("-", "");
                String redisKey = AI_APPROVE_PREFIX + userId + ":" + recordId;
                redisTemplate.opsForValue().set(redisKey, token, AI_APPROVE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                response.put("approveToken", token);
            }
            
            return response;
        } catch (Exception e) {
            log.error("AI修改判断失败，使用默认规则: {}", e.getMessage());
            // 降级处理
            boolean allow = reason != null && (reason.contains("错误") || reason.contains("备注"));
            Map<String, Object> response = new HashMap<>();
            response.put("allow", allow);
            response.put("reason", allow ? "系统判断允许修改" : "系统判断不允许修改");
            
            if (allow) {
                String token = UUID.randomUUID().toString().replace("-", "");
                String redisKey = AI_APPROVE_PREFIX + userId + ":" + recordId;
                redisTemplate.opsForValue().set(redisKey, token, AI_APPROVE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                response.put("approveToken", token);
            }
            
            return response;
        }
    }

    private void checkAiApproval(Long userId, Long recordId, String approveToken) {
        if (approveToken == null || approveToken.isEmpty()) {
            throw new BusinessException(ErrorCode.RECORD_MODIFY_NOT_ALLOWED, "请先通过AI审批");
        }
        String redisKey = AI_APPROVE_PREFIX + userId + ":" + recordId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(approveToken)) {
            throw new BusinessException(ErrorCode.RECORD_MODIFY_NOT_ALLOWED, "审批token无效或已过期");
        }
        // 使用后删除token（一次性）
        redisTemplate.delete(redisKey);
    }

    @Override
    public StudyRecordVO updateStudyRecord(Long userId, Long recordId, UpdateStudyRecordRequest request) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }

        // 检查AI审批token
        checkAiApproval(userId, recordId, request.getApproveToken());

        if (request.getMood() != null) {
            record.setMood(request.getMood());
        }
        if (request.getFocusLevel() != null) {
            record.setFocusLevel(request.getFocusLevel());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        if (request.getInterruptionCount() != null) {
            record.setInterruptionCount(request.getInterruptionCount());
        }
        if (request.getInterruptionReason() != null) {
            record.setInterruptionReason(request.getInterruptionReason());
        }

        studyRecordMapper.updateById(record);
        return convertToVO(record);
    }

    @Override
    public void deleteStudyRecord(Long userId, Long recordId, String approveToken) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }
        // 检查AI审批token
        checkAiApproval(userId, recordId, approveToken);
        studyRecordMapper.deleteById(recordId);
    }

    @Override
    public StudyStatsVO getStudyStats(Long userId) {
        StudyStatsVO stats = new StudyStatsVO();
        LocalDate today = LocalDate.now();

        // 使用 SQL 聚合查询获取总统计
        Map<String, Object> aggregated = studyRecordMapper.getStudyStatsAggregated(userId);
        stats.setTotalDays(((Number) aggregated.get("totalDays")).intValue());
        stats.setTotalDuration(((Number) aggregated.get("totalDuration")).intValue());

        // 今日学习时长
        Integer todayDuration = studyRecordMapper.getDurationBetween(userId, today, today);
        stats.setTodayDuration(todayDuration);

        // 本周学习时长
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        Integer weekDuration = studyRecordMapper.getDurationBetween(userId, weekStart, today);
        stats.setWeekDuration(weekDuration);

        // 本月学习时长
        LocalDate monthStart = today.withDayOfMonth(1);
        Integer monthDuration = studyRecordMapper.getDurationBetween(userId, monthStart, today);
        stats.setMonthDuration(monthDuration);

        // 科目学习时长（SQL 聚合）
        List<Map<String, Object>> subjectStatsList = studyRecordMapper.getSubjectStats(userId);
        Map<String, Integer> subjectStats = new HashMap<>();
        for (Map<String, Object> row : subjectStatsList) {
            String subjectName = (String) row.get("subjectName");
            int duration = ((Number) row.get("totalDuration")).intValue();
            if (subjectName != null) {
                subjectStats.put(subjectName, duration);
            }
        }
        stats.setSubjectStats(subjectStats);

        // 连续打卡天数
        int currentStreak = calculateCurrentStreak(userId, today);
        stats.setCurrentStreak(currentStreak);

        // 最近7天每日时长（SQL 聚合）
        List<Map<String, Object>> dailyDurationsList = studyRecordMapper.getDailyDurationsBetween(
                userId, today.minusDays(6), today);
        Map<LocalDate, Integer> dailyDurationMap = new HashMap<>();
        for (Map<String, Object> row : dailyDurationsList) {
            LocalDate date = ((java.sql.Date) row.get("studyDate")).toLocalDate();
            int duration = ((Number) row.get("totalDuration")).intValue();
            dailyDurationMap.put(date, duration);
        }
        List<Integer> weeklyDurations = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weeklyDurations.add(dailyDurationMap.getOrDefault(date, 0));
        }
        stats.setWeeklyDurations(weeklyDurations);

        return stats;
    }

    private int calculateCurrentStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckIn::getUserId, userId)
                   .eq(CheckIn::getCheckDate, checkDate)
                   .eq(CheckIn::getIsCompleted, 1);
            CheckIn checkIn = checkInMapper.selectOne(wrapper);

            if (checkIn == null) {
                break;
            }

            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private boolean isTimerRunning(Long userId) {
        String key = TIMER_PREFIX + userId;
        return "true".equals(redisTemplate.opsForValue().get(key + TIMER_RUNNING_SUFFIX));
    }

    private StudyRecordVO convertToVO(StudyRecord record) {
        Subject subject = subjectMapper.selectById(record.getSubjectId());
        Map<Long, Subject> singleMap = subject != null
                ? Collections.singletonMap(record.getSubjectId(), subject)
                : Collections.emptyMap();
        return convertToVO(record, singleMap);
    }

    private StudyRecordVO convertToVO(StudyRecord record, Map<Long, Subject> subjectMap) {
        StudyRecordVO vo = new StudyRecordVO();
        vo.setId(record.getId());
        vo.setSubjectId(record.getSubjectId());
        vo.setStudyDate(record.getStudyDate());
        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setDuration(record.getDuration());
        vo.setRemark(record.getRemark());
        vo.setMood(record.getMood());
        vo.setFocusLevel(record.getFocusLevel());
        vo.setAiFocusLevel(record.getAiFocusLevel());
        vo.setInterruptionCount(record.getInterruptionCount());
        vo.setInterruptionReason(record.getInterruptionReason());
        vo.setCreateTime(record.getCreateTime());

        Subject subject = subjectMap.get(record.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }

        return vo;
    }
}
