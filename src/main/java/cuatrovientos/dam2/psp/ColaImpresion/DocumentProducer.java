package cuatrovientos.dam2.psp.ColaImpresion;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class DocumentProducer {

    public static void main(String[] args) {

        String topic = "documents.raw";

        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(props);

        String json = """
        {
          "titulo":"Informe DAM",
          "documento":"%s",
          "tipo":"Color",
          "sender":"Miguel"
        }
        """.formatted("Texto largo ".repeat(80));

        producer.send(new ProducerRecord<>(topic, json));

        producer.flush();
        producer.close();

        System.out.println("Documento enviado ✔");
    }
}
