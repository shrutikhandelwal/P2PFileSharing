import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

import utilities.Pair;


public class FileManager implements FileManagerGUI_IF, FileManagerCoordinator_IF, FileManagerUserManager_IF {
	private static final String  PUBLIC_DIRECTORY = "./Public/";
	private static final String  GOLDEN_CHEST_DIRECTORY = "./GoldenChest/";
	private static final String INVITATION_FILE_NAME = "invitation.txt";
	private ArrayList<String>              golden_chest;
	private Map<String, ArrayList<String> > file_ledger;
	private Map<Pair<String, String>, FileInputStream>     pending_sending_files; // each entry is <username, filename>, fileinputstream
	private Map<Pair<String, String>, FileOutputStream>    pending_recving_files; // each entry is <username, filename>, fileoutputstream
	
	NetworkCoordinatorFileManager_IF netcoordinator;
	GUIFileManager_IF gui;
	
	private void showState(){
		System.out.print("Golden Chest = {");
		for(String s: golden_chest){
			System.out.print(s + ", ");
		}
		System.out.println(" }");
		System.out.println("File ledger = {");
		for(Entry<String, ArrayList<String>> e: file_ledger.entrySet() ){
			System.out.print(e.getKey() + ":{");
			for(String s: e.getValue()){
				System.out.print(s + ", ");
			}
			System.out.println("}");
		}
		System.out.println(" }");
	}
	
	FileManager(){
		golden_chest = new ArrayList<String>();
		file_ledger = new HashMap<String, ArrayList<String>>();
		pending_sending_files = new HashMap<Pair<String, String>, FileInputStream>();
		pending_recving_files = new HashMap<Pair<String, String>, FileOutputStream>();
		File golden_chest_dir = new File(GOLDEN_CHEST_DIRECTORY);
		golden_chest_dir.mkdirs();
		File public_dir = new File(PUBLIC_DIRECTORY);
		public_dir.mkdirs();

	}


	public void linkGui(GUIManager g) {
		gui = g;
	}

	public void linkNC(NetworkCoordinator n) {
		netcoordinator = n;
	}


	private static void copyFiles(File in_file, String outdir) throws FileNotFoundException, IOException{
		int b;
		try (
				FileInputStream in = new FileInputStream(in_file); 
				FileOutputStream out = new FileOutputStream(outdir);
		) {
			while((b = in.read()) != -1){
				out.write(b);
			}			
		}
	}
	
	public String getIPFromInvitationFile(File invitation){
		try(
		Scanner in = new Scanner(invitation);
		){
			String test = in.nextLine();
			String splitter [] = test.split(" "); 
			if(splitter[0].matches("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") == true){
				return splitter[0];
			}
		} catch(FileNotFoundException e){
			// TODO: Should never happen because of checking by the GUI
		} catch (NoSuchElementException e1) {
			System.out.println("File Manager: Invalid invitation file");
			throw e1;
		}
		return "";
	}
	
	public String generateInvitationFile(String IP){
		try (
		PrintStream out = new PrintStream(new FileOutputStream(PUBLIC_DIRECTORY + INVITATION_FILE_NAME));
		){
			out.print(IP);
		} catch (FileNotFoundException e){
			// TODO: Should never happen since constructor should be called before...
		}
		return (PUBLIC_DIRECTORY + INVITATION_FILE_NAME);
	}
	
	public void addUser(String username){
		if(file_ledger.containsKey(username)){
			// TODO: UserManager: Notify username already exists in file ledger (should never happen)
		}
		file_ledger.put(username, new ArrayList<String>());
		File golden_chest_dir = new File(PUBLIC_DIRECTORY + username + "/");
		golden_chest_dir.mkdirs();
		showState();
	}
	
	public void removeUser(String username){
		if(!file_ledger.containsKey(username)){
			// TODO: UserManager: Notify username is not in file ledger (should never happen)
		}
		file_ledger.remove(username);
		showState();
	}
	
