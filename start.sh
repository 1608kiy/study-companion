#!/bin/bash
echo "========================================="
echo "  智学伴 - AI学习陪伴平台  "
echo "========================================="
echo ""

# 检查 MySQL
echo "[1/3] 等待 MySQL 启动..."
until mysqladmin ping -h localhost -u root -proot --silent 2>/dev/null; do
  sleep 2
done
echo "  ✅ MySQL 已就绪"

# 初始化数据库
echo "[2/3] 初始化数据库..."
mysql -h localhost -u root -proot < backend/sql/init.sql 2>/dev/null || true
echo "  ✅ 数据库已初始化"

# 启动后端
echo "[3/3] 启动后端服务..."
cd backend
mvn spring-boot:run -q &
BACKEND_PID=$!
cd ..

echo ""
echo "========================================="
echo "  服务已启动!"
echo "========================================="
echo ""
echo "  后端 API: http://localhost:8080"
echo "  前端页面: http://localhost:3000"
echo ""
echo "  前端启动命令:"
echo "    cd frontend && npm run dev"
echo ""
echo "  按 Ctrl+C 停止所有服务"
echo "========================================="

wait $BACKEND_PID
