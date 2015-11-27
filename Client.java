import java.util.*;
import java.net.*;
import java.io.*;
import java.io.IOException;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;


/**
 * Client class
 */
public class Client extends JFrame {

    DatagramSocket sendSock;
    DatagramSocket listenSock;
    DatagramPacket sendPack;
    DatagramPacket listenPack;
    InetSocketAddress sockAddr;
    int TIMEOUT; // timeout in milliseconds
    boolean up2date = true;
    JTextArea out;
    HashMap<String, Node> dVector;
    HashMap<String, Node> neighbors;
    static int INF = 9999;
        
    public Client(int timeout)
    {
        TIMEOUT = timeout;   
    }

    // perform distributed Bellman-Ford processing
    public boolean process(DatagramPacket packet)
    {
        Scanner scanner = new Scanner(new String(packet.getData()));
        String tmp = "BADSTRING";
        String srcIP = packet.getAddress().getHostName();
        int tmpPort = packet.getPort();

        // read start of message
        if (!scanner.next().equals("DISTANCE_VECTOR"))
        {
            System.err.println("error reading packet data");
            return false;
        }

        int neighbCost = scanner.nextInt();

        // if message is valid, add source to distance vector if new
        if (dVector.get(srcIP) == null || 
                (dVector.get(srcIP).addr.getPort() != tmpPort))
        {
            dVector.put(srcIP, 
                    new Node(srcIP, tmpPort, neighbCost, 
                        new Node(srcIP, tmpPort, 0, null))); // where is the cost going to come from ?
        }
        
        // read line by line, updating distance vector as neccessary
        while (!(tmp = scanner.nextLine()).equals(""))
        {
           Scanner tmpTmpScan = new Scanner(tmp);
           String tmpTmpName = tmpTmpScan.next();
           int tmpTmpPort = tmpTmpScan.nextInt();
           int tmpTmpCost = tmpTmpScan.nextInt();
           String tmpNextName = tmpTmpScan.next();
           int tmpNextPort = tmpTmpScan.nextInt();

           Node dVectorNode = dVector.get(tmpTmpName);
           
           // if node does not exist in distance vector, add it
           if (dVectorNode == null) 
           {
                dVector.put(tmpTmpName,
                        new Node(tmpTmpName, tmpTmpPort, tmpTmpCost,
                            new Node(srcIP, tmpPort, 0, null)));
           } 
           
           // if node already exists in distance vector, add it
           // only if the cost to the next node + cost from next node
           // to dest node is less than current distance for that node
           else
           {
                int sumCost = tmpTmpCost + neighbCost;
                if (sumCost < dVector.get(tmpTmpName).dist)
                {
                    // need to update the value for the node
                    dVector.put(tmpTmpName, 
                            new Node(tmpTmpName, tmpTmpPort, sumCost,
                                new Node(srcIP, tmpPort, 0, null)));
                }
           }

        }
        return true;
    }


    public boolean dateCompare(Date dateOld, Date dateNew)
    {
        long oldMilli = dateOld.getTime();
        long newMilli = dateNew.getTime();

        return (newMilli - oldMilli) > (3 * TIMEOUT * 1000);
    }

    public StringBuffer consolidate() 
    {
        StringBuffer data = new StringBuffer();
        data.append("DISTANCE_VECTOR\n");

        for (Node node : dVector.values()) 
        {
            String tmpName = node.addr.getHostName();
            int tmpPort = node.addr.getPort();
            int tmpCost = node.dist;
            String tmpNextName = null;
            int tmpNextPort = 0;
            if (node.next != null) 
            { 
                tmpNextName = node.next.addr.getHostName();
                tmpNextPort = node.next.addr.getPort();
            }
            data.append(tmpName); data.append(" ");
            data.append(tmpPort); data.append(" ");
            data.append(tmpCost); data.append(" ");
            data.append(tmpNextName); data.append(" ");
            data.append(tmpNextPort); data.append("\n");
        }

        data.append("\n");
        return data;
    }

