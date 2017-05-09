import java.util.Iterator;

// File Manager Interface to the NetworkCoordinator (handles Network requests)
public interface FileManagerCoordinator_IF {
	public void addNetworkFile(String filename, String username);
	public void updateNetworkFile(String filename, String username);
	public void removeNetworkFile(String filename, String username);
	public long getFileSize(String filename);
	public void writeNetworkFileInit(String username, String filename);
	public void writeNetworkFileChunk(String username, String filename, byte[] bytes, int len);
	public void writeNetworkFileDone(String username, String filename);
	public void readNetworkFileInit(String username,String filename);
	public int  readNetworkFileChunk(String username,String filename, byte[] bytes);
	public void readNetworkFileDone(String username,String filename);
	public Iterator<String> getOwnFiles();
}
