#!/bin/bash

# 명령 에러 발생 시 종료
set -e

# --- 변수 설정 ---
PROJECT_ROOT="/home/ubuntu/zupzup"
APP_NAME="sejong-zupzup"
DOCKER_COMPOSE_FILE="$PROJECT_ROOT/docker-compose.yml"
UPSTREAM_FILE="/etc/nginx/sites-available/upstream.conf"
DEPLOY_LOG="$PROJECT_ROOT/logs/deploy/deploy.log"

# 배포 중 에러 발생으로 중단 시 로그 기록 함수
on_error() {
  echo "********** [배포 중 에러 발생] : $(date +'%Y-%m-%d %H:%M:%S')" >> $DEPLOY_LOG
  echo "   -> 실패한 명령어: '$BASH_COMMAND'" >> $DEPLOY_LOG
  echo "   -> 위치: ${BASH_SOURCE[0]}:${LINENO}" >> $DEPLOY_LOG
  exit 1
}
trap on_error ERR

# --- 배포 시작 ---
echo "=========== [배포 시작] : $(date +'%Y-%m-%d %H:%M:%S')" >> $DEPLOY_LOG
cd $PROJECT_ROOT

# 1. 현재 활성화된 환경을 확인하여 배포할 타겟 환경을 결정
if sudo grep -q "8080" $UPSTREAM_FILE; then
  CURRENT_ENV="blue"
  TARGET_PORT=8081
  TARGET_ENV="green"
else
  CURRENT_ENV="green"
  TARGET_PORT=8080
  TARGET_ENV="blue"
fi

# 2. 새로운 버전의 애플리케이션을 실행
echo "> 새로운 '$TARGET_ENV'($APP_NAME) 애플리케이션 실행" >> $DEPLOY_LOG
echo "  → Docker Hub에서 최신 이미지를 pull" >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE pull web-$TARGET_ENV
echo "  → '$TARGET_ENV' 컨테이너를 실행" >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE up -d --no-deps web-$TARGET_ENV

# 3. 새 애플리케이션 실행 여부 체크
echo "> '$TARGET_ENV' 컨테이너 Health Check" >> $DEPLOY_LOG
for i in {1..10}; do
  if curl -s --fail http://localhost:$TARGET_PORT > /dev/null; then
    echo "  → $APP_NAME 애플리케이션 실행 성공!" >> $DEPLOY_LOG

    # 4. Nginx 트래픽을 새로운 컨테이너로 전환
    echo "> Nginx 트래픽을 '$TARGET_ENV'(으)로 전환" >> $DEPLOY_LOG
    echo "upstream zupzup_api_servers { server 127.0.0.1:$TARGET_PORT; }" | sudo tee $UPSTREAM_FILE
    sudo systemctl restart nginx
    echo "  → Nginx 재시작 완료" >> $DEPLOY_LOG

    # 5. 기존 애플리케이션을 종료
    echo "> 기존 '$CURRENT_ENV' 애플리케이션을 종료" >> $DEPLOY_LOG
    sudo docker-compose -f $DOCKER_COMPOSE_FILE stop web-$CURRENT_ENV
    sudo docker-compose -f $DOCKER_COMPOSE_FILE rm -f web-$CURRENT_ENV
    sudo docker image prune -af
    echo "   → 종료 완료" >> $DEPLOY_LOG

    # 6. 보안을 위해 환경변수 파일을 삭제
    # rm -f .env

    echo "=========== [배포 완료] : $(date +'%Y-%m-%d %H:%M:%S')" >> $DEPLOY_LOG
    exit 0
  fi
  echo "  → Health Check 대기 중... ($i/12)" >> $DEPLOY_LOG
  sleep 5
done

# 1분 동안 Health Check에 실패한 경우
echo "  → $APP_NAME 애플리케이션 실행 실패" >> $DEPLOY_LOG
echo "  → 배포 롤백을 시작합니다." >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE stop web-$TARGET_ENV
sudo docker-compose -f $DOCKER_COMPOSE_FILE rm -f web-$TARGET_ENV
echo "********** [배포 실패] : $(date +'%Y-%m-%d %H:%M:%S') **********" >> $DEPLOY_LOG
exit 1
