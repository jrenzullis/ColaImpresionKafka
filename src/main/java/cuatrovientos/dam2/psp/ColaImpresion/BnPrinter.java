package cuatrovientos.dam2.psp.ColaImpresion;


import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class BnPrinter {

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();
        p.setProperty("bootstrap.servers","127.0.0.1:9092");
        p.setProperty("group.id","printers-bn");
        p.setProperty("key.deserializer",StringDeserializer.class.getName());
        p.setProperty("value.deserializer",StringDeserializer.class.getName());
        p.setProperty("auto.offset.reset","earliest");

        KafkaConsumer<String,String> c = new KafkaConsumer<>(p);
        c.subscribe(List.of("print.bn"));

        Files.createDirectories(Paths.get("data/printed/bn"));

        while(true){
            var recs = c.poll(Duration.ofSeconds(1));
            for(var r: recs){
                Files.writeString(
                    Paths.get("data/printed/bn/"+System.nanoTime()+".txt"),
                    r.value()
                );
                System.out.println("Impreso BN");
            }
        }
    }
}

