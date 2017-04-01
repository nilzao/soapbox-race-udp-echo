package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.map.PlayerInfo;
import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpSender;

public class FreeRoamBHandler extends PacketHandler {

	private PlayerInfo playerInfo;

	public FreeRoamBHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		if (playerInfo == null) {
			playerInfo = new PlayerInfo(packet);
		} else {
			playerInfo.parseInputData(packet);
		}
		// to debug player packet
		// byte[] playerPacket = playerInfo.getPlayerPacket();
		// System.out.println(UdpDebug.byteArrayToHexString(playerPacket));
		byte[] answer = packet.clone();
		byte[] dataTmp = new byte[12];
		dataTmp[0] = answer[0];
		dataTmp[1] = answer[1];
		for (int i = 2; i < 12; i++) {
			int pos = i + 1;
			dataTmp[i] = answer[pos];
		}
		dataTmp[7] = (byte) 0x00;
		dataTmp[8] = (byte) 0x01;
		dataTmp[9] = (byte) 0x3f;
		sendPacket(dataTmp);

	}

}
