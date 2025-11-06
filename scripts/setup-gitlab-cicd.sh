#!/bin/bash

# ============================================
# GitLab CI/CD 快速配置脚本
# ============================================

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}GitLab CI/CD 快速配置${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""

# 检查GitLab是否运行
echo -e "${BLUE}[1/6] 检查GitLab状态...${NC}"
if docker ps | grep -q gitlab; then
    echo "✅ GitLab容器运行中"
    GITLAB_CONTAINER=$(docker ps --filter "name=gitlab" --format "{{.Names}}" | head -n 1)
    echo "容器名称: $GITLAB_CONTAINER"
else
    echo "❌ GitLab未运行"
    echo "是否启动GitLab? (y/n)"
    read -r start_gitlab
    if [ "$start_gitlab" == "y" ]; then
        echo "==> 启动GitLab容器..."
        docker run -d \
            --name gitlab \
            --hostname gitlab \
            -p 8084:80 \
            -p 2222:22 \
            -v gitlab-data:/var/opt/gitlab \
            -v gitlab-logs:/var/log/gitlab \
            -v gitlab-config:/etc/gitlab \
            gitlab/gitlab-ce:latest
        
        echo "⏳ 等待GitLab启动（需要2-3分钟）..."
        sleep 120
        GITLAB_CONTAINER="gitlab"
    else
        echo "请先启动GitLab"
        exit 1
    fi
fi
echo ""

# 创建网络
echo -e "${BLUE}[2/6] 配置Docker网络...${NC}"
if docker network ls | grep -q gitlab-network; then
    echo "✅ gitlab-network 已存在"
else
    echo "==> 创建gitlab-network..."
    docker network create gitlab-network
fi

# 连接容器到网络
echo "==> 连接容器到网络..."
docker network connect gitlab-network $GITLAB_CONTAINER 2>/dev/null || echo "GitLab已在网络中"
docker network connect gitlab-network collabtask-nexus 2>/dev/null || echo "Nexus已在网络中"
echo "✅ 网络配置完成"
echo ""

# 启动GitLab Runner
echo -e "${BLUE}[3/6] 配置GitLab Runner...${NC}"
if docker ps | grep -q gitlab-runner; then
    echo "✅ GitLab Runner已运行"
else
    echo "==> 启动GitLab Runner..."
    docker run -d \
        --name gitlab-runner \
        --restart always \
        --network gitlab-network \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v gitlab-runner-config:/etc/gitlab-runner \
        gitlab/gitlab-runner:latest
    
    sleep 5
    echo "✅ GitLab Runner启动完成"
fi
echo ""

# 获取GitLab密码
echo -e "${BLUE}[4/6] 获取GitLab登录信息...${NC}"
echo "==> GitLab访问地址: http://localhost:8084"
echo "==> 用户名: root"
echo -n "==> 密码: "
docker exec $GITLAB_CONTAINER grep 'Password:' /etc/gitlab/initial_root_password 2>/dev/null | awk '{print $2}' || echo "(需在GitLab容器中查看)"
echo ""

# Git分支设置
echo -e "${BLUE}[5/6] Git分支配置...${NC}"
cd /Users/zgy/Documents/workspace-web3/CollaborativeTasks2

if [ ! -d ".git" ]; then
    echo "==> 初始化Git仓库..."
    git init
    git add .
    git commit -m "Initial commit"
fi

# 创建分支
echo "==> 创建分支结构..."
git checkout -b main 2>/dev/null || git checkout main
git checkout -b develop 2>/dev/null || git checkout develop
git checkout -b test 2>/dev/null || git checkout test

echo "✅ 分支创建完成"
echo "   - main (生产)"
echo "   - develop (开发)"
echo "   - test (测试 - 自动部署)"
echo ""

# 下一步指引
echo -e "${BLUE}[6/6] 完成配置...${NC}"
echo ""
echo -e "${GREEN}===========================================================${NC}"
echo -e "${GREEN}✅ 基础配置完成！${NC}"
echo -e "${GREEN}===========================================================${NC}"
echo ""
echo -e "${YELLOW}📋 接下来的步骤：${NC}"
echo ""
echo "1. 访问GitLab并登录"
echo "   http://localhost:8084"
echo ""
echo "2. 创建新项目: CollaborativeTasks2"
echo ""
echo "3. 获取项目URL和Registration Token"
echo "   Settings → CI/CD → Runners"
echo ""
echo "4. 注册GitLab Runner："
echo "   docker exec -it gitlab-runner gitlab-runner register \\"
echo "     --non-interactive \\"
echo "     --url \"http://gitlab\" \\"
echo "     --registration-token \"YOUR_TOKEN\" \\"
echo "     --executor \"docker\" \\"
echo "     --docker-image docker:24 \\"
echo "     --description \"docker-runner\" \\"
echo "     --tag-list \"docker,build\" \\"
echo "     --docker-volumes \"/var/run/docker.sock:/var/run/docker.sock\" \\"
echo "     --docker-network-mode \"gitlab-network\""
echo ""
echo "5. 配置环境变量（在GitLab项目中）"
echo "   Settings → CI/CD → Variables"
echo "   - NEXUS_REGISTRY = host.docker.internal:5000"
echo "   - NEXUS_USERNAME = admin"
echo "   - NEXUS_PASSWORD = 123456"
echo "   - NEXUS_MAVEN_REPO = http://host.docker.internal:8081/repository/maven-public/"
echo ""
echo "6. 推送代码到GitLab："
echo "   git remote add gitlab http://localhost:8084/root/CollaborativeTasks2.git"
echo "   git push -u gitlab --all"
echo ""
echo "7. 测试CI/CD流程："
echo "   git checkout develop"
echo "   echo '# Test' >> README.md"
echo "   git commit -am 'test: CI/CD'"
echo "   git push gitlab develop"
echo "   git checkout test"
echo "   git merge develop"
echo "   git push gitlab test"
echo "   # 查看Pipeline: http://localhost:8084/root/CollaborativeTasks2/-/pipelines"
echo ""
echo -e "${GREEN}详细文档: docs/GitLab-CI-CD完整配置.md${NC}"
echo ""

