#!/bin/bash

# 명령 에러 발생 시 종료
set -e

PROJECT_ROOT="/home/ubuntu/zupzup"
APP_NAME="sejong-zupzup"

APP_LOG_DIR="/var/log/zupzup"
APP_ERROR_LOG="$APP_LOG_DIR/jvm_error.log"
DEPLOY_LOG="$APP_LOG_DIR/deploy.log"

# 배포 중 에러 발생으로 중단 시 로그 기록 함수
on_error() {
  echo "********** [배포 중 에러 발생] : $(date +%Y-%m-%d\ %H:%M:%S) **********" >> $DEPLOY_LOG
  echo "   -> 실패한 명령어: '$BASH_COMMAND'" >> $DEPLOY_LOG
  echo "   -> 위치: ${BASH_SOURCE[1]}:${LINENO[1]}" >> $DEPLOY_LOG
  exit 1
}
trap on_error ERR

echo "=========== [배포 시작] : $(date +%Y-%m-%d\ %H:%M:%S) ===========" >> $DEPLOY_LOG

cd $PROJECT_ROOT


# 1. jar 파일 선택
JAR_FILE=$PROJECT_ROOT/build/libs/*.jar


# 2. 실행 중인 sejong-zupzup 애플리케이션이 PID 조회
CURRENT_PID=$(pgrep -f "$APP_NAME" || true)


# 3. 실행 중인 sejong-zupzup 애플리케이션이 있으면 종료
echo "> 실행 중인 sejong-zupzup 애플리케이션이 있다면 종료 " >> $DEPLOY_LOG
if [ -z "$CURRENT_PID" ]; then
  echo "  → 현재 실행 중인 애플리케이션이 없습니다." >> $DEPLOY_LOG
else
  echo "  → 실행 중인 애플리케이션 종료 (PID: $CURRENT_PID)" >> $DEPLOY_LOG
  kill -15 $CURRENT_PID

  for _ in {1..10}
  do
    if ! ps -p $CURRENT_PID > /dev/null 2>&1; then
      echo "   → 종료 완료" >> $DEPLOY_LOG
      break
    fi
    sleep 1
  done

  if ps -p $CURRENT_PID > /dev/null 2>&1; then
    echo "   → 정상 종료 실패, 강제 종료 시도." >> $DEPLOY_LOG
    kill -9 $CURRENT_PID
    sleep 2
  fi
fi


# 4. 새 애플리케이션 백그라운드 실행
echo "> 새로운 sejong-zupzup 애플리케이션 실행" >> $DEPLOY_LOG
nohup java \
    -Dspring.profiles.active=prod \
    -DDB_HOST="$DB_HOST" \
    -DDB_USERNAME="$DB_USERNAME" \
    -DDB_PASSWORD="$DB_PASSWORD" \
    -DAWS_S3_ACCESS_KEY="$AWS_S3_ACCESS_KEY" \
    -DAWS_S3_SECRET_ACCESS_KEY="$AWS_S3_SECRET_ACCESS_KEY" \
    -DAWS_S3_BUCKET_NAME="$AWS_S3_BUCKET_NAME" \
    -DACCESS_TOKEN_SECRET_KEY="$ACCESS_TOKEN_SECRET_KEY" \
    -DACCESS_TOKEN_EXPIRATION="$ACCESS_TOKEN_EXPIRATION" \
    -DSTUDENT_VERIFICATION_SESSION_TIME="$STUDENT_VERIFICATION_SESSION_TIME" \
    -jar $JAR_FILE > /dev/nul 2> $APP_ERROR_LOG &


# 5. 애플리케이션 실행 여부 체크
NEW_PID=$(pgrep -f "$APP_NAME")
if [ -n "$NEW_PID" ]; then
  echo "  → sejong-zupzup 애플리케이션 실행 성공 (PID: $NEW_PID)" >> $DEPLOY_LOG
else
  echo "  → sejong-zupzup 애플리케이션 실행 실패" >> $DEPLOY_LOG
  exit 1
fi

echo "=========== [배포 완료] : $(date +%Y-%m-%d\ %H:%M:%S) ===========" >> $DEPLOY_LOG
