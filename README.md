# 需求

1. 往kafka高性能生产数据，总结producer的优化

2. 总结kafka brokers的优化，从存储/复制线程等方面

3. spark streaming多线程高性能消费kafka数据，总结消费优化以及spark streaming优化

4. structured streaming多线程高性能消费kafka数据，和上面比较性能

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

















