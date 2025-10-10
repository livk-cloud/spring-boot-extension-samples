package com.livk.mongo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;

/**
 * @author livk
 */
@Data
@Document("user")
@Accessors(chain = true)
public class User {

	@Id
	@JsonSerialize(using = ObjectIdJsonSerializer.class)
	@JsonDeserialize(using = ObjectIdJsonDeserializer.class)
	private ObjectId id;

	private String name;

	private Integer age;

	private static class ObjectIdJsonSerializer extends JsonSerializer<ObjectId> {

		@Override
		public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {
			jsonGenerator.writeString(objectId.toHexString());
		}

	}

	private static class ObjectIdJsonDeserializer extends JsonDeserializer<ObjectId> {

		@Override
		public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
				throws IOException {
			var hex = jsonParser.getValueAsString();
			return hex == null ? null : new ObjectId(hex);
		}

	}

}
