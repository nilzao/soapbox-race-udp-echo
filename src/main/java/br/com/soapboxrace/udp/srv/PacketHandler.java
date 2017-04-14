package br.com.soapboxrace.udp.srv;

import java.nio.ByteBuffer;

public abstract class PacketHandler {

	private UdpSender udpSender;

	public PacketHandler(UdpSender udpSender) {
		this.udpSender = udpSender;

	}

	protected void sendPacket(byte[] packet) {
		udpSender.sendData(packet);
	}

	public byte[] getDiffTimeBytes() {
		long timeDiff = UdpListener.getTimeDiff();
		return ByteBuffer.allocate(2).putShort((short) timeDiff).array();
	}

	public abstract void handlePacket(byte[] packet);

}
