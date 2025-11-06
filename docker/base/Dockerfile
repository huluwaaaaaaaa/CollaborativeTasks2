# ============================================
# CollaborativeTasks 基础镜像
# 基于 Eclipse Temurin OpenJDK 17 (官方OpenJDK发行版)
# 支持 ARM64 (Apple Silicon) 和 AMD64 架构
# ============================================

FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="collabtask-team"
LABEL description="CollaborativeTasks基础运行环境"
LABEL version="1.0"
LABEL java.version="17"

# 设置环境变量
ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8 \
    TZ=Asia/Shanghai

# 安装基础软件
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        wget \
        curl \
        vim \
        net-tools \
        telnet \
        ca-certificates \
        tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 验证Java安装
RUN java -version

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

# 创建应用目录
RUN mkdir -p /app /app/logs && \
    chmod -R 755 /app

# 设置工作目录
WORKDIR /app

# JVM默认参数（应用镜像可以覆盖）
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Duser.timezone=Asia/Shanghai \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/"

# 健康检查脚本
RUN echo '#!/bin/bash\ncurl -f http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1' > /app/health-check.sh && \
    chmod +x /app/health-check.sh

# 默认命令（会被应用镜像覆盖）
CMD ["echo", "This is a base image. Please use application-specific images."]
