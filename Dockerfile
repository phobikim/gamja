# 1. Gradle 빌드에 필요한 JDK 이미지
FROM gradle:8.5-jdk17-alpine AS builder

# 2. 프로젝트 복사 & 빌드
WORKDIR /app
COPY . .
RUN gradle build -x test

# 3. 실행용 JDK 경량 이미지
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# 4. 위에서 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 5. 포트 설정 (Render가 자동으로 넘김)
EXPOSE 8080

# 6. 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
