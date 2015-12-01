import java.net.InetAddress;
import java.net.*;

public class Test2
{
    public static void main(String[] args) throws UnknownHostException
    {
        InetAddress myAddr = InetAddress.getLocalHost();
        System.out.println(myAddr);
    }
}
