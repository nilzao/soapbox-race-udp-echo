package br.com.soapboxrace.udp.handler;

import java.nio.ByteBuffer;

import br.com.soapboxrace.udp.srv.PacketHandler;
import br.com.soapboxrace.udp.srv.UdpDebug;
import br.com.soapboxrace.udp.srv.UdpListener;
import br.com.soapboxrace.udp.srv.UdpSender;

public class HelloHandler extends PacketHandler {

	private byte[] cliTime;

	public HelloHandler(UdpSender udpSender) {
		super(udpSender);
	}

	@Override
	public void handlePacket(byte[] packet) {
		cliTime = new byte[] { packet[52], packet[53] };
		System.out.println("HELLOOOOOOOO!!");
		byte[] timeArray = getDiffTimeBytes();
		byte[] helloPacket = { //
				(byte) 0x00, (byte) 0x00, // seq
				(byte) 0x01, // hello header
				timeArray[0], timeArray[1], // time
				cliTime[0], cliTime[1], // cli time?
				(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01 // crc
		};
		sendPacket(helloPacket);
	}

	public byte[] getCliTime() {
		return cliTime;
	}

}
