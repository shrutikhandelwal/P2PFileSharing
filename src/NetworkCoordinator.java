import java.net.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import utilities.*;

public class NetworkCoordinator extends Thread implements NetworkCoordinatorUserManager_IF, NetworkCoordinatorFileManager_IF {
	private static final int HDR_LENGTH = 3;
	private static final int P2P_PORT = 5002;
	UserManagerCoordinator_IF usermanager;
	FileManagerCoordinator_IF filemanager;
	GUINetworkCoordinator_IF gui;
	private ServerSocket listening_socket;
	static private boolean sendFileStarted = false;
	private String getFilename;

	private enum MESSAGE_TYPE {
		// BCASTS
		ADD_USER_BCAST(0), REMOVE_USER_BCAST(1), ADD_FILE(2), UPDATE_FILE_BCAST(3), REMOVE_FILE_BCAST(4), CHAT_MESSAGE(5),
		// Join Network
		JOIN_GROUP(6), USERNAME_TAKEN(7), ADD_USER_SINGLE(8),
		// Get File
		GET_FILE(9);
		

		private final byte _type;

		MESSAGE_TYPE(int b){
			this._type = (byte)b;
		}
		
		public boolean equals(byte b){
			return this._type == b;
		}

		public byte toByte() {
			return _type;
		}
	};

	public void linkGui(GUIManager g) {
		gui = g;
	}

	public void linkUM(UserManager u) {
		usermanager = u;
	}

	public void linkFM(FileManager f) {
		filemanager = f;
	}

	public NetworkCoordinator() throws IOException{
		super("Coordinator Listening Thread");
		listening_socket = new ServerSocket(P2P_PORT);
		this.start();
	}


