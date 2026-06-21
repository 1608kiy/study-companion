#!/bin/bash
# 跨端测试脚本 - 模拟 Web 和小程序共用后端
# 测试所有 API 端点的数据同步

BASE_URL="http://localhost:8080/api/v1"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "=========================================="
echo "智学伴 - 跨端数据同步测试"
echo "=========================================="
echo ""

# 测试函数
test_api() {
    local desc="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local token="$5"
    
    local headers="-H 'Content-Type: application/json'"
    if [ -n "$token" ]; then
        headers="$headers -H 'Authorization: Bearer $token'"
    fi
    
    if [ "$method" = "GET" ]; then
        result=$(curl -s $headers "$BASE_URL$endpoint" 2>/dev/null)
    else
        result=$(curl -s -X $method $headers -d "$data" "$BASE_URL$endpoint" 2>/dev/null)
    fi
    
    if echo "$result" | grep -q '"code":200'; then
        echo -e "${GREEN}✓${NC} $desc"
        echo "$result"
        return 0
    else
        echo -e "${RED}✗${NC} $desc"
        echo "$result"
        return 1
    fi
}

# 1. 注册用户（Web端）
echo -e "${YELLOW}=== 1. 用户注册（Web端）===${NC}"
WEB_RESULT=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"web_user@test.com","password":"123456","nickname":"Web用户"}' 2>/dev/null)
WEB_TOKEN=$(echo "$WEB_RESULT" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Web Token: ${WEB_TOKEN:0:20}..."

# 2. 注册用户（小程序端）
echo ""
echo -e "${YELLOW}=== 2. 用户注册（小程序端）===${NC}"
MINI_RESULT=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"mini_user@test.com","password":"123456","nickname":"小程序用户"}' 2>/dev/null)
MINI_TOKEN=$(echo "$MINI_RESULT" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Mini Token: ${MINI_TOKEN:0:20}..."

# 3. 测试用户信息同步
echo ""
echo -e "${YELLOW}=== 3. 用户信息同步测试 ===${NC}"
echo "Web端获取用户信息:"
curl -s "$BASE_URL/user/profile" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  昵称: {d[\"data\"][\"nickname\"]}')" 2>/dev/null

echo "小程序端获取用户信息:"
curl -s "$BASE_URL/user/profile" -H "Authorization: Bearer $MINI_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  昵称: {d[\"data\"][\"nickname\"]}')" 2>/dev/null

# 4. 测试科目数据同步
echo ""
echo -e "${YELLOW}=== 4. 科目数据同步测试 ===${NC}"
echo "Web端获取科目列表:"
curl -s "$BASE_URL/subjects" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  科目数量: {len(d[\"data\"])}')" 2>/dev/null

echo "小程序端获取科目列表:"
curl -s "$BASE_URL/subjects" -H "Authorization: Bearer $MINI_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  科目数量: {len(d[\"data\"])}')" 2>/dev/null

# 5. 测试学习记录同步
echo ""
echo -e "${YELLOW}=== 5. 学习记录同步测试 ===${NC}"
echo "Web端开始计时:"
START_RESULT=$(curl -s -X POST "$BASE_URL/study-records/timer/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $WEB_TOKEN" \
  -d '{"subjectId":1}' 2>/dev/null)
echo "  $START_RESULT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  状态: {d[\"data\"][\"isRunning\"]}')" 2>/dev/null

echo "小程序端获取计时状态:"
curl -s "$BASE_URL/study-records/timer/state" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  运行中: {d[\"data\"][\"isRunning\"]}')" 2>/dev/null

# 6. 测试AI对话历史同步
echo ""
echo -e "${YELLOW}=== 6. AI对话历史同步测试 ===${NC}"
echo "Web端发送AI消息:"
curl -s -X POST "$BASE_URL/ai/chat" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $WEB_TOKEN" \
  -d '{"question":"我今天学了多少？"}' 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  AI回复: {d[\"data\"][\"answer\"][:50]}...')" 2>/dev/null

echo "小程序端获取聊天历史:"
curl -s "$BASE_URL/ai/chat/history?limit=5" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  历史消息数: {len(d[\"data\"])}')" 2>/dev/null

# 7. 测试打卡数据同步
echo ""
echo -e "${YELLOW}=== 7. 打卡数据同步测试 ===${NC}"
echo "Web端获取打卡状态:"
curl -s "$BASE_URL/check-in/today" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  已打卡: {d[\"data\"][\"isCompleted\"]}')" 2>/dev/null

# 8. 测试日记数据同步
echo ""
echo -e "${YELLOW}=== 8. 日记数据同步测试 ===${NC}"
echo "Web端创建日记:"
DIARY_RESULT=$(curl -s -X POST "$BASE_URL/diaries" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $WEB_TOKEN" \
  -d '{"content":"今天学习了Java","summary":"学习Java基础"}' 2>/dev/null)
echo "  $DIARY_RESULT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  状态: {d[\"message\"]}')" 2>/dev/null

echo "小程序端获取日记:"
curl -s "$BASE_URL/diaries" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  日记数量: {len(d[\"data\"])}')" 2>/dev/null

# 9. 测试目标数据同步
echo ""
echo -e "${YELLOW}=== 9. 目标数据同步测试 ===${NC}"
echo "Web端创建目标:"
curl -s -X POST "$BASE_URL/goals" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $WEB_TOKEN" \
  -d '{"goalType":1,"goalValue":120}' 2>/dev/null | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  状态: {d[\"message\"]}')" 2>/dev/null

echo "小程序端获取目标:"
curl -s "$BASE_URL/goals" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  目标数量: {len(d[\"data\"])}')" 2>/dev/null

# 10. 测试统计数据同步
echo ""
echo -e "${YELLOW}=== 10. 统计数据同步测试 ===${NC}"
echo "Web端获取统计:"
curl -s "$BASE_URL/study-records/stats" -H "Authorization: Bearer $WEB_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  总天数: {d[\"data\"][\"totalDays\"]}')" 2>/dev/null

echo "小程序端获取统计:"
curl -s "$BASE_URL/study-records/stats" -H "Authorization: Bearer $MINI_TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'  总天数: {d[\"data\"][\"totalDays\"]}')" 2>/dev/null

echo ""
echo "=========================================="
echo "跨端测试完成"
echo "=========================================="
