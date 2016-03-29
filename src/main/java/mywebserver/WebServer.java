package mywebserver;

/*
 * MyWebServer.java
 * Attributed to Elliott, after Hughes, Shoffner, Winslow with *significant alterations* by Kevin Westropp
 * Also is having influenced by "Java Network Programming" by Elliotte Rusty Harold and 
 * Florida Tech Networking Programming website - http://www.cs.fit.edu/~mmahoney/cse4232/
 * & http://cs.fit.edu/~mmahoney/cse3103/java/Webserver.java .
 */
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * A multi-threaded Web server for web browsers which can handle multiple
 * requests/clients for HTTP requests and returns a dynamic index html of all
 * the server's contents/directories and subdirectories (no super-directories)
 * and by using the worker class above.
 */
public class WebServer {

    private static File rootDirectory; //* for saving the rootDirectory location
    private final static String SERVER_LOG = ("serverlog.txt"); //* for logging the requests/info */
    private static File index = new File("index.html"); //* index file for the root directory */
    private static String root = ("http://localhost:2540"); //* string of root directory, currently set at localhost */
    private static File addnums = new File("addnums.html"); //* addnums html file for user input/computation */

    /**
     * Private method which gets called at server start up and creates the add
     * numbers html form for user input.
     */
    private static void setAddNums() {
        try {
            if (!addnums.exists()) {
                addnums.createNewFile();
            }
            FileWriter file = null;
            file = new FileWriter(addnums);
            BufferedWriter out = new BufferedWriter(file);
            try (PrintWriter write = new PrintWriter(out)) {
                write.write("<HTML>\r\n");
                write.write("<HEAD><TITLE>Form for AddNum</TITLE></HEAD>\r\n");
                write.write("<BODY>");
                write.write("<H1> Addnum </H1>");
                write.write("\r\n\r\n");
                write.write("<FORM method='GET' action='http://localhost:2540/addnums.fake-cgi'>\r\n");
                write.write("\r\n\r\n");
                write.write("Enter your name and two numbers:\r\n");
                write.write("\r\n\r\n");
                write.write("<INPUT TYPE='text' NAME='person' size=20 value='YourName'><P>\r\n");
                write.write("\r\n\r\n");
                write.write("<INPUT TYPE='text' NAME='num1' size=5 value='4'><br>\r\n");
                write.write("\r\n\r\n");
                write.write("<INPUT TYPE='text' NAME='num2' size=5 value='5'><br>\r\n");
                write.write("\r\n\r\n");
                write.write("<INPUT TYPE='submit' VALUE='Submit Numbers'>\r\n");
                write.write("\r\n\r\n");
                write.write("</FORM>\r\n");
                write.write("<a href='javascript:history.back()'>Back</a><br>\r\n");
                write.write("\r\n\r\n");
                write.write("</BODY></HTML>\r\n");
                write.write("\r\n\r\n");
                write.close();
            }
        } catch (Exception ex) {
            System.out.println(ex + ": addnums program cannot be created.");
        }
    }

    /**
     * When server starts it calls this method to set the root directory to
     * where ever this server is running.
     */
    private static void setRootDirectory() {
        try {
            MyWebServer.rootDirectory = new File(".").getCanonicalFile();
        } catch (IOException ex) {
            System.out.println(ex + ": Root directory cannot be set.");
        }
    }

    /**
     * This method updates the index with the new directory location.
     *
     * @param newDir string of new directory location
     * @param oldDir string of old directory location
     * @return String of index file name
     */
    public synchronized static String getIndex(String newDir, String oldDir) {
        updateIndex(newDir, oldDir);
        String filename = index.getName();
        return filename;
    }

    /**
     * This method updates the index with the root directory.
     *
     * @return string of the index file name
     */
    public synchronized static String getIndex() {
        updateIndex("", root);
        String filename = index.getName();
        return filename;
    }