	private void sendMsg(byte message_type, int message_length, byte[] message, InetAddress IP){
		byte[] frame = new byte[message_length + HDR_LENGTH];
		frame[0] = message_type;
		frame[1] = (byte) ( (message_length & 0xff00) >> 8 );
		frame[2] = (byte) ( (message_length & 0x00ff) >> 0 );
		for(int i = HDR_LENGTH; i < HDR_LENGTH + message_length; i++){
			frame[i] = message[i - HDR_LENGTH];
		}
		Socket s = new Socket();
		try{
			s.connect(new InetSocketAddress(IP, P2P_PORT), 100);
			OutputStream o = s.getOutputStream();
			o.write(frame);
			o.close();
			s.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void sendBcastMsg( byte message_type, int message_length, byte[] message){
		Collection<InetAddress> ips = usermanager.getAllIPs();
		for(InetAddress ip: ips){
			sendMsg(message_type, message_length, message, ip);
		}
	}

	private static byte[] createAddUserMsg(Pair<String,InetAddress> username_ip_pair){
		String username = username_ip_pair.first;
		InetAddress IP = username_ip_pair.second;
		byte [] message = new byte[username.length() + 4];
		int i = 0;
		for(byte b: IP.getAddress()){
			message[i++] = b;
		}
		for(byte b: username.getBytes()) {
			message[i++] = b;
		}
		return message;
	}
	
	private static byte[] createGetFileMsg(Pair<String, Integer> username_port_pair){
		String username = username_port_pair.first;
		int port = username_port_pair.second;
		byte[] message = new byte[username.length() + 2];
		int i = 0;
		message[i++] = (byte) ( (port >> 8) & 0xff );
		message[i++] = (byte) ( (port >> 0) & 0xff );
		for(byte b: username.getBytes()) {
			message[i++] = b;
		}
		return message;
	}
	
	// UserManager BCASTS
	public void addUserBcast(Pair<String, InetAddress> username_ip_pair) {
		byte[] message = createAddUserMsg(username_ip_pair);
		byte message_type = MESSAGE_TYPE.ADD_USER_BCAST.toByte();
		sendBcastMsg(message_type, message.length, message);
	}
	
	// UserManager Join Network
	public void joinGroup(String username, InetAddress IP) {
		byte[] message = username.getBytes();
		byte message_type = MESSAGE_TYPE.JOIN_GROUP.toByte();
		sendMsg(message_type, message.length, message, IP);
	}
	
	public void usernameTaken(InetAddress IP) {
		byte message_type = MESSAGE_TYPE.USERNAME_TAKEN.toByte();
		sendMsg(message_type, 0, null, IP);
	}
	
//	public void addUserSingle(Pair<String, InetAddress> username_ip_pair, String username) {
//		byte[] message = createAddUserMsg(username_ip_pair);
//		byte message_type = MESSAGE_TYPE.ADD_USER_SINGLE.toByte();
//		sendMsg(message_type, message.length, message, usermanager.getNetworkUserIP(username));
//	}
	
	public void addUserSingle(Pair<String, InetAddress> username_ip_pair, InetAddress address) {
		byte[] message = createAddUserMsg(username_ip_pair);
		byte message_type = MESSAGE_TYPE.ADD_USER_SINGLE.toByte();
		sendMsg(message_type, message.length, message, address);
	}
	
//	public void addFileSingle(String filename, String username) {
//		byte[] message = filename.getBytes();
//		byte message_type = MESSAGE_TYPE.ADD_FILE.toByte();
//		sendMsg(message_type, message.length, message, usermanager.getNetworkUserIP(username));
//	}
	
	public void addFileSingle(String filename, InetAddress address) {
		byte[] message = filename.getBytes();
		byte message_type = MESSAGE_TYPE.ADD_FILE.toByte();
		sendMsg(message_type, message.length, message, address);
	}
	
	public void exit() {
		byte[] message = usermanager.getMyUsername().getBytes();
		byte message_type = MESSAGE_TYPE.REMOVE_USER_BCAST.toByte();
		sendBcastMsg(message_type, message.length, message);
		try{
			listening_socket.close();
		} catch(IOException e){
			e.printStackTrace();
		}

	}
	
	// FileManager BCASTS
	public void addFileBcast(String filename) {
		byte[] message = filename.getBytes();
		byte message_type = MESSAGE_TYPE.ADD_FILE.toByte();
		sendBcastMsg(message_type, message.length, message);
	}
	
	public void updateFileBcast(String filename) {
		byte[] message = filename.getBytes();
		byte message_type = MESSAGE_TYPE.UPDATE_FILE_BCAST.toByte();
		sendBcastMsg(message_type, message.length, message);
	}
	
	public void removeFileBcast(String filename) {
		byte[] message = filename.getBytes();
		byte message_type = MESSAGE_TYPE.REMOVE_FILE_BCAST.toByte();
		sendBcastMsg(message_type, message.length, message);
	}
	
	// FileManager Get File
	public void getFile(String username, String filename){
		FileRecvHandler recv_handler = new FileRecvHandler(username, filename);
		recv_handler.setInterfaces(filemanager, usermanager, gui);
		int port = recv_handler.bindToPort();
		recv_handler.start();
		byte[] message = createGetFileMsg(new Pair<>(filename, port));
		byte message_type = MESSAGE_TYPE.GET_FILE.toByte();
		sendMsg(message_type, message.length, message, usermanager.getNetworkUserIP(username));
	}
	
	// Receive Messages
	@Override
	public void run(){
		while (true) {
			try (Socket new_socket = listening_socket.accept();
				InputStream in = new_socket.getInputStream();) {
				byte[] hdr = new byte[HDR_LENGTH];
				in.read(hdr);
				int message_length = (int)( (hdr[1] << 8) + hdr[2] );
				byte[] message = new byte[message_length];
				in.read(message);
				byte message_type = hdr[0];
				InetAddress IP = new_socket.getInetAddress();
				recvMsg(message_type, message, IP);
				in.close();
				new_socket.close();
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
	}
	
	static private Pair<String,Integer> parseGetFileMsg(byte[] message){
		byte[] port_msg = new byte[2];
		byte[] name_msg = new byte[message.length - 2];
		int j = 0;
		int k = 0;
		for(int i = 0; i < message.length; i++){
			if(i < 2){
				port_msg[j++] = message[i];
			} else {
				name_msg[k++] = message[i];
			}
		}
		String username = new String(name_msg, StandardCharsets.UTF_8);
		int first_byte = port_msg[0];
		if(port_msg[0] < 0){
			first_byte = port_msg[0] + 256;
		}
		int second_byte = port_msg[0];
		if(port_msg[1] < 0){
			second_byte = port_msg[1] + 256;
		}
		int port = (first_byte << 8) | (second_byte);
		return new Pair<>(username, port);
	}

	static private Pair<String,InetAddress> parseAddUserMsg(byte[] message){
		byte[] ip_msg = new byte[4];
		byte[] name_msg = new byte[message.length - 4];
		int j = 0;
		int k = 0;
		for(int i = 0; i < message.length; i++){
			if(i < 4 ){
				ip_msg[j++] = message[i];
			} else {
				name_msg[k++] = message[i];
			}
		}
		String new_name = new String(name_msg, StandardCharsets.UTF_8);
		try {
			InetAddress new_ip_address = InetAddress.getByAddress(ip_msg);
			return new Pair<>(new_name, new_ip_address);
		} catch(UnknownHostException e){
			e.printStackTrace();
			return null;
		}
	}
	
	private void recvMsg(byte message_type, byte[] message, InetAddress srcIP) {
		String msg_string = new String(message, StandardCharsets.UTF_8);
		if(  ( MESSAGE_TYPE.ADD_USER_BCAST.equals(message_type) ) || (MESSAGE_TYPE.ADD_USER_SINGLE.equals(message_type) ) ) {
			Pair<String, InetAddress> username_ip_pair = parseAddUserMsg(message);
			String new_username = username_ip_pair.first;
			InetAddress new_ip = username_ip_pair.second;
			if(message_type == MESSAGE_TYPE.ADD_USER_SINGLE.toByte()){
				gui.connectionStatus(true, true);
			}
			usermanager.addNetworkUser(new_username, new_ip);
			if(message_type == MESSAGE_TYPE.ADD_USER_BCAST.toByte()){
				Iterator<String> ownFilePtr = filemanager.getOwnFiles();
				while(ownFilePtr.hasNext()) {
					String filename = (String)ownFilePtr.next();
					addFileSingle(filename, username_ip_pair.second);
				}
			}
		} else if (MESSAGE_TYPE.REMOVE_USER_BCAST.equals(message_type)) {
			usermanager.removeNetworkUser(msg_string, srcIP);
		} else if (MESSAGE_TYPE.ADD_FILE.equals(message_type)) {
			filemanager.addNetworkFile(msg_string, usermanager.getNetworkUserName(srcIP));
		} else if (MESSAGE_TYPE.UPDATE_FILE_BCAST.equals(message_type)) {
			filemanager.updateNetworkFile(msg_string, usermanager.getNetworkUserName(srcIP));
		} else if (MESSAGE_TYPE.REMOVE_FILE_BCAST.equals(message_type)) {
			filemanager.removeNetworkFile(msg_string, usermanager.getNetworkUserName(srcIP));
		} else if (MESSAGE_TYPE.JOIN_GROUP.equals(message_type)) {
			String username = msg_string;
			if(usermanager.isUsernameTaken(username)){
				usernameTaken(srcIP);
			} else {
				Pair<String, InetAddress> new_user_ip_pair = new Pair<>(username, srcIP);
				// Step 1) 1(2) 2->1
				addUserSingle(new Pair<String, InetAddress>(usermanager.getMyUsername(), UserManager.getMyIP()), srcIP);
				// Step 2) 1(2.files) 2->1
				Iterator<String> ownFilePtr = filemanager.getOwnFiles();
				while(ownFilePtr.hasNext()){
					String filename = (String) ownFilePtr.next();
					addFileSingle(filename, srcIP);
				}
				// Step 3) 1(3.*) 2->1
				for (Entry<String, InetAddress> e: usermanager.getAllUsernameIPPairs() ) {
					addUserSingle(new Pair<>(e.getKey(), e.getValue()) , srcIP);
				}
				// Step 4) 3.*(1), 1(3.*.files)  2->3.* 
				addUserBcast(new_user_ip_pair);
				// Step 5) 2(1)
				usermanager.addNetworkUser(username, srcIP);
			}
		} else if (MESSAGE_TYPE.USERNAME_TAKEN.equals(message_type)) {
			gui.connectionStatus(true, false);
		} else if (MESSAGE_TYPE.GET_FILE.equals(message_type)) {
			Pair<String, Integer> file_port_pair = parseGetFileMsg(message);
			String filename = file_port_pair.first;
			int port = file_port_pair.second;
			FileSendHandler send_handler = new FileSendHandler(port, filename, usermanager.getNetworkUserName(srcIP));
			send_handler.setInterfaces(filemanager, usermanager, gui);
			send_handler.start();
		} else {
			throw new IllegalArgumentException("Invalid Message Type");
		}
	}
}
