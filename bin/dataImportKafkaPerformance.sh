#!/usr/bin/env bash
spark2-submit \
--class dataImportKafkaPerformance \
--master yarn \
--deploy-mode client \
./sparkstreamingkafkaperformance-1.0-SNAPSHOT.jar \
-cachename yc \
-igniteconfxml /opt/ignite/ignite-config-client.xml \
-brokers datanode1:9092 \
-partitionNum 44 \
-groupid yc \
-hiveTableName default.mm \
-topic yc \
-appName kafkainput