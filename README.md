# Chat-Application
Java Client Server Chat Application
1) First compile the "Server.java" using "javac Server.java" command
2) Run the server using "java Server" command

3) Compile the "Client.java" using "javac Client.java" command
4) Run each client using "java Client" command

5) The application creates client directories for connected clients using their specified "username"
6) Each user can keep files in their respective directories

7) The application supports the following functions -
	a) Broadcast message - simply type on the terminal window
	b) Unicast message - use format --> "@username message"
	c) Blockcast message - use format --> "!username message"
	d) Broadcast file - use format --> "bFILE /sender_username/filename.extension"
	e) Unicast file - use format --> "uFILE recipient_username /sender_username/filename.extension"
