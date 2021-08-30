package conflunt.schema.registry;

import java.io.IOException;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.GenericData.Record;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class FetchSechemaFromRegistryToSendRecordExample {

	//https://github.com/irajhedayati/mcit/blob/master/schema-registry/src/main/scala/ca/mcit/bigdata/sr/SR05ProduceAvroGeneric.scala
	public static void main(String[] args) throws IOException, RestClientException {
		String kafkaTopicName = "new.movie";
		String movieSubjectName = "movie-value";
				
		// 1- Connect to Schema Registry
		CachedSchemaRegistryClient srClient = new CachedSchemaRegistryClient("http://localhost:8081", 10);

		// 2- Fetch schema form registry
		SchemaMetadata movieMetadata = srClient.getLatestSchemaMetadata(movieSubjectName);
		Schema movieSchema = (Schema) srClient.getSchemaById(movieMetadata.getId()).rawSchema();
		
		// 3. Build Avro Record
		Record avroRecord = new GenericRecordBuilder(movieSchema).set("mId", "101").set("title", "Humgama 2")
				.set("year", 2020).set("director", "Shambhu Raj").build();

		// 4. Produce to kafka
		Properties producerProperties = new Properties();
		producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
