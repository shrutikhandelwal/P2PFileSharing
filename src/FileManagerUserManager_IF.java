import java.io.File;

public interface FileManagerUserManager_IF {
	public String generateInvitationFile(String IP);
	public void addUser(String username);
	public void removeUser(String username);
	public String getIPFromInvitationFile(File invitation);
}
