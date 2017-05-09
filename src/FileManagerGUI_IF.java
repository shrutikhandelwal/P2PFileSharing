import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;

// File Manager Interface to the GUI (handles User requests)
public interface FileManagerGUI_IF {
	public void addUserFile(File f);
	public void getUserFile(String filename, String username);
	public void removeUserFile(String filename);
	public void updateUserFile(File f);	
	public Set<Entry<String, ArrayList<String>>> getLedger();
}
