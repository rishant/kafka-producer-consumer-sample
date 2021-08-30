package conflunt.schema.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class KafkaAvroProducerRegisterSchemaExample {
//	https://github.com/irajhedayati/mcit/blob/master/schema-registry/src/main/scala/ca/mcit/bigdata/sr/SR06ProduceAvroAmdRegisterSchema.scala
	public static void main(String[] args) throws IOException {
		String kafkaTopicName = "new.movie";

		// 2. create schema
		InputStream movieSchemaSource = KafkaAvroProducerRegisterSchemaExample.class.getResourceAsStream("/movie.avsc");
		Schema movieSchema = new Schema.Parser().parse(movieSchemaSource);

		// 3. Build record
		Record avroRecord = new GenericRecordBuilder(movieSchema).set("mId", "101").set("title", "Humgama 2")
				.set("year", 2020).set("director", "Shambhu Raj").build();

		// 4. Produce to kafka
		Properties producerProperties = new Properties();
		producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		producerProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "AvroProducer");
		producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				KafkaAvroSerializer.class.getName());
		producerProperties.setProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
				"http://localhost:8081");
		/* If auto schema registration is not desired, use this configuration */
		// producerProperties.setProperty(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS,
		// "false")
		
		// 5. Build kafkaProducer
		KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(producerProperties);

		// 6. Build kafka Producer Record
		ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<>(kafkaTopicName,
				avroRecord.get("mId").toString(), avroRecord);
		producer.send(producerRecord);
		producer.flush();
		producer.close();
		
		System.out.println("done");
	}
}
