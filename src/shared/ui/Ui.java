package shared.ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
public final class Ui {
    private Ui(){}
    public static JPanel page(){JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(14,14,14,14));return p;}
    public static void error(Component parent,Throwable ex){String m=ex.getMessage();if(m==null||m.isBlank())m=ex.getClass().getSimpleName();JOptionPane.showMessageDialog(parent,m,"Operation failed",JOptionPane.ERROR_MESSAGE);}
    public static void info(Component parent,String m){JOptionPane.showMessageDialog(parent,m,"Supermarket",JOptionPane.INFORMATION_MESSAGE);}
    public static void style(JFrame f){f.setMinimumSize(new Dimension(880,560));f.setLocationRelativeTo(null);}
}
