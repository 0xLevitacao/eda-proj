import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class RouteCalculatorGUI extends JFrame {
    private Graph graph;
    private JComboBox<String> sourceCity;
    private JComboBox<String> destinationCity;
    private JTextArea resultArea;
    private JTextField autonomyField;
    private JButton calculateButton;
    private JButton loadDataButton;

    public RouteCalculatorGUI() {
        // Set up the frame
        setTitle("Route Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize graph
        graph = new Graph();

        // Create panels
        JPanel inputPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Add padding
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // GridBag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create components
        sourceCity = new JComboBox<>();
        destinationCity = new JComboBox<>();
        autonomyField = new JTextField(10);
        calculateButton = new JButton("Calculate Route");
        loadDataButton = new JButton("Load Data");
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);

        // Add components to input panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Vehicle Range (km):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(autonomyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Source City:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(sourceCity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Destination City:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(destinationCity, gbc);

        // Add buttons to button panel
        buttonPanel.add(loadDataButton);
        buttonPanel.add(calculateButton);

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // Add button listeners
        loadDataButton.addActionListener(e -> handleDataLoad());
        calculateButton.addActionListener(e -> calculateRoute());

        // Set default autonomy
        autonomyField.setText("300");

        // Finalize frame
        pack();
        setLocationRelativeTo(null);
    }

    private void handleDataLoad() {
        try {
            double autonomy = Double.parseDouble(autonomyField.getText());

            // Build new graph from CSV with specified autonomy
            graph.buildFromCSV("src/resources/worldcities.csv", autonomy);

            // Save it for future use
            graph.saveToFile("src/resources/savedGraph.dat");

            updateCityLists();
            resultArea.setText("Data loaded successfully!\nReady to calculate routes.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number for autonomy",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculateRoute() {
        if (sourceCity.getSelectedItem() == null || destinationCity.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both source and destination cities",
                    "Missing Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String source = sourceCity.getSelectedItem().toString();
        String destination = destinationCity.getSelectedItem().toString();

        // Redirect System.out to capture the output
        PrintStreamCapturer capturer = new PrintStreamCapturer(resultArea, destination);
        System.setOut(new PrintStream(capturer));

        // Calculate and display route
        graph.findShortestPath(source, destination);
    }

    private void updateCityLists() {
        sourceCity.removeAllItems();
        destinationCity.removeAllItems();

        // Add all cities to both combo boxes
        for (City city : graph.cities) {
            sourceCity.addItem(city.name);
            destinationCity.addItem(city.name);
        }
    }

    // Helper class to capture System.out and redirect to JTextArea
    private class PrintStreamCapturer extends OutputStream {
        private JTextArea textArea;
        private StringBuilder buffer;
        private String destination;

        public PrintStreamCapturer(JTextArea textArea, String destination) {
            this.textArea = textArea;
            this.buffer = new StringBuilder();
            this.destination = destination;
        }

        @Override
        public void write(int b) {
            char c = (char) b;
            buffer.append(c);
            if (c == '\n') {
                final String text = buffer.toString();

                // If this is the route line (contains "->"), transform it
                if (text.contains("->")) {
                    String[] parts = text.split(" -> ");
                    StringBuilder formattedText = new StringBuilder();

                    // Add the header lines if they exist in buffer
                    int routeIndex = buffer.toString().indexOf("Route:");
                    if (routeIndex >= 0) {
                        formattedText.append(buffer, 0, routeIndex + 6).append("\n");
                    }

                    // Format the route with each segment on its own line
                    for (int i = 0; i < parts.length - 2; i += 2) {
                        formattedText.append(parts[i]).append(" -> ").append(parts[i + 1]).append("\n");
                    }

                    // Add the final city
                    formattedText.append(destination);

                    SwingUtilities.invokeLater(() -> textArea.setText(formattedText.toString()));
                } else {
                    SwingUtilities.invokeLater(() -> textArea.setText(text));
                }

                buffer.setLength(0);
            }
        }
    }


}