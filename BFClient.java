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
 * BFClient class
 */
public class BFClient extends JFrame {

    DatagramSocket sendSock;
    DatagramSocket listenSock;
    DatagramPacket sendPack;
    DatagramPacket listenPack;
    InetSocketAddress sockAddr;
    int TIMEOUT; // timeout in milliseconds
    boolean up2date = true;
    JTextField out;
    volatile HashMap<InetSocketAddress, Node> dVector;
    volatile HashMap<InetSocketAddress, Double> neighbors; // address as key and weight as value
    static int INF = 9999;
    int listenPort;
    InetSocketAddress homeAddr;
        
    public BFClient(int timeout, int port)
    {
        TIMEOUT = timeout;   
        listenPort = port;
        try {
            homeAddr = new InetSocketAddress(
                            InetAddress.getLocalHost(),
                            listenPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void printNeighbor() 
    {
        for (InetSocketAddress address : neighbors.keySet())
            System.err.println(address.getPort() + " " + neighbors.get(address)); 
    }


    // perform distributed Bellman-Ford processing
    public boolean process(DatagramPacket packet) throws UnknownHostException
    {
        boolean change = false;
        
        Scanner scanner = new Scanner(new String(packet.getData()));
        String tmp = "BADSTRING";

        // read start of message
        if (!scanner.nextLine().equals("DISTANCE_VECTOR"))
        {
            System.err.println("error reading packet data");
            return change;
        }
        
        Double distance2nb = scanner.nextDouble();
        
        InetSocketAddress srcAddr = new InetSocketAddress(
                scanner.next(), scanner.nextInt());

        // System.err.println("\n\nRECIEVING PACKET FROM: " + packet.getAddress() + " at time " + (new Date()) + "\n\n");

        // if message is valid and if source is new add to dVector
        // add to neighbors with same condition
        if (dVector.get(srcAddr) == null)
        {
            change = true;
            dVector.put(srcAddr, 
                    new Node(srcAddr, distance2nb, 
                                srcAddr));
            neighbors.put(srcAddr, (double) distance2nb);
        } 
        // else if message from unlinked neighbor
        else if (neighbors.get(srcAddr) < 0)
        {
            // System.err.println(srcAddr + " about to be ignored");
            return change;
        }
        // else if source is not new
        else
        {
            // update timestamp
            Node node = dVector.get(srcAddr);
            // System.err.println(srcAddr + " " + neighbors.get(srcAddr) + "packet source: " + packet.getAddress());
            node.date = new Date();
            dVector.put(node.addr, node);
            neighbors.put(node.addr, distance2nb);
        }

        // pass over sole "\n"
        scanner.nextLine();

        // read line by line, updating distance vector as neccessary
        while (!(tmp = scanner.nextLine()).equals(""))
        {
           Scanner tempScan = new Scanner(tmp);

           // create address for each distance vector item
           // from the foreign BFClient
           InetSocketAddress tempAddress = new InetSocketAddress(
                   InetAddress.getByName(tempScan.next()), tempScan.nextInt());
           
           // get the cost to that address
           double tempCost = tempScan.nextDouble();
           
           String tempTemp = tempScan.next();
           InetAddress nextAddrTmp = InetAddress.getByName(tempTemp);

           // create address for the "next" node from foreign DV
           InetSocketAddress tempNextAddress = new InetSocketAddress(
                   nextAddrTmp, tempScan.nextInt());

           Node dVectorNode = dVector.get(tempAddress);
           
           // if node does not exist in distance vector, add it
           if (dVectorNode == null) 
           {
                change = true;
                dVector.put(tempAddress,
                        new Node(tempAddress, tempCost, // current node 
                            srcAddr)); // next node
           } 
          
           // if node already exists in home distance vector, add it
           // only if the cost to the next node + cost from next node
           // to dest node is less than current distance for that node
           else
           {
                double sumCost = tempCost + distance2nb;

                // System.err.println("My choices are " + sumCost + 
                //         " or " + dVector.get(tempAddress).dist + " from " + 
                //         tempAddress.getPort());

                // if address of packet is same as that of node being considered
                /** if (srcAddr.equals(tempAddress) && 
                        distance2nb < dVector.get(tempAddress).dist)
                {
                    Node temporary = dVector.get(tempAddress);
                    temporary.dist = distance2nb;
                    temporary.next = new Node(tempAddress, 0, null);
                    dVector.put(tempAddress, temporary);
                }*/

                // Bellman-ford algo
                if (sumCost < dVector.get(tempAddress).dist) 
                {
                    change = true;
                    // need to update the value for the node
                    Node tmpNodeI = dVector.get(tempAddress);
                    tmpNodeI.addr = tempAddress;
                    tmpNodeI.dist = sumCost;
                    tmpNodeI.next = srcAddr;
                    dVector.put(tempAddress, 
                        tmpNodeI);
                }
                else if (srcAddr.equals(dVector.get(tempAddress).next))
                {
                    // change = true;
                    // need to update the value for the node
                    Node tmpNodeI = dVector.get(tempAddress);
                    tmpNodeI.addr = tempAddress;
                    tmpNodeI.dist = sumCost;
                    tmpNodeI.next = srcAddr;
                    dVector.put(tempAddress, 
                                tmpNodeI);
                }
           }

        }
        return change;
    }


    public boolean dateCompare(Date dateOld, Date dateNew)
    {
        long oldMilli = dateOld.getTime();
        long newMilli = dateNew.getTime();

        boolean ret = (newMilli - oldMilli) > (3 * TIMEOUT * 1000);
        
        // want to print out only the time
        // System.err.println("old: " + dateOld.toString() +
        //                    "\nnew: " + dateNew.toString() + 
        //                    "\nYIELDS: " + ret);
        
        return ret; 
    }

    public String consolidate() throws UnknownHostException 
    {
        StringBuffer data = new StringBuffer();
        data.append(InetAddress.getLocalHost().getHostAddress() + " ");
        data.append(listenPort + "\n");

        for (Node node : dVector.values()) 
        {
            String tmpName = node.addr.getAddress().getHostAddress();
            int tmpPort = node.addr.getPort();
            double tmpCost = node.dist;
            String tmpNextName = null;
            int tmpNextPort = 0;
            if (node.next != null) 
            { 
                tmpNextName = node.next.getAddress().getHostAddress();
                tmpNextPort = node.next.getPort();
            }
            data.append(tmpName); data.append(" ");
            data.append(tmpPort); data.append(" ");
            data.append(tmpCost); data.append(" ");
            data.append(tmpNextName); data.append(" ");
            data.append(tmpNextPort); data.append("\n");
        }

        data.append("\n");
        return data.toString();
    }

    // deactivate neighbor(address) link
    public void updateNeighb(InetSocketAddress address)
    {
        double dist = neighbors.get(address);
        dist*=-1;
        neighbors.put(address, dist);
    }

    // update entire DV to reflect deactivation of 
    // address link
    public void updateDV(InetSocketAddress address)
    {
        for (Node node : dVector.values())
        {
            if (address.equals(node.next))
            {
                node.dist+=INF;
                dVector.put(node.addr, node);
            }
        }

    }
    
    public void check4Dead(InetSocketAddress address)
    {
        Node node = dVector.get(address);
        double dVectorDist = node.dist;
        double dist = neighbors.get(address);
        if (dist < 0 || dVectorDist >= INF || address.equals(homeAddr))
            return ;

        if (dateCompare(node.date, new Date()))
        {
            updateDV(address);
            // System.err.println("looks like " + address + " is dead with dist " + node.dist);
            // System.err.println("with old date " + node.date + " and current date " + (new Date()));
        }
    }

    public void sendChanges() throws IOException 
    {
        sendSock = new DatagramSocket();      
        sendSock.setReuseAddress(true);

        Node homeNode = dVector.get(homeAddr);
        // homeNode.date = new Date();
        dVector.put(homeAddr, homeNode);
        
        String tmpString = consolidate();
        for (InetSocketAddress key : neighbors.keySet()) 
        {
            check4Dead(key); // will set node dist to INF if 3*TIMEOUT seconds passed

            // if dist is INF or more to neighbor
            // then the link has been shut down
            double dist = neighbors.get(key);
            double distDV = dVector.get(key).dist;

            if (dist < 0 || dateCompare(dVector.get(key).date, new Date()) || homeAddr.equals(key))
            { 
                continue;
            }
            
            // System.out.println(key + " " + dist);
            
            StringBuffer tmpBuffer = new StringBuffer();
            tmpBuffer.append("DISTANCE_VECTOR\n");
            tmpBuffer.append(dist + "\n");
            tmpBuffer.append(tmpString);
            byte[] tmpData = tmpBuffer.toString().getBytes();
            sendPack = new DatagramPacket(tmpData, tmpData.length, 
                        key.getAddress(), key.getPort());

            // System.err.println("TO " + key + " " + distDV);
            // System.err.println(new String(sendPack.getData()));
            //
            sendSock.send(sendPack);
        }
          
        sendSock.close(); 
    }

    // class to listen for commands
    // when a command is heard, a new DV will be sent to all neighbors and
    // the listen socket will be closed thereby restarting the listen thread
    public class Command extends JFrame implements Runnable, ActionListener 
    {
        public Command(String initDV) throws FileNotFoundException, UnknownHostException 
        {
            /* set up GUI */
            Container cp = getContentPane();
            cp.setLayout(new FlowLayout(FlowLayout.LEADING));
            JTextField field;
            JButton submit;
     
            submit = new JButton("Submit"); 
            field = new JTextField("", 20);
            field.setEditable(true);
            cp.add(field);
            cp.add(submit);
            cp.setComponentOrientation(
                    ComponentOrientation.LEFT_TO_RIGHT);
            
            submit.addActionListener(this);
            // field.setMaximumSize(getMaximumSize());
            JScrollPane scroll = new JScrollPane (field, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            cp.add(scroll);
            out = field;
          
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
            setTitle("DV program"); // "this" JFrame sets title
            setSize(370, 70);
            setVisible(true);

            /* set up initial distance vector */
            dVector = new HashMap<InetSocketAddress, Node>();
            neighbors = new HashMap<InetSocketAddress, Double>();
            Scanner scanner = new Scanner(new File(initDV));
            while (scanner.hasNext()) 
            {
                String tmp = scanner.next();
                int port = scanner.nextInt();
                double cost = scanner.nextDouble();
                InetSocketAddress tmpAddress = 
                    new InetSocketAddress(InetAddress.getByName(tmp), port);
                dVector.put(tmpAddress, 
                        new Node(tmpAddress, cost, tmpAddress)); // next node is null because not known
                neighbors.put(tmpAddress, cost);
                // System.out.println(tmpAddress.getAddress() + " " + tmpAddress.getPort() + " " + cost); // for testing
            }
        }

        public boolean linkdown(String command) throws UnknownHostException
        {
            Scanner scanner = new Scanner(command);
            boolean change = false;
            
            scanner.next(); // command
            
            String ip;
            int port;
           
            ip = scanner.next(); 
            port = scanner.nextInt();

            InetSocketAddress tmpAddr = 
                new InetSocketAddress(
                        InetAddress.getByName(ip),
                        port);

            double dist = neighbors.get(tmpAddr);
            if (dist < 0) ;
            else 
            {
                updateNeighb(tmpAddr);
                updateDV(tmpAddr);
                change = true;
                // System.out.println(tmpAddr + " " + neighbors.get(tmpAddr));
            }

            scanner.close();
            return change;
        }
        
        public boolean linkup(String command) throws UnknownHostException
        {
            Scanner scanner = new Scanner(command);
            boolean change = false;
            
            scanner.next(); // command
            
            String ip;
            int port;
           
            ip = scanner.next(); 
            port = scanner.nextInt();

            InetSocketAddress tmpAddr = 
                new InetSocketAddress(
                        InetAddress.getByName(ip),
                        port);

            double tmpNodeDist = neighbors.get(tmpAddr);
            Node tmpNode = dVector.get(tmpAddr);

            if (tmpNodeDist > 0) ;
            else 
            {
                tmpNodeDist*=-1;
                neighbors.put(tmpAddr, tmpNodeDist);
                
                tmpNode.date = new Date();
                dVector.put(tmpAddr, tmpNode);
                
                change = true;
                System.out.println(tmpAddr + " to " + neighbors.get(tmpAddr));
            }

            scanner.close();
            return change;
        }
        
        public void showrt()
        {
            System.out.println("DISTANCE VECTOR");
            System.out.println("IP | port | cost | next (IP) | next (port)");
            for (Node node : dVector.values())
            {
                InetAddress nextAddr = node.next.getAddress();
                int nextPort = node.next.getPort();

                System.out.println(node.addr.getAddress() + " " +
                        node.addr.getPort() + " " + 
                        node.dist + " " +
                        nextAddr + " " + 
                        nextPort);
            }
            System.out.println("");
        }
        
        public void close()
        {
            System.exit(1);
        }
        
        public boolean processCommand(String com) throws UnknownHostException 
        {
            if (com.equals("")) return false; 
        	
            Scanner scan = new Scanner(com);
        	String order = scan.next().toLowerCase();
            boolean change = false;
        	
            if (order.equals("linkdown")) change=linkdown(com);
            else if (order.equals("linkup")) change=linkup(com);
            else if (order.equals("showrt")) showrt();
            else if (order.equals("close")) close();
            else ; // confused(); // or just ";"?

            scan.close();

            return change;
        }

        // check if I should throw exception or catch
        public void actionPerformed(ActionEvent e)
        {    
            String userText = out.getText();
            out.setText("");

            // send dv if change is made
            try {
            if (processCommand(userText))
                    listenSock.close();
            } catch (UnknownHostException er) {
                er.printStackTrace(); }
        }
        
        public void run() {
            // System.out.println("Command");
        }
    }

    // class to listen for distance vector updates from neighbors
    public class Listen implements Runnable {

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
                    Date temporaryDate = new Date();
                    while (((new Date()).getTime() - temporaryDate.getTime())  < TIMEOUT * 1000)
                    {
                        listenSock.setReuseAddress(true);
                        listenPack = new DatagramPacket(new byte[1000], 1000);
                        listenSock.receive(listenPack);
                        // System.err.println("Information received");
                        process(listenPack);
                        // try { Thread.sleep(TIMEOUT * 100); }
                        // catch (InterruptedException error) { error.printStackTrace(); }
                    }
                    throw new IOException();
                    // printNeighbor();
                    // sendChanges();
                    // System.err.println(new String(listenPack.getData()) + "\n\n");
                    // want to send out dv if any changes have occurred
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
        InetSocketAddress next;
        double dist;
        Date date;
        
        public Node(String name, int port, double distance, String nameNext, int portNext)
        {
            try { addr = new InetSocketAddress(InetAddress.getByName(name), port); }
            catch (UnknownHostException e) { System.exit(1); }
            try { next = new InetSocketAddress(InetAddress.getByName(nameNext), portNext); }
            catch (UnknownHostException e) { System.exit(1); }
            dist = distance;
            date = new Date();
        }

        public Node(InetSocketAddress address, double distance, InetSocketAddress addressNext)
        {
            addr = address;
            dist = distance;
            next = addressNext;
            date = new Date();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length < 3) {
            System.out.println("usage: java BFClient <localport> <timeout> <DV_file>");
            System.exit(1);
        }

        BFClient client = new BFClient(Integer.parseInt(args[1]), Integer.parseInt(args[0]));

        // initiate neighbor listening worker
        Listen listen = client.new Listen();
        Thread listenThread = new Thread(listen);

        // initiate sdin listening worker
        Command command = client.new Command(args[2]);
        Thread commandThread = new Thread(command);

        client.sendChanges();

        listenThread.start();
        commandThread.start();
    }
}
