import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileSendHandler extends FileTransferHandler {
	private int serverPort;
	
	public FileSendHandler(int serverPort, String filename, String username) {
		super(filename, username);
		this.serverPort = serverPort;
	}
	
	private void updatePercent(int num_chunks_complete, int total_chunks){
		float percent = (float)num_chunks_complete / (float)total_chunks;
		gui.uploadPercentComplete(otherUserName, filename, percent);
	}
	
	@Override
	public void run() {
		// Call FileManager to get the size of the file
		long fileSize = file_manager.getFileSize(filename);
		int numChunks = (int) ( fileSize / (long)FILE_CHUNK_SIZE );
		if(fileSize % FILE_CHUNK_SIZE != 0){
			numChunks++;
		}
		int numChunksSent = 0;
		byte[] num_chunks_msg = fillChunkLenMsg(numChunks);
		Socket s = new Socket();
		try{
			s.connect(new InetSocketAddress(user_manager.getNetworkUserIP(otherUserName), serverPort), 100);
			OutputStream o = s.getOutputStream();
			o.write(num_chunks_msg);
			file_manager.readNetworkFileInit(otherUserName, filename);
			gui.uploadStarted(otherUserName, filename);
			while(numChunksSent != numChunks){
				byte[] file_chunk_msg = new byte[FILE_CHUNK_SIZE]; 
				int len = file_manager.readNetworkFileChunk(otherUserName, filename, file_chunk_msg);
				o.write(file_chunk_msg, 0, len);
				numChunksSent++;
				updatePercent(numChunksSent, numChunks);
			}
			file_manager.readNetworkFileDone(otherUserName, filename);
			o.close();
			s.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

}
