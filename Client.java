import java.util.*;
import java.net.*;

/**
 * Client class
 */
public class Client implements Runnable {

    public void run() {
        DatagramSocket send = new DatagramSocket();
        send.setReuseAddress(true);
        DatagramPacket packet = new DatagramPacket(new byte[1000], 1000);

        while (true) {
            Scanner scanner = new Scanner(System.in);
            packet.setData(scanner.next().getBytes());
            send.send(packet);
        }
    }

    // class to listen for distance vector updates
    public static class Worker implements Runnable {

        public void run() {
            DatagramSocket listen = new DatagramSocket(9997);
            listen.setReuseAddress(true);
            
            while (true) {
                byte[] buf = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                listen.receive(packet);
                System.out.println(Arrays.toString(packet.getData()));
            }
        }
    }

    // class to represent neighbors in distance vector
    public static class Neighbor {


    }

    public static void main(String[] args)
    {
        
        Worker worker = new Worker();
        Thread threadWorker = new Thread(worker);

        Client client = new Client();
        Thread threadClient = new Thread(client);

        threadWorker.start();
        threadClient.start();
    }
}
