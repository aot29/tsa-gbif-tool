#! /bin/bash
docker compose up -d
sleep 60
curl -v --request DELETE localhost:9000/cleanup
#curl -v --request PUT localhost:9000/markAllUsed
#curl -v --request PUT localhost:9000/matchAll
sleep 60
docker compose down