#!/bin/bash

# 1. 변수 설정
REPOSITORY=/home/ubuntu/event-notification
PROJECT_NAME=board # build.gradle.kts에 있는 rootProject.name
JAR_PATH=$REPOSITORY/build/libs/*.jar
CURRENT_PID=$(pgrep -fl $PROJECT_NAME | grep jar | awk '{print $1}')

# 2. 기존 프로세스 종료
if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

# 3. 새 애플리케이션 배포
echo "> 새 애플리케이션 배포"
JAR_NAME=$(ls -tr $JAR_PATH | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
# 로컬 프로파일 대신 운영(prod) 프로파일로 실행하도록 세팅
nohup java -jar -Dspring.profiles.active=prod $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &