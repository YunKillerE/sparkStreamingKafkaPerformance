# 数据导入kafka的性能测试

数据集：331.3G

数据行数： 

## 1，正常运行，采用默认参数

    spark2-submit \
    --executor-memory 8G --executor-cores 4 --num-executors 60 \
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
    
|类别|值|
|:---|:---|
|总时间|10.7分钟   |

## 2，增加参数并发以及executor优化

    spark2-submit \
    --executor-memory 8G --executor-cores 4 --num-executors 60 \
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
    -partitionNum 44 \
    -groupid yc \
    -hiveTableName default.mm \
    -topic yc \
    -appName kafkainput

|类别|值|
|:---|:---|
|总时间|12.7分钟   |


## 3，减少参数，优化executor的数量

    spark2-submit \
    --executor-memory 20G --executor-cores 5 --num-executors 20 \
    --conf spark.storage.memoryFraction=0.7 \
    --conf spark.shuffle.memoryFraction=0.1 \
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

|类别|值|
|:---|:---|
|总时间|13.6分钟   |


## 4.增加GC的优化

    spark2-submit \
    --executor-memory 8G --executor-cores 4 --num-executors 60 \
    --conf spark.default.parallelism=480 \
    --conf spark.executor.extraJavaOptions="-XX:MaxGCPauseMillis=100 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=2 -XX:+UseG1GC "
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

|类别|值|
|:---|:---|
|总时间|11.9分钟   |

好奇该，怎么调参数都没法加快时间，感觉是写入kafka的时候太慢了，想办法优化，增大写入线程









