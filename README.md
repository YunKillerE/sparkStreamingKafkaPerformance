# 代码结构

1. textProject 写入kafka是一行数据，写入ignite的也是一行数据，此场景把ignite当初分布式内存数据库使用

2. objectProject 写入kafka是一个对象，写入ignite的也是一个对象，此场景把ignite当作Key-Value分布式存储系统使用，目前生产环境采用的是这种方式

# 需求

1. 往kafka高性能生产数据，总结producer的优化

2. 总结kafka brokers的优化，从存储/复制线程等方面

3. spark streaming多线程高性能消费kafka数据，总结消费优化以及spark streaming优化

4. structured streaming多线程高性能消费kafka数据，和上面比较性能，还未测试

# 数据集

银行交易数据，300G

# 性能测试环境

6台计算节点，200 cores，800G memory

# kafka参数调优

[KafkaTuning.md](./KafkaTuning.md)

# spark参数调优

[spark参数调优](./bin/pef.sh)

# 功能测试结果

[FunctionTestResult](./FunctionTestResult.md)

# 性能测试结果

[PerformanceTestResult](./PerformanceTestResult.md)


问题：平均写入tps只能达到1万左右，而且写入数据量越多越慢，初步分析是三个联合主键导致，将主键改为单个主键的情况，关闭事务，tps可以达到6万左右














