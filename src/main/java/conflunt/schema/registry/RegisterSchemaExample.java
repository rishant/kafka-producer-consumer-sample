package conflunt.schema.registry;

import java.io.IOException;

import org.apache.avro.Schema;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public class RegisterSchemaExample {

	//https://github.com/irajhedayati/mcit/blob/master/schema-registry/src/main/scala/ca/dataedu/sr/RegisterSchema.scala
	public static void main(String[] args) throws IOException, RestClientException {
		// 1. Read the schema from a file
		StringBuilder sb = new StringBuilder();
		sb.append(
				"{ \"type\": \"record\", \"name\": \"Movie\", \"namespace\": \"ca.dataedu.avro\", \"fields\": [{ \"name\": \"mID\", \"type\": \"int\" },{ \"name\":  \"title\", \"type\": \"string\" },{ \"name\":  \"year\", \"type\": \"int\" },{\"name\": \"director\",\"type\": [ \"null\", \"string\" ],\"default\": null}]}");
		String movieSchemaText = sb.toString();
		// 2. Parse schema
		Schema movieSchema = new Schema.Parser().parse(movieSchemaText);

		// 3. Create a client
		CachedSchemaRegistryClient srClient = new CachedSchemaRegistryClient("http://localhost:8081", 1);
		srClient.register("movie", movieSchema);
	}
}
