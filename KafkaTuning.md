# producer tuning

Most important configurations which needs to be taken care at Producer side are：

**1. Compression**

**2. Batch size**

**3. Sync or Async**

主要是如下参数：
    
    batch.size： 基于大小的batching策略
    linger.ms： 基于时间的batching策略
    compression.type：压缩的速度上lz4=snappy<gzip。
    max.in.flight.requests.per.connection (affects ordering，设置为1可以保证有序性，但是发送性能会受影响。不为1的时候，如果发生消息重发则会乱序)
    acks (affects durability)

通常为了保证有序和消息不丢会设置： 

    max.in.flight.requests.per.connection=1
    acks=all

# kafka tuning

## 参考linkedin的生产配置

      # Replication configurations
      num.replica.fetchers=4
      replica.fetch.max.bytes=1048576
      replica.fetch.wait.max.ms=500
      replica.high.watermark.checkpoint.interval.ms=5000
      replica.socket.timeout.ms=30000
      replica.socket.receive.buffer.bytes=65536
      replica.lag.time.max.ms=10000
    
      controller.socket.timeout.ms=30000
      controller.message.queue.size=10
    
      # Log configuration
      num.partitions=8
      message.max.bytes=1000000
      auto.create.topics.enable=true
      log.index.interval.bytes=4096
      log.index.size.max.bytes=10485760
      log.retention.hours=168
      log.flush.interval.ms=10000
      log.flush.interval.messages=20000
      log.flush.scheduler.interval.ms=2000
      log.roll.hours=168
      log.retention.check.interval.ms=300000
      log.segment.bytes=1073741824
    
      # ZK configuration
      zookeeper.connection.timeout.ms=6000
      zookeeper.sync.time.ms=2000
    
      # Socket server configuration
      num.io.threads=8
      num.network.threads=8
      socket.request.max.bytes=104857600
      socket.receive.buffer.bytes=1048576
      socket.send.buffer.bytes=1048576
      queued.max.requests=16
      fetch.purgatory.purge.interval.requests=100
      producer.purgatory.purge.interval.requests=100

## JVM的优化

    调整KAFKA_HEAP_OPTS="-Xmx16G -Xms16G”的值
    
## 网络和ios操作线程配置优化：

    # broker处理消息的最大线程数
    num.network.threads=9
    # broker处理磁盘IO的线程数
    num.io.threads=16

推荐配置：

num.network.threads主要处理网络io，读写缓冲区数据，基本没有io等待，配置线程数量为cpu核数加1。

num.io.threads主要进行磁盘io操作，高峰期可能有些io等待，因此配置需要大些。配置线程数量为cpu核数2倍，最大不超过3倍。

## socket server可接受数据大小(防止OOM异常)：

    socket.request.max.bytes=2147483600

推荐配置：

根据自己业务数据包的大小适当调大。这里取值是int类型的，而受限于java int类型的取值范围又不能太大：

java int的取值范围为（-2147483648~2147483647），占用4个字节（-2的31次方到2的31次方-1，不能超出，超出之后报错：org.apache.kafka.common.config.ConfigException: Invalid value 8589934592 for configuration socket.request.max.bytes: Not a number of type INT。


## log数据文件刷盘策略
	
    # 每当producer写入10000条消息时，刷数据到磁盘
    log.flush.interval.messages=10000
    # 每间隔1秒钟时间，刷数据到磁盘
    log.flush.interval.ms=1000

推荐配置：

为了大幅度提高producer写入吞吐量，需要定期批量写文件。一般无需改动，如果topic的数据量较小可以考虑减少log.flush.interval.ms和log.flush.interval.messages来强制刷写数据，减少可能由于缓存数据未写盘带来的不一致。推荐配置分别message 10000，间隔1s。

## 日志保留策略配置
	
    # 日志保留时长
    log.retention.hours=72
    # 段文件配置
    log.segment.bytes=1073741824

推荐配置：

日志建议保留三天，也可以更短；段文件配置1GB，有利于快速回收磁盘空间，重启kafka加载也会加快（kafka启动时是单线程扫描目录(log.dir)下所有数据文件）。如果文件过小，则文件数量比较多。


## replica复制配置

    num.replica.fetchers=3
    replica.fetch.min.bytes=1
    replica.fetch.max.bytes=5242880

推荐配置：

每个follow从leader拉取消息进行同步数据，follow同步性能由这几个参数决定，分别为:

拉取线程数(num.replica.fetchers):fetcher配置多可以提高follower的I/O并发度，单位时间内leader持有更多请求，相应负载会增大，需要根据机器硬件资源做权衡，建议适当调大；

最小字节数(replica.fetch.min.bytes):一般无需更改，默认值即可；

最大字节数(replica.fetch.max.bytes)：默认为1MB，这个值太小，推荐5M，根据业务情况调整

最大等待时间(replica.fetch.wait.max.ms):follow拉取频率，频率过高，leader会积压大量无效请求情况，无法进行数据同步，导致cpu飙升。配置时谨慎使用，建议默认值，无需配置。


# comsumer tuning

The most important consumer configuration is the fetch size.

    每次请求，kafka返回的最小的数据量。如果数据量不够，这个请求会等待，直到数据量到达最小指标时，才会返回给消费者。如果设置大于1，会提高kafka的吞吐量，但是会有额外的等待期的代价。
    max.partition.fetch.bytes
    
    fetch.max.bytes
    send.buffer.bytes

