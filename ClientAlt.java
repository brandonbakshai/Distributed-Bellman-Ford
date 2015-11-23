import java.util.*;
import java.net.*;
import java.io.IOException;
import java.lang.Thread;

/**
 * Client class
 */
public class ClientAlt {

    static boolean var = true;


    public class Command implements Runnable {
        
        public void foo() {
            var = false;
        }

        public void run() {
            System.out.println("Command thread var = " + var);
            foo();
            System.out.println("Command thread var = " + var);
        }
    }


    public class Worker implements Runnable {

        public void run() {
            // var = 5;
            System.out.println("Worker thread var = " + var);
        }
    }

    public static void main(String[] args) throws SocketException, InterruptedException
    {
        ClientAlt client = new ClientAlt();

        System.out.println("Initial client var = " + client.var);

        Worker worker = client.new Worker();
        Thread threadWorker = new Thread(worker);

        Command command = client.new Command();
        Thread threadCommand = new Thread(command);

        threadWorker.start();
        threadCommand.start();

        while (client.var)
            ;


        System.out.println("Final client var = " + client.var);
    }
}
