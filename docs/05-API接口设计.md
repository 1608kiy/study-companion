# 智学伴 - API接口设计

## 一、接口规范

### 1.1 基础规范
- **协议**：HTTP/HTTPS
- **数据格式**：JSON
- **字符编码**：UTF-8
- **认证方式**：JWT Token
- **API版本**：/api/v1/

### 1.2 请求头
```
Content-Type: application/json
Authorization: Bearer {token}
```

### 1.3 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.4 错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 二、用户模块接口

### 2.1 用户注册
```
POST /api/v1/auth/register
```

**请求参数**：
```json
{
  "email": "user@example.com",
  "password": "123456",
  "nickname": "昵称"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "token": "jwt_token"
  }
}
```

### 2.2 用户登录
```
POST /api/v1/auth/login
```

**请求参数**：
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "昵称",
    "token": "jwt_token"
  }
}
```

### 2.3 获取用户信息
```
GET /api/v1/user/profile
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "昵称",
    "avatar": "头像URL",
    "dailyGoal": 120,
    "weeklyGoal": 840,
    "monthlyGoal": 3600,
    "darkMode": false,
    "notificationEnabled": true
  }
}
```

### 2.4 更新用户信息
```
PUT /api/v1/user/profile
```

**请求参数**：
```json
{
  "nickname": "新昵称",
  "avatar": "新头像URL",
  "dailyGoal": 180,
  "weeklyGoal": 1260,
  "monthlyGoal": 5400,
  "darkMode": true,
  "notificationEnabled": true
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 2.5 注销账号
```
DELETE /api/v1/user/delete
```

**响应**：
```json
{
  "code": 200,
  "message": "注销成功",
  "data": null
}
```

---

## 三、科目模块接口

### 3.1 获取科目列表
```
GET /api/v1/subjects
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "行测",
      "color": "#409EFF",
      "icon": "icon-xingce",
      "sortOrder": 1,
      "isPreset": true
    },
    {
      "id": 2,
      "name": "申论",
      "color": "#67C23A",
      "icon": "icon-shenlun",
      "sortOrder": 2,
      "isPreset": true
    }
  ]
}
```

### 3.2 获取预设科目
```
GET /api/v1/subjects/preset
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "name": "行测",
      "color": "#409EFF",
      "icon": "icon-xingce"
    },
    {
      "name": "申论",
      "color": "#67C23A",
      "icon": "icon-shenlun"
    },
    {
      "name": "公共基础知识",
      "color": "#E6A23C",
      "icon": "icon-gonggong"
    }
  ]
}
```

### 3.3 创建科目
```
POST /api/v1/subjects
```

**请求参数**：
```json
{
  "name": "数量关系",
  "color": "#F56C6C",
  "icon": "icon-shuliang"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "name": "数量关系",
    "color": "#F56C6C",
    "icon": "icon-shuliang",
    "sortOrder": 3,
    "isPreset": false
  }
}
```

### 3.4 更新科目
```
PUT /api/v1/subjects/{id}
```

**请求参数**：
```json
{
  "name": "新名称",
  "color": "#FFFFFF",
  "icon": "新图标",
  "sortOrder": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 3.5 删除科目
```
DELETE /api/v1/subjects/{id}
```

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 四、番茄钟模块接口

### 4.1 开始计时
```
POST /api/v1/timer/start
```

**请求参数**：
```json
{
  "subjectId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "开始计时",
  "data": {
    "recordId": 1,
    "startTime": "2024-01-01T08:00:00",
    "subjectId": 1,
    "subjectName": "行测"
  }
}
```

### 4.2 暂停计时
```
POST /api/v1/timer/pause
```

**请求参数**：
```json
{
  "recordId": 1,
  "reason": "喝水"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "暂停成功",
  "data": null
}
```

### 4.3 恢复计时
```
POST /api/v1/timer/resume
```

**请求参数**：
```json
{
  "recordId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "恢复成功",
  "data": null
}
```

### 4.4 停止计时
```
POST /api/v1/timer/stop
```

**请求参数**：
```json
{
  "recordId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "停止成功",
  "data": {
    "recordId": 1,
    "duration": 45,
    "startTime": "2024-01-01T08:00:00",
    "endTime": "2024-01-01T08:45:00",
    "subjectId": 1,
    "subjectName": "行测"
  }
}
```

### 4.5 获取计时状态
```
GET /api/v1/timer/status
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isRunning": true,
    "recordId": 1,
    "startTime": "2024-01-01T08:00:00",
    "elapsedSeconds": 1800,
    "subjectId": 1,
    "subjectName": "行测"
  }
}
```

---

## 五、学习记录模块接口

### 5.1 获取学习记录列表
```
GET /api/v1/records
```

**查询参数**：
- `subjectId`：科目ID（可选）
- `startDate`：开始日期（可选）
- `endDate`：结束日期（可选）
- `page`：页码（默认1）
- `pageSize`：每页数量（默认20）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "subjectId": 1,
        "subjectName": "行测",
        "subjectColor": "#409EFF",
        "studyDate": "2024-01-01",
        "startTime": "08:00",
        "endTime": "08:45",
        "duration": 45,
        "remark": "今天学习了数量关系",
        "mood": "开心",
        "focusLevel": 4,
        "aiFocusLevel": 4
      }
    ]
  }
}
```

