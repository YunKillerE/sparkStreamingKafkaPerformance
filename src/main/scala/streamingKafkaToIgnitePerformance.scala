import com.beust.jcommander.JCommander
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.ignite.spark.IgniteDataFrameSettings._

class streamingKafkaToIgnitePerformance {

}

object streamingKafkaToIgnitePerformance {

  private val log = Logger.getLogger(classOf[streamingKafkaToIgnitePerformance])

  def main(args: Array[String]): Unit = {

    /**
      * 获取输入参数与定义全局变量
      */

    log.info("获取输入变量")
    val argv = new Args()
    JCommander.newBuilder().addObject(argv).build().parse(args: _*)

    /**
      * 创建source/dest context
      */
    log.info("初始sparkcontext和kuducontext")
    val spark = SparkSession.builder().appName(argv.appName).enableHiveSupport().getOrCreate()
    spark.sparkContext.getConf.registerKryoClasses(Array(classOf[Args]))

    val ssc = new StreamingContext(spark.sparkContext, Duration(argv.durationTime))
    ssc.checkpoint("/tmp/streamingToIgnite")

    /**
      * 初始化igniteContext
      */
    /*
        log.info("========================================== 初始化ignite ==========================================")
        val igniteContext = new IgniteContext(spark.sparkContext, argv.igniteconfxml, true)
        val fromCache: IgniteRDD[String, String] = igniteContext.fromCache(argv.cachename)
    */

    /**
      * 创建多线程kafka数据流
      */
    log.info("初始化kafka数据流")
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> argv.brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> argv.groupid,
      "auto.offset.reset" -> "latest",
      "session.timeout.ms" -> "30000",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array(argv.topic)
/*    val numStreams = argv.partitionNum
    val kafkaStreams = (1 to numStreams).map { i =>
      KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams))
    }
    val stream = ssc.union(kafkaStreams)*/

    val stream =  KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams))

    /**
      * 开始处理数据
      */
    log.info("开始处理数据")

    var offsetRanges = Array[OffsetRange]()

    stream.foreachRDD(rdd => {
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      /**
        * 仅测试，输出offset， key， value
        *
        * 如果要存储offset也可以用同样的方法去做
        */
      /*
            for (record <- rdd) {
                System.out.printf("offset = %d, key = %s, value = %s\n",
                record.offset(), record.key(), record.value());
            }
      */

      val valueRDD = rdd.map(_.value().split(","))

      log.info("开始写入kudu")
      import spark.implicits._
      val df = valueRDD.map(x => eventRow(x(0).replace("(",""), x(1), x(2), x(3), x(4), x(5), x(6), x(7), x(8), x(9), x(10), x(11), x(12).replace(")",""))).toDF()
      df.write
        .format(FORMAT_IGNITE)
        .option(OPTION_CONFIG_FILE, argv.igniteconfxml)
        .option(OPTION_TABLE, argv.cachename)
        .mode("Append")
        .option(OPTION_STREAMER_ALLOW_OVERWRITE,true)
        .option(OPTION_CREATE_TABLE_PRIMARY_KEY_FIELDS, "guiyls,kehhao")
        .option(OPTION_CREATE_TABLE_PARAMETERS, "BACKUPS=2, ATOMICITY=TRANSACTIONAL, CACHE_NAME=yc, CACHE_GROUP=ym")
        .save()
      stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)

    })

    ssc.start()
    ssc.awaitTermination()

  }

}
