brandon bakhshai
bab2209
Programming Assigment 3
This code works exactly as specified in the lab instructions


Client.java is my client class with the problem of sharing the global variable

ClientAlt.java is a reduction of the Client.java class to replicate just the sharing global variable problem. 
I found that the situation I set up should work, because it does in this class.

What I need to do right now:
    I have everything set up in terms of the mechanics of the communication.
    Every TIMEOUT seconds, or every time the client's distance vector changes 
        (whether through neighbors sending updates or local commands), the 
        new distance vector is sent out to all neigbors in the following format
            DISTANCE_VECTOR // beginning of message

            <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
            <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
            ...
            <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
            (newline) // end of message
    Now I need to set up the listening so that upon receipt of this message, 
        a client will process the message, update its distance vector according
        to distributed Bellman-Ford (where the heart of the program is). 
    The program is already set up to send out distance vector after getting an update
        from any other node, so if I process the date and modify the client's distance vector,
        it will as it is send out the new distance vector immediately.


    for each line above, the processing checks if the cost to a dest node + the cost to that node
        is greater is less than current cost to that node



SECOND PART
now I need implement the timeout values for each node, checking at most every TIMEOUT seconds if the timeout values have been passed
also need to make sure I am sending out the distance vector when info is received (what if is old foreign dv data) from other nodes


I have everything done but what I need to do now is to make a special
linkdown message. When you linkdown a neighbor from the home node, the home
node will deactivate the link but also the home node should send a message
(that the link distance is -1) to invalidate the link from the side of the
neighbor.

But then in this case when you linkup from the home node the neighbor node
needs to linkup as well.

So really the best method would be not to send anything from the home node
to the neighbor node so that it eventually sees that the home node is dead
on its own. So when the home node linkups the connection, the neighbor node
will reactivate automatically.
