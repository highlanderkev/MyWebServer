package mywebserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * BCLooper Class which creates new sockets on the back channel for
 * marshalling/demarshalling xml data between the client and the server.
 */
public class BCLooper implements Runnable {

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