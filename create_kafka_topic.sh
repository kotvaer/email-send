#!/bin/bash

BROKER="localhost:9092"
TOPIC="email-task"
PARTITIONS=6
REPLICATION=2

KAFKA_TOPICS_CMD="docker exec broker1 /bin/kafka-topics"

# 创建 Topic
echo "🔧 正在创建 Kafka Topic: $TOPIC ..."
$KAFKA_TOPICS_CMD \
  --create \
  --if-not-exists \
  --topic "$TOPIC" \
  --bootstrap-server "$BROKER" \
  --partitions "$PARTITIONS" \
  --replication-factor "$REPLICATION"

sleep 2

# 获取 Topic 描述信息
echo "📋 正在获取 Topic 分区详情..."
DESC_OUTPUT=$($KAFKA_TOPICS_CMD \
  --describe \
  --topic "$TOPIC" \
  --bootstrap-server "$BROKER")

echo "$DESC_OUTPUT"

echo "🔍 正在自动检查每个分区是否健康..."
echo ""

TOTAL=$PARTITIONS
GOOD=0
BROKEN=0

# 使用 while 循环避免管道子进程
while read -r line; do
  if [[ "$line" =~ Partition: ]]; then
    PART=$(echo "$line" | awk -F'Partition: ' '{print $2}' | awk '{print $1}')
    LEADER=$(echo "$line" | awk -F'Leader: ' '{print $2}' | awk '{print $1}')
    REPLICAS=$(echo "$line" | awk -F'Replicas: ' '{print $2}' | awk '{print $1}')
    ISR=$(echo "$line" | awk -F'Isr: ' '{print $2}' | awk '{print $1}')

    REPLICA_COUNT=$(echo "$REPLICAS" | awk -F',' '{print NF}')
    ISR_COUNT=$(echo "$ISR" | awk -F',' '{print NF}')

    echo "🧩 Partition $PART | Leader: $2 | Replicas: $REPLICAS | ISR: $ISR"

    if [[ "$LEADER" == "-1" ]]; then
      echo "❌ 分区 $PART 没有 Leader"
      ((BROKEN++))
    elif [[ "$REPLICA_COUNT" -ne "$REPLICATION" ]]; then
      echo "❌ 分区 $PART 的副本数异常，应为 $REPLICATION"
      ((BROKEN++))
    elif [[ "$ISR_COUNT" -lt "$REPLICATION" ]]; then
      echo "⚠️ 分区 $PART 同步副本数不足，可能有副本滞后"
      ((BROKEN++))
    else
      echo "✅ 分区 $PART 正常"
      ((GOOD++))
    fi

    echo ""
  fi
done <<< "$DESC_OUTPUT"

echo "📊 总结：$GOOD/$TOTAL 分区健康，$BROKEN 异常"

if [[ "$BROKEN" -eq 0 ]]; then
  echo "🎉 Kafka Topic $TOPIC 健康 ✅"
else
  echo "⚠️ Kafka Topic $TOPIC 存在问题 ❌，请检查 Kafka Broker 是否全部运行中"
fi