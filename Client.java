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
    static int TIMEOUT = 1000*1000;
    boolean up2date = true;
    JTextArea out;
    HashMap<String, Node> dVector;
    HashMap<String, Node> neighbors;
        
    public byte[] consolidate() 
    {
        StringBuffer data = new StringBuffer();
        data.append("DISTANCE_VECTOR\n");

        for (Node node : dVector.values()) 
        {
            String tmpName = node.addr.getHostName();
            int tmpPort = node.addr.getPort();
            int tmpCost = node.dist;
            data.append(tmpName); data.append(" ");
            data.append(tmpPort); data.append(" ");
            data.append(tmpCost); data.append(" ");
            data.append("\n");
        }

        data.append("\n");
        return data.toString().getBytes();
    }

    public void sendChanges() throws IOException {
        sendSock = new DatagramSocket();
        sendSock.setReuseAddress(true);
            
        byte[] tmpData = consolidate();
        for (Node node : neighbors.values()) 
        {
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
                dVector.put(tmp, new Node(tmp, port, cost));
                neighbors.put(tmp, new Node(tmp, port, cost));
                System.out.println(tmp + " " + port + " " + cost); // for testing
            }
        }


        public void actionPerformed(ActionEvent e) {
            try { sendChanges(); }
            catch (IOException err) { err.printStackTrace(); }
            listenSock.close();
            out.setText(out.getText() + "\n" + "sent");
        }
        
        public void run() {
            System.out.println("Command");
        }
    }

    // class to listen for distance vector updates from neighbors
    public class Listen implements Runnable {

        public void run() {
            while (true) {
                try { 
                    if (listenSock == null || listenSock.isClosed()) 
                      {
                          listenSock = new DatagramSocket(10002); 
                          listenSock.setReuseAddress(true);
                      }
                } // establish and bind socket
                catch (IOException e) { e.printStackTrace(); }
                try {
                    listenSock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                    listenSock.setReuseAddress(true);
                    listenPack = new DatagramPacket(new byte[1000], 1000);
                    System.err.println("Socket listening on port " + listenSock.getLocalPort());
                    listenSock.receive(listenPack);
                    System.err.println("Information received.");
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
        int dist;
        
        public Node(String name, int port, int distance)
        {
            try { addr = new InetSocketAddress(InetAddress.getByName(name), port); }
            catch (UnknownHostException e) { System.exit(1); }
            dist = distance;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length < 2) {
            System.out.println("usage: java Client <remote_host> <remote_port>");
            System.exit(1);
        }

        Client client = new Client();

        // initiate neighbor listening worker
        Listen listen = client.new Listen();
        Thread listenThread = new Thread(listen);

        // initiate sdin listening worker
        Command command = client.new Command();
        Thread commandThread = new Thread(command);

        listenThread.start();
        commandThread.start();
    }
}
