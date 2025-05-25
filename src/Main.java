import javax.swing.SwingUtilities;
import view.frontend.LoginFrame.AuthFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AuthFrame();
            }
        });
    }
}
