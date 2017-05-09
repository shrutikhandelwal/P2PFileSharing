import utilities.*;

import java.net.InetAddress;

public interface NetworkCoordinatorUserManager_IF {
	// Join Network
	public void joinGroup(String username, InetAddress IP);

	// These methods are unused
    /*
	public void usernameTaken(String username, String IP);
	public void addUserSingle(Pair<String,String> username_ip_pair, String username);
	public void addFileSingle(String filename, String IP);
	*/
	public void exit();
}
