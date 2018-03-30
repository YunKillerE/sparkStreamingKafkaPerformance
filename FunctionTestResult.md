# 数据导入

## 环境准备

hive表：

            > show create table mm;
        OK
        CREATE TABLE `mm`(
          `jioyrq` string, 
          `jioysj` string, 
          `guiyls` string, 
          `cpznxh` string, 
          `jiaoym` string, 
          `jiedbz` string, 
          `jio1je` string, 
          `kemucc` string, 
          `kehuzh` string, 
          `kehhao` string, 
          `zhyodm` string, 
          `hmjsjc` string, 
          `huobdh` string)
        ROW FORMAT SERDE 
          'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe' 
        WITH SERDEPROPERTIES ( 
          'field.delim'=',', 
          'serialization.encoding'='GBK') 
        STORED AS INPUTFORMAT 
          'org.apache.hadoop.mapred.TextInputFormat' 
        OUTPUTFORMAT 
          'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
        LOCATION
          'hdfs://namenode1:8020/user/hive/warehouse/mm'
        TBLPROPERTIES (
          'COLUMN_STATS_ACCURATE'='false', 
          'last_modified_by'='root', 
          'last_modified_time'='1521495928', 
          'numFiles'='1', 
          'numRows'='-1', 
          'rawDataSize'='-1', 
          'totalSize'='1010', 
          'transient_lastDdlTime'='1521495928')
        Time taken: 0.198 seconds, Fetched: 34 row(s)

topic：

    kafka-topics --describe --topic yc --zookeeper datanode1

    Topic:yc	PartitionCount:3	ReplicationFactor:1	Configs:
    	Topic: yc	Partition: 0	Leader: 133	Replicas: 133	Isr: 133
    	Topic: yc	Partition: 1	Leader: 131	Replicas: 131	Isr: 131
    	Topic: yc	Partition: 2	Leader: 132	Replicas: 132	Isr: 132

## 执行命令：

这里简单的测试就不指定资源了，生产环境一定要指定

    spark2-submit \
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


## hive表中数据及表结构：

    hive> select * from mm;
    OK
    20180201	115655	200000010000001	5	7983	1	10000.00 	S	62259910005001	11000001	其他代码	11/10/2018	01
    20180201	115656	200000010000002	5	7983	1	10000.00 	S	62259910005002	11000002	其他代码	11/11/2018	02
    20180201	115657	200000010000003	5	7983	1	10000.00 	S	62259910005003	11000003	其他代码	11/12/2018	03
    20180201	115658	200000010000004	5	7983	1	10000.00 	S	62259910005004	11000001	其他代码	11/13/2018	04
    20180201	115659	200000010000005	5	7983	1	10000.00 	S	62259910005005	11000002	其他代码	11/14/2018	05
    20180201	115660	200000010000006	5	7983	1	10000.00 	S	62259910005006	11000003	其他代码	11/15/2018	06
    20180201	115661	200000010000007	5	7983	1	10000.00 	S	62259910005007	11000001	其他代码	11/16/2018	07
    20180201	115662	200000010000008	5	7983	1	10000.00 	S	62259910005008	11000002	其他代码	11/17/2018	08
    20180201	115663	200000010000009	5	7983	1	10000.00 	S	62259910005009	11000003	其他代码	11/18/2018	09
    Time taken: 1.571 seconds, Fetched: 9 row(s)

## 执行结果：

kafka-console-consumer --topic yc --bootstrap-server datanode1:9092 --partition 0

    18/03/20 08:25:48 INFO utils.AppInfoParser: Kafka version : 0.10.2-kafka-2.2.0
    18/03/20 08:25:48 INFO utils.AppInfoParser: Kafka commitId : unknown
    eventRow(20180201,115655,200000010000001,5,7983,1,10000.00 ,S,62259910005001,11000001,其他代码,11/10/2018,01)
    eventRow(20180201,115658,200000010000004,5,7983,1,10000.00 ,S,62259910005004,11000001,其他代码,11/13/2018,04)
    eventRow(20180201,115661,200000010000007,5,7983,1,10000.00 ,S,62259910005007,11000001,其他代码,11/16/2018,07)
    
kafka-console-consumer --topic yc --bootstrap-server datanode1:9092 --partition 1

    18/03/20 08:25:54 INFO utils.AppInfoParser: Kafka version : 0.10.2-kafka-2.2.0
    18/03/20 08:25:54 INFO utils.AppInfoParser: Kafka commitId : unknown
    eventRow(20180201,115656,200000010000002,5,7983,1,10000.00 ,S,62259910005002,11000002,其他代码,11/11/2018,02)
    eventRow(20180201,115659,200000010000005,5,7983,1,10000.00 ,S,62259910005005,11000002,其他代码,11/14/2018,05)
    eventRow(20180201,115662,200000010000008,5,7983,1,10000.00 ,S,62259910005008,11000002,其他代码,11/17/2018,08)

kafka-console-consumer --topic yc --bootstrap-server datanode1:9092 --partition 2

    18/03/20 08:25:59 INFO utils.AppInfoParser: Kafka version : 0.10.2-kafka-2.2.0
    18/03/20 08:25:59 INFO utils.AppInfoParser: Kafka commitId : unknown
    eventRow(20180201,115657,200000010000003,5,7983,1,10000.00 ,S,62259910005003,11000003,其他代码,11/12/2018,03)
    eventRow(20180201,115660,200000010000006,5,7983,1,10000.00 ,S,62259910005006,11000003,其他代码,11/15/2018,06)
    eventRow(20180201,115663,200000010000009,5,7983,1,10000.00 ,S,62259910005009,11000003,其他代码,11/18/2018,09)


# 流式计算

## 执行命令

    spark2-submit \
    --class streamingKafkaToIgnitePerformance \
    --master yarn \
    --deploy-mode client \
    ./sparkstreamingkafkaperformance-1.0-SNAPSHOT.jar \
    -cachename yc \
    -igniteconfxml /opt/ignite/config/default-config.xml \
    -brokers datanode1:9092 \
    -partitionNum 3 \
    -groupid yc \
    -hiveTableName default.mm \
    -topic yc \
    -appName streamingToIgnite


## ignite写入结果

    注意：这里只显示出了部分列，应该是ignite的问题，可以直接指定列区查询，就能显示所有

    0: jdbc:ignite:thin://datanode2/> select * from yc;
    +--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+---------+
    |             JIOYRQ             |             JIOYSJ             |             GUIYLS             |             CPZNXH             |             JIAOYM             |             JIEDBZ             |         |
    +--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+---------+
    | 20180201                       | 115655                         | 200000010000001                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115662                         | 200000010000008                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115661                         | 200000010000007                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115660                         | 200000010000006                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115658                         | 200000010000004                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115657                         | 200000010000003                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115656                         | 200000010000002                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115663                         | 200000010000009                | 1                              | 7983                           | 1                              | 10000.0 |
    | 20180201                       | 115659                         | 200000010000005                | 1                              | 7983                           | 1                              | 10000.0 |
    +--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+--------------------------------+---------+



















