import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileRecvHandler extends FileTransferHandler {
	ServerSocket server;
	private static final int STARTING_PORT = 5002;
	private static final int ENDING_PORT = 6002;
	private int listening_port;
	
	public int bindToPort(){
		for(int i = STARTING_PORT; i <= ENDING_PORT; i++){
			try {
				server = new ServerSocket(i);
				listening_port = i;
				return listening_port;
			} catch (IOException e){
				continue;
			}
		}
		gui.IOexception();
		return 0;
	}
	
	public FileRecvHandler(String username, String filename){
		super(filename, username);
		
	}
	
	private void updatePercent(int num_chunks_complete, int total_chunks){
		float percent = (float)num_chunks_complete / (float)total_chunks;
		gui.downloadPercentComplete(otherUserName, filename, percent);
	}

	
	@Override
	public void run() {
		try (Socket new_socket = server.accept();
			InputStream in = new_socket.getInputStream();) {
			byte[] first_msg = new byte[NUM_CHUNKS_MSG_LEN];
			in.read(first_msg);
			int numChunks = parseChunkLenMsg(first_msg);
			int numChunksRcvd = 0;
			file_manager.writeNetworkFileInit(otherUserName, filename);
			while(numChunksRcvd != numChunks){
				byte[] message = new byte[FILE_CHUNK_SIZE];
				int bytes_read = in.read(message);
				assert((bytes_read == FILE_CHUNK_SIZE) || (numChunksRcvd == numChunks - 1));
				file_manager.writeNetworkFileChunk(otherUserName, filename, message, bytes_read);
				numChunksRcvd++;
				updatePercent(numChunksRcvd, numChunks);
			}
			file_manager.writeNetworkFileDone(otherUserName, filename);
			in.close();
			new_socket.close();
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}

	}

}
