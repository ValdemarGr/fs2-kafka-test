#!/bin/bash
[ "$(docker ps -a | grep redpanda)" ] && docker stop redpanda
docker run -d --name redpanda --rm -p 9092:9092 -p 9644:9644 docker.vectorized.io/vectorized/redpanda:latest redpanda start --overprovisioned --smp 1 --memory 1G --reserve-memory 0M --node-id 0 --check=false
until nc -vz 127.0.0.1 9092; do
  sleep 1
done
docker exec redpanda rpk topic create test-topic -p 1
for i in `seq 10`
do
  echo "$i" | docker exec -i redpanda rpk topic produce test-topic -k $i 
done
