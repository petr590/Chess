package x590.chess;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Enum, который при сериализации в Json записывает имя в нижнем регистре.
 * Всё, что нужно для того, чтобы это работало, это реализовать этот интерфейс в enum-классе
 * и зарегистрировать {@link #TYPE_ADAPTER_FACTORY} и {@link #DESERIALIZER} в {@link com.google.gson.Gson}
 */
public interface LowercaseEnumJsonSerializable {

//	JsonSerializer<LowercaseEnumJsonSerializable> SERIALIZER =
//			(enumValue, type, context) -> new JsonPrimitive(enumValue.name().toLowerCase());


	TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
			return LowercaseEnumJsonSerializable.class.isAssignableFrom(typeToken.getRawType()) ?
					(TypeAdapter<T>) TYPE_ADAPTER :
					null;
		}

		private static final TypeAdapter<LowercaseEnumJsonSerializable> TYPE_ADAPTER = new TypeAdapter<>() {

			/**
			 * Записывает имя enum-объекта в нижнем регистре (с сохранением нижних подчёркиваний)
			 */
			@Override
			public void write(JsonWriter out, LowercaseEnumJsonSerializable enumValue) throws IOException {
				out.value(enumValue.name().toLowerCase());
			}

			@Override
			public LowercaseEnumJsonSerializable read(JsonReader jsonReader) {
				return null;
			}
		};
	};


	/**
	 * Читает enum-объект через рефлексию. Регистр записанного имени не важен.
	 */
	JsonDeserializer<LowercaseEnumJsonSerializable> DESERIALIZER =
			(jsonElement, type, context) -> {
				if (!(type instanceof Class<?>)) {
					throw new IllegalStateException("illegal type " + type);
				}

				@SuppressWarnings("unchecked")
				var enumConstants = ((Class<? extends LowercaseEnumJsonSerializable>)type)
						.getEnumConstants();

				if (enumConstants == null) {
					throw new IllegalStateException("class " + type + " is not an enum class");
				}

				String name = jsonElement.getAsString();

				for (var enumConstant : enumConstants) {
					if (enumConstant.name().equalsIgnoreCase(name)) {
						return enumConstant;
					}
				}

				throw new JsonParseException("Enum constant \"" + name + "\" is not found");
			};

	String name();
}