### 5.2 获取学习记录详情
```
GET /api/v1/records/{id}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "subjectId": 1,
    "subjectName": "行测",
    "subjectColor": "#409EFF",
    "studyDate": "2024-01-01",
    "startTime": "08:00",
    "endTime": "08:45",
    "duration": 45,
    "remark": "今天学习了数量关系",
    "mood": "开心",
    "focusLevel": 4,
    "aiFocusLevel": 4,
    "interruptionCount": 0,
    "interruptionReason": null
  }
}
```

### 5.3 创建学习记录
```
POST /api/v1/records
```

**请求参数**：
```json
{
  "subjectId": 1,
  "studyDate": "2024-01-01",
  "startTime": "08:00",
  "endTime": "08:45",
  "duration": 45,
  "mood": "开心",
  "focusLevel": 4
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

### 5.4 更新学习记录（AI判断）
```
PUT /api/v1/records/{id}
```

**请求参数**：
```json
{
  "reason": "记录有误",
  "mood": "一般"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "AI判断允许修改",
  "data": {
    "allowed": true
  }
}
```

### 5.5 删除学习记录（AI判断）
```
DELETE /api/v1/records/{id}
```

**请求参数**：
```json
{
  "reason": "重复记录"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "AI判断不允许删除",
  "data": {
    "allowed": false,
    "reason": "学习记录不能删除，保证数据真实性"
  }
}
```

### 5.6 获取学习统计
```
GET /api/v1/records/stats
```

**查询参数**：
- `startDate`：开始日期
- `endDate`：结束日期

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDuration": 1200,
    "totalDays": 30,
    "avgDailyDuration": 40,
    "subjectStats": [
      {
        "subjectId": 1,
        "subjectName": "行测",
        "duration": 600,
        "percentage": 50
      },
      {
        "subjectId": 2,
        "subjectName": "申论",
        "duration": 600,
        "percentage": 50
      }
    ]
  }
}
```

---

## 六、打卡模块接口

### 6.1 获取今日打卡状态
```
GET /api/v1/checkin/today
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isCompleted": true,
    "totalDuration": 180,
    "streak": 7,
    "hasDiary": true
  }
}
```

### 6.2 打卡
```
POST /api/v1/checkin
```

**响应**：
```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "streak": 7,
    "totalDuration": 180
  }
}
```

### 6.3 获取连续打卡天数
```
GET /api/v1/checkin/streak
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "currentStreak": 7,
    "maxStreak": 30,
    "totalDays": 100
  }
}
```

### 6.4 获取打卡历史
```
GET /api/v1/checkin/history
```

**查询参数**：
- `month`：月份（格式：2024-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2024-01-01",
      "isCompleted": true,
      "totalDuration": 180
    },
    {
      "date": "2024-01-02",
      "isCompleted": false,
      "totalDuration": 0
    }
  ]
}
```

---

## 七、断签模块接口

### 7.1 记录断签原因
```
POST /api/v1/miss/record
```

**请求参数**：
```json
{
  "missDate": "2024-01-02",
  "reason": "生病了"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "记录成功",
  "data": {
    "id": 1
  }
}
```

### 7.2 AI判断是否允许补签
```
POST /api/v1/miss/ai-judge
```

**请求参数**：
```json
{
  "missDate": "2024-01-02",
  "reason": "生病了"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "allowed": true,
    "reason": "生病属于不可抗力，允许补签"
  }
}
```

### 7.3 补签
```
POST /api/v1/miss/replenish
```

**请求参数**：
```json
{
  "missDate": "2024-01-02"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "补签成功",
  "data": {
    "streak": 7
  }
}
```

---

## 八、目标模块接口

### 8.1 获取目标列表
```
GET /api/v1/goals
```

**查询参数**：
- `goalType`：目标类型（1每日 2每周 3每月）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "goalType": 1,
      "goalValue": 120,
      "currentValue": 90,
      "goalDate": "2024-01-01",
      "isCompleted": false,
      "aiSuggested": false
    }
  ]
}
```

