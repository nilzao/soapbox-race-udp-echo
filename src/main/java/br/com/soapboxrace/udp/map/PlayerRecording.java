package br.com.soapboxrace.udp.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import br.com.soapboxrace.udp.srv.UdpDebug;

public class PlayerRecording {

	private List<BufferedReader> readers;
	private PlayerInfo playerInfo;

	public PlayerRecording() {
		startReading();
	}

	private List<File> readFolder() {
		ArrayList<File> fileList = new ArrayList<>();
		File folder = new File("recorded-players");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				File file = new File("recorded-players/" + listOfFiles[i].getName());
				fileList.add(file);
			}
		}
		return fileList;
	}

	private void startReading() {
		try {
			readers = new ArrayList<>();
			List<File> readFolder = readFolder();
			for (File file : readFolder) {
				readers.add(new BufferedReader(new FileReader(file)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getNextLine() {
		byte[] byteReturn = null;
		ByteBuffer byteBufferTmp = ByteBuffer.allocate(2048);
		int size = 0;
		try {
			String readLine = "";
			for (BufferedReader byteBufferedReader : readers) {
				if ((readLine = byteBufferedReader.readLine()) != null) {
					byte[] inputDataTmp = UdpDebug.hexStringToByteArray(readLine);
					if (playerInfo == null) {
						playerInfo = new PlayerInfo(inputDataTmp);
						byte[] playerPacket = playerInfo.getPlayerPacket();
						byteBufferTmp.put((byte) 0x00);
						byteBufferTmp.put(playerInfo.getPlayerPacket());
						byteBufferTmp.put((byte) 0xff);
						size = size + playerPacket.length + 2;
					} else {
						playerInfo.parseInputData(inputDataTmp);
						byte[] playerPacket = playerInfo.getPlayerPacket();
						byteBufferTmp.put((byte) 0x00);
						byteBufferTmp.put(playerPacket);
						byteBufferTmp.put((byte) 0xff);
						size = size + playerPacket.length + 2;
					}
				} else {
					startReading();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (size > 0) {
			byteReturn = new byte[size];
			byte[] byteArray = byteBufferTmp.array();
			System.arraycopy(byteArray, 0, byteReturn, 0, byteReturn.length);
		}

		return byteReturn;
	}

	public void closeBuffers() {
		try {
			for (BufferedReader bufferedReader : readers) {
				bufferedReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
