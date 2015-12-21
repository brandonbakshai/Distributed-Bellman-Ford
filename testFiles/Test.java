import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Test extends JFrame
{
    public Test() 
    {
        this.setTitle("Test Frame");
        this.setSize(400, 400);
        
        this.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
        });

        JPanel main = new JPanel(new BorderLayout());
        
        JButton button = new JButton("Submit");
        JTextField text = new JTextField("", 20);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(text);
        buttonPanel.add(button);
        
        JLabel row1 = new JLabel("Cost", 10);
        JTextField row2 = new JTextField("", 10);
        JTextField row3 = new JTextField("", 10);
        JTextField row4 = new JTextField("", 10);
        JTextField row5 = new JTextField("", 10);
        JTextField row6 = new JTextField("", 10);
        JTextField row7 = new JTextField("", 10);
        JTextField row8 = new JTextField("", 10);
        JTextField row9 = new JTextField("", 10);
        JTextField row10 = new JTextField("", 10);
        JTextField row11 = new JTextField("", 10);
        JTextField row12 = new JTextField("", 10);
        
        JLabel rowA1 = new JLabel("IP:Port", 10);
        JTextField rowA2 = new JTextField("", 10);
        JTextField rowA3 = new JTextField("", 10);
        JTextField rowA4 = new JTextField("", 10);
        JTextField rowA5 = new JTextField("", 10);
        JTextField rowA6 = new JTextField("", 10);
        JTextField rowA7 = new JTextField("", 10);
        JTextField rowA8 = new JTextField("", 10);
        JTextField rowA9 = new JTextField("", 10);
        JTextField rowA10 = new JTextField("", 10);
        JTextField rowA11 = new JTextField("", 10);
        JTextField rowA12 = new JTextField("", 10);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        infoPanel.add(row1);
        infoPanel.add(row2);
        infoPanel.add(row3);
        infoPanel.add(row4);
        infoPanel.add(row5);
        infoPanel.add(row6);
        infoPanel.add(row7);
        infoPanel.add(row8);
        infoPanel.add(row9);
        infoPanel.add(row10);
        infoPanel.add(row11);
        infoPanel.add(row12);

        JPanel infoPanelA = new JPanel();
        infoPanelA.setLayout(new BoxLayout(infoPanelA, BoxLayout.PAGE_AXIS));
        infoPanelA.add(rowA1);
        infoPanelA.add(rowA2);
        infoPanelA.add(rowA3);
        infoPanelA.add(rowA4);
        infoPanelA.add(rowA5);
        infoPanelA.add(rowA6);
        infoPanelA.add(rowA7);
        infoPanelA.add(rowA8);
        infoPanelA.add(rowA9);
        infoPanelA.add(rowA10);
        infoPanelA.add(rowA11);
        infoPanelA.add(rowA12);
        
        main.add(infoPanelA, BorderLayout.CENTER);
        main.add(infoPanel, BorderLayout.LINE_END);
        main.add(buttonPanel, BorderLayout.PAGE_END);
        
        this.add(main);
        
        this.show();


    }

    public static void main(String[] args) throws InterruptedException
    {
        Test test = new Test();
    }
}
