import java.io.File;

public interface UserManagerGUI_IF {
	public String generateInvitationFile() throws NoIPFoundException;
	public void createGroup(String username);
	public void joinGroup(File invitation, String username) throws NoIPFoundException;
	public void close();

	// dwei: Is there a special method for a local user to leave the group? or should I just use close()?
	
}