	public void addNetworkFile(String filename, String username){
		if(!file_ledger.containsKey(username)){
			// TODO: NetworkCoordinator: Notify user name requested not found
		} else {
			ArrayList<String> file_list = file_ledger.get(username);
			if(file_list.contains(filename)){
				// TODO: Network Coordinator: Notify file by this user already exists
				updateNetworkFile(filename, username);
			} else {
				file_list.add(filename);
				gui.addNewFile(username, filename);
			}
		}
		showState();
	}
	
	@Override
	public long getFileSize(String filename) {
		if(!golden_chest.contains(filename)){
			// TODO: NetworkCoordinator: Notify file name requested not found
		} else {
			File f = new File(GOLDEN_CHEST_DIRECTORY + filename);
			return f.length();
		}
		return 0;
	}	
	
	public void writeNetworkFileInit(String username, String filename) {
		ArrayList<String> filenames = file_ledger.get(username);
		if(filenames == null){
			// TODO: NetworkCoordinator: Notify user name requested not found (NC - Filemanager Username Mismatch)
		}
		else if(!filenames.contains(filename)){
			// TODO: NetworkCoordinator: Notify file name requested not found (NC -Filemanager FileExistence Mismatch)
		}
		else {
			try {
				FileOutputStream out = new FileOutputStream(PUBLIC_DIRECTORY + username + "/" + filename);
				pending_recving_files.put(new Pair<String, String>(username, filename), out);
				// 
			} catch (FileNotFoundException e){
				// TODO: NetworkCoordinator: Notify filename requested not found
			} catch (SecurityException e){
				// TODO: NetworkCoordinator: File already open which throws a security exception
			}
		}
		showState();
	}
	
	public void writeNetworkFileChunk(String username, String filename, byte[] bytes, int len){
		FileOutputStream out = null;
		Pair<String, String> p = new Pair<String, String>(username, filename);
		for(Entry<Pair<String,String>, FileOutputStream> entry: pending_recving_files.entrySet()){
			if(entry.getKey().equals(p)){
				out = entry.getValue();
			}
		}
		if(out == null){
			// TODO: Network Coordinator: writeNetworkFileInit was not called properly
		} else {
			try {
				out.write(bytes, 0, len);
				out.flush();
			} catch(IOException e){
				// TODO: Network Coordinator: IO Exception occurred.
			}
		}
		showState();
	}
	
	public void writeNetworkFileDone(String username, String filename){
		FileOutputStream out = null;
		Pair<String, String> p = new Pair<>(username, filename);
		for(Entry<Pair<String,String>, FileOutputStream> entry: pending_recving_files.entrySet()){
			if(entry.getKey().equals(p)){
				out = entry.getValue();
			}
		}
		if(out == null){
			// TODO Network Coordinator: writeNetworkFileInit was not called properly
		} else {
			try {
				out.close();
			} catch(IOException e){
				// TODO Network Coordinator: IO Exception occurred.
			}
		}
		showState();
	}
	
	public void addUserFile(File f) {
		// If golden chest already has a file with this name, we update instead of add
		if(golden_chest.contains(f.getName())){
			updateUserFile(f);
			return;
		}
		try {
			copyFiles(f, GOLDEN_CHEST_DIRECTORY + f.getName());
			golden_chest.add(f.getName());
			netcoordinator.addFileBcast(f.getName());
			// TODO GUI: Notify Success
		} catch(FileNotFoundException e) {
			// TODO GUI: Notify File not Found
		} catch(IOException e){
			// TODO GUI: Notify IO Exception Occurred
		}
		showState();
	}
	
	public void getUserFile(String filename, String username){
		ArrayList<String> filenames = file_ledger.get(username);
		if(filenames == null) {
			// TODO GUI: Notify user name requested not found (GUI - Filemanager Username Mismatch)
			return;
		}
		if(filenames.contains(filename)) {
			if(pending_recving_files.containsKey(new Pair<String, String>(filename, username))){
				// TODO GUI: Notify user that this file is already being downloaded 
			} else {
				netcoordinator.getFile(username, filename);
			}
		} else {
			// TODO GUI: Notify file not found in the ledger (file does not exist)
		}
		showState();
	}
	
