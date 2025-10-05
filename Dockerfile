FROM openjdk:17

WORKDIR /app

# JAR 파일 메인 디렉토리에 복사
COPY ./build/libs/modura-server-0.0.1-SNAPSHOT.jar build/libs/modura-server-0.0.1-SNAPSHOT.jar

RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

ENV SPRING_PROFILES_ACTIVE=prod

# 시스템 진입점 정의
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]