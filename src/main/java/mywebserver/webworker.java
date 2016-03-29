package mywebserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Class definition for worker class, extends the Java thread class; this class
 * handles the thread created by the MyWebServer Class; central interaction with
 * the web browser/HTTP requests.
 *
 * @author Kevin Patrick Westropp
 */
public class Worker extends Thread {

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

    public void printRequest(request){
        //* Uncomment the line below to log each request to a server log file */
        //MyWebServer.writeToLog(request.toString());
        //* otherwise it will print to the console */
        System.out.println(request.toString());
    }

    public void parseRequest(input){
        StringBuilder request = new StringBuilder(80);
            
        while (true) {
            int c = in.read();
            if (c == '\r' || c == '\n') {
                break;
            }
            request.append((char) c);
        }
        printRequest(request);
        
        return request.toString();
    }

    /**
     * Get I/O streams from the socket, override annotation to override Thread
     * which is inherited from parent class.
     */
    @Override
    public void run() {
        try {
            PrintStream out = new PrintStream(sock.getOutputStream());
            BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            String request = parseRequest(input);

            //* create String tokenizer to parse in request into specific elements */
            StringTokenizer st = new StringTokenizer(request);
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