	public void removeUserFile(String filename){
		if(!golden_chest.remove(filename)){
			// TODO GUI: Notify file not found in golden chest
			return;
		}
		File f = new File(GOLDEN_CHEST_DIRECTORY + filename);
		f.delete();
		netcoordinator.removeFileBcast(filename);
		// TODO GUI: Notify file successfully removed
		showState();
	}
	
	public void updateUserFile(File f){
		showState();
		if(golden_chest.contains(f.getName())){
			for(Map.Entry<Pair<String, String>, FileInputStream> entry: pending_sending_files.entrySet()){
				if(entry.getKey().second == f.getName()){
					// TODO GUI: Notify file is currently being downloaded...how do we handle this case?
				}
			}
			try {
				copyFiles(f, GOLDEN_CHEST_DIRECTORY + f.getName());
				netcoordinator.updateFileBcast(f.getName());
				// TODO GUI: Notify successful file update
			} catch (FileNotFoundException e) {
				// TODO GUI: File not found
			} catch (IOException e){
				// TODO  GUI: IO Exception
			}
			return;
		}
		addUserFile(f);
		// TODO GUI: File not found in golden chest, call add file
	}
	
	@Override
	public Set<Entry<String, ArrayList<String>>> getLedger() {
		return file_ledger.entrySet();
	}
	
	@Override
	public Iterator<String> getOwnFiles() {
		return golden_chest.iterator();
	}
		
	@Override
	public void updateNetworkFile(String filename,String username){
		if(!file_ledger.containsKey(username)){
			// TODO: Coordinator: Notify that username does not exist
		} else {
			ArrayList<String> file_list = file_ledger.get(username);
			if(file_list.contains(filename)){
				gui.updateFile(username, filename);
			} else {
				// TODO: Coordinator: Notify file name filename does not exist to user username
				addNetworkFile(filename, username);
			}
		}
	}
	
	public void removeNetworkFile(String filename, String username){
		
		if(!file_ledger.containsKey(username)){
			//TODO: GUI: Notify user not found in file_ledger
			return;
		}else{
			ArrayList<String> file_list = file_ledger.get(username);
			if(!file_list.contains(filename)){
				//TODO: GUI: Notify file not found in ledger	
				return;
			}
			file_list.remove(filename);
			gui.removeFile(username, filename);
		}
	}
		
	// network read 
		
	public void readNetworkFileInit(String username, String filename) {
		if(!golden_chest.contains(filename)){
		 // TODO:NetworkCoordinator: Notify user name requested not found (NC - Filemanager Username Mismatch)
		}
		else {
			try {
				FileInputStream in = new FileInputStream(GOLDEN_CHEST_DIRECTORY + filename);
				pending_sending_files.put(new Pair<String, String>(username, filename), in);
			} catch (FileNotFoundException e){
				// TODO:NetworkCoordinator: Notify filename requested not found
			} catch (SecurityException e){
				// TODO:NetworkCoordinator: File already open which throws a security exception
			}
		}
	}
		
	
	public int readNetworkFileChunk(String username, String filename,byte[] bytes){
		FileInputStream in = null;
		Pair<String,String> p = new Pair<String, String>(username, filename);
		for(Entry<Pair<String,String>, FileInputStream> entry: pending_sending_files.entrySet()){
			if(entry.getKey().equals(p)){
				in = entry.getValue();
			}
		}
		if(in == null){
			//  readNetworkFileChunk was not called properly
		} else {
			try {
				  return in.read(bytes);
				
			} catch(IOException e){
				//TODO: Network Coordinator: IO Exception occurred.
			}
		}
		return 0;
	}
		
	public void readNetworkFileDone(String username, String filename){
		Pair<String, String> p = new Pair<String, String>(username, filename);
		FileInputStream in = null;
		for(Entry<Pair<String,String>, FileInputStream> entry: pending_sending_files.entrySet()){
			if(entry.getKey().equals(p)){
				in = entry.getValue();
			}
		}
		if(in == null){
			// TODO:Network Coordinator: writeNetworkFileInit was not called properly
		} else {
			try {
				in.close();
				pending_sending_files.remove(p);
			} catch(IOException e){
				// TODO:Network Coordinator: IO Exception occurred.
			}
		}
	}		
}
		
