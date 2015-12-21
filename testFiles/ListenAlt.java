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
    DatagramSocket sendSock;
    DatagramPacket sendPack;


    public class Listen implements Runnable {

        public void run() {
            while (true) {
                try { sock = new DatagramSocket(21344); var = true;} // establish and bind socket
                catch (IOException e) { e.printStackTrace(); }
                while (var) {
                    try {
                        sock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                        sock.setReuseAddress(true);
                        pack = new DatagramPacket(new byte[100], 100);
                        System.err.println("Socket listening on port " + sock.getLocalPort());
                        sock.receive(pack);
                        System.err.println("Information received.");
                        System.err.println(new String(pack.getData()));
                    } catch (IOException e) { var = false; } // if sock timeouts, set var equal to false
                }
            }
        }
    }

    public class Send implements Runnable {

        public void run() {
            // while (true) {
                try {
                    sendSock = new DatagramSocket();
                    sendSock.connect(InetAddress.getByName("prague.clic.cs.columbia.edu"), 9998); // *** hardcoded address ***
                    sendSock.setSoTimeout(30 * 1000); // timeout for 30 seconds
                    sendPack = new DatagramPacket("this is my message".getBytes(), "this is my message".getBytes().length);
                    System.err.println("Socket ready on port " + sendSock.getLocalPort());
                    sendSock.send(sendPack);
                    System.err.println("Information sent.");
                    var = true;
                } catch (IOException e) { sendSock.close(); var = false; }
           //  }
        }
    }
    public static void main(String[] args) throws SocketException, InterruptedException
    {   
        ListenAlt client = new ListenAlt();
        
        System.out.println("Initial client var = " + client.var);

        Listen listen = client.new Listen();
        Thread threadListen = new Thread(listen);

        Send send = client.new Send();
        Thread threadSend = new Thread(send);

        threadListen.start();
        threadListen.join();
        // threadSend.start();
        // threadSend.join();


        System.out.println("Final client var = " + client.var);
    }
}
