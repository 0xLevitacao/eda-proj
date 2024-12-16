import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Nimbus look and feel
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Nimbus look and feel not available");
            }

            // Create and show GUI
            new RouteCalculatorGUI().setVisible(true);
        });
    }
}