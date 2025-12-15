# 실행 환경만 있으면 되므로 가벼운 버전 사용
FROM amazoncorretto:21-alpine3.21

# 빌드 시점에 JAR 파일 위치를 변수로 받음
ARG JAR_FILE=build/libs/*.jar

# JAR 파일 복사
COPY ${JAR_FILE} app.jar

# 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]