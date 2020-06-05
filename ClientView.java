import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ClientView extends JFrame implements View {

	/**
	 * Client object registered with this view to observe events.
	 */
	private FileClient client;

	private JLabel lTitle;

	private JTable tFilesAvailable;
	private DefaultTableModel model;

	private final JButton bRetrieve, bExit;

	/**
	 * Default constructor.
	 */
	public ClientView() {
		/*
		 * Call the JFrame (superclass) constructor with a String parameter to
		 * name the window in its title bar
		 */
		super("File Sharing Platform");

		// Set up the GUI widgets
		this.lTitle = new JLabel("Files Available", JLabel.CENTER);
		this.lTitle.setFont(lTitle.getFont().deriveFont(Font.BOLD));

		this.tFilesAvailable = new JTable();

		this.bRetrieve = new JButton("Retrieve");
		this.bExit = new JButton("Exit");

		/*
		 * Add header to the table
		 */
		String header[] = new String[] { "File Name", "Holder IP Address", "Holder Port Number" };
		this.model = new DefaultTableModel(0, 0);
		this.model.setColumnIdentifiers(header);
		this.tFilesAvailable.setModel(this.model);
		this.tFilesAvailable.getTableHeader()
				.setFont(this.tFilesAvailable.getTableHeader().getFont().deriveFont(Font.BOLD));
		
		/*
		 * Allow only one row to be selected at a time
		 */
		this.tFilesAvailable.setRowSelectionAllowed(true);
		this.tFilesAvailable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/*
		 * Create scroll panes
		 */
		JScrollPane filesScrollPane = new JScrollPane(this.tFilesAvailable);
		filesScrollPane.setPreferredSize(new Dimension(500, 300));

		/*
		 * Set borders for scroll panes
		 */
		filesScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createEtchedBorder()));
		filesScrollPane.setBackground(null);

		/*
		 * Buttons are initialized to be disabled
		 */
		this.bRetrieve.setEnabled(false);
		this.bExit.setEnabled(false);

		/*
		 * Button panel
		 */
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		buttonPanel.add(this.bRetrieve);
		buttonPanel.add(this.bExit);

		// Set up the observers

		/*
		 * Register this object as the observer for all GUI events
		 */
		this.bRetrieve.addActionListener(this);
		this.bExit.addActionListener(this);

		// Set up the main application window

		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.add(this.lTitle);
		this.add(filesScrollPane);
		this.add(buttonPanel);
		this.pack();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (exitProgram() == 0) {
					client.exitProgram();
					System.exit(0);
				}
			}
		});
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setVisible(false);
	}

	public String getServerAddress() {
		return JOptionPane.showInputDialog(this, "Please enter IP address of the server: ", "Server IP Address",
				JOptionPane.PLAIN_MESSAGE);
	}

	public Map<String, String> getSharedFiles() {
		Map<String, String> files = new HashMap<String, String>();

		FileDialog fileChooser = new FileDialog(this, "Choose files to be shared, or click \"cancel\" to skip",
				FileDialog.LOAD);
		fileChooser.setMultipleMode(true);
		fileChooser.setVisible(true);

		File[] selectedFiles = fileChooser.getFiles();
		for (int i = 0; i < selectedFiles.length; i++) {
			files.put(selectedFiles[i].getName(), selectedFiles[i].getAbsolutePath());
		}

		return files;
	}

	public int exitProgram() {
		return JOptionPane.showConfirmDialog(this, "Are you sure to exit the current session?", "Exit",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public void showApp() {
		this.setVisible(true);
		this.bRetrieve.setEnabled(true);
		this.bExit.setEnabled(true);
	}

	public void invalidIPAddress() {
		JOptionPane.showConfirmDialog(this, "IP address not valid!", "Error!", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE);
	}

	public void noSelectedRow() {
		JOptionPane.showConfirmDialog(this, "No file is selected!", "Error!", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE);
	}

	public void chooseOwn() {
		JOptionPane.showConfirmDialog(this, "File already exists in this machine!", "Error!",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public void fileReceived() {
		JOptionPane.showConfirmDialog(this, "File has been received!", "Success!", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE);
	}

	public void clearTable() {
		this.model.setRowCount(0);
	}

	public void updateTable(String fileName, String ipAddr, String portNum) {
		Object[] row = { fileName, ipAddr, portNum };
		this.model.addRow(row);
	}

	@Override
	public void registerObserver(FileClient c) {
		this.client = c;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == this.bRetrieve) {
			int selectedRow = this.tFilesAvailable.getSelectedRow();
			if (selectedRow != -1) {
				String fileName = this.tFilesAvailable.getValueAt(selectedRow, 0).toString();
				String portNum = this.tFilesAvailable.getValueAt(selectedRow, 2).toString();

				if (Integer.parseInt(portNum) == this.client.getPortNumber()) {
					this.chooseOwn();
				} else {
					// Inform the server
					this.client.getOutputStream().println("CONNECTION_REQUEST");
					this.client.getOutputStream().println(portNum);
					this.client.setFileRequested(fileName);
				}
			} else {
				this.noSelectedRow();
			}
		} else if (source == this.bExit) {
			if (this.exitProgram() == 0) {
				this.client.exitProgram();
				System.exit(0);
			}
		}
	}
}