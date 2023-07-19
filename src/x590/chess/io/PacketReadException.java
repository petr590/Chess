package x590.chess.io;

import java.io.IOException;
import java.io.Serial;

public class PacketReadException extends IOException {

	@Serial
	public static final long serialVersionUID = 1;

	public PacketReadException() {
		super();
	}

	public PacketReadException(String message) {
		super(message);
	}

	public PacketReadException(Throwable cause) {
		super(cause);
	}

	public PacketReadException(String message, Throwable cause) {
		super(message, cause);
	}
}
