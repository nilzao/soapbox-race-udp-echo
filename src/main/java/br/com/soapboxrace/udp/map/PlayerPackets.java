package br.com.soapboxrace.udp.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.soapboxrace.udp.srv.UdpDebug;

public class PlayerPackets {

	private List<PlayerInfo> playersInside = new ArrayList<>();

	public PlayerPackets() {
		try {
			File f = new File("players.txt");
			BufferedReader byteBufferedReader = new BufferedReader(new FileReader(f));
			String readLine = "";
			while ((readLine = byteBufferedReader.readLine()) != null) {
				byte[] inputData = UdpDebug.hexStringToByteArray(readLine);
				PlayerInfo playerInfo = new PlayerInfo(inputData);
				playersInside.add(playerInfo);
			}
			byteBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<PlayerInfo> getPlayersInside() {
		return playersInside;
	}

}
