
public abstract class FileTransferHandler extends Thread {
	protected static final int FILE_CHUNK_SIZE = 1024;
	protected static final int NUM_CHUNKS_MSG_LEN = 4;
	protected FileManagerCoordinator_IF file_manager;
	protected UserManagerCoordinator_IF user_manager;
	protected GUINetworkCoordinator_IF gui;
	protected String filename;
	protected String otherUserName;
	
	public FileTransferHandler(String filename, String otherUserName){
		this.filename = filename;
		this.otherUserName = otherUserName;
	}
	
	protected int parseChunkLenMsg(byte[] bytes){
		assert(bytes.length != NUM_CHUNKS_MSG_LEN);
		int num_chunks = 0;
		num_chunks += bytes[3] << 0;
		num_chunks += bytes[2] << 8;
		num_chunks += bytes[1] << 16;
		num_chunks += bytes[0] << 24;
		return num_chunks;
	}
	
	protected byte[] fillChunkLenMsg(int numChunks){
		byte[] bytes = new byte[NUM_CHUNKS_MSG_LEN];
		bytes[0] = (byte)( (numChunks >> 24) & 0xff);
		bytes[1] = (byte)( (numChunks >> 16) & 0xff);
		bytes[2] = (byte)( (numChunks >> 8) & 0xff);
		bytes[3] = (byte)( (numChunks >> 0) & 0xff);
		return bytes;
	}
	
	public void setInterfaces(FileManagerCoordinator_IF file_manager, UserManagerCoordinator_IF user_manager, GUINetworkCoordinator_IF gui){
		this.file_manager = file_manager;
		this.user_manager = user_manager;
		this.gui = gui;
	}
	
	public abstract void run();
	
}
