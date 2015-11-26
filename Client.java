import java.util.*;
import java.net.*;
import java.io.*;
import java.io.IOException;

/**
 * Client class
 */
public class Client {

    DatagramSocket sendSock;
    DatagramSocket listenSock;
    DatagramPacket sendPack;
    DatagramPacket listenPack;
    InetSocketAddress sockAddr;
    static int TIMEOUT = 1000*1000;
    boolean up2date = true;

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
        up2date = true;
    }

    // class to check standard input for commands
    public class Command implements Runnable {

        public void checkIn() throws IOException {
            InputStreamReader in = new InputStreamReader(System.in);
            char[] buf = new char[1000];
            while(in.read(buf, 0, 1000) > 0) {
                System.err.println("Command updated to up2date = " + up2date);
                up2date = false;
                System.err.println("Command updated to up2date = " + up2date);
            }
        }

        public void run() {
            while (true) {
                System.out.println("Command");
                try { checkIn(); }
                catch (IOException e) {e.printStackTrace(); }
            }
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
                          up2date = true;
                      }
                } // establish and bind socket
                catch (IOException e) { e.printStackTrace(); }
                while (up2date) {
                    try {
                        listenSock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                        listenSock.setReuseAddress(true);
                        listenPack = new DatagramPacket(new byte[100], 100);
                        System.err.println("Socket listening on port " + listenSock.getLocalPort());
                        listenSock.receive(listenPack);
                        System.err.println("Information received.");
                        System.err.println(new String(listenPack.getData()));
                    } catch (IOException e) { up2date = false; } // if sock timeouts, set var equal to false
                }
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

        PrintWriter writer = new PrintWriter("./output.txt", "UTF-8");
        
        Client client = new Client();

        // initiate neighbor listening worker
        Listen listen = client.new Listen();
        Thread listenThread = new Thread(listen);

        // initiate sdin listening worker
        Command command = client.new Command();
        Thread commandThread = new Thread(command);

        listenThread.start();
        // commandThread.start();

        while (true) {

            System.out.println("starting inf loop");
        
            int i = 0;
            while (client.up2date)
                Thread.sleep(100);

            System.err.println("broke out of up2date loop");

            client.listenSock.close();
            client.sendChanges(args[0], Integer.parseInt(args[1]));
        }
    
    }
}
