package com.livk.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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

	private static class ObjectIdJsonSerializer extends ValueSerializer<ObjectId> {

		@Override
		public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializationContext ctxt)
				throws JacksonException {
			jsonGenerator.writeString(objectId.toHexString());
		}

	}

	private static class ObjectIdJsonDeserializer extends ValueDeserializer<ObjectId> {

		@Override
		public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
			var hex = jsonParser.getValueAsString();
			return hex == null ? null : new ObjectId(hex);
		}

	}

}