### 8.2 创建目标
```
POST /api/v1/goals
```

**请求参数**：
```json
{
  "goalType": 1,
  "goalValue": 180,
  "goalDate": "2024-01-01"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

### 8.3 更新目标
```
PUT /api/v1/goals/{id}
```

**请求参数**：
```json
{
  "goalValue": 200,
  "currentValue": 150
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

### 8.4 删除目标
```
DELETE /api/v1/goals/{id}
```

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 8.5 AI推荐目标
```
POST /api/v1/goals/ai-suggest
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyGoal": 180,
    "weeklyGoal": 1260,
    "monthlyGoal": 5400,
    "reason": "根据你最近一周的学习数据，建议每天学习3小时"
  }
}
```

---

## 九、日记模块接口

### 9.1 获取日记列表
```
GET /api/v1/diaries
```

**查询参数**：
- `month`：月份（格式：2024-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "diaryDate": "2024-01-01",
      "summary": "今天学习了行测",
      "hasImages": true,
      "aiGenerated": false
    }
  ]
}
```

### 9.2 获取指定日期日记
```
GET /api/v1/diaries/{date}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "diaryDate": "2024-01-01",
    "content": "# 今日日记\n\n今天学习了行测...",
    "summary": "今天学习了行测",
    "plan": "明天继续学习",
    "reflection": "感觉不错",
    "problems": "数量关系有点难",
    "aiGenerated": false,
    "aiGenerateCount": 0,
    "images": [
      {
        "id": 1,
        "imageUrl": "/uploads/diary/1/1.jpg",
        "imageSize": 1024
      }
    ],
    "studyRecords": [
      {
        "id": 1,
        "subjectName": "行测",
        "duration": 45
      }
    ]
  }
}
```

### 9.3 创建日记
```
POST /api/v1/diaries
```

**请求参数**：
```json
{
  "diaryDate": "2024-01-01",
  "content": "# 今日日记\n\n今天学习了行测...",
  "summary": "今天学习了行测",
  "plan": "明天继续学习",
  "reflection": "感觉不错",
  "problems": "数量关系有点难"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

### 9.4 上传日记图片
```
POST /api/v1/diaries/{id}/images
```

**请求**：
- Content-Type: multipart/form-data
- 参数：file（图片文件）

**响应**：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "imageUrl": "/uploads/diary/1/1.jpg",
    "imageSize": 1024
  }
}
```

### 9.5 删除日记图片
```
DELETE /api/v1/diaries/{id}/images/{imageId}
```

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 9.7 AI生成日记
```
POST /api/v1/diaries/ai-generate
```

**请求参数**：
```json
{
  "diaryDate": "2024-01-01",
  "keywords": "行测,数量关系,有点难",
  "length": "medium"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "content": "# 今日日记\n\n今天主要学习了行测的数量关系部分...",
    "summary": "学习行测数量关系",
    "plan": "明天继续复习数量关系",
    "reflection": "感觉有点难度，需要多练习",
    "problems": "排列组合不太理解"
  }
}
```

### 9.8 重新生成日记
```
POST /api/v1/diaries/ai-regenerate
```

**请求参数**：
```json
{
  "diaryDate": "2024-01-01",
  "keywords": "行测,图形推理",
  "length": "long"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "content": "# 今日日记\n\n今天学习了行测的图形推理部分...",
    "aiGenerateCount": 2
  }
}
```

---

## 十、统计模块接口

### 10.1 获取每日统计
```
GET /api/v1/stats/daily
```

**查询参数**：
- `date`：日期（格式：2024-01-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "date": "2024-01-01",
    "totalDuration": 180,
    "subjectStats": [
      {
        "subjectId": 1,
        "subjectName": "行测",
        "duration": 90,
        "percentage": 50
      },
      {
        "subjectId": 2,
        "subjectName": "申论",
        "duration": 90,
        "percentage": 50
      }
    ]
  }
}
```

