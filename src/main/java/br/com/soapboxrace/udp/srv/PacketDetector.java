package br.com.soapboxrace.udp.srv;

public class PacketDetector {

	public static PacketHandleType detectHandle(byte[] dataPacket) {
		byte[] clone = dataPacket.clone();
		PacketType detectType = detectType(clone);
		switch (detectType) {
		case HELLO:
			return PacketHandleType.HELLO;
		case FREEROAM:
			return PacketHandleType.FREEROAM;
		default:
			return null;
		}
	}

	public static PacketType detectType(byte[] dataPacket) {
		if (isHello(dataPacket)) {
			return PacketType.HELLO;
		}
		if (isFreeRoam(dataPacket)) {
			return PacketType.FREEROAM;
		}
		return null;
	}

	private static boolean isHello(byte[] dataPacket) {
		if (dataPacket[2] == 0x06) {
			return true;
		}
		return false;
	}

	private static boolean isFreeRoam(byte[] dataPacket) {
		if (dataPacket[2] == 0x07 && dataPacket[3] == 0x02) {
			return true;
		}
		return false;
	}

}
