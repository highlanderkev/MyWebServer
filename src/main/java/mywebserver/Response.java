package mywebserver;

import java.io.*;
import java.net.*;
import java.util.*;

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