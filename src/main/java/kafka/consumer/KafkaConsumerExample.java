package kafka.consumer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaConsumerExample {
	//https://www.youtube.com/watch?v=cmzhqv1ZqGA&list=PLa6iDxjj_9qVGTh3jia-DAnlQj9N-VLGp
//https://kafka.apache.org/21/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
	public static void main(String[] args) throws UnknownHostException {
		Properties config = new Properties();
		
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "JavaProgramConsumerGroup-1");
		config.put(ConsumerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		
		config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
		config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 10);
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		String topic = "test";

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);
		consumer.subscribe(Arrays.asList(topic));
		
		System.out.println(consumer.partitionsFor(topic));
		boolean flag = true;
		while (flag) {
			try {
				synchronizePollingAndCommit(consumer);
				asynchronizePollingAndCommit(consumer);
			} catch (Exception e) {
				System.out.println("Exception : " + e);
				flag = false;
			}
		}
		consumer.close();
	}

	private static void asynchronizePollingAndCommit(KafkaConsumer<String, String> consumer) {
		ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
		process(records);

		// 2. Asynchronous
		// java 1.6 syntax
		java6SyntaxCommitCallback(consumer);
		
		// java 1.8 syntax
		java8SyntaxCommitCallback(consumer);
	}

	private static void java8SyntaxCommitCallback(KafkaConsumer<String, String> consumer) {
		consumer.commitAsync((Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) -> {
			if (offsets != null) {
				offsets.forEach((topicPartition, offset) -> {
					System.out.println(topicPartition.topic());
					System.out.println(offset.metadata());
				});
			}

			if (exception != null) {
				System.out.println("Commit failed for offsets " + offsets + " : " + exception);
			}
		});
	}

	private static void java6SyntaxCommitCallback(KafkaConsumer<String, String> consumer) {
		consumer.commitAsync(new OffsetCommitCallback() {
			@Override
			public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
				if (offsets != null) {
					offsets.forEach((topicPartition, offset) -> {
						System.out.println(topicPartition.topic());
						System.out.println(offset.metadata());
					});
				}

				if (exception != null) {
					System.out.println("Commit failed for offsets " + offsets + " : " + exception);
				}
			}
		});
	}

	private static void synchronizePollingAndCommit(KafkaConsumer<String, String> consumer) {
		ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
		process(records);

		// 1. Commit Offset Synchronously  
		consumer.commitSync();
	}

	private static void process(ConsumerRecords<String, String> records) {
		records.forEach(record -> {
			System.out.println(record.serializedKeySize());
			System.out.println(record.key());
			System.out.println(record.serializedValueSize());
			System.out.println(record.value());
		});
	}
}