    public void sendChanges() throws IOException {
        sendSock = new DatagramSocket();
        sendSock.setReuseAddress(true);
            
        StringBuffer tmpBuffer = consolidate();
        for (Node node : neighbors.values()) 
        {
            tmpBuffer.append(node.dist);
            byte[] tmpData = tmpBuffer.toString().getBytes();
            sendPack = new DatagramPacket(tmpData, tmpData.length, 
                        node.addr.getAddress(), node.addr.getPort());
            sendSock.send(sendPack);
        }
        sendSock.close();
    }

    // class to listen for commands
    // when a command is heard, a new DV will be sent to all neighbors and
    // the listen socket will be closed thereby restarting the listen thread
    public class Command extends JFrame implements Runnable, ActionListener 
    {
        public Command() throws FileNotFoundException 
        {
            /* set up GUI */
            Container cp = getContentPane();
            cp.setLayout(new BorderLayout());
            JTextArea field;
            JButton submit;
     
            submit = new JButton("Submit"); 
            field = new JTextArea("");
            field.setEditable(true);
            cp.add(field, BorderLayout.PAGE_START);
            cp.add(submit, BorderLayout.PAGE_END);
            submit.addActionListener(this);
            // field.setMaximumSize(getMaximumSize());
            JScrollPane scroll = new JScrollPane (field, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            cp.add(scroll);
            out = field;
          
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
            setTitle("DV program"); // "this" JFrame sets title
            setSize(300, 300);
            setVisible(true);

            /* set up initial distance vector */
            dVector = new HashMap<String, Node>();
            neighbors = new HashMap<String, Node>();
            Scanner scanner = new Scanner(new File("./triples"));
            while (scanner.hasNext()) 
            {
                String tmp = scanner.next();
                int port = scanner.nextInt();
                int cost = scanner.nextInt();
                dVector.put(tmp, 
                        new Node(tmp, port, cost, null)); // next node is null because not known
                neighbors.put(tmp, 
                        new Node(tmp, port, cost, 
                            new Node(tmp, port, 0, null))); // next node is itself
                System.out.println(tmp + " " + port + " " + cost); // for testing
            }
        }


        public void actionPerformed(ActionEvent e) {
            // try { sendChanges(); }
            // catch (IOException err) { err.printStackTrace(); }
            listenSock.close();
            out.setText(out.getText() + "\n" + "sent");
        }
        
        public void run() {
            System.out.println("Command");
        }
    }

    // class to listen for distance vector updates from neighbors
    public class Listen implements Runnable {

        int listenPort;

        public Listen(int port)
        {
            listenPort = port;
        }

        public void run() {
            while (true) {
                try { 
                    if (listenSock == null || listenSock.isClosed()) 
                      {
                          listenSock = new DatagramSocket(listenPort); 
                          listenSock.setReuseAddress(true);
                      }
                } // establish and bind socket
                catch (IOException e) { e.printStackTrace(); }
                try {
                    listenSock.setSoTimeout(TIMEOUT * 1000); // timeout for TIMEOUT seconds
                    listenSock.setReuseAddress(true);
                    listenPack = new DatagramPacket(new byte[1000], 1000);
                    System.err.println("Socket listening on port " + listenSock.getLocalPort());
                    listenSock.receive(listenPack);
                    System.err.println("Information received.");
                    process(listenPack); // if packet received, process it
                    System.err.println(new String(listenPack.getData()));
                } catch (IOException e) { 
                    try { sendChanges(); }
                    catch (IOException er) { er.printStackTrace(); }
                }
            }
        }        
    }
        
    // class to represent neighbors in distance vector
    public class Node 
    {
        InetSocketAddress addr;
        Node next;
        int dist;
        
        public Node(String name, int port, int distance, Node nextNode)
        {
            try { addr = new InetSocketAddress(InetAddress.getByName(name), port); }
            catch (UnknownHostException e) { System.exit(1); }
            dist = distance;
            next = nextNode;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length < 2) {
            System.out.println("usage: java Client <localport> <timeout>");
            System.exit(1);
        }

        Client client = new Client(Integer.parseInt(args[1]));

        // initiate neighbor listening worker
        Listen listen = client.new Listen(Integer.parseInt(args[0]));
        Thread listenThread = new Thread(listen);

        // initiate sdin listening worker
        Command command = client.new Command();
        Thread commandThread = new Thread(command);

        listenThread.start();
        commandThread.start();
    }
}
