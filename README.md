# File-Sharing-Platform

CSE 3461

Nov 20, 2018


#### Project description:
The project is an application of File Sharing platform.

The user connects to the server by entering the server IP address. Then the application will popup a file chooser. The user can either choose one or more files to upload to the server or click cancel to skip this step. After that, the user will see the file sharing platform.

File Sharing Platform shows a table which contains all files available, IP addresses of files, and port numbers of files. The user can select at most one file in the table. There are two buttons at the bottom of the platform: "Retrieve" to download the selected file, and "Exit" to exit the program. The downloaded files will be saved in the same directory as the program is located.

Server can accept connections from multiple clients. Multiple clients can have the same file, and server can handle when more than one client has the same file. Server deletes entries from list when a client disconnects from server.


#### The project contains following file:
 * FileServer.java - The server file builds a server and deals with multiple user connections.

 * FileClient.java - The client file runs a client application.

 * ClientThread.java - The class deals with a single client thread.

 * ClientDatabase.java - The class represents a client database. 

 * View.java - It is the interface for ClientView.java.

 * ClientView.java - The class provides the graphical user interface for the client side.
 

#### How to compile and execute the program:
 1. Open the terminal and go to the project directory.
 2. Compile server and client files by entering following commands:
	 $ javac FileServer.java
	 $ javac FileClient.java
 3. Run a server by entering:
	 $ java FileServer &
    The terminal will show "The server is running." if the server runs successfully.
 4. Run a client application by entering: 
	 $ java FileClient &
    The client application GUI will show up.

#### Note:
 1. The project was written and tested on Mac OS. It may not work on other operation systems.
