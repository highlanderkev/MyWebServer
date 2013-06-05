My Web Server
======================
* Simple Java Web Server : [view](https://github.com/highlanderkev/MyWebServer/blob/master/MyWebServer.java)

This is a Multi-threaded web server that I wrote for CSC 435 - Distributed Systems. 

Current Design:
---------------
Currently, this web server makes a dynamic index page of whatever directory it is started in which it then serves back to the client (Web Browser in this case).

Using this hyperlinked HTML page, the user can then navigate the file system of where ever it is deployed (the Web Server that is). 

It only allows for the current directory and any sub-directories. 

*IMPORTANT - NO super directories, because then the user could do some nasty things (like deleting the entire file system of the computer/web server).

Looking to the future:
----------------------

I hope to get some free time to continue to improve on this basic design. 

Perhaps even develop a more robust version with more security built in. 

  It then gives this index back to the client for  The client then can access any files in those directories.