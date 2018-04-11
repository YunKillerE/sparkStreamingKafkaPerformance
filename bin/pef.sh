#!/usr/bin/env bash

## 1，正常运行，采用默认参数

    spark2-submit \
    --executor-memory 8G --executor-cores 7 --num-executors 10 \
    --class dataImportKafkaPerformance \
    --master yarn \
    --deploy-mode client \
    ./sparkstreamingkafkaperformance-1.0-SNAPSHOT.jar \
    -cachename yc \
    -igniteconfxml /opt/ignite/ignite-config-client.xml \
    -brokers datanode1:9092 \
    -partitionNum 3 \
    -groupid yc \
    -hiveTableName default.mm \
    -topic yc \
    -appName kafkainput


## 2，增加参数并发以及executor优化

    spark2-submit \
    --executor-memory 8G --executor-cores 7 --num-executors 10 \
    --conf spark.default.parallelism=1000 \
    --conf spark.storage.memoryFraction=0.5 \
    --conf spark.shuffle.memoryFraction=0.3 \
    --class dataImportKafkaPerformance \
    --master yarn \
    --deploy-mode client \
    ./sparkstreamingkafkaperformance-1.0-SNAPSHOT.jar \
    -cachename yc \
    -igniteconfxml /opt/ignite/ignite-config-client.xml \
    -brokers datanode1:9092 \
    -partitionNum 3 \
    -groupid yc \
    -hiveTableName default.mm \
    -topic yc \
    -appName kafkainput

## 3.增加GC的优化

    spark2-submit \
    --executor-memory 8G --executor-cores 7 --num-executors 10 \
    --conf spark.default.parallelism=1000 \
    --conf spark.storage.memoryFraction=0.5 \
    --conf spark.shuffle.memoryFraction=0.3 \
    --conf spark.executor.extraJavaOptions="-XX:MaxGCPauseMillis=100 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=2 -XX:+UseG1GC "
    --class dataImportKafkaPerformance \
    --master yarn \
    --deploy-mode client \
    ./sparkstreamingkafkaperformance-1.0-SNAPSHOT.jar \
    -cachename yc \
    -igniteconfxml /opt/ignite/ignite-config-client.xml \
    -brokers datanode1:9092 \
    -partitionNum 3 \
    -groupid yc \
    -hiveTableName default.mm \
    -topic yc \
    -appName kafkainput
