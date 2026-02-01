package cuatrovientos.dam2.psp.ColaImpresion;

import com.fasterxml.jackson.databind.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import java.time.Duration;
import java.util.*;

public class PrintRouter {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        Properties c = new Properties();
        c.setProperty("bootstrap.servers","127.0.0.1:9092");
        c.setProperty("group.id","router-group");
        c.setProperty("key.deserializer",StringDeserializer.class.getName());
        c.setProperty("value.deserializer",StringDeserializer.class.getName());
        c.setProperty("auto.offset.reset","earliest");

        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(c);
        consumer.subscribe(List.of("documents.transformed"));

        Properties p = new Properties();
        p.setProperty("bootstrap.servers","127.0.0.1:9092");
        p.setProperty("key.serializer",StringSerializer.class.getName());
        p.setProperty("value.serializer",StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<>(p);

        while(true){
            var recs = consumer.poll(Duration.ofSeconds(1));
            for(var r: recs){
                JsonNode n = mapper.readTree(r.value());
                String tipo = n.get("tipo").asText();

                String out = tipo.equalsIgnoreCase("Color")
                        ? "print.color"
                        : "print.bn";

                producer.send(new ProducerRecord<>(out,r.value()));
            }
        }
    }
}
