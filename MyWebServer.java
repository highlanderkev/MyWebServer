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
 * Basic data holder/transfer object for storing data and sending
 * data between server and client. Note: Had to change the name
 * or else it throws an exception because two of the same classes, 
 * if you are working out of the same directory and the BChandler
 * has the same class name.
 */
class myDaArray {

    int num_lines = 0;
    String[] lines = new String[10];
}

/**
 * BC Worker thread which communicates on a back channel with the
 * BCClient/BCHandler.
 */
class BCWorker extends Thread {

    private Socket sock; //* socket for the communication */
    private int i;

    /**
     * Basic constructor to create a new BC worker thread via a socket provided
     * by the BC looper.
     */
    BCWorker(Socket s) {
        sock = s;
    }
    PrintStream out = null; //* print stream to print data */
    BufferedReader in = null; //* buffered reader for reading data from socket */
    String[] xmlLines = new String[15]; //* string array for holding xml data */
    String[] testLines = new String[10]; //* string array for test lines */
    String xml;
    String temp;
    XStream xstream = new XStream(new DomDriver()); //* new xtream for marshalling/serializing xml data */
    final String newLine = System.getProperty("line.separator"); //* line seperator of the system */
    myDataArray da = new myDataArray(); //* data array object for storing deserialized data */

    /**
     * Main method which runs the thread of back channel worker.
     *
     */
    public void run() {
        System.out.println("Called BC worker.");
        try {
            //* create a buffered reader from reading data from the socket */
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream()); //* to send info back to client */
            i = 0;
            xml = "";
            StringBuilder sb = new StringBuilder();
            //* while loop to read in data, breaks when end of data is reached */
            while (true) {
                temp = in.readLine();
                if (temp.indexOf("end_of_xml") > -1) {
                    break;
                } else {
                    xml = xml + temp + newLine;
                }
            }
            System.out.println("The XML marshaled data:");
            System.out.println(xml);
            //* send the back acknowledgment that data was received */
            out.println("Acknowledging Back Channel Data Receipt"); 
            //* then flush and close socket */
            out.flush();
            sock.close();
            
            //* create an alias for myDataArray Class to stop resolution between class issues */
            xstream.alias("myDataArray", myDataArray.class);
            // deserialize and unmarshal  the data from xstream */
            da = (myDataArray)xstream.fromXML(xml);
            System.out.println("Here is the restored data: ");
            for (i = 0; i < da.num_lines; i++) {
                System.out.println(da.lines[i]);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage().toString());
        }
    }
}

/**
 * BCLooper Class which creates new sockets on the back channel for
 * marshalling/demarshalling xml data between the client and the server.
 */
class BCLooper implements Runnable {

    public static boolean adminControlSwitch = true; //* admin control switch */

    // run the Admin listen loop to communicate on a back channel with MyWebServer */
    public void run() {
        //* Print out that the BC looper is looping in it's own thread */
        System.out.println("In BC Looper thread, waiting for 2570 connections");

        int q_len = 6; /* Number of requests for OpSys to queue */
        int port = 2570;  // Listen on port 2570 for Back Channel Connections */
        Socket sock; // socket for incoming connections */

        try {
            // create a new server socket for incoming connections */
            ServerSocket servsock = new ServerSocket(port, q_len);
            while (adminControlSwitch) {
                // wait for the next ADMIN client connection:
                sock = servsock.accept();
                new BCWorker(sock).start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage().toString());
        }
    }
}

/*
 * This class is responsible for holding all the formatted HTTP responses
 * that will come up in the HTTP requests/MIME headers.
 */
class Response {

    Response thisResponse; //* reference to this object */
    @SuppressWarnings("deprecation") //* suppresswarnings for deprecated method toGMTString */
    String date = ("Date: " + new Date().toGMTString() + "\r\n");
    String standard = ("HTTP/1.1 200 OK\r\n");
    String notimpl = ("HTTP/1.1 501 Not Implemented\r\n");
    String forbidden = ("HTTP/1.1 403 Forbidden\r\n");
    String notfound = ("HTTP/1.1 404 Not Found\r\n");
    String moved = ("HTTP/1.1 301 Moved Permanently\r\n" + "Location: /");
    String serverType = ("Server: Java/1.7 (Windows 7)\r\n");
    String textContent = ("Content-Type: text/plain\r\n");
    String htmlContent = ("Content-Type: text/html\r\n");
    String jpegContent = ("Content-Type: image/jpeg\r\n");
    String gifContent = ("Content-Type: image/gif\r\n");
    String xyzContent = ("Content-Type: application/xyz\r\n");
    String classContent = ("Content-Type: application/octet-stream\r\n");
    String doubleReturn = ("\r\n\r\n"); //* double return to signal end of HTTP header */
    String contentLength = ("Content-Length: ");
    String closeConnection = ("Connection: close\r\n");
    String keepOpenConnection = ("Connection: Keep-Alive\r\n");

