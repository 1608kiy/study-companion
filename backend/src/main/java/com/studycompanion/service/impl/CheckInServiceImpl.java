package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.entity.CheckIn;
import com.studycompanion.entity.Diary;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.UserStatistics;
import com.studycompanion.mapper.CheckInMapper;
import com.studycompanion.mapper.DiaryMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.UserStatisticsMapper;
import com.studycompanion.service.CheckInService;
import com.studycompanion.vo.CheckInStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInMapper checkInMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final DiaryMapper diaryMapper;
    private final UserStatisticsMapper userStatisticsMapper;

    @Override
    public CheckInStatusVO getTodayCheckInStatus(Long userId) {
        LocalDate today = LocalDate.now();
        return getCheckInStatus(userId, today);
    }

    @Override
    @Transactional
    public CheckInStatusVO checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        CheckInStatusVO status = getCheckInStatus(userId, today);

        if (!status.getCanCheckIn()) {
            return status;
        }

        // 计算今日学习时长
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .eq(StudyRecord::getStudyDate, today);
        List<StudyRecord> todayRecords = studyRecordMapper.selectList(wrapper);
        int totalDuration = todayRecords.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();

        // 获取或创建今日打卡记录
        LambdaQueryWrapper<CheckIn> checkInWrapper = new LambdaQueryWrapper<>();
        checkInWrapper.eq(CheckIn::getUserId, userId)
                      .eq(CheckIn::getCheckDate, today);
        CheckIn checkIn = checkInMapper.selectOne(checkInWrapper);

        if (checkIn == null) {
            checkIn = new CheckIn();
            checkIn.setUserId(userId);
            checkIn.setCheckDate(today);
        }

        checkIn.setTotalDuration(totalDuration);
        checkIn.setIsCompleted(1);

        // 计算连续打卡天数
        int streak = calculateStreak(userId, today);
        checkIn.setStreak(streak);

        if (checkIn.getId() == null) {
            checkInMapper.insert(checkIn);
        } else {
            checkInMapper.updateById(checkIn);
        }

        // 更新用户统计
        updateUserStatistics(userId, today, streak);

        return getCheckInStatus(userId, today);
    }

    @Override
    public List<CheckInStatusVO> getCheckInHistory(Long userId, String month) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
               .ge(CheckIn::getCheckDate, startDate)
               .le(CheckIn::getCheckDate, endDate)
               .orderByAsc(CheckIn::getCheckDate);

        List<CheckIn> checkIns = checkInMapper.selectList(wrapper);
        return checkIns.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CheckInStatusVO getCheckInStatus(Long userId, LocalDate date) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
               .eq(CheckIn::getCheckDate, date);
        CheckIn checkIn = checkInMapper.selectOne(wrapper);

        CheckInStatusVO status = new CheckInStatusVO();
        status.setCheckDate(date);

        if (checkIn != null) {
            status.setIsCompleted(checkIn.getIsCompleted() == 1);
            status.setTotalDuration(checkIn.getTotalDuration());
            status.setStreak(checkIn.getStreak());
        } else {
            status.setIsCompleted(false);
            status.setTotalDuration(0);
            status.setStreak(0);
        }

        // 检查是否可以打卡（今日有学习记录+写日记且未打卡）
        boolean hasStudyRecord = hasStudyRecord(userId, date);
        boolean hasDiary = hasDiary(userId, date);
        status.setCanCheckIn(hasStudyRecord && hasDiary && !Boolean.TRUE.equals(status.getIsCompleted()));

        return status;
    }

    private boolean hasStudyRecord(Long userId, LocalDate date) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .eq(StudyRecord::getStudyDate, date);
        return studyRecordMapper.selectCount(wrapper) > 0;
    }

    private boolean hasDiary(Long userId, LocalDate date) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .eq(Diary::getDiaryDate, date);
        return diaryMapper.selectCount(wrapper) > 0;
    }

    private int calculateStreak(Long userId, LocalDate today) {
        // 优化：一次查询获取最近的打卡记录
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckIn::getUserId, userId)
               .eq(CheckIn::getIsCompleted, 1)
               .le(CheckIn::getCheckDate, today)
               .orderByDesc(CheckIn::getCheckDate)
               .last("LIMIT 366");
        List<CheckIn> checkIns = checkInMapper.selectList(wrapper);
        
        int streak = 0;
        LocalDate expectedDate = today;
        
        for (CheckIn checkIn : checkIns) {
            if (checkIn.getCheckDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (checkIn.getCheckDate().isBefore(expectedDate)) {
                break;
            }
        }
        
        return Math.max(1, streak); // 至少返回1（当天已打卡）
    }

    private void updateUserStatistics(Long userId, LocalDate today, int streak) {
        LambdaQueryWrapper<UserStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStatistics::getUserId, userId);
        UserStatistics stats = userStatisticsMapper.selectOne(wrapper);

        if (stats == null) {
            stats = new UserStatistics();
            stats.setUserId(userId);
            stats.setTotalDays(1);
            stats.setTotalDuration(0);
            stats.setCurrentStreak(streak);
            stats.setMaxStreak(streak);
            stats.setLastCheckDate(today);
            userStatisticsMapper.insert(stats);
        } else {
            stats.setTotalDays(stats.getTotalDays() + 1);
            stats.setCurrentStreak(streak);
            if (streak > stats.getMaxStreak()) {
                stats.setMaxStreak(streak);
            }
            stats.setLastCheckDate(today);
            userStatisticsMapper.updateById(stats);
        }
    }

    private CheckInStatusVO convertToVO(CheckIn checkIn) {
        CheckInStatusVO vo = new CheckInStatusVO();
        vo.setCheckDate(checkIn.getCheckDate());
        vo.setIsCompleted(checkIn.getIsCompleted() == 1);
        vo.setTotalDuration(checkIn.getTotalDuration());
        vo.setStreak(checkIn.getStreak());
        vo.setCanCheckIn(false);
        return vo;
    }
}
