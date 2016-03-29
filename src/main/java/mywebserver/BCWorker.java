package mywebserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * BC Worker thread which communicates on a back channel with the
 * BCClient/BCHandler.
 */
public class BCWorker extends Thread {

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