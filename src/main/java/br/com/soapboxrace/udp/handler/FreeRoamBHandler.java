package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpSender;

public class FreeRoamBHandler extends PacketHandler {

	public FreeRoamBHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] answer = packet.clone();
		byte[] dataTmp = new byte[12];
		dataTmp[0] = answer[0];
		dataTmp[1] = answer[1];
		for (int i = 2; i < 12; i++) {
			int pos = i + 1;
			dataTmp[i] = answer[pos];
		}
		// dataTmp[7] = (byte) 0x00;
		// dataTmp[8] = (byte) 0x01;
		// dataTmp[9] = (byte) 0x3f;
		sendPacket(dataTmp);
	}

}
