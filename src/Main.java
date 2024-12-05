import java.io.File;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();
        String graphFile = "src/resources/savedGraph.dat";

        // build new graph and save it
        graph.buildFromCSV("src/resources/worldcities.csv", 300);
        graph.saveToFile(graphFile);

        // test the shortest path functionality
        System.out.println("\nTesting shortest paths:");
        graph.findShortestPath("Entroncamento", "Paris");

        // TEST - print connections from Lisbon
        //graph.printCityConnections("Lisbon");
    }
}