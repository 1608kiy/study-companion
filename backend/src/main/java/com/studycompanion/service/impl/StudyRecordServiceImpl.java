package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public StudyRecordVO getStudyRecordById(Long userId, Long recordId) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }
        return convertToVO(record);
    }

    @Override
    public StudyRecordVO updateStudyRecord(Long userId, Long recordId, UpdateStudyRecordRequest request) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }

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
    public void deleteStudyRecord(Long userId, Long recordId) {
        StudyRecord record = studyRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECORD_NOT_FOUND);
        }
        studyRecordMapper.deleteById(recordId);
    }

    @Override
    public StudyStatsVO getStudyStats(Long userId) {
        StudyStatsVO stats = new StudyStatsVO();
        LocalDate today = LocalDate.now();

        // 查询所有学习记录
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        // 统计总学习天数
        Set<LocalDate> uniqueDays = records.stream()
                .map(StudyRecord::getStudyDate)
                .collect(Collectors.toSet());
        stats.setTotalDays(uniqueDays.size());

        // 统计总学习时长
        int totalDuration = records.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setTotalDuration(totalDuration);

        // 统计今日学习时长
        int todayDuration = records.stream()
                .filter(r -> r.getStudyDate().equals(today))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setTodayDuration(todayDuration);

        // 统计本周学习时长
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        int weekDuration = records.stream()
                .filter(r -> !r.getStudyDate().isBefore(weekStart))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setWeekDuration(weekDuration);

        // 统计本月学习时长
        LocalDate monthStart = today.withDayOfMonth(1);
        int monthDuration = records.stream()
                .filter(r -> !r.getStudyDate().isBefore(monthStart))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setMonthDuration(monthDuration);

        // 统计科目学习时长（批量查询避免 N+1）
        Set<Long> subjectIds = records.stream()
                .map(StudyRecord::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, s -> s));
        Map<String, Integer> subjectStats = new HashMap<>();
        for (StudyRecord record : records) {
            Subject subject = subjectMap.get(record.getSubjectId());
            if (subject != null) {
                subjectStats.merge(subject.getName(), record.getDuration(), Integer::sum);
            }
        }
        stats.setSubjectStats(subjectStats);

        // 新增：连续打卡天数
        int currentStreak = calculateCurrentStreak(userId, today);
        stats.setCurrentStreak(currentStreak);

        // 新增：最近7天每日时长
        List<Integer> weeklyDurations = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int dayDuration = records.stream()
                    .filter(r -> r.getStudyDate().equals(date))
                    .mapToInt(StudyRecord::getDuration)
                    .sum();
            weeklyDurations.add(dayDuration);
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
