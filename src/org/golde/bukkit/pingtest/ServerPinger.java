package org.golde.bukkit.pingtest;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;

import com.google.gson.Gson;

import lombok.Getter;

public class ServerPinger {


	@Getter private String ip;
	@Getter private int port;

	@Getter private boolean online = true;

	@Getter private StatusResponse response;

	private final static Gson gson = new Gson();

	public ServerPinger(String ip, int port, int timeout) {
		this.ip = ip;
		this.port = port;

		Socket socket = null;
		OutputStream oStr = null;
		InputStream inputStream = null;

		try {
			socket = new Socket(ip, port);
			socket.setSoTimeout(timeout);

			oStr = socket.getOutputStream();
			DataOutputStream dataOut = new DataOutputStream(oStr);

			inputStream = socket.getInputStream();
			DataInputStream dIn = new DataInputStream(inputStream);

			sendPacket(dataOut, prepareHandshake());
			sendPacket(dataOut, preparePing());

			response = receiveResponse(dIn);

			dIn.close();
			dataOut.close();

		} catch (Exception ex) {
			//you may want to do something here
			online = false;
		} finally {
			if (oStr != null) {
				try {
					oStr.close();
				} catch (IOException e) { }
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) { }
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) { }
			}
		}
	}

	private StatusResponse receiveResponse(DataInputStream dIn) throws IOException {  
		int size = readVarInt(dIn);
		int packetId = readVarInt(dIn);

		if (packetId != 0x00) {
			throw new IOException("Invalid packetId");
		}

		int stringLength = readVarInt(dIn);

		if (stringLength < 1) {
			throw new IOException("Invalid string length.");
		}

		byte[] responseData = new byte[stringLength];     
		dIn.readFully(responseData);    
		String jsonString = new String(responseData, Charset.forName("utf-8")); 
		StatusResponse response = gson.fromJson(jsonString, StatusResponse.class);
		
		response.whitelisted = (response.getVersion().protocol.equals("-1")); //Whitelist fix plugin. Uses protocolib to change the version to - if the server is whitelisted.
		
		return response;
	}

	private void sendPacket(DataOutputStream out, byte[] data) throws IOException {
		writeVarInt(out, data.length);
		out.write(data);
	}

	private byte[] preparePing() throws IOException {
		return new byte[] {0x00};
	}

	private byte[] prepareHandshake() throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DataOutputStream handshake = new DataOutputStream(bOut);
		bOut.write(0x00); //packet id
		writeVarInt(handshake, 4); //protocol version
		writeString(handshake, ip);
		handshake.writeShort(port);
		writeVarInt(handshake, 1); //target state 1       
		return bOut.toByteArray();
	}

	public void writeString(DataOutputStream out, String string) throws IOException {
		writeVarInt(out, string.length());
		out.write(string.getBytes(Charset.forName("utf-8")));
	}

	public int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;
		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}
		return i;
	}

	public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.write(paramInt);
				return;
			}

			out.write(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}


	@Getter
	public class StatusResponse {
		private String description;
		private Players players;
		private Version version;
		private String favicon;
		private int time;
		private boolean whitelisted;

		@Getter
		public class Players {
			private int max;
			private int online;
			private List<Player> sample;    
			
			@Getter
			public class Player {
				private String name;
				private String id;

			}
		}
		
		@Getter
		public class Version {
			private String name;
			private String protocol;
		}
	}

}


