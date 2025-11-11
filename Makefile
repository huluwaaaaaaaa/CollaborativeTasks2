# ============================================
# CollaborativeTasks2 项目管理 Makefile
# ============================================

.PHONY: help build test docker-build docker-up docker-down clean ci-cd

# 配置
NEXUS_REGISTRY ?= localhost:5000
# 每次发版生成唯一版本号（时间戳）
VERSION := v$(shell date +%Y%m%d-%H%M%S)
# 环境配置（可通过命令行指定：make release ENV=prod）
ENV ?= test

# 默认目标
help:
	@echo "CollaborativeTasks2 项目管理命令"
	@echo ""
	@echo "开发命令："
	@echo "  make build          - Maven编译项目"
	@echo "  make test           - 运行单元测试"
	@echo "  make run-api        - 启动API服务"
	@echo "  make run-gateway    - 启动Gateway服务"
	@echo ""
	@echo "Docker命令："
	@echo "  make docker-build   - 构建Docker镜像"
	@echo "  make docker-push    - 推送镜像到Nexus"
	@echo "  make docker-up      - 启动所有Docker服务"
	@echo "  make docker-down    - 停止所有Docker服务"
	@echo "  make docker-logs    - 查看Docker日志"
	@echo "  make docker-restart - 重启Docker服务"
	@echo ""
	@echo "CI/CD命令："
	@echo "  make release-test   - 🌟 发版到测试环境（推荐）"
	@echo "  make release-prod   - 发版到生产环境"
	@echo "  make release-dev    - 发版到开发环境"
	@echo "  make release ENV=xxx - 发版到指定环境"
	@echo "  make ci-cd          - 完整CI/CD流程"
	@echo "  make deploy-local   - 部署到本地"
	@echo ""
	@echo "清理命令："
	@echo "  make clean          - 清理编译产物"
	@echo "  make clean-all      - 清理所有（包括Docker卷）"

# Maven编译
build:
	@echo "==> 编译项目..."
	mvn clean package -DskipTests

# 运行测试
test:
	@echo "==> 运行单元测试..."
	mvn test

# 启动API服务
run-api:
	@echo "==> 启动API服务..."
	cd collabtask-api && mvn spring-boot:run

# 启动Gateway服务
run-gateway:
	@echo "==> 启动Gateway服务..."
	cd collabtask-gateway && mvn spring-boot:run

# 构建Docker镜像
docker-build:
	@echo "==> 构建Docker镜像..."
	docker-compose build

# 启动所有Docker服务
docker-up:
	@echo "==> 启动Docker服务..."
	docker-compose up -d
	@echo "==> 等待服务启动..."
	@sleep 10
	@echo "==> 服务状态："
	@docker-compose ps

# 停止所有Docker服务
docker-down:
	@echo "==> 停止Docker服务..."
	docker-compose down

# 查看Docker日志
docker-logs:
	docker-compose logs -f --tail=100

# 重启Docker服务
docker-restart:
	@echo "==> 重启Docker服务..."
	docker-compose restart
	@docker-compose ps

# 清理编译产物
clean:
	@echo "==> 清理编译产物..."
	mvn clean

# 完全清理
clean-all: clean docker-down
	@echo "==> 删除Docker卷..."
	docker-compose down -v
	@echo "==> 清理完成"

# 查看服务状态
status:
	@echo "==> Docker服务状态："
	@docker-compose ps
	@echo ""
	@echo "==> Nacos控制台: http://localhost:8848/nacos"
	@echo "==> Gateway服务: http://localhost:8001"
	@echo "==> API服务: http://localhost:8002/collabtask-api"

# 初始化环境
init:
	@echo "==> 初始化开发环境..."
	@echo "==> 1. 检查Docker..."
	@docker --version
	@echo "==> 2. 检查Docker Compose..."
	@docker-compose --version
	@echo "==> 3. 检查Maven..."
	@mvn --version
	@echo "==> 4. 检查JDK..."
	@java -version
	@echo "==> 环境检查完成！"

# CI/CD相关命令
ci-cd: build docker-build docker-push deploy-local
	@echo ""
	@echo "=========================================="
	@echo "✅ CI/CD流程完成！"
	@echo "=========================================="

ci-cd-local:
	@bash scripts/ci-cd-local.sh

docker-push:
	@echo "==> 推送镜像到Nexus..."
	@echo "123456" | docker login $(NEXUS_REGISTRY) -u admin --password-stdin
	@docker push $(NEXUS_REGISTRY)/collabtask-api:latest
	@docker push $(NEXUS_REGISTRY)/collabtask-gateway:latest
	@echo "✅ 镜像推送完成"

deploy-local:
	@echo "==> 部署到本地..."
	@docker compose -f docker-compose-nexus.yml down
	@docker compose -f docker-compose-nexus.yml up -d
	@echo "✅ 本地部署完成"
	@echo "Gateway: http://localhost:8001"
	@echo "API:     http://localhost:8002"

# ============================================
# 一键发版（任何分支都可用，完全自动）
# ============================================
release:
	@echo "=========================================="
	@echo "🚀 一键发版"
	@echo "当前分支: $$(git rev-parse --abbrev-ref HEAD)"
	@echo "目标环境: $(ENV)"
	@echo "新版本号: $(VERSION)"
	@echo "=========================================="
	@echo ""
	@echo "[1/5] Maven编译..."
	@mvn clean package -DskipTests -q
	@echo "[2/5] 构建镜像（环境: $(ENV)）..."
	@docker build -f collabtask-api/Dockerfile -t $(NEXUS_REGISTRY)/collabtask-api:$(VERSION) . -q
	@docker build -f collabtask-gateway/Dockerfile -t $(NEXUS_REGISTRY)/collabtask-gateway:$(VERSION) . -q
	@echo "[3/5] 推送到Nexus..."
	@echo "123456" | docker login $(NEXUS_REGISTRY) -u admin --password-stdin > /dev/null 2>&1
	@docker push $(NEXUS_REGISTRY)/collabtask-api:$(VERSION) -q
	@docker push $(NEXUS_REGISTRY)/collabtask-gateway:$(VERSION) -q
	@echo "[4/5] 部署到$(ENV)环境..."
	@IMAGE_TAG=$(VERSION) DEPLOY_ENV=$(ENV) docker compose -f docker-compose-nexus.yml up -d --quiet-pull
	@echo ""
	@echo "=========================================="
	@echo "✅ 发版完成！"
	@echo ""
	@echo "🎯 环境: $(ENV)"
	@echo "📦 版本: $(VERSION)"
	@echo "🌐 访问: http://localhost:8001"
	@echo "=========================================="

# CI专用：构建、推送并自动部署
release-ci:
	@echo "=========================================="
	@echo "🚀 CI自动化部署"
	@echo "当前分支: $$(git rev-parse --abbrev-ref HEAD)"
	@echo "目标环境: $(ENV)"
	@echo "新版本号: $(VERSION)"
	@echo "=========================================="
	@echo ""
	@echo "[1/5] Maven编译（跳过测试）..."
	@mvn clean package -Dmaven.test.skip=true
	@echo "[2/5] 构建镜像（环境: $(ENV)）..."
	@docker build -f collabtask-api/Dockerfile -t $(NEXUS_REGISTRY)/collabtask-api:$(VERSION) . -q
	@docker build -f collabtask-gateway/Dockerfile -t $(NEXUS_REGISTRY)/collabtask-gateway:$(VERSION) . -q
	@echo "[3/5] 推送到Nexus..."
	@echo "123456" | docker login $(NEXUS_REGISTRY) -u admin --password-stdin > /dev/null 2>&1
	@docker push $(NEXUS_REGISTRY)/collabtask-api:$(VERSION) -q
	@docker push $(NEXUS_REGISTRY)/collabtask-gateway:$(VERSION) -q
	@echo "[4/5] 停止旧服务..."
	@docker stop collabtask-api collabtask-gateway 2>/dev/null || true
	@docker rm -f collabtask-api collabtask-gateway 2>/dev/null || true
	@echo "[5/5] 部署新服务..."
	@IMAGE_TAG=$(VERSION) DEPLOY_ENV=$(ENV) NEXUS_REGISTRY=$(NEXUS_REGISTRY) docker compose -f docker-compose-nexus.yml up -d
	@echo ""
	@echo "等待服务启动..."
	@sleep 10
	@echo ""
	@echo "=========================================="
	@echo "✅ CI自动部署完成！"
	@echo "=========================================="
	@echo ""
	@echo "📦 版本: $(VERSION)"
	@echo "🎯 环境: $(ENV)"
	@echo "🌐 访问: http://localhost:8001"
	@echo ""
	@echo "🔍 服务状态:"
	@IMAGE_TAG=$(VERSION) DEPLOY_ENV=$(ENV) docker compose -f docker-compose-nexus.yml ps
	@echo ""
	@echo "📊 查看日志:"
	@echo "   docker logs -f collabtask-gateway"
	@echo "   docker logs -f collabtask-api"
	@echo "=========================================="

# 快捷命令：发版到测试环境
release-test:
	@$(MAKE) release ENV=test

# 快捷命令：发版到生产环境
release-prod:
	@$(MAKE) release ENV=prod

# 快捷命令：发版到开发环境
release-dev:
	@$(MAKE) release ENV=dev

# 强制发版（先停止所有服务）
release-force:
	@echo "=========================================="
	@echo "🚀 强制发版（清理端口占用）"
	@echo "=========================================="
	@echo "停止所有服务..."
	@docker compose -f docker-compose-nexus.yml down 2>/dev/null || true
	@docker stop collabtask-api collabtask-gateway 2>/dev/null || true
	@docker rm collabtask-api collabtask-gateway 2>/dev/null || true
	@sleep 2
	@echo ""
	@$(MAKE) release ENV=$(ENV)

# 强制发版到测试环境
release-test-force:
	@$(MAKE) release-force ENV=test

# 强制发版到生产环境
release-prod-force:
	@$(MAKE) release-force ENV=prod

# 快速开始
quick-start: docker-up
	@echo ""
	@echo "=========================================="
	@echo "✅ 服务已启动！"
	@echo "=========================================="
	@echo "Nacos控制台: http://localhost:8848/nacos"
	@echo "  账号/密码: nacos/nacos"
	@echo ""
	@echo "Gateway网关: http://localhost:8001"
	@echo "API服务: http://localhost:8002/collabtask-api"
	@echo ""
	@echo "查看日志: make docker-logs"
	@echo "停止服务: make docker-down"
	@echo "=========================================="

