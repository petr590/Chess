package x590.chess.config;

import com.google.gson.*;

import javax.swing.*;
import java.io.*;

public class Config {

	public static final String DEFAULT_PATH = "config.json";

	private final String name;

	private Config(Reader reader) throws JsonParseException {

		JsonObject object = new JsonParser().parse(reader).getAsJsonObject();

		this.name = object.has("name") ?
				object.get("name").getAsString() :
				JOptionPane.showInputDialog("Введите имя:");
	}

	private Config() {
		this.name = JOptionPane.showInputDialog("Введите имя:");
	}

	public static Config readOrCreateConfig(String path) {
		try {
			return new Config(new FileReader(path));
		} catch (FileNotFoundException | IllegalStateException ex) {
			Config config = new Config();

			Gson gson = new Gson();

//			JsonObject object = new JsonObject();
//			object.add("name", new JsonPrimitive(config.name));

			try(var writer = new FileWriter(path)) {
				gson.toJson(config, writer);
				writer.flush();
			} catch (IOException ioEx) {
				System.err.println("Cannot write config to \"" + path + "\"");
				ioEx.printStackTrace();
			}

			return config;
		}
	}

	public String getName() {
		return name;
	}
}
