import java.util.*;
import java.net.*;
import java.io.IOException;

/**
 * Client class
 */
public class ClientAlt implements Runnable {

    public static void setup() throws IOException {
        DatagramSocket send = new DatagramSocket();
        send.setReuseAddress(true);
        DatagramPacket packet = new DatagramPacket(new byte[1000], 1000);
        InetSocketAddress sockAddr = new InetSocketAddress("tokyo.clic.cs.columbia.edu", 9998);

        while (true) {
            Scanner scanner = new Scanner(System.in);
            packet.setData(scanner.next().getBytes());
            packet.setSocketAddress(sockAddr);
            send.send(packet);
        }
    }

    
    public void run() {
        try { setup(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // class to listen for distance vector updates
    public static class Worker implements Runnable {

        public static void setup() throws IOException {
            DatagramSocket listen = new DatagramSocket(9998);
            listen.setReuseAddress(true);
            
            while (true) {
                byte[] buf = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                listen.receive(packet);
                System.out.println(new String(packet.getData()));
            }
        }

        
        public void run() {
            try { setup(); }
            catch (IOException e) { e.printStackTrace(); }
        }

    }

    // class to represent neighbors in distance vector
    public static class Neighbor {


    }

    public static void main(String[] args) throws SocketException
    {
        
        Worker worker = new Worker();
        Thread threadWorker = new Thread(worker);

        // Client client = new Client();
        // Thread threadClient = new Thread(client);

        threadWorker.start();
        //threadClient.start();
    }
}
