
public interface NetworkCoordinatorFileManager_IF {
	
	// BCASTS
	public void addFileBcast(String filename);
	public void updateFileBcast(String filename);
	public void removeFileBcast(String filename);

	// Get File
	public void getFile(String username, String filename);
	
	public void start();
}
