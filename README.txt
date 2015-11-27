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
            <hostname> <listening_port> <cost>
            <hostname> <listening_port> <cost>
            ...
            <hostname> <listening_port> <cost>
            (newline) // end of message
    Now I need to set up the listening so that upon receipt of this message, 
        a client will process the message, update its distance vector according
        to distributed Bellman-Ford (where the heart of the program is). 
    The program is already set up to send out distance vector after getting an update
        from any other node, so if I process the date and modify the client's distance vector,
        it will as it is send out the new distance vector immediately.
