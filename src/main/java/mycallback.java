import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class mycallback implements Callback {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {

    }
}
