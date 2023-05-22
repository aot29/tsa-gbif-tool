#! /bin/bash
curl -v --request DELETE localhost:9000/cleanup
curl -v --request PUT localhost:9000/markAllUsed
curl -v --request PUT localhost:9000/matchAll
