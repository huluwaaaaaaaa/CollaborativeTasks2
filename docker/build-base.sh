#!/bin/bash

# ============================================
# 构建基础镜像脚本
# ============================================

set -e

REGISTRY=${NEXUS_REGISTRY:-"localhost:5000"}
IMAGE_NAME="collabtask-base"
VERSION="1.0"
NEXUS_USERNAME=${NEXUS_USERNAME:-"admin"}
NEXUS_PASSWORD=${NEXUS_PASSWORD:-"123456"}

echo "=========================================="
echo "构建CollaborativeTasks基础镜像"
echo "=========================================="
echo ""
echo "镜像名称: ${IMAGE_NAME}:${VERSION}"
echo "Registry: ${REGISTRY}"
echo ""

# 构建基础镜像
echo "==> 构建基础镜像..."
cd "$(dirname "$0")"
docker build -t ${IMAGE_NAME}:${VERSION} -t ${IMAGE_NAME}:latest -f base/Dockerfile .

echo ""
echo "✅ 基础镜像构建完成"
echo ""

# 是否推送到Nexus
read -p "是否推送到Nexus私服？(y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "==> 登录Nexus (${REGISTRY})..."
    echo "使用默认凭证: ${NEXUS_USERNAME}/******"
    
    echo ${NEXUS_PASSWORD} | docker login ${REGISTRY} -u ${NEXUS_USERNAME} --password-stdin
    
    echo "==> 打标签..."
    docker tag ${IMAGE_NAME}:${VERSION} ${REGISTRY}/${IMAGE_NAME}:${VERSION}
    docker tag ${IMAGE_NAME}:latest ${REGISTRY}/${IMAGE_NAME}:latest
    
    echo "==> 推送到Nexus..."
    docker push ${REGISTRY}/${IMAGE_NAME}:${VERSION}
    docker push ${REGISTRY}/${IMAGE_NAME}:latest
    
    echo ""
    echo "✅ 推送完成"
    echo "镜像地址: ${REGISTRY}/${IMAGE_NAME}:${VERSION}"
fi

echo ""
echo "=========================================="
echo "基础镜像信息"
echo "=========================================="
docker images | grep ${IMAGE_NAME}
echo ""
echo "使用方式："
echo "  FROM ${IMAGE_NAME}:${VERSION}"
echo ""

