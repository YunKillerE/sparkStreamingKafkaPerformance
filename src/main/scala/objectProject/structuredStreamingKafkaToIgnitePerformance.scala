package objectProject


import com.beust.jcommander.JCommander
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import common.{Args, igniteWriter}

class structuredStreamingKafkaToIgnitePerformance {

}

object structuredStreamingKafkaToIgnitePerformance {

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
    log.info("初始sparkcontext")
    val spark = SparkSession.builder().appName(argv.appName).enableHiveSupport().getOrCreate()
    spark.sparkContext.getConf.registerKryoClasses(Array(classOf[Args]))

    val kafkaParams = Map[String, String](
      "subscribe" -> argv.topic,
      "kafka.bootstrap.servers" -> argv.brokers,
      "group.id" -> argv.groupid,
      "auto.offset.reset" -> "latest",
      "session.timeout.ms" -> "30000"
    )

    val records = spark.readStream.format("kafka").options(kafkaParams)
      .option("enable.auto.commit", (false: java.lang.Boolean))
      .option("checkpointLocation", "/tmp/structuredStreaming")
      .load()

    /**
      * 开始处理数据
      */

    val recordsVlues = records.selectExpr("CAST(value AS STRING)")

    val igniteJdbc = "jdbc:ignite:cfg://file://" + argv.igniteconfxml
    recordsVlues.writeStream.foreach(new igniteWriter(igniteJdbc)).outputMode("append").start().awaitTermination()

  }

}

