package kafka.producer.avro;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;

public class AvroKafkaProducerExample {
	//https://github.com/confluentinc/examples/blob/6.2.0-post/clients/cloud/java/src/main/java/io/confluent/examples/clients/cloud/ProducerAvroExample.java
	//https://www.youtube.com/watch?v=kKW_0HPCAOU&t=579s
	public static void main(String[] args) throws IOException, RestClientException {
		String kafkaTopic = "movie-topic";
		String schemaSubject = "movie-subject";

		Properties props = producerProperties();
		String schemaRegistryURL = (String) props.get(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG);
		
		// Get Schema From Schema Registry
		SchemaMetadata smd = getSchemaFromSchemaRegistry(schemaRegistryURL, schemaSubject);

		// Create Producer 
		KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);
		
		// Create Avro Message Record
		Record avroRecord = new GenericRecordBuilder(new Schema.Parser().parse(smd.getSchema())).set("mId", "101")
				.set("title", "Humgama 2").set("year", 2020).set("director", "Shambhu Raj").build();

		// Create Producer Record
		ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(kafkaTopic, "Hello", avroRecord);
		
		// Send Message
		producer.send(record);
		
		producer.flush();
		producer.close();
	}

	private static SchemaMetadata getSchemaFromSchemaRegistry(String schemaRegistryURL, String schemaSubject)
			throws IOException, RestClientException {
		CachedSchemaRegistryClient csrc = new CachedSchemaRegistryClient(schemaRegistryURL, 1);
		List<Integer> movieSchemaVersionIdsList = csrc.getAllVersions(schemaSubject);
		movieSchemaVersionIdsList.forEach(id -> System.out.println("Movie schema id: " + id));

		SchemaMetadata smd = csrc.getSchemaMetadata(schemaSubject, 1);
		System.out.println(smd.getSchema());
		System.out.println(smd.getSchemaType());
		return smd;
	}

	private static Properties producerProperties() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
		props.put(ProducerConfig.ACKS_CONFIG, "1");
		return props;
	}
}
