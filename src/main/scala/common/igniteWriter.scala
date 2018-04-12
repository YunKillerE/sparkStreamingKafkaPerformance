package common

import java.sql.{Connection, DriverManager}

import com.sun.mail.iap.ConnectionException
import org.apache.spark.sql.{ForeachWriter, Row}

class igniteWriter(igniteJdbc: String) extends ForeachWriter[Row] {


  //"jdbc:ignite:cfg://file:///etc/config/ignite-jdbc.xml"
  //"INSERT INTO Person(_key, name, age) VALUES(CAST(? as BIGINT), ?, ?)"

  var connection: Connection = null
  Class.forName("org.apache.ignite.IgniteJdbcDriver")

  override def open(partitionId: Long, version: Long): Boolean = {

    try {
      connection = DriverManager.getConnection(igniteJdbc)
    } catch {
      case ex: ConnectionException => {
        ex.printStackTrace()
        println("连接ignite错误："+igniteJdbc)
      }
    }

    true
  }

  override def process(value: Row): Unit = {
    val stmt =  connection.prepareStatement("MERGE INTO yc(jioyrq,jioysj,guiyls,cpznxh,jiaoym,jiedbz,jio1je,kemucc,kehuzh,kehhao," +
      "zhyodm,hmjsjc,huobdh) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
    val tmp:eventRow = eventRow(value.mkString(",")(0).toString.replace("(",""),value.mkString(",")(0).toString,
      value.mkString(",")(0).toString,value.mkString(",")(0).toString,value.mkString(",")(0).toString,value.mkString(",")(0).toString,
      value.mkString(",")(0).toString,value.mkString(",")(0).toString,value.mkString(",")(0).toString,value.mkString(",")(0).toString,
      value.mkString(",")(0).toString,value.mkString(",")(0).toString,value.mkString(",")(0).toString)
    stmt.setString(1,tmp.jioyrq)
    stmt.setString(2,tmp.jioysj)
    stmt.setString(3,tmp.guiyls)
    stmt.setString(4,tmp.cpznxh)
    stmt.setString(5,tmp.jiaoym)
    stmt.setString(6,tmp.jiedbz)
    stmt.setString(7,tmp.jio1je)
    stmt.setString(8,tmp.kemucc)
    stmt.setString(9,tmp.kehuzh)
    stmt.setString(10,tmp.kehhao)
    stmt.setString(11,tmp.zhyodm)
    stmt.setString(12,tmp.hmjsjc)
    stmt.setString(13,tmp.huobdh)
    stmt.execute()
  }

  override def close(errorOrNull: Throwable): Unit = connection.close()
}
