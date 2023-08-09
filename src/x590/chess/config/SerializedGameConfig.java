package x590.chess.config;

import com.google.gson.*;
import x590.util.annotation.Immutable;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Настройки игры, сохранённые в конфиг
 */
@Immutable
public class SerializedGameConfig extends GameConfig {

	private static final SideGetter DEFAULT_SIDE_GETTER = SideGetter.WHITE;

	public static final SerializedGameConfig DEFAULT_INSTANCE =
			new SerializedGameConfig(DEFAULT_SIDE_GETTER, DEFAULT_ALLOW_MOVE_CANCEL, DEFAULT_TIMEOUT);

	private final SideGetter sideGetter;

	public SerializedGameConfig(SideGetter sideGetter, boolean allowMoveCancel, int offerADrawTimeout) {
		super(sideGetter.getSide(), allowMoveCancel, offerADrawTimeout);
		this.sideGetter = sideGetter;
	}

	public static final class Serializer implements JsonSerializer<SerializedGameConfig> {

		public static final Serializer INSTANCE = new Serializer();

		private Serializer() {}

		@Override
		public JsonElement serialize(SerializedGameConfig gameConfig, Type type, JsonSerializationContext context) {
			var jsonObject = new JsonObject();
			jsonObject.add("side", context.serialize(gameConfig.sideGetter));
			jsonObject.add("allowMoveCancel", context.serialize(gameConfig.allowMoveCancel()));
			jsonObject.add("offerADrawTimeout", context.serialize(gameConfig.offerADrawTimeout()));
			return jsonObject;
		}
	}

	public static final class Deserializer implements JsonDeserializer<SerializedGameConfig> {

		public static final Deserializer INSTANCE = new Deserializer();

		private Deserializer() {}

		@Override
		public SerializedGameConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
			if (!jsonElement.isJsonObject()) {
				return DEFAULT_INSTANCE;
			}

			var jsonObject = jsonElement.getAsJsonObject();

			return new SerializedGameConfig(
					Objects.requireNonNullElse(context.deserialize(jsonObject.get("side"), SideGetter.class), DEFAULT_SIDE_GETTER),
					jsonObject.get("allowMoveCancel").getAsBoolean(),
					jsonObject.get("offerADrawTimeout").getAsInt()
			);
		}
	}
}
