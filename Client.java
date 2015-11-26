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

    // class to listen for commands
    // when a command is heard, a new DV will be sent to all neighbors and
    // the listen socket will be closed thereby restarting the listen thread
    public class Command extends JFrame implements Runnable, ActionListener {
    
        public Command() 
        {
            Container cp = getContentPane();
            cp.setLayout(new GridLayout());
            JTextArea field;
            JButton submit;
     
            submit = new JButton("Submit"); 
            field = new JTextArea("");
            field.setEditable(true);
            cp.add(field);
            cp.add(submit);
            submit.addActionListener(this);
            field.setMaximumSize(getMaximumSize());
            JScrollPane scroll = new JScrollPane (field, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            cp.add(scroll);
            out = field;
          
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
            setTitle("DV program"); // "this" JFrame sets title
            setSize(300, 300);         // "this" JFrame sets initial size
            setVisible(true);
        }

        public void sendChanges(String host, int port) throws IOException {
            sendSock = new DatagramSocket();
            sendSock.setReuseAddress(true);
            // sendPack = new DatagramPacket(new byte[1000], 1000);
            String data = "There was a change!";
            sendPack = new DatagramPacket(data.getBytes(), data.getBytes().length);
            sockAddr = new InetSocketAddress(host, port);

            System.out.println(new String(sendPack.getData()));
            sendPack.setSocketAddress(sockAddr);
            sendSock.send(sendPack);
        
            sendSock.close();
        }

        public void actionPerformed(ActionEvent e) {
            try { sendChanges("athens.clic.cs.columbia.edu", 21344); }
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
                          listenSock = new DatagramSocket(21344); 
                          listenSock.setReuseAddress(true);
                      }
                } // establish and bind socket
                catch (IOException e) { e.printStackTrace(); }
                try {
                    listenSock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                    listenSock.setReuseAddress(true);
                    listenPack = new DatagramPacket(new byte[100], 100);
                    System.err.println("Socket listening on port " + listenSock.getLocalPort());
                    listenSock.receive(listenPack);
                    System.err.println("Information received.");
                    System.err.println(new String(listenPack.getData()));
                } catch (IOException e) { ; } // if sock timeouts, set var equal to false
            }
        }        
    }
        
        /**
        public void setup() throws IOException {
            listen = new DatagramSocket(9997);
            listen.setReuseAddress(true);
            listen.setSoTimeout(30 * 1000);

            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            listen.receive(packet);

            up2date = false;

            System.out.println(new String(packet.getData()));
        }
        
        public void run() {
            while (true) {
                try { 
                    System.out.println("Listen");
                    setup();
                } catch (IOException e) { up2date = false; }
            }
        }*/

    // class to represent neighbors in distance vector
    public class Neighbor {}

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
