package x590.chess.packet;

import x590.chess.io.PacketInputStream;
import x590.chess.packet.game.*;
import x590.chess.packet.setup.GameConfigPacket;
import x590.chess.packet.setup.SayNamePacket;
import x590.util.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketReaders {

	@FunctionalInterface
	public interface PacketReader {
		Packet read(PacketInputStream in) throws IOException;

		static PacketReader from(Supplier<Packet> supplier) {
			return in -> supplier.get();
		}
	}

	private static final Map<String, PacketReader> READERS = new HashMap<>();

	static {
		READERS.put(GameConfigPacket.NAME,             GameConfigPacket::new);
		READERS.put(SayNamePacket.NAME,                SayNamePacket::new);

		READERS.put(MakeMovePacket.NAME,               MakeMovePacket::new);
		READERS.put(CancelMovePacket.NAME,             CancelMovePacket::new);
		READERS.put(RepeatLastCanceledMovePacket.NAME, RepeatLastCanceledMovePacket::new);

		READERS.put(GiveUpPacket.NAME,                 PacketReader.from(GiveUpPacket::getInstance));
		READERS.put(OfferADrawPacket.NAME,             PacketReader.from(OfferADrawPacket::getInstance));
		READERS.put(DrawOfferResponsePacket.NAME,      DrawOfferResponsePacket::new);
	}

	public static void addReader(String name, PacketReader reader) {
		READERS.put(name, reader);
	}

	public static @Nullable PacketReader get(String name) {
		return READERS.get(name);
	}
}
