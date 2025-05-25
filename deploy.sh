#!/bin/bash

SERVER_USER=gamja
SERVER_IP=211.208.163.16
SERVER_PATH=/home/gamja/gamjadan/app
JAR_NAME=gamja-1.0.0.jar
DEPLOY_SCRIPT=deploy.sh

# 1. jar 존재 확인
if [ ! -f "$JAR_NAME" ]; then
  echo "❌ $JAR_NAME 파일이 없습니다. 먼저 빌드하세요."
  exit 1
fi

# 2. 서버로 jar 전송
echo "📦 $JAR_NAME 파일 서버로 전송 중..."
scp $JAR_NAME $SERVER_USER@$SERVER_IP:$SERVER_PATH/

# 3. 서버에서 deploy.sh 실행 (nohup + 백그라운드)
echo "🚀 서버에서 배포 스크립트 실행 중..."
ssh $SERVER_USER@$SERVER_IP "cd $SERVER_PATH && nohup ./$DEPLOY_SCRIPT > deploy.log 2>&1 &"

echo "✅ 로컬 → 서버 자동 배포 완료!"