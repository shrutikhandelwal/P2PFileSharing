import java.io.File;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

//import java.net.Socket;

public class UserManager implements UserManagerGUI_IF, UserManagerCoordinator_IF {
	private String own_user_name;
	private Map<String, InetAddress> user_ledger; // mapping from username to ip
	private FileManagerUserManager_IF filemanager;
	private NetworkCoordinatorUserManager_IF net_coordinator;
	private GUIUserManager_IF gui;

	UserManager()
	{
		own_user_name = "";
		user_ledger = new HashMap<String, InetAddress>();
	}

	public void linkGui(GUIManager g) {
		gui = g;
	}

	public void linkNC(NetworkCoordinator n) {
		net_coordinator = n;
	}

	public void linkFM(FileManager f) {
		filemanager = f;
	}


	private void showState(){
		System.out.println("Own username = " + own_user_name);
		System.out.println("User ledger = {");
		for(Entry<String, InetAddress> e: user_ledger.entrySet() ){
			System.out.print(e.getKey() + ":" + e.getValue() + ", ");
		}
		System.out.println(" }");
	}
	
	static public InetAddress getMyIP(){
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                if(addr instanceof Inet4Address){
		                return addr;
	                }
	            }
	        }
	    } catch (SocketException e) {
	        // Should never happen
	    }
	    return null;
	}
	
	public Collection<InetAddress> getAllIPs(){
		return user_ledger.values();
	}
	
	public Set<String> getAllUsernames(){
		return user_ledger.keySet();
	}
	
	public void addNetworkUser(String username, InetAddress IP) {
		if(user_ledger.containsKey(username) || (username.equals(own_user_name) ) ){
			// Notify Coordinator that user name already exists
			return;
		}
		user_ledger.put(username, IP);
		filemanager.addUser(username);
		gui.addUser(username);
		showState();
	}
	
	public boolean isUsernameTaken(String username){
		return (user_ledger.containsKey(username) || (username.equals(own_user_name) ) );
	}
	
	public void removeNetworkUser(String username, InetAddress ip){
		showState();
		if(!user_ledger.containsKey(username)){
			// Notify Coordinator that user name does not exist
		} else if( !user_ledger.get(username).equals(ip) ){
			// Notify Coordinator that this user name does not have that ip
		} else{
			user_ledger.remove(username);
			filemanager.removeUser(username);
			gui.removeUser(username);
		}
	}
	
	public InetAddress getNetworkUserIP(String username){
		showState();
		if(!user_ledger.containsKey(username)){
			// Notify Coordinator that username does not exist
			return null;
		}
		return user_ledger.get(username);
	}
	
	public String getNetworkUserName(InetAddress IP){
		showState();
		if(!user_ledger.containsValue(IP)){
			// Notify Coordinator that IP does not exist
			return "";
		}
		for(Map.Entry<String, InetAddress> entry: user_ledger.entrySet()){
			InetAddress to_compare = entry.getValue();
			if(to_compare.equals(IP)){
				return entry.getKey();
			}
		}
		return "";
	}
	
	@Override
	public void joinGroup(File invitation, String username) throws NoIPFoundException{
		own_user_name = username;
		showState();
		String join_ip;
		try {
			join_ip = filemanager.getIPFromInvitationFile(invitation);
			InetAddress ip = InetAddress.getByName(join_ip);
			net_coordinator.joinGroup(username, ip);
		} catch (NoSuchElementException e){
			throw new NoIPFoundException();
		} catch (UnknownHostException e){
			throw new NoIPFoundException();
		}
	}
	
	public void createGroup(String username){
		own_user_name = username;
		showState();
	}
	
	public String generateInvitationFile() throws NoIPFoundException{
		InetAddress ip = getMyIP();
		showState();
		if( ip.equals(null) ){
			throw new NoIPFoundException();
		}
		return filemanager.generateInvitationFile(ip.toString());
	}
	
	public void close(){
		showState();
		net_coordinator.exit();
	}

	public String getMyUsername() {
		return own_user_name;
	}

	@Override
	public Set<Entry<String, InetAddress>> getAllUsernameIPPairs() {
		return user_ledger.entrySet();
	}
}
