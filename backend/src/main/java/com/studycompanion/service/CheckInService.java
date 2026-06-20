package com.studycompanion.service;

import com.studycompanion.vo.CheckInStatusVO;

import java.util.List;

public interface CheckInService {

    CheckInStatusVO getTodayCheckInStatus(Long userId);

    CheckInStatusVO checkIn(Long userId);

    List<CheckInStatusVO> getCheckInHistory(Long userId, String month);
}
