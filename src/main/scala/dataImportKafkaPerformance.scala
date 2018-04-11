import java.util.Properties

import com.beust.jcommander.JCommander
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession

class dataImportKafkaPerformance() {

}

object dataImportKafkaPerformance {

  private val log = Logger.getLogger(classOf[dataImportKafkaPerformance])
  val sTime: Long = System.currentTimeMillis

  def main(args: Array[String]): Unit = {

    dataImportKafka(args)

  }

  def dataImportKafka(args: Array[String]): Unit = {
    //获取传入参数
    log.info("========================================== 初始化jcommander ==========================================")
    val argv = new Args()
    JCommander.newBuilder().addObject(argv).build().parse(args: _*)

    //创建sparksession
    val spark = SparkSession
      .builder()
      .appName(argv.appName)
      .enableHiveSupport()
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    spark.sparkContext.getConf.registerKryoClasses(Array(classOf[Args],classOf[eventRow]))

    import spark.implicits._

    /**
      * only used for test
      */
    log.warn("打印出所有的配置项，供优化参考： \n" + spark.conf.getAll)

    log.info("========================================== 初始化kafka producer ==========================================")
    val kafkaProducer: Broadcast[KafkaSink[String, String]] = {
      val kafkaProducerConfig = {
        val p = new Properties()
        p.setProperty("bootstrap.servers", argv.brokers)
        p.setProperty("acks", "all")
        p.setProperty("max.in.flight.requests.per.connection", argv.perConnection)
        p.setProperty("batch.size", argv.batchSize)
        p.setProperty("retries", argv.retries)
        p.setProperty("linger.ms", argv.lingerMs)
        p.setProperty("buffer.memory", argv.bufferMem)
        p.setProperty("compression.type", argv.topicCompression)
        p.setProperty("key.serializer", classOf[StringSerializer].getName)
        p.setProperty("value.serializer", classOf[StringSerializer].getName)
        p
      }
      log.warn("kafka producer init done!")
      spark.sparkContext.broadcast(KafkaSink[String, String](kafkaProducerConfig))
    }

    /**
      * read data from hive
      */

    val tableDF = spark.table(argv.hiveTableName).select(
      "jioyrq",
      "jioysj",
      "guiyls",
      "cpznxh",
      "jiaoym",
      "jiedbz",
      "jio1je",
      "kemucc",
      "kehuzh",
      "kehhao",
      "zhyodm",
      "hmjsjc",
      "huobdh")

    /**
      * 一行读取出来，然后判断一行中部分字段是否有业务逻辑问题，如有则记录，发送到error kafka topic中
      *
      * 输入的是df，然后需要对每一行的每一个字段进行逻辑判断，满足要求则直接取值，不满足要求则改变其值，然后返回一行新的row，最后返回一个新的df
      */

    log.info("========================================== 开始转换df ==========================================")
    val filterTableDF = tableDF.map(newRow =>
        (newRow(0).toString, if ((!(newRow(1).toString).equals(""))) newRow(1).toString else "0",
          newRow(2).toString, if (!((newRow(3).toString).equals(""))) newRow(3).toString else "0", newRow(4).toString,
          newRow(5).toString, if (!((newRow(6).toString).equals(""))) newRow(6).toString else "0", newRow(7).toString, newRow(8).toString,
          newRow(9).toString, newRow(10).toString, newRow(11).toString, newRow(12).toString)
      )

    /**
      * 进行二次排序
      */
    log.info("========================================== 开始二次排序 ==========================================")

    import org.apache.spark.sql._
    val sortFilterTableDF = filterTableDF.repartition(argv.partitionNum,new Column("_10")).sortWithinPartitions("_12")

    /**
      * 然后调用foreatchPartition写入对应的分区，这里是否需要自定义partitioner？
      */

    log.info("========================================== 开始写入kafka ==========================================")
/*
    sortFilterTableDF.rdd.mapPartitions(rows => {
      log.info("========================================== kafka 1 ==========================================")
      rows.map(row => {
        val kafkaPartition: Int = row.kehhao.toInt % argv.partitionNum
        log.info("kafkaPartition===============" + kafkaPartition)
        kafkaProducer.value.send(argv.topic, kafkaPartition ,row.kehhao.toString, row.toString)
      })
    }).collect()
*/

    sortFilterTableDF.foreachPartition(rows=>{
      while (rows.hasNext){
        val tmp = rows.next()
        var kafkaPartition = 0
        try {
          kafkaPartition = tmp._10.trim.toInt % argv.partitionNum
        }catch{
          case ex: NumberFormatException =>{
            println(ex.getMessage)
            log.warn("异常数据："+tmp.toString())
          }
          case ex: Any => {
            println("Unkown error!!")
          }
        }
        //log.info("kafkaPartition===============" + kafkaPartition)
        kafkaProducer.value.send(argv.topic, kafkaPartition ,tmp._10.toString, tmp.toString())
      }
    })

    kafkaProducer.value.producer.flush()
    kafkaProducer.value.producer.close()

    spark.close()
  }

}
