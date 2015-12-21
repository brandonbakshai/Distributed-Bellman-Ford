import java.util.HashMap;

public class Test2
{
    public static void change(boolean input)
    {
        input = true;
    }
    
    public static void main(String[] args)
    {
        boolean change = false;
        
        System.out.println("initial change = " + change);
        change(change);
        System.out.println("initial change = " + change);
    }
}
