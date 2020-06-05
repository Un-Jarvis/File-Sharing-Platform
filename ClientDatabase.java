import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The database in which stores all client information, with related functions.
 * 
 * @author Jarvis Huang
 * 
 */
public final class ClientDatabase {

	private class Client {
		public String ipAddress;
		public int portNumber;
		public List<String> sharedFiles;

		/**
		 * Public constructor for Client class.
		 */
		public Client(String ip, int portNumber, List<String> sharedFiles) {
			this.ipAddress = ip;
			this.portNumber = portNumber;
			this.sharedFiles = sharedFiles;
		}
	}

	private Set<Client> database;

	/**
	 * Public constructor.
	 */
	public ClientDatabase() {
		this.database = new HashSet<Client>();
	}

	public void register(String ipAddress, int portNumber, List<String> sharedFiles) {
		this.database.add(new Client(ipAddress, portNumber, sharedFiles));
	}

	private Client getClientByPortNumber(int port) {
		Client client = null;
		for (Client c : this.database) {
			if (c.portNumber == port) {
				client = c;
			}
		}
		return client;
	}

	public void removeClient(int clientPortNumber) {
		this.database.remove(getClientByPortNumber(clientPortNumber));
	}

	public String allSharedFiles() {
		String all = "";
		for (Client c : this.database) {
			for (String file : c.sharedFiles) {
				all += file + "/" + c.ipAddress + "/" + c.portNumber + "\n";

			}
		}
		return all;
	}

	public String getHolderIP(String file) {
		String ip = "";
		for (Client c : this.database) {
			if (c.sharedFiles.contains(file)) {
				ip = c.ipAddress;
			}
		}
		return ip;
	}

	public int getHolderPort(String file) {
		int port = 0;
		for (Client c : this.database) {
			if (c.sharedFiles.contains(file)) {
				port = c.portNumber;
			}
		}
		return port;
	}

	public int getUniquePort() {
		int port = 0;
		boolean unique = false;
		while (!unique) {
			Random r = new Random();
			port = 8000 + r.nextInt(1000);
			unique = true;
			for (Client c : this.database) {
				if (port == c.portNumber) {
					unique = false;
				}
			}
		}
		return port;
	}
}
