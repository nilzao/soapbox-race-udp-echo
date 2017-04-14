package br.com.soapboxrace.udp.map;

import java.nio.ByteBuffer;

import br.com.soapboxrace.udp.srv.UdpListener;

public class PlayerInfo {

	private byte[] channel;
	private byte[] id;
	private byte[] statePos;

	public PlayerInfo(byte[] inputData) {
		parseInputData(inputData);
	}

	public void parseInputData(byte[] inputData) {
		byte[] fullPacket = inputData.clone();
		byte[] dataTmp = new byte[(inputData.length - 20)];
		int o = 0;
		for (int i = 16; i < (inputData.length - 4); i++) {
			dataTmp[o] = fullPacket[i];
			o++;
		}
		if (dataTmp.length > 1) {
			splitPacket(dataTmp);
		}
	}

	private void splitPacket(byte[] parsedPacket) {
		int packetType = 0;
		int packetSize = 0;
		int pos = 0;
		do {
			packetType = parsedPacket[pos];
			pos++;
			packetSize = parsedPacket[pos];
			pos++;
			byte[] dataTmp = new byte[packetSize];
			for (int i = 0; i < packetSize; i++) {
				dataTmp[i] = parsedPacket[pos];
				pos++;
			}
			switch (packetType) {
			case 0:
				channel = dataTmp;
				break;
			case 1:
				id = dataTmp;
				break;
			default:
				statePos = dataTmp;
				break;
			}
			packetType = parsedPacket[pos];
		} while (packetType != -1);
	}

	public byte[] getPlayerPacket() {
		byte[] statePosPacket = getStatePosPacket();
		int bufferSize = channel.length + id.length + statePosPacket.length + 4;
		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
		byteBuffer.put((byte) 0x00);
		byteBuffer.put((byte) channel.length);
		byteBuffer.put(channel);

		byteBuffer.put((byte) 0x01);
		byteBuffer.put((byte) id.length);
		byteBuffer.put(id);

		byteBuffer.put(statePosPacket);
		return byteBuffer.array();
	}

	public byte[] getStatePosPacket() {
		byte[] clone = statePos.clone();
		long timeDiff = UdpListener.getTimeDiff() - 20L;
		byte[] timeDiffBytes = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
		clone[0] = timeDiffBytes[0];
		clone[1] = timeDiffBytes[1];
		int bufferSize = clone.length + 2;
		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
		byteBuffer.put((byte) 0x12);
		byteBuffer.put((byte) clone.length);
		byteBuffer.put(clone);
		return byteBuffer.array();
	}

}
