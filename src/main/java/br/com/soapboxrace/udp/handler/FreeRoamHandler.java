package br.com.soapboxrace.udp.handler;

import java.nio.ByteBuffer;

import br.com.soapboxrace.udp.map.PlayerInfo;
import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.PacketType;
import br.com.soapboxrace.udp.srv.UdpDebug;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class FreeRoamHandler extends PacketHandler {

	private PlayerInfo playerInfo;

	private static int countA = 1;

	public FreeRoamHandler(UdpSender udpSender) {
		super(udpSender);
	}

	// 00:02:02:6a:b0:56:7a:00:02:bf:ff:01:
	private byte[] header() {
		byte[] seqArray = ByteBuffer.allocate(2).putShort((short) countA).array();
		countA++;
		byte[] timeDiffBytes = getDiffTimeBytes();
		HelloHandler helloHandler = (HelloHandler) UdpListener.getPacketHandler(PacketType.HELLO);
		byte[] helloCliTime = helloHandler.getCliTime();
		byte[] header = new byte[] { //
				seqArray[0], seqArray[1], // seq
				(byte) 0x02, // fixo
				timeDiffBytes[0], timeDiffBytes[1], // time
				helloCliTime[0], helloCliTime[1], //
				seqArray[0], seqArray[1], // counter?? (with counter, need to start at same time, cli like it and stop sending id packets)
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
		byte[] emptyPacket = null;
		if (playerInfo == null) {
			long timeDiff = UdpListener.getTimeDiff() - 20L;
			byte[] timeDiffBytes = ByteBuffer.allocate(2).putShort((short) timeDiff).array();

			String[] ghostPackets = new String[3];
			ghostPackets[0] = "00:22:00:00:63:68:61:6e:6e:65:6c:2e:45:4e:5f:5f:31:00:00:01:67:c3:7b:d0:fc:33:00:63:ef:76:00:00:00:00:00:d0:";
			ghostPackets[1] = "01:41:00:47:48:4f:53:54:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:c7:8a:a9:00:00:00:00:00:c8:00:00:00:00:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:72:67:90:a3:00:00:00:00:";
			// ghostPackets[2] = "12:1a:00:33:98:03:71:cd:28:db:f2:f3:88:4b:13:88:3f:6b:f8:1f:ef:2d:2d:2d:34:50:00:7f:";
			ghostPackets[2] = "12:1a:00:84:98:0f:1b:01:c2:30:f7:89:c4:25:89:c4:20:38:2b:fb:e3:96:96:96:9a:28:00:3f:";

			byte[] ghostPacket1 = UdpDebug.hexStringToByteArray(ghostPackets[0]);
			byte[] ghostPacket2 = UdpDebug.hexStringToByteArray(ghostPackets[1]);
			byte[] ghostPacket3 = UdpDebug.hexStringToByteArray(ghostPackets[2]);
			ghostPacket3[2] = timeDiffBytes[0];
			ghostPacket3[3] = timeDiffBytes[1];

			String[] botPackets = new String[3];
			botPackets[0] = "00:22:00:00:63:68:61:6e:6e:65:6c:2e:45:4e:5f:5f:31:00:00:01:67:c3:7b:d0:fc:33:00:63:ef:76:00:00:00:00:00:90:";
			botPackets[1] = "01:41:00:42:4f:54:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:c7:8a:a9:00:00:00:00:00:2c:01:00:00:00:00:00:00:b0:79:e6:cf:ee:1e:9c:fb:bd:0b:d7:a2:00:00:00:00:";
			botPackets[2] = "12:1a:00:31:98:0f:19:3d:c2:70:f7:d9:c4:25:89:c4:20:38:5b:fb:e0:96:96:96:9a:28:00:3f:";
			byte[] botPacket1 = UdpDebug.hexStringToByteArray(botPackets[0]);
			byte[] botPacket2 = UdpDebug.hexStringToByteArray(botPackets[1]);
			byte[] botPacket3 = UdpDebug.hexStringToByteArray(botPackets[2]);
			botPacket3[2] = timeDiffBytes[0];
			botPacket3[3] = timeDiffBytes[1];

			int packetSize = ghostPacket1.length + ghostPacket2.length + ghostPacket3.length + botPacket1.length + botPacket2.length + botPacket3.length + 4;
			ByteBuffer byteBuffer = ByteBuffer.allocate(packetSize);

			byteBuffer.put((byte) 0x00);
			byteBuffer.put(ghostPacket1);
			byteBuffer.put(ghostPacket2);
			byteBuffer.put(ghostPacket3);
			byteBuffer.put((byte) 0xff);
			byteBuffer.put((byte) 0x00);
			byteBuffer.put(botPacket1);
			byteBuffer.put(botPacket2);
			byteBuffer.put(botPacket3);
			byteBuffer.put((byte) 0xff);

			emptyPacket = byteBuffer.array();
			playerInfo = new PlayerInfo(packet);
		} else {
			// String ghostPacketStr = "12:1a:00:33:98:03:71:cd:28:db:f2:f3:88:4b:13:88:3f:6b:f8:1f:ef:2d:2d:2d:34:50:00:7f:";
			String ghostPacketStr = "12:1a:00:84:98:0f:1b:01:c2:30:f7:89:c4:25:89:c4:20:38:2b:fb:e3:96:96:96:9a:28:00:3f:";
			byte[] ghostPacket = UdpDebug.hexStringToByteArray(ghostPacketStr);
			String botPacketStr = "12:1a:00:31:98:0f:19:3d:c2:70:f7:d9:c4:25:89:c4:20:38:5b:fb:e0:96:96:96:9a:28:00:3f";
			byte[] botPacket = UdpDebug.hexStringToByteArray(botPacketStr);

			long timeDiff = UdpListener.getTimeDiff() - 20L;
			byte[] timeDiffBytes = ByteBuffer.allocate(2).putShort((short) timeDiff).array();
			ghostPacket[2] = timeDiffBytes[0];
			ghostPacket[3] = timeDiffBytes[1];
			botPacket[2] = timeDiffBytes[0];
			botPacket[3] = timeDiffBytes[1];

			ByteBuffer byteBuffer = ByteBuffer.allocate(ghostPacket.length + botPacket.length + 4);
			byteBuffer.put((byte) 0x00);
			byteBuffer.put(ghostPacket);
			byteBuffer.put((byte) 0xff);
			byteBuffer.put((byte) 0x00);
			byteBuffer.put(botPacket);
			byteBuffer.put((byte) 0xff);

			emptyPacket = byteBuffer.array();

			playerInfo.parseInputData(packet);
		}
		// byte[] playerPacket = playerInfo.getPlayerPacket();
		// System.out.println(UdpDebug.byteArrayToHexString(playerPacket));
		byte[] header = header();
		byte[] crc = crc();
		ByteBuffer allocate = ByteBuffer.allocate(header.length + emptyPacket.length + crc.length);
		allocate.put(header);
		allocate.put(emptyPacket);
		allocate.put(crc);
		byte[] byteArray = allocate.array();
		// System.out.println(UdpDebug.byteArrayToHexString(byteArray));
		sendPacket(byteArray);
	}

}
