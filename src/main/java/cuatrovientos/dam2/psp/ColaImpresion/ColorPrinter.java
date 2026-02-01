package cuatrovientos.dam2.psp.ColaImpresion;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class ColorPrinter {

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();
        p.setProperty("bootstrap.servers","127.0.0.1:9092");
        p.setProperty("group.id","printers-color");
        p.setProperty("key.deserializer",StringDeserializer.class.getName());
        p.setProperty("value.deserializer",StringDeserializer.class.getName());
        p.setProperty("auto.offset.reset","earliest");

        KafkaConsumer<String,String> c = new KafkaConsumer<>(p);
        c.subscribe(List.of("print.color"));

        Files.createDirectories(Paths.get("data/printed/color"));

        while(true){
            var recs = c.poll(Duration.ofSeconds(1));
            for(var r: recs){
                Files.writeString(
                    Paths.get("data/printed/color/"+System.nanoTime()+".txt"),
                    r.value()
                );
                System.out.println("Impreso COLOR");
            }
        }
    }
}

