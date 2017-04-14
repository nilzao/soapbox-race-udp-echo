package br.com.soapboxrace.udp.srv;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import br.com.soapboxrace.udp.handler.FreeRoamHandler;
import br.com.soapboxrace.udp.handler.HelloHandler;

public class UdpListener extends Thread {

	private static UdpSender udpSender;
	private DatagramPacket receivePacket;
	private DatagramSocket serverSocket;
	private static HashMap<PacketType, PacketHandler> packetHandlers = new HashMap<>();
	private static long timeLong = 0L;
	public static byte[] helloTime;
	public static byte[] helloSync;

	public UdpListener(DatagramSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public static long getTimeDiff() {
		return System.currentTimeMillis() - timeLong;
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[1024];
		receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while (true) {
			try {
				serverSocket.receive(receivePacket);
				if (udpSender == null) {
					timeLong = System.currentTimeMillis();
					udpSender = new UdpSender(serverSocket, receivePacket);
				}
				byte[] bytes = getBytes(receivePacket);
				PacketType detect = PacketDetector.detectType(bytes);
				// System.out.println("incomming packet type: [" + detect.toString() + "] time: [" + getTimeDiff() + "ms]");
				System.out.println("in: " + UdpDebug.byteArrayToHexString(bytes));
				PacketHandler packetHandler = getPacketHandler(detect);
				packetHandler.handlePacket(bytes);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static PacketHandler getPacketHandler(PacketType packetType) {
		PacketHandler packetHandler = packetHandlers.get(packetType);
		if (packetHandler == null) {
			switch (packetType) {
			case HELLO:
				packetHandler = new HelloHandler(udpSender);
				break;
			case FREEROAM:
				packetHandler = new FreeRoamHandler(udpSender);
				break;
			default:
				break;
			}
			packetHandlers.put(packetType, packetHandler);
		}
		return packetHandler;
	}

	private byte[] getBytes(DatagramPacket receivePacket) {
		int length = receivePacket.getLength();
		byte[] data = new byte[length];
		System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
		return data;
	}

}
