import java.util.*;
import java.net.*;
import java.io.IOException;
import java.lang.Thread;

/**
 * ListenAlt
 * The purpose of this class is to test the listening function 
 * of the worker thread.
 */
public class ListenAlt {

    static boolean var = true;
    DatagramSocket sock;
    DatagramPacket pack;


    public class Worker implements Runnable {

        public void run() {
            while (true) {
                try {
                    sock = new DatagramSocket(8888);
                    sock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                    pack = new DatagramPacket(new bytes[100], 100);
                    System.err.println("Socket listening on port " + sock.getPort());
                    sock.receive(pack);
                    System.err.println("Information received.");
                    System.err.println(new String(pack.getData()));
                } catch (IOException e) { sock.close(); var = false; }
            }
        }
    }

    public static void main(String[] args) throws SocketException, InterruptedException
    {   
        System.out.println("Initial client var = " + client.var);
        
        ListenAlt client = new ClientAlt();

        Worker worker = client.new Worker();
        Thread threadWorker = new Thread(worker);

        threadWorker.start();

        while (client.var)
            ;


        System.out.println("Final client var = " + client.var);
    }
}
