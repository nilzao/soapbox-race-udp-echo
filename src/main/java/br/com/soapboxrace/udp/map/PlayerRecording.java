package br.com.soapboxrace.udp.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.soapboxrace.udp.srv.UdpDebug;

public class PlayerRecording {

	private List<BufferedReader> readers = new ArrayList<>();
	private BufferedReader byteBufferedReader;
	private File file;
	private PlayerInfo playerInfo;

	public PlayerRecording() {
		startReading();
	}

	private void startReading() {
		try {
			file = new File("foxo.txt");
			byteBufferedReader = new BufferedReader(new FileReader(file));
			readers.add(byteBufferedReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getNextLine() {
		byte[] inputData = null;
		try {
			String readLine = "";
			if ((readLine = byteBufferedReader.readLine()) != null) {
				byte[] inputDataTmp = UdpDebug.hexStringToByteArray(readLine);
				if (playerInfo == null) {
					playerInfo = new PlayerInfo(inputDataTmp);
					inputData = playerInfo.getPlayerPacket();
				} else {
					playerInfo.parseInputData(inputDataTmp);
					inputData = playerInfo.getStatePosPacket();
				}
			} else {
				startReading();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputData;
	}

	public void closeBuffer() {
		try {
			byteBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
