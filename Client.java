import java.util.*;
import java.net.*;
import java.io.*;
import java.io.IOException;

/**
 * Client class
 */
public class Client {

    DatagramSocket sendSock;
    DatagramSocket listen;
    DatagramPacket sendPack;
    InetSocketAddress sockAddr;
    static int TIMEOUT = 1000*1000;
    boolean up2date = true;

    public void setup(String host, int port) throws IOException {
        sendSock = new DatagramSocket();
        sendSock.setReuseAddress(true);
        // sendPack = new DatagramPacket(new byte[1000], 1000);
        String data = "There was a change!";
        sendPack = new DatagramPacket(data.getBytes(), data.getBytes().length);
        sockAddr = new InetSocketAddress(host, port);

        System.out.println(new String(sendPack.getData()));
     }
    
    public void sendChanges(String host, int port) throws IOException {
        setup(host, port);
        sendPack.setData("There was a change!".getBytes());
        sendPack.setSocketAddress(sockAddr);
        sendSock.send(sendPack);
        //up2date = true;
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

        public void setup() throws IOException {
            listen = new DatagramSocket(9997);
            listen.setReuseAddress(true);

            byte[] buf = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            listen.receive(packet);

            up2date = false;

            // System.out.println(up2date + " " + new String(packet.getData()));
        }
        
        public void run() {
            try { 
                while (true) {
                    System.out.println("Listen");
                    setup();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }

    }

    // class to represent neighbors in distance vector
    public class Neighbor {}

    public static void main(String[] args) throws IOException
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

        // listenThread.start();
        commandThread.start();

        // while (true) {

            System.out.println("starting inf loop");
        
            int i = 0;
            while (client.up2date)
                ;

            writer.println("broke out of up2date loop");
            writer.close();

            // client.listen.close();
            // client.sendChanges(args[0], Integer.parseInt(args[1]));
        // }
    
    }
}
