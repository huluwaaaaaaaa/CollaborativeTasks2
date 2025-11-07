# ============================================
# Gateway服务 Dockerfile
# ============================================

FROM localhost:5000/collabtask-base:1.0

ENV APPID=collabtask-gateway
ENV SERVER_PORT=8001

# 复制JAR包
ADD collabtask-gateway/target/${APPID}.jar /app/app.jar

# 暴露端口
EXPOSE 8001

# 启动命令（环境由运行时环境变量SPRING_PROFILES_ACTIVE决定）
CMD ["sh", "-c", "java -Xms256m -Xmx512m ${JAVA_OPTS} -Dnacos.logging.default.config.enabled=false -jar /app/app.jar"]
