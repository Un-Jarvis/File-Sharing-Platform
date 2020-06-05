import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FileServer {

	private static final int PORT = 8080;

	public static void main(String args[]) throws IOException {

		// Client database
		ClientDatabase clientDB = new ClientDatabase();

		// The set of all the printWriter for all the clients
		Map<Integer, PrintWriter> outputList = new HashMap<Integer, PrintWriter>();

		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("The server is running.");

		try {
			while (true) {
				Socket clientSocket = serverSocket.accept();

				// Display client connection information
				System.out.println("	Connection established: " + clientSocket);

				ClientThread thread = new ClientThread(clientSocket, clientDB, outputList);
				thread.start();
			}
		} catch (Exception e) {
		} finally {
			serverSocket.close();
		}
	}
}