### 10.2 获取每周统计
```
GET /api/v1/stats/weekly
```

**查询参数**：
- `startDate`：开始日期（格式：2024-01-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "weekStart": "2024-01-01",
    "weekEnd": "2024-01-07",
    "totalDuration": 1200,
    "dailyStats": [
      {
        "date": "2024-01-01",
        "duration": 180
      },
      {
        "date": "2024-01-02",
        "duration": 120
      }
    ],
    "subjectStats": [
      {
        "subjectId": 1,
        "subjectName": "行测",
        "duration": 600,
        "percentage": 50
      }
    ]
  }
}
```

### 10.3 获取每月统计
```
GET /api/v1/stats/monthly
```

**查询参数**：
- `month`：月份（格式：2024-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "month": "2024-01",
    "totalDuration": 5400,
    "totalDays": 30,
    "avgDailyDuration": 180,
    "subjectStats": [
      {
        "subjectId": 1,
        "subjectName": "行测",
        "duration": 2700,
        "percentage": 50
      }
    ]
  }
}
```

### 10.4 获取科目统计
```
GET /api/v1/stats/subject
```

**查询参数**：
- `startDate`：开始日期
- `endDate`：结束日期

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "subjectId": 1,
      "subjectName": "行测",
      "duration": 2700,
      "percentage": 50,
      "avgDailyDuration": 90
    },
    {
      "subjectId": 2,
      "subjectName": "申论",
      "duration": 2700,
      "percentage": 50,
      "avgDailyDuration": 90
    }
  ]
}
```

### 10.5 获取月历数据
```
GET /api/v1/stats/calendar
```

**查询参数**：
- `month`：月份（格式：2024-01）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2024-01-01",
      "isStudied": true,
      "duration": 180,
      "isCheckedIn": true
    },
    {
      "date": "2024-01-02",
      "isStudied": false,
      "duration": 0,
      "isCheckedIn": false
    }
  ]
}
```

---

## 十一、AI模块接口

### 11.1 获取AI建议
```
GET /api/v1/ai/advice
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "advice": "你最近一周学习时长稳定，建议适当增加学习强度",
    "analysis": "本周平均每天学习2小时，比上周增加10%",
    "suggestions": [
      "建议增加申论的练习时间",
      "数量关系需要加强"
    ]
  }
}
```

### 11.2 获取周报
```
GET /api/v1/ai/report/weekly
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "report": "# 本周学习报告\n\n## 学习概况\n本周共学习12小时...",
    "totalDuration": 720,
    "totalDays": 6,
    "avgDailyDuration": 120,
    "highlights": [
      "连续学习6天",
      "行测学习时间最长"
    ],
    "issues": [
      "周三没有学习",
      "申论学习时间较少"
    ],
    "suggestions": [
      "建议保持学习连续性",
      "增加申论练习"
    ]
  }
}
```

### 11.3 获取月报
```
GET /api/v1/ai/report/monthly
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "report": "# 本月学习报告\n\n## 学习概况\n本月共学习54小时...",
    "totalDuration": 3240,
    "totalDays": 28,
    "avgDailyDuration": 116,
    "highlights": [
      "学习天数达标",
      "学习时长稳定"
    ],
    "issues": [
      "周末学习时间较少",
      "申论进步空间大"
    ],
    "suggestions": [
      "建议周末也保持学习",
      "重点突破申论"
    ]
  }
}
```

### 11.4 AI问答
```
POST /api/v1/ai/chat
```

**请求参数**：
```json
{
  "question": "我这周学了多少"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "answer": "你本周共学习了12小时，平均每天2小时。其中行测6小时，申论6小时。",
    "data": {
      "totalDuration": 720,
      "subjectStats": [
        {
          "subjectName": "行测",
          "duration": 360
        },
        {
          "subjectName": "申论",
          "duration": 360
        }
      ]
    }
  }
}
```

### 11.5 AI判断专注度
```
POST /api/v1/ai/focus
```

**请求参数**：
```json
{
  "recordId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "focusLevel": 4,
    "reason": "中断次数较少，专注度较高"
  }
}
```

---

## 十二、分享模块接口

### 12.1 生成分享图片
```
POST /api/v1/share/image
```

**请求参数**：
```json
{
  "date": "2024-01-01"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "imageUrl": "/uploads/share/2024-01-01.png"
  }
}
```

---

**下一步**：阅读 [06-前端设计.md](./06-前端设计.md) 了解前端设计
