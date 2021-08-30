package kafka.consumer.avro;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

public class AvroKafkaConsumerExample {

	// https://github.com/irajhedayati/mcit/blob/master/schema-registry/src/main/scala/ca/mcit/bigdata/sr/SR07ConsumeAvro.scala
	// https://github.com/confluentinc/examples/blob/6.2.0-post/clients/cloud/java/src/main/java/io/confluent/examples/clients/cloud/ConsumerAvroExample.java
	public static void main(String[] args) {

		String kafkaTopic = "movie-topic";
		String schemaSubject = "movie-subject";

		// 1. prepare Kafka consumer properties
		Properties consumerProperties = consumerProperties();

		// 2. instantiate a Kafka consumer
		KafkaConsumer<String, GenericRecord> consumer = null;
		try {
			consumer = new KafkaConsumer<>(consumerProperties);
			consumer.subscribe(Arrays.asList(kafkaTopic));

			// 3. consume
			System.out.println("| Movie ID | Title | Year | Director |");
			boolean runFlag = true;
			while (runFlag) {
				try {
					ConsumerRecords<String, GenericRecord> polledRecords = consumer.poll(Duration.ofSeconds(1));
					if (!polledRecords.isEmpty()) {
						handle(polledRecords);
					}
				} catch (Exception e) {
					System.out.println(e);
					runFlag = false;
					consumer.close();
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			if(consumer != null)
				consumer.close();
		}
	}

	private static void handle(ConsumerRecords<String, GenericRecord> polledRecords) {
		Iterator<ConsumerRecord<String, GenericRecord>> recordIterator = polledRecords.iterator();
		while (recordIterator.hasNext()) {
			GenericRecord r = recordIterator.next().value();
			System.out.println("mId : " + r.get("mId") + ", title : " + r.get("title") + ", year : "
					+ r.get("year") + ", director : " + r.get("director"));
		}
	}

	private static Properties consumerProperties() {
		Properties consumerProperties = new Properties();
		consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		consumerProperties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "AvroConsumer");
		consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "movie-consumer");
		
		consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		
		consumerProperties.setProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		/* What is this configuration for ('auto.offset.reset')? */
		consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return consumerProperties;
	}
}
