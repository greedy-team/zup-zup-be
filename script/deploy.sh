#!/bin/bash

# 명령 실행 중 에러가 발생하면 즉시 스크립트를 종료합니다.
set -e

# --- 변수 설정 ---
PROJECT_ROOT="/home/ubuntu/zupzup"
APP_NAME="sejong-zupzup"
DOCKER_COMPOSE_FILE="docker-compose-prod.yml"
UPSTREAM_CONFIG_FILE="/etc/nginx/sites-available/upstream.conf"
DEPLOY_LOG="$PROJECT_ROOT/logs/deploy/deploy.log"

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

# 1. 현재 Nginx가 바라보는 포트 번호를 확인하여 타겟 환경을 결정
if sudo grep -q "proxy_pass http://127.0.0.1:8080" $NGINX_CONFIG_FILE; then
  CURRENT_ENV="blue"
  TARGET_PORT=8081
  TARGET_ENV="green"
else
  CURRENT_ENV="green"
  TARGET_PORT=8080
  TARGET_ENV="blue"
fi

# 2. 새로운 버전의 애플리케이션을 실행
echo "> 새로운 '$TARGET_ENV'($APP_NAME) 애플리케이션 실행 (Port: $TARGET_PORT)" >> $DEPLOY_LOG
echo "  → Docker Hub에서 최신 이미지를 pull" >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE pull "web-$TARGET_ENV"
echo "  → '$TARGET_ENV' 컨테이너를 실행" >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE up -d --no-deps "web-$TARGET_ENV"

# 3. 새 애플리케이션이 완전히 실행될 때까지 Health Check를 수행
echo "> '$TARGET_ENV' 컨테이너 Health Check" >> $DEPLOY_LOG
for i in {1..12}; do
  # curl 명령으로 새 컨테이너가 응답하는지 확인합니다.
  if curl -s --fail http://localhost:$TARGET_PORT > /dev/null; then
    echo "  → $APP_NAME 애플리케이션 실행 성공!" >> $DEPLOY_LOG

    # 4. Nginx 트래픽을 새로운 컨테이너로 안전하게 전환
    echo "> Nginx 트래픽을 '$TARGET_ENV'(으)로 전환" >> $DEPLOY_LOG
    sudo sed -i "s/127.0.0.1:[0-9]\{4\}/127.0.0.1:$TARGET_PORT/g" $NGINX_CONFIG_FILE

    # Nginx 설정에 문법 오류가 없는지 테스트한 후, 재시작하여 변경사항을 적용
    sudo nginx -t && sudo systemctl restart nginx
    echo "  → Nginx 재시작 완료" >> $DEPLOY_LOG

    # 5. 기존에 실행되던 구버전 애플리케이션을 종료
    echo "> 기존 '$CURRENT_ENV' 애플리케이션을 종료" >> $DEPLOY_LOG
    sudo docker-compose -f $DOCKER_COMPOSE_FILE stop "web-$CURRENT_ENV"
    sudo docker-compose -f $DOCKER_COMPOSE_FILE rm -f "web-$CURRENT_ENV"
    # 사용하지 않는 도커 이미지를 정리하여 용량을 확보
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

# Health Check가 최종적으로 실패한 경우, 롤백을 시작
echo "  → $APP_NAME 애플리케이션 실행 실패" >> $DEPLOY_LOG
echo "  → 실패한 '$TARGET_ENV' 컨테이너의 마지막 로그 50줄을 출력" >> $DEPLOY_LOG
sudo docker logs --tail 50 "zupzup-$TARGET_ENV" >> $DEPLOY_LOG 2>&1

echo "  → 배포 롤백을 시작합니다." >> $DEPLOY_LOG
sudo docker-compose -f $DOCKER_COMPOSE_FILE stop "web-$TARGET_ENV"
sudo docker-compose -f $DOCKER_COMPOSE_FILE rm -f "web-$TARGET_ENV"
echo "********** [배포 실패] : $(date +'%Y-%m-%d %H:%M:%S') **********" >> $DEPLOY_LOG
exit 1
