#!/bin/bash

# ============================================
# 本地部署脚本
# 用于本地GitLab CI/CD或手动部署
# ============================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置（可以通过环境变量覆盖）
NEXUS_REGISTRY=${NEXUS_REGISTRY:-"localhost:5000"}
NEXUS_USERNAME=${NEXUS_USERNAME:-"admin"}
NEXUS_PASSWORD=${NEXUS_PASSWORD:-"123456"}
VERSION=${VERSION:-$(git rev-parse --short HEAD)}

echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}CollaborativeTasks2 本地部署脚本${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""
echo "Nexus Registry: ${NEXUS_REGISTRY}"
echo "Version: ${VERSION}"
echo ""

# 步骤1：Maven编译
echo -e "${YELLOW}==> 步骤1: Maven编译...${NC}"
mvn clean package -DskipTests
echo -e "${GREEN}✅ Maven编译完成${NC}"
echo ""

# 步骤2：构建Docker镜像
echo -e "${YELLOW}==> 步骤2: 构建Docker镜像...${NC}"
docker build -f collabtask-api/Dockerfile -t ${NEXUS_REGISTRY}/collabtask-api:${VERSION} -t ${NEXUS_REGISTRY}/collabtask-api:latest .
docker build -f collabtask-gateway/Dockerfile -t ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION} -t ${NEXUS_REGISTRY}/collabtask-gateway:latest .
echo -e "${GREEN}✅ Docker镜像构建完成${NC}"
echo ""

# 步骤3：登录Nexus Docker Registry
echo -e "${YELLOW}==> 步骤3: 登录Nexus Docker Registry...${NC}"
echo ${NEXUS_PASSWORD} | docker login ${NEXUS_REGISTRY} -u ${NEXUS_USERNAME} --password-stdin
echo -e "${GREEN}✅ 登录成功${NC}"
echo ""

# 步骤4：推送镜像到Nexus
echo -e "${YELLOW}==> 步骤4: 推送镜像到Nexus...${NC}"
docker push ${NEXUS_REGISTRY}/collabtask-api:${VERSION}
docker push ${NEXUS_REGISTRY}/collabtask-api:latest
docker push ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION}
docker push ${NEXUS_REGISTRY}/collabtask-gateway:latest
echo -e "${GREEN}✅ 镜像推送完成${NC}"
echo ""

# 步骤5：部署服务
echo -e "${YELLOW}==> 步骤5: 部署服务...${NC}"

# 设置环境变量
export NEXUS_REGISTRY=${NEXUS_REGISTRY}
export IMAGE_TAG=${VERSION}

# 停止旧服务
echo "停止旧服务..."
docker-compose -f docker-compose-nexus.yml down

# 启动新服务
echo "启动新服务..."
docker-compose -f docker-compose-nexus.yml up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 健康检查
echo "健康检查..."
if curl -f http://localhost:8001/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Gateway健康检查通过${NC}"
else
    echo -e "${RED}❌ Gateway健康检查失败${NC}"
    exit 1
fi

if curl -f http://localhost:8002/collabtask-api/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ API健康检查通过${NC}"
else
    echo -e "${RED}❌ API健康检查失败${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}🎉 部署成功！${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""
echo "服务地址："
echo "  - Nacos控制台: http://localhost:8848/nacos"
echo "  - Gateway: http://localhost:8001"
echo "  - API: http://localhost:8002/collabtask-api"
echo ""
echo "镜像版本："
echo "  - API: ${NEXUS_REGISTRY}/collabtask-api:${VERSION}"
echo "  - Gateway: ${NEXUS_REGISTRY}/collabtask-gateway:${VERSION}"
echo ""
echo "查看日志: docker-compose -f docker-compose-nexus.yml logs -f"
echo "停止服务: docker-compose -f docker-compose-nexus.yml down"
echo ""

