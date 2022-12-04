# 백그라운드 실행, 강제 재생성
db-up:
	docker-compose up -d --force-recreate

# 볼륨 삭제
db-down:
	docker-compose down -v
