# ============================================
# CollaborativeTasks2 项目管理 Makefile
# ============================================

.PHONY: help build test docker-build docker-up docker-down clean ci-cd

# 配置
NEXUS_REGISTRY ?= localhost:5000
VERSION ?= $(shell git rev-parse --short HEAD 2>/dev/null || echo "latest")

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
	@echo "  make ci-cd          - 完整CI/CD流程"
	@echo "  make ci-cd-local    - 本地模拟CI/CD"
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
	@docker push $(NEXUS_REGISTRY)/collabtask-api:$(VERSION)
	@docker push $(NEXUS_REGISTRY)/collabtask-api:latest
	@docker push $(NEXUS_REGISTRY)/collabtask-gateway:$(VERSION)
	@docker push $(NEXUS_REGISTRY)/collabtask-gateway:latest
	@echo "✅ 镜像推送完成"

deploy-local:
	@echo "==> 部署到本地..."
	@docker-compose -f docker-compose-nexus.yml down
	@docker-compose -f docker-compose-nexus.yml up -d
	@echo "✅ 本地部署完成"
	@echo "Gateway: http://localhost:8001"
	@echo "API:     http://localhost:8002"

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

