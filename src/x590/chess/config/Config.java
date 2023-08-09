package x590.chess.config;

import com.google.gson.*;
import x590.chess.LowercaseEnumJsonSerializable;

import javax.swing.*;
import java.io.*;

public class Config {

	public static final String DEFAULT_PATH = "config.json";

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapterFactory(LowercaseEnumJsonSerializable.TYPE_ADAPTER_FACTORY)
			.registerTypeAdapter(LowercaseEnumJsonSerializable.class, LowercaseEnumJsonSerializable.DESERIALIZER)
			.registerTypeAdapter(SerializedGameConfig.class, SerializedGameConfig.Serializer.INSTANCE)
			.registerTypeAdapter(SerializedGameConfig.class, SerializedGameConfig.Deserializer.INSTANCE)
			.create();

	private String name;

	private SerializedGameConfig defaultGameConfig;

	// Вызывается из gson через рефлексию, поэтому должен быть пустым
	private Config() {}

	private Config initDefaultValues() {
		if (name == null) {
			name = JOptionPane.showInputDialog("Введите имя:");
		}

		if (defaultGameConfig == null) {
			defaultGameConfig = SerializedGameConfig.DEFAULT_INSTANCE;
		}

		return this;
	}

	public static Config readOrCreateConfig(String path) {
		try(var reader = new FileReader(path)) {
			return GSON.fromJson(reader, Config.class).initDefaultValues();

		} catch (IOException | JsonIOException | JsonSyntaxException ex) {
			System.err.println("Cannot read config from \"" + path + "\": ");
			ex.printStackTrace();

			return new Config().initDefaultValues().trySave(path);

		}
	}

	public Config trySave(String path) {
		try(var writer = new FileWriter(path)) {
			GSON.toJson(this, writer);
			writer.flush();
		} catch (IOException ioException) {
			System.err.println("Cannot write config to \"" + path + "\"");
			ioException.printStackTrace();
		}

		return this;
	}

	public String getName() {
		return name;
	}

	public GameConfig defaultGameConfig() {
		return defaultGameConfig;
	}

	public void setAndTrySave(String name, SerializedGameConfig gameConfig) {
		this.name = name;
		this.defaultGameConfig = gameConfig;
		trySave(DEFAULT_PATH);
	}
}
