package common

import org.apache.kafka.clients.producer.{Callback, RecordMetadata}

class mycallback extends Callback{
  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = ???
}
