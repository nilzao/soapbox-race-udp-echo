package br.com.soapboxrace.udp.handler;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpSender;

public class FreeRoamHandler extends PacketHandler {

	public FreeRoamHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet, long timeDiff) {
		byte[] answer = packet.clone();
		byte[] dataTmp = new byte[(answer.length - 1)];
		dataTmp[0] = answer[0];
		dataTmp[1] = answer[1];
		// 00:01:07:01:d2:ce:2e:8c:35:fc:bb:e7:
		for (int i = 2; i < dataTmp.length; i++) {
			int pos = i + 1;
			dataTmp[i] = answer[pos];
		}
		// dataTmp[7] = (byte) 0x00;
		// dataTmp[8] = (byte) 0x01;
		// dataTmp[9] = (byte) 0x3f;
		sendPacket(dataTmp);
	}

}
