package kafka.producer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerExample {
	//https://www.youtube.com/watch?v=cmzhqv1ZqGA&list=PLa6iDxjj_9qVGTh3jia-DAnlQj9N-VLGp
//https://kafka.apache.org/21/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
	public static void main(String[] args) throws UnknownHostException, InterruptedException, ExecutionException {
		Properties config = new Properties();
	
		 config.put(ProducerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
		 config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		 config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		 config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		 // Optional: Only for producer which require a acknowledgement for message push to the leader broker.
		 config.put(ProducerConfig.ACKS_CONFIG, "all");
		 // Optional: If we need to add transaction support in producers.
		 config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");
		
		String topic = "test";
		KafkaProducer<String,String> producer = new KafkaProducer<String, String>(config);		
		// 1. Synchronous
		synchronousWrite(producer, topic, "key1", "value1");
		// 2. Asynchronous
		asynchronousWrite(producer, topic, "key2", "value2");
		producer.close();
		
		// Kafka producer supports transactions multiple thread ACID compliance
		producer = new KafkaProducer<String, String>(config);
		producer.initTransactions();
		transactionEnableProcedure(producer, topic);
		producer.close();
	}

	private static void transactionEnableProcedure(KafkaProducer<String, String> producer, String topic) {
		try {
			producer.beginTransaction();
			for (int i = 0; i < 100; i++) {
				asynchronousWrite(producer, topic, "key"+i, "value"+i);				
			}
			producer.commitTransaction();
		} catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
			// We can't recover from these exceptions, so our only option is to close the
			// producer and exit.
			producer.close();
		} catch (KafkaException e) {
			// For all other exceptions, just abort the transaction and try again.
			producer.abortTransaction();
		}
	}

	private static void asynchronousWrite(KafkaProducer<String, String> producer, String topic, String key,
			String value) {
		final ProducerRecord<String,String> record = new ProducerRecord<>(topic, key, value);
		// Asynchronous writes
		// java 1.6 syntax
//		producer.send(record, new Callback() {
//			@Override
//			public void onCompletion(RecordMetadata metadata, Exception exception) {
//				if(metadata != null) {
//					System.out.println(metadata);
//				}
//				
//				if (exception != null)
//					System.out.println("Send failed for record " + record + " -> " + exception);
//			}
//		});
		// java 1.8 lambda syntax
		producer.send(record, (RecordMetadata metadata, Exception exception) -> {
			if(metadata != null) {
				System.out.println(metadata);
			}
			
			if (exception != null)
				System.out.println("Send failed for record " + record + " -> " + exception);
		});
	}

	private static void synchronousWrite(KafkaProducer<String, String> producer, String topic, String key, String value)
			throws InterruptedException, ExecutionException {
		final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
		Future<RecordMetadata> future = producer.send(record);
		RecordMetadata metadata = future.get();
		System.out.println(metadata);
	}
}
