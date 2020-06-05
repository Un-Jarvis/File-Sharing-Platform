import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClientThread extends Thread {

	private final Socket clientSocket;

	private String clientIPAddr;
	private int clientPortNumber;
	private ClientDatabase database;
	private Map<Integer, PrintWriter> outputList;

	private BufferedReader in;
	private PrintWriter out;

	public ClientThread(Socket socket, ClientDatabase clientDatabase, Map<Integer, PrintWriter> outputList)
			throws IOException {
		this.clientSocket = socket;
		this.database = clientDatabase;
		this.outputList = outputList;

		// Initialize streams
		this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);

		// Get client's shared files
		List<String> sharedFiles = new ArrayList<String>();
		String line = this.in.readLine();
		if (line != null && line.equals("UPLOAD")) {
			String fileName = this.in.readLine();
			while (fileName != null && !fileName.equals("UPLOAD_END")) {
				sharedFiles.add(fileName);
				fileName = this.in.readLine();
			}
		}

		// Get client's connection information
		line = this.in.readLine();
		this.clientIPAddr = "";
		this.clientPortNumber = 0;
		if (line != null && line.equals("CONNECTION_INFO")) {
			this.clientIPAddr = this.in.readLine();
			this.clientPortNumber = this.database.getUniquePort();
			this.out.println(this.clientPortNumber);
		}

		// Add client information and shared files to the database
		this.database.register(this.clientIPAddr, this.clientPortNumber, sharedFiles);
		this.outputList.put(this.clientPortNumber, this.out);
		this.updateFilesAvailable();
	}

	public void updateFilesAvailable() {
		for (Map.Entry<Integer, PrintWriter> entry : this.outputList.entrySet()) {
			PrintWriter writer = entry.getValue();
			writer.println("UPDATE");
			writer.println(this.database.allSharedFiles());
			writer.println("UPDATE_END");
		}
	}

	private void handleClient() throws InterruptedException, SocketException {
		try {

			// Process all messages from client, according to the protocol
			while (true) {
				String line = this.in.readLine();
				if (line == null) {
					break;
				} else if (line.equals("CONNECTION_REQUEST")) {
					int holderPort = Integer.parseInt(this.in.readLine());
					// Inform the file holder that someone wants to connect
					this.outputList.get(holderPort).println("CONNECTION");
					// Provide the receiver's port number to the holder
					this.outputList.get(holderPort).println(this.clientPortNumber);
				} else if (line.equals("SOCKET_OPENED")) {
					int receiverPort = Integer.parseInt(this.in.readLine());
					// Inform the receiver that the holder has opened the socket
					this.outputList.get(receiverPort).println("SOCKET_CONNECT");
					// Provide the holder's IP and port number to the receiver
					this.outputList.get(receiverPort).println(this.clientIPAddr);
					this.outputList.get(receiverPort).println(this.clientPortNumber);
				} else if (line.equals("EXIT")) {
					this.outputList.remove(this.clientPortNumber);
					this.database.removeClient(this.clientPortNumber);

					this.updateFilesAvailable();
					break;
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				this.clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			this.handleClient();
		} catch (InterruptedException e) {
		} catch (SocketException e) {
		}
	}
}