import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class FileClient {

	private static final int SERVER_PORT = 8080;

	private String localIP;
	private int localPort;

	private BufferedReader in;
	private PrintWriter out;

	private Socket socket;

	private Map<String, String> sharedFiles;

	private String fileRequested;

	private ClientView view;

	public FileClient() {
		// GUI
		this.view = new ClientView();
		this.view.registerObserver(this);

		// Make connection to the server
		String serverAddress = this.view.getServerAddress();
		while (serverAddress.equals("")) {
			this.view.invalidIPAddress();
			serverAddress = this.view.getServerAddress();
		}
		try {
			this.socket = new Socket(serverAddress, SERVER_PORT);
		} catch (IOException e) {
		}

		// Initialize streams
		try {
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (IOException e) {
		}

		// Initialize shared files
		this.sharedFiles = new HashMap<String, String>();
	}
	
	public int getPortNumber() {
		return this.localPort;
	}

	public PrintWriter getOutputStream() {
		return this.out;
	}

	public BufferedReader getInputStream() {
		return this.in;
	}

	public void setFileRequested(String file) {
		this.fileRequested = file;
	}

	public void exitProgram() {
		this.out.println("EXIT");
	}

	private void uploadFile() throws IOException {
		this.out.println("UPLOAD");
		this.sharedFiles = this.view.getSharedFiles();
		for (Map.Entry<String, String> file : this.sharedFiles.entrySet()) {
			this.out.println(file.getKey());
		}
		this.out.println("UPLOAD_END");
	}

	public void openSocket(int receiverPort) throws IOException {
		// Open socket and wait for other clients to connect
		ServerSocket localServerSocket = new ServerSocket(this.localPort);

		// Inform the server that the socket has been opened
		this.out.println("SOCKET_OPENED");
		this.out.println(receiverPort);

		Socket receiverSocket = localServerSocket.accept();
		this.sendFile(receiverSocket);

		receiverSocket.close();
		localServerSocket.close();
	}

	public void updateFilesAvailable() throws IOException {
		this.view.clearTable();

		String line = this.in.readLine();
		while (line != null && !line.equals("UPDATE_END")) {
			String info[] = line.split("/", 3);
			if (info.length == 3) {
				this.view.updateTable(info[0], info[1], info[2]);
			}
			line = this.in.readLine();
		}
	}

	private void sendFile(Socket receiverSocket) throws IOException {
		// Initialize the input stream of the receiver
		BufferedReader receiverIn = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
		// Get the name of the requested file
		String fileName = receiverIn.readLine();
		File file = new File(this.sharedFiles.get(fileName));

		// Send the file if it existed
		if (file.exists()) {
			// Create input and output streams for reading and sending data
			BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
			BufferedOutputStream fileOut = new BufferedOutputStream(receiverSocket.getOutputStream());
			byte buffer[] = new byte[1024];
			int read;
			while ((read = fileIn.read(buffer)) != -1) {
				fileOut.write(buffer, 0, read);
				fileOut.flush();
			}

			// Close input and output streams
			receiverIn.close();
			fileIn.close();
			fileOut.close();
		}
	}

	public void retrieveFile(String fileName, String holderIP, String holderPort) throws IOException {
		// Make a connection to the holder
		Socket socketToHolder = new Socket(holderIP, Integer.parseInt(holderPort));

		// Create a new file in local directory
		File fileReceived = new File(fileName);

		// Create input and output streams for receiving and writing data
		BufferedInputStream fileIn = new BufferedInputStream(socketToHolder.getInputStream());
		BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(fileReceived));

		// Give the name of the request file to the holder
		PrintWriter holderOut = new PrintWriter(socketToHolder.getOutputStream(), true);
		holderOut.println(fileName);

		byte buffer[] = new byte[1024];
		int read;
		while ((read = fileIn.read(buffer)) != -1) {
			fileOut.write(buffer, 0, read);
			fileOut.flush();
		}

		this.view.fileReceived();

		// Close input and output streams
		holderOut.close();
		fileIn.close();
		fileOut.close();

		// Close the connection to the holder
		socketToHolder.close();
	}

	private String getLinuxLocalIp() throws SocketException {
		String ip = "";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				String name = intf.getName();
				if (!name.contains("docker") && !name.contains("lo")) {
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							String ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::") && !ipaddress.contains("0:0:")
									&& !ipaddress.contains("fe80")) {
								ip = ipaddress;
							}
						}
					}
				}
			}
		} catch (SocketException e) {
		}
		return ip;
	}

	private void run() throws UnknownHostException, IOException {
		// Setup for the connection to the server
		// Upload files
		this.uploadFile();
		// Send connection information to the server
		this.out.println("CONNECTION_INFO");
		this.localIP = getLinuxLocalIp();
		this.out.println(this.localIP);
		this.localPort = Integer.parseInt(this.in.readLine());

		this.view.showApp();

		try {
			while (true) {
				String line = this.in.readLine();
				if (line == null) {
				} else if (line.equals("UPDATE")) {
					this.updateFilesAvailable();
				} else if (line.equals("CONNECTION")) {
					int receiverPort = Integer.parseInt(this.in.readLine());
					this.openSocket(receiverPort);
				} else if (line.equals("SOCKET_CONNECT")) {
					// Get the IP address and port number of the holder from the server
					String holderIPAddr = this.in.readLine();
					String holderPortNum = this.in.readLine();

					this.retrieveFile(this.fileRequested, holderIPAddr, holderPortNum);
				}
			}
		} finally {
			this.exitProgram();

			// Close the socket and streams
			socket.close();
			in.close();
			out.close();
		}
	}

	public static void main(String args[]) throws IOException {
		FileClient client = new FileClient();
		client.run();
	}
}