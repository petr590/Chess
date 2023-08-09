package x590.chess.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class ResourceLoader {

	private ResourceLoader() {}

	public static final String TEXTURES = "textures";

	/**
	 * Загружает изображение из папки {@value #TEXTURES}
	 * по переданному пути из текущего jar файла или из текущей папки
	 * @return Загруженное изображение
	 * @throws UncheckedIOException при возникновении {@link IOException}
	 */
	public static Image loadTexture(String path) {
		path = TEXTURES + File.separatorChar + path;

		try(var in = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
			return ImageIO.read(in != null ? in : new FileInputStream(path));

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