    /**
     * Private method which updates/creates the index for the directory.
     *
     * @param newDir string of new directory
     * @param oldDir string of old directory
     */
    private synchronized static void updateIndex(String newIndex, String currentDir) {
        try {
            //* delete the index if one exists then recreate dynamically as an html file */
            File tempIndex;
            File[] strFilesDirs;
            //* if the newDir location is not specified it defaults to root directory 
            //* also for security if there is a request to get a parent directory outside of
            //* the server directory/subdirectory it just defaults back to root. */
            if (newIndex.isEmpty() || "..".equals(new File(newIndex).getCanonicalFile().getName())) {
                tempIndex = new File("index.html");
                newIndex = root;
                strFilesDirs = rootDirectory.listFiles();
                System.out.println(newIndex);
            } else {
                String temp;
                temp = rootDirectory.getCanonicalPath();
                temp += currentDir.replace('/', File.separatorChar);
                temp += (File.separatorChar + "index.html");
                System.out.println(temp);
                tempIndex = new File(temp);
                tempIndex.createNewFile();
                strFilesDirs = (new File(newIndex).listFiles());
            }
            FileWriter file = null;
            file = new FileWriter(index);
            BufferedWriter out = new BufferedWriter(file);
            try (PrintWriter write = new PrintWriter(out)) {
                //* write the new index file with the new contents */
                write.write("<HTML>\r\n");
                write.write("<HEAD><TITLE>INDEX</TITLE>\r\n");
                write.write("</HEAD>\r\n");
                write.write("<BODY>\r\n");
                write.write(String.format("<H1>Index for %s</H1><br>\r\n", newIndex));
                write.write("<a href='javascript:history.back()'>Back</a><br>\r\n");
                for (int i = 0; i < strFilesDirs.length; i++) {
                    if (strFilesDirs[i].isDirectory()) {
                        String dir = strFilesDirs[i].getName();
                        String[] array = dir.split("\"");
                        String directory = array[array.length - 1];
                        write.write(String.format("<a href='%s'>Directory: %s</a><br>\r\n", currentDir + "/" + directory, directory));
                    } else if (strFilesDirs[i].isFile()) {
                        String f = strFilesDirs[i].getName();
                        String[] array = f.split("\"");
                        String filename = array[array.length - 1];
                        write.write(String.format("<a href='%s'>File: %s</a><br>\r\n", currentDir + "/" + filename, filename));
                    }
                }
                write.write("</BODY>\r\n");
                write.write("</HTML>\r\n");
                write.close();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage().toString() + "Index cannot be updated.");
        }
    }

    /**
     * This method prints out the remote address requested by the user/client.
     *
     * @param name of remote address to lookup
     * @param out printStream back to client
     */
    public synchronized static void writeToLog(String toLog) throws IOException {
        String rootDir = rootDirectory.getPath();
        rootDir += (File.separatorChar + SERVER_LOG);
        File file = new File(rootDir);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter toFile = new FileWriter(rootDir, true);
        BufferedWriter out = new BufferedWriter(toFile);
        PrintWriter write = new PrintWriter(out);
        write.write(toLog);
        write.println();
        write.close();
    }

    /**
     * This method prints out the local IP address and name to the console.
     */
    private static void printLocalAddress() {
        try {
            // gets local host and assign it to variable me */
            InetAddress me = InetAddress.getLocalHost();
            System.out.println("My local name is:      " + me.getHostName());
            System.out.println("My local IP address is: " + toText(me.getAddress()));
        } catch (UnknownHostException x) {
            System.out.println(x.getMessage().toString());
            System.out.println("I appear to be unknown to myself. Firewall?:");
        }
    }

    /**
     * This method makes a string of the IP address using a
     * StringBuffer/StringBuilder.
     *
     * @param ip byte array
     * @return String of IP array
     */
    private static String toText(byte ip[]) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ip.length; i++) {
            if (i > 0) {
                result.append(".");
            }
            result.append(0xff & ip[i]);
        }
        return result.toString();
    }

    /**
     * Main method which starts the server to loop and wait for incoming client
     * connections.
     */
    public static void main(String a[]) throws IOException {
        int q_len = 6; // Number of requests for OpSys to queue */
        int port = 2540; // start listening on port 2540 */
        Socket sock; // intialize Socket variable sock */

        printLocalAddress(); //* print local address info */
        setRootDirectory(); //* set the root directory to where this program is running */
        setAddNums(); //* setup the addnums html file for user input */
        updateIndex("", root); //* update the current index to this root directory */
        System.out.println("Kevin's WebServer is starting up, accepting connections at port 2540.\n");
        System.out.println("Press Ctrl-C to shutdown server."); //* only way to shut down server, is to send Ctrl-C command */

        // create a DIFFERENT thread for Back Door Channel via the BC looper */
        BCLooper AL = new BCLooper(); 
	Thread t = new Thread(AL); //* start it running it its own thread */
	t.start();  //* start it, waiting for Back Channel input */
        
        //intialize and set a new server socket to servsock which only accepts connections on the local machine */
        ServerSocket servsock = new ServerSocket(port, q_len);

        while (true) {
            // accept/listen for incoming connections */
            sock = servsock.accept();
            // new worker(thread) to handle client */
            new Worker(sock).start();
        }
    }
}
