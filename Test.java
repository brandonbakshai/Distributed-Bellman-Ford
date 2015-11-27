import java.util.Date;

public class Test
{
    static int TIMEOUT; // timeout value in seconds

    // will return true if diff > 3 * TIMEOUT
    public static boolean dateCompare(Date dateOld, Date dateNew, int timeout)
    {
        Test.TIMEOUT = timeout;
        long oldMilli = dateOld.getTime();
        long newMilli = dateNew.getTime();

        return (newMilli - oldMilli) > (3 * TIMEOUT * 1000);
    }

    public static void main(String[] args) throws InterruptedException
    {
        Date dateOld = new Date();
        Thread.sleep(Integer.parseInt(args[0]));
        Date dateNew = new Date();
        System.out.println(dateCompare(dateOld, dateNew, Integer.parseInt(args[1])));
    }
}
