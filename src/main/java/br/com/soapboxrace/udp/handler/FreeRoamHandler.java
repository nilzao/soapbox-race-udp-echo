package br.com.soapboxrace.udp.handler;

import java.nio.ByteBuffer;
import java.util.List;

import br.com.soapboxrace.udp.map.PlayerInfo;
import br.com.soapboxrace.udp.map.PlayerPackets;
import br.com.soapboxrace.udp.map.PlayerRecording;
import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.PacketType;
import br.com.soapboxrace.udp.srv.UdpDebug;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class FreeRoamHandler extends PacketHandler {

	private static int countA = 1;
	private static int countB = 1;
	private PlayerPackets playerPackets;
	private PlayerRecording playerRecording;
	private boolean handShakeOk = false;
	private static final int LIMIT = 2048; // 490

	public FreeRoamHandler(UdpSender udpSender) {
		super(udpSender);
		playerPackets = new PlayerPackets();
		playerRecording = new PlayerRecording();
	}

	// 00:02:02:6a:b0:56:7a:00:02:bf:ff:01:
	private byte[] header() {
		byte[] seqArray = ByteBuffer.allocate(2).putShort((short) countA).array();
		byte[] seqArray2 = ByteBuffer.allocate(2).putShort((short) countB).array();
		countA++;
		byte[] timeDiffBytes = getDiffTimeBytes();
		HelloHandler helloHandler = (HelloHandler) UdpListener.getPacketHandler(PacketType.HELLO);
		byte[] helloCliTime = helloHandler.getCliTime();
		byte[] header = new byte[] { //
				seqArray[0], seqArray[1], // seq
				(byte) 0x02, // fixo
				timeDiffBytes[0], timeDiffBytes[1], // time
				helloCliTime[0], helloCliTime[1], //
				seqArray2[0], seqArray2[1], // counter?? (with counter, need to start at same time, cli like it and stop sending id packets)
				// (byte) 0xff, (byte) 0xff, // counter?? (without counter, can start any time, cli dont like it and keep sending id packets, need sync time
				// bytes
				// on 12:1a packets)
				(byte) 0xff, //
				(byte) 0xff, //
				(byte) 0x00//
		};
		return header;
	}

	private byte[] crc() {
		byte[] crc = new byte[] { (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01 };
		return crc;
	}

	@Override
	public void handlePacket(byte[] packet) {
		staticPlayers(packet);
		// recordedPlayers(packet);
	}

	private void recordedPlayers(byte[] packet) {
		System.out.println(UdpDebug.byteArrayToHexString(packet));
		byte[] nextLine = playerRecording.getNextLine();
		if (nextLine != null) {
			sendFullPacket(nextLine);
			countB++;
		}
	}

	private void staticPlayers(byte[] packet) {
		if (!handShakeOk) {
			sendPlayersInfo(packet);
		} else {
			// System.out.println(UdpDebug.byteArrayToHexString(packet));
			sendPlayersStatePos();
		}
	}

	private void sendFirstPlayer() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(LIMIT);
		int size = 0;
		List<PlayerInfo> playersInside = playerPackets.getPlayersInside();
		PlayerInfo playerInfoTmp = playersInside.get(0);
		byte[] playerPacket = playerInfoTmp.getPlayerPacket();
		size = size + playerPacket.length + 2;
		byteBuffer.put((byte) 0x00);
		byteBuffer.put(playerPacket);
		byteBuffer.put((byte) 0xff);
		for (int i = 0; i < playersInside.size(); i++) {
			byteBuffer.put((byte) 0xff);
			byteBuffer.put((byte) 0xff);
			size = size + 2;
		}
		byte[] byteTmpReturn = new byte[size];
		System.arraycopy(byteBuffer.array(), 0, byteTmpReturn, 0, byteTmpReturn.length);
		sendFullPacket(byteTmpReturn);
	}

	private void sendPlayersInfo(byte[] packet) {
		sendFirstPlayer();
		List<PlayerInfo> playersInside = playerPackets.getPlayersInside();
		for (int i = 1; i < playersInside.size(); i++) {
			ByteBuffer byteBuffer = ByteBuffer.allocate(LIMIT);
			int size = 0;
			for (int i2 = 0; i2 < i; i2++) {
				byteBuffer.put((byte) 0x00);
				byteBuffer.put((byte) 0xff);
				size = size + 2;
			}
			PlayerInfo playerInfoTmp = playersInside.get(i);
			byte[] playerPacket = playerInfoTmp.getPlayerPacket();
			size = size + playerPacket.length + 2;
			byteBuffer.put((byte) 0x00);
			byteBuffer.put(playerPacket);
			byteBuffer.put((byte) 0xff);
			byte[] byteTmpReturn = new byte[size];
			System.arraycopy(byteBuffer.array(), 0, byteTmpReturn, 0, byteTmpReturn.length);
			sendFullPacket(byteTmpReturn);
		}
		countB++;
		handShakeOk = true;
		// System.out.println("bytes to copy ==============================================");
		System.out.println(UdpDebug.byteArrayToHexString(packet));
		// System.out.println("============================================================");
	}

	private void sendPlayersStatePos() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(LIMIT);
		int size = 0;
		List<PlayerInfo> playersInside = playerPackets.getPlayersInside();
		for (int i = 0; i < playersInside.size(); i++) {
			PlayerInfo playerInfoTmp = playersInside.get(i);
			byte[] playerPacket = playerInfoTmp.getStatePosPacket();
			size = size + playerPacket.length + 2;
			byteBuffer.put((byte) 0x00);
			byteBuffer.put(playerPacket);
			byteBuffer.put((byte) 0xff);
		}
		byte[] byteTmpReturn = new byte[size];
		System.arraycopy(byteBuffer.array(), 0, byteTmpReturn, 0, byteTmpReturn.length);
		sendFullPacket(byteTmpReturn);
		countB++;
	}

	private void sendFullPacket(byte[] packet) {
		byte[] header = header();
		byte[] crc = crc();
		ByteBuffer allocate = ByteBuffer.allocate(header.length + packet.length + crc.length);
		allocate.put(header);
		allocate.put(packet);
		allocate.put(crc);
		byte[] byteArray = allocate.array();
		// System.out.println("size: [" + byteArray.length + "]");
		// System.out.println(UdpDebug.byteArrayToHexString(byteArray));
		sendPacket(byteArray);
	}

}
