brandon bakhshai
bab2209
Programming Assigment 3


I have one main class BFClient and three nested classes, Command, Listen, and
Node. Command and Listen operate as threads independent from the main
thread, while the Node class is purely for data encapsulation. The Command
class/thread is just listening via the GUI for user commands. The Listen
class/thread is listening for distance vector updates from neighbors and
initiating processing, as well as sending host distance vector to neighbors.




The program is run as follows
    
    java Client <listen_port> <TIMEOUT> <DV_file>
    
the <DV_file> argument is the name of a file containing initial neighbor information (addresses and cost). 
I found this to be much cleaner and more convenient than just putting a set of triples as a command line argument. 
Hopefully you agree :) (or at least understand and don't deduct points).

Now, each DV_file is of the following format

    <hostname> <listening_port> <cost>
    <hostname> <listening_port> <cost>
    ...
    <source_hostname> <source_listening_port> 0
    ...
    <hostname> <listening_port> <cost>

    the <source_hostname> ... is just the same host and listening port
    as the home machine from which you are testing. So for example I
    assigned the host machine of file aDV to be for tokyo.clic.cs.columbia.edu:10005, 
    and therefore there is a line in aDV listing this address with cost of 0.




To run with my test example network:
    ssh into tokyo.clic.cs.columbia.edu
    ssh into athens.clic.cs.columbia.edu
    ssh into brussels.clic.cs.columbia.edu
    ssh into paris.clic.cs.columbia.edu
    type "make"
    on tokyo, type:
        java Client 10005 <timeout in seconds> aDV
    press enter 
    on athens, type:
        java Client 10004 <timeout in seconds> bDV
    press enter 
    on brussels, type:
        java Client 10003 <timeout in seconds> cDV
    press enter 
    on paris, type:
        java Client 10002 <timeout in seconds> dDV
    press enter 

    you can press enter - and therefore startup the nodes - in
    any combination you want, doesn't have to be tokyo then athens, etc.

    Now test with linkdown,linkup, close.
    
    then, whenever you would like, start up a node that is completely
    new to the network on prague (directions are same but listed below)
    and the network will quickly converge.

    ssh into prague.clic.cs.columbia.edu
    on prague, type:
        java Client 10006 <timeout in seconds> eDV
    press enter
    



To run with any example network:
    construct the DV files necessary for your network, making sure
    the DV file correspond to a node address has the node address listed
    in the file with 0 cost. 

    proceed as above, starting up each node with it's corresponding DV file

    it is important to make sure the DV files corresponds to the
    machine and address you are testing on.




Here is an outline of my designed protocol for route updates

    DISTANCE_VECTOR // beginning of message
    <cost from source to destination neighbor>
    <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
    <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
    ...
    <hostname> <listening_port> <cost> <next_hostname> <next_listening_port>
    (newline) // end of message

The "DISTANCE_VECTOR" to start introduces the message, only when a node
reads this line succesfully will it begin to process the route update
message. The newline at the end allows the processing node to detect the end
of the message.




How to input commands:
    I use a Java GUI for this, so you will just type the commands in the
    text field and press submit.
    Here are some exampes:
        linkdown tokyo.clic.cs.columbia.edu 10005
        linkup tokyo.clic.cs.columbia.edu 10005
        showrt
        close