    /*
     * Basic constructor for new response class.
     */
    Response() {
    }

    /**
     * This is a method to fill in the name of the file that someone is
     * requesting.
     *
     * @param name of file
     * @return string for html file
     */
    String fileMoved(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(moved);
        sb.append(name);
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * This is a method to fill in content length; it can be used to return a
     * formated html line with the specified content length, although not used
     * currently.
     *
     * @param length of content
     * @return string of html with content length for MIME header
     */
    String addContentLength(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(contentLength);
        sb.append(length);
        sb.append("\r\n");
        return sb.toString();
    }
}

/**
 * Class definition for worker class, extends the Java thread class; this class
 * handles the thread created by the MyWebServer Class; central interaction with
 * the web browser/HTTP requests.
 *
 * @author Kevin Patrick Westropp
 */
class Worker extends Thread {

    //* a String to hold the current directory that this thread is in */
    String currentDirectory;
    //* Data holder class contains all the responses for the webserver. */
    Response response = new Response();
    //* Class member, socket, local to Worker. */
    Socket sock;

    /**
     * Worker Constructor to assign s to local socket for communication with
     * whatever client.
     */
    Worker(Socket s) {
        sock = s;
    }

    /**
     * Get I/O streams from the socket, override annotation to override Thread
     * which is inherited from parent class.
     */
    @Override
    public void run() {
        // intialize and set new PrintStream out to null */
        PrintStream out = null;
        // intialize and set new BufferedReader in to null */
        BufferedReader in = null;
        try {
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //* String builder for parsing the HTTP request */
            StringBuilder request = new StringBuilder(80);
            //* Read in request as individual charaters(ints) 
            //* Heavyliy influenced by "Java Network Programming"
            //* by Elliotte Rusty Harold - Chapter 10.2.2 HTTP Servers */
            int c;
            while (true) {
                c = in.read();
                if (c == '\r' || c == '\n') {
                    break;
                }
                request.append((char) c);
            }

            //* Uncomment the line below to log each request to a server log file */
            //MyWebServer.writeToLog(request.toString());
            //* otherwise it will print to the console */
            System.out.println(request.toString());

            //* create String tokenizer to parse in request into specific elements */
            StringTokenizer st = new StringTokenizer(request.toString());
            String method = st.nextToken();
            String version = "";
            String filename;

            //* if it is a GET request then we enter and try to locate the file or
            //* process the addnums input, otherwise we return a Forbidden operation */
            if (method.equals("GET")) {
                filename = st.nextToken();
                currentDirectory = filename;
                if (st.hasMoreTokens()) {
                    version = st.nextToken();
                }
                //* if the version is not implemented(not HTTP) we send back a not implemented page/code*/
                if (version.startsWith("HTTP")) {
                    if (filename.endsWith("/")) {
                        filename += "index.html";
                    }
                    while (filename.indexOf("/") == 0) {
                        filename = filename.substring(1);
                    }
                    //* change from internet char separator to machine dependent/file system separator
                    filename = filename.replace('/', File.separatorChar);
                    //* create an InputStream file for reading the index file */ 
                    InputStream file;
                    if (new File(filename).isDirectory()) {
                        String index = MyWebServer.getIndex(filename, currentDirectory);
                        file = new FileInputStream(index);
                        out.print(response.standard);
                        out.print(response.htmlContent);
                        out.print(response.closeConnection);
                        out.print(response.doubleReturn);
                        //* Taken from "Java Network Programming" by Harold
                        //* create a new byte array to read index file and then
                        //* write file out to the browser/client */
                        byte[] a = new byte[4096];
                        int n;
                        while ((n = file.read(a)) > 0) {
                            out.write(a, 0, n);
                        }
                        out.flush();
                        out.close();
                        return;
                    } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                        //* if file is an html or htm we send the proper response*/
                        out.print(response.standard);
                        out.print(response.htmlContent);
                        out.print(response.doubleReturn);
                    } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                        //* if file is an jpg or jpeg we send the proper response*/
                        out.print(response.standard);
                        out.print(response.jpegContent);
                        out.print(response.doubleReturn);
                    } else if (filename.endsWith(".gif")) {
                        //* if file is an gif we send the proper response*/
                        out.print(response.standard);
                        out.print(response.gifContent);
                        out.print(response.doubleReturn);
                    } else if (filename.endsWith(".class")) {
                        //* if file is an class we send the proper response so the web browser can download*/
                        out.print(response.standard);
                        out.print(response.classContent);
                        out.print(response.doubleReturn);
                    } else if (filename.endsWith(".xyz")) {
                        //* if file is .xyz we want to run our shim shell script*/
                        out.print(response.standard);
                        out.print(response.xyzContent);
                        out.print(response.doubleReturn);
                    } else if (filename.startsWith("addnums") && filename.contains("fake-cgi")) {
                        //* processing of the addnums html file happens in here instead of on the server 
                        //* we create an array of the GET for addnums cgi and parse it for sending back
                        //* the HTML response */
                        String[] array = filename.split("=");
                        String person = array[1];
                        String num1 = array[2];
                        String num2 = array[3];
                        int regex = person.indexOf("&");
                        person = person.substring(0, regex);
                        regex = num1.indexOf("&");
                        num1 = num1.substring(0, regex);
                        int firstNum = Integer.parseInt(num1);
                        int secondNum = Integer.parseInt(num2);
                        int compute = firstNum + secondNum;
                        out.print(response.standard);
                        out.print(response.htmlContent);
                        out.print(response.doubleReturn);
                        out.print("<HTML>\r\n");
                        out.print("<HEAD><TITLE>Computed</TITLE>\r\n");
                        out.print("</HEAD>\r\n");
                        out.print("<BODY>\r\n");
                        out.print(String.format("<H1>Congratulations %s! Your computed number is %s!</H1>\r\n", person, String.valueOf(compute)));
                        out.print("<a href='javascript:history.back()'>Back</a><br>\r\n");
                        out.print("</BODY></HTML>\r\n");
                        out.flush();
                        out.close();
                    } else {
                        //* else response is just standard text and we try sending it as such */
                        out.print(response.standard);
                        out.print(response.textContent);
                        out.print(response.doubleReturn);
                    }
                    //send file if it exists */
                    file = new FileInputStream(filename);
                    //* Taken from "Java Network Programming" by Harold
                    //* create a new byte array to read file and then
                    //* write file out to the browser/client */
                    byte[] a = new byte[4096];
                    int n;
                    while ((n = file.read(a)) > 0) {
                        out.write(a, 0, n);
                    }
                    out.flush();
                    out.close();
                } else {
                    //* if request is not HTTP we send a not implemented response */
                    out.print(response.notimpl);
                    out.print(response.date);
                    out.print(response.serverType);
                    out.print(response.htmlContent);
                    out.print(response.doubleReturn);
                    out.print("<HTML>\r\n");
                    out.print("<HEAD><TITLE>Not Implemented</TITLE>\r\n");
                    out.print("</HEAD>\r\n");
                    out.print("<BODY>\r\n");
                    out.print("<H1>HTTP Error 501: Not Implemented</H1>\r\n");
                    out.print("</BODY></HTML>\r\n");
                    out.flush();
                    out.close();
                }
            } else {
                //* if request is not GET we send a forbidden operation response */
                out.print(response.forbidden);
                out.print(response.date);
                out.print(response.serverType);
                out.print(response.htmlContent);
                out.print(response.doubleReturn);
                out.print("<HTML>\r\n");
                out.print("<HEAD><TITLE>Forbidden</TITLE>\r\n");
                out.print("</HEAD>\r\n");
                out.print("<BODY>\r\n");
                out.print("<H1>HTTP Error 403: Operation Forbidden</H1>\r\n");
                out.print("</BODY></HTML>\r\n");
                out.flush();
                out.close();
            }
        } catch (IOException ex) {
            //* an exception occured but the browser is still waiting for something so we
            //* send back a File Not Found response and print the error to the console.
            //* we could also change to log all errors to the server log */
            System.out.println("Error: " + ex.getMessage().toString());
            out.print(response.notfound);
            out.print(response.date);
            out.print(response.serverType);
            out.print(response.htmlContent);
            out.print(response.doubleReturn);
            out.print("<HTML>\r\n");
            out.print("<HEAD><TITLE>File Not Found</TITLE>\r\n");
            out.print("</HEAD>\r\n");
            out.print("<BODY>\r\n");
            out.print("<H1>HTTP Error 404: File Not Found</H1>\r\n");
            out.print("</BODY></HTML>\r\n");
            out.flush();
            out.close();
        }
    }
}

/**
 * A multi-threaded Web server for web browsers which can handle multiple
 * requests/clients for HTTP requests and returns a dynamic index html of all
 * the server's contents/directories and subdirectories (no super-directories)
 * and by using the worker class above.
 */
public class MyWebServer {

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
