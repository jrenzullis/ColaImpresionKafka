package cuatrovientos.dam2.psp.ColaImpresion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class DocumentProcessor {

	public static void main(String[] args) throws Exception {

		String inTopic = "documents.raw";
		String outTopic = "documents.transformed";

		ObjectMapper mapper = new ObjectMapper();

		// --- CONSUMER ---
		Properties cprops = new Properties();
		cprops.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		cprops.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		cprops.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		cprops.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "processor-group");
		cprops.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(cprops);
		consumer.subscribe(List.of(inTopic));

		// --- PRODUCER ---
		Properties pprops = new Properties();
		pprops.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		pprops.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		pprops.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		KafkaProducer<String, String> producer = new KafkaProducer<>(pprops);

		System.out.println("Processor arrancado...");

		while (true) {

			ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

			for (var r : records) {

				String json = r.value();
				JsonNode node = mapper.readTree(json);

				String sender = node.get("sender").asText();
				String titulo = node.get("titulo").asText();
				String texto = node.get("documento").asText();
				String tipo = node.get("tipo").asText();

				// --- hilo guardar original ---
				new Thread(() -> {
					try {
						Path dir = Paths.get("data/originals/" + sender);
						Files.createDirectories(dir);
						Files.writeString(dir.resolve(titulo + ".json"), json);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();

				// --- hilo transformar ---
				new Thread(() -> {
					try {
						int pagina = 1;
						for (int i = 0; i < texto.length(); i += 400) {
							String parte = texto.substring(i, Math.min(i + 400, texto.length()));

							String pageJson = """
									{"titulo":"%s","tipo":"%s","sender":"%s","pagina":%d,"contenido":"%s"}
									""".formatted(titulo, tipo, sender, pagina++, parte);

							producer.send(new ProducerRecord<>(outTopic, pageJson));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();

			}
		}
	}
}
