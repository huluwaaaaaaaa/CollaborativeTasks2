#!/bin/bash

# ============================================
# 本地CI/CD模拟脚本
# 模拟GitLab CI/CD的完整流程
# ============================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
NEXUS_REGISTRY=${NEXUS_REGISTRY:-"localhost:5000"}
NEXUS_USERNAME=${NEXUS_USERNAME:-"admin"}
NEXUS_PASSWORD=${NEXUS_PASSWORD:-"123456"}
VERSION=${VERSION:-$(git rev-parse --short HEAD 2>/dev/null || echo "latest")}

# 项目根目录
PROJECT_ROOT="/Users/zgy/Documents/workspace-web3/CollaborativeTasks2"

echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}本地CI/CD流程开始${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""

# ============================================
# 阶段1：编译
# ============================================
echo -e "${BLUE}[阶段1/4] Maven编译...${NC}"
cd "${PROJECT_ROOT}"

echo "==> 清理旧的构建产物..."
mvn clean

echo "==> 开始编译..."
mvn package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Maven编译失败！${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Maven编译完成${NC}"
echo ""

# ============================================
# 阶段2：测试（可选）
# ============================================
echo -e "${BLUE}[阶段2/4] 运行测试...${NC}"
read -p "是否运行单元测试? (y/n): " run_tests

if [ "$run_tests" == "y" ]; then
    echo "==> 运行单元测试..."
    mvn test || echo -e "${YELLOW}⚠️ 部分测试失败（继续）${NC}"
else
    echo "==> 跳过测试"
fi
echo -e "${GREEN}✅ 测试阶段完成${NC}"
echo ""

# ============================================
# 阶段3：构建Docker镜像
# ============================================
echo -e "${BLUE}[阶段3/4] 构建Docker镜像...${NC}"

# 登录Nexus
echo "==> 登录Nexus Docker Registry..."
echo "${NEXUS_PASSWORD}" | docker login ${NEXUS_REGISTRY} -u ${NEXUS_USERNAME} --password-stdin

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 登录Nexus失败！${NC}"
    exit 1
fi

# 构建API镜像
echo "==> 构建API镜像..."
docker build -f collabtask-api/Dockerfile \
    -t ${NEXUS_REGISTRY}/collabtask-api:${VERSION} \
    -t ${NEXUS_REGISTRY}/collabtask-api:latest \
    .

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ API镜像构建失败！${NC}"
    exit 1
fi

# 构建Gateway镜像
echo "==> 构建Gateway镜像..."
docker build -f collabtask-gateway/Dockerfile \
    -t ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION} \
    -t ${NEXUS_REGISTRY}/collabtask-gateway:latest \
    .

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Gateway镜像构建失败！${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker镜像构建完成${NC}"
echo ""

# ============================================
# 阶段4：推送到Nexus
# ============================================
echo -e "${BLUE}[阶段4/4] 推送镜像到Nexus...${NC}"

echo "==> 推送API镜像..."
docker push ${NEXUS_REGISTRY}/collabtask-api:${VERSION}
docker push ${NEXUS_REGISTRY}/collabtask-api:latest

echo "==> 推送Gateway镜像..."
docker push ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION}
docker push ${NEXUS_REGISTRY}/collabtask-gateway:latest

echo -e "${GREEN}✅ 镜像推送完成${NC}"
echo ""

# ============================================
# 部署（可选）
# ============================================
echo -e "${BLUE}[可选] 部署到本地环境...${NC}"
read -p "是否重新部署服务? (y/n): " deploy

if [ "$deploy" == "y" ]; then
    echo "==> 停止旧服务..."
    docker compose -f docker-compose-nexus.yml down
    
    echo "==> 启动新服务..."
    docker compose -f docker-compose-nexus.yml up -d
    
    echo "==> 等待服务启动..."
    sleep 10
    
    echo "==> 检查服务状态..."
    docker compose -f docker-compose-nexus.yml ps
    
    echo ""
    echo "==> 测试服务健康..."
    curl -f http://localhost:8002/collabtask-api/ || echo -e "${YELLOW}⚠️ API服务未就绪${NC}"
    curl -f http://localhost:8001/ || echo -e "${YELLOW}⚠️ Gateway服务未就绪${NC}"
fi

# ============================================
# 完成
# ============================================
echo ""
echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}🎉 CI/CD流程完成！${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""
echo -e "${BLUE}镜像信息：${NC}"
echo "  - ${NEXUS_REGISTRY}/collabtask-api:${VERSION}"
echo "  - ${NEXUS_REGISTRY}/collabtask-api:latest"
echo "  - ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION}"
echo "  - ${NEXUS_REGISTRY}/collabtask-gateway:latest"
echo ""
echo -e "${BLUE}访问地址：${NC}"
echo "  - Gateway: http://localhost:8001"
echo "  - API:     http://localhost:8002"
echo "  - Nexus:   http://localhost:8081"
echo ""

