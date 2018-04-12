package common

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}

object kryoSerializer {

  def setSerializationObjectByKryo(ob: Object):Array[Byte] = {

    var by = new ByteArrayOutputStream()
    var output = new Output(by)
    try {
      val kryo = new Kryo()
      kryo.writeObject(output, ob)
      output.close()
    }catch {
      case ex:Any => {
        ex.printStackTrace()
      }
    }
    by.toByteArray

  }

  def getSerializationObjectByKryo(bytes: Array[Byte]) = {

    var input = new Input(bytes)
    var event:eventRow = null

    try {
      val kryo = new Kryo()
      event = kryo.readObject(input,classOf[eventRow])
      input.close()
    }catch {
      case ex:Any => {
        ex.printStackTrace()
      }
    }

    event

  }

}
