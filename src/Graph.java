import java.io.*;
import java.util.*;

public class Graph {
    // inner class to represent edges with destination and weight
    private static class Edge implements Serializable {
        int destination;
        double weight;

        Edge(int destination, double weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }

    // list of all cities (vertices)
    private List<City> cities;
    // adjacency list representation (city index -> list of neighbor indices and distances)
    private Map<Integer, List<Edge>> adjacencyList;
    private double autonomy; // store autonomy used to create the graph

    public Graph() {
        cities = new ArrayList<>();
        adjacencyList = new HashMap<>();
    }

    // save graph to file
    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            // save autonomy, cities and adjacency list
            out.writeDouble(autonomy);
            out.writeObject(cities);
            out.writeObject(adjacencyList);
            System.out.println("Graph saved to file: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }

    // load graph from file
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            // read autonomy, cities and adjacency list
            this.autonomy = in.readDouble();
            this.cities = (List<City>) in.readObject();
            this.adjacencyList = (Map<Integer, List<Edge>>) in.readObject();
            System.out.println("Graph loaded from file: " + filename);
            System.out.println("Loaded " + cities.size() + " cities");
            System.out.println("Autonomy range: " + autonomy + "km");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading graph: " + e.getMessage());
        }
    }

    // build graph from CSV
    public void buildFromCSV(String filename, double autonomy) {
        this.autonomy = autonomy;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                // check if city is from Portugal, Spain, or France
                String country = data[4].replace("\"", "");
                if (country.equals("Portugal") || country.equals("Spain") || country.equals("France")) {
                    City city = new City(
                            data[1].replace("\"", ""),  // name
                            country,
                            Double.parseDouble(data[2].replace("\"", "")),  // latitude
                            Double.parseDouble(data[3].replace("\"", ""))   // longitude
                    );
                    cities.add(city);
                }
            }

            // build adjacency list based on autonomy range
            for (int i = 0; i < cities.size(); i++) {
                adjacencyList.put(i, new ArrayList<>());

                for (int j = 0; j < cities.size(); j++) {
                    if (i != j) {
                        double distance = cities.get(i).distanceTo(cities.get(j));
                        if (distance <= autonomy) {
                            adjacencyList.get(i).add(new Edge(j, distance));
                        }
                    }
                }
            }

            System.out.println("Loaded " + cities.size() + " cities");
            System.out.println("Created graph with autonomy range of " + autonomy + "km");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    // helper method to get city index by name
    public int getCityIndex(String name) {
        for (int i = 0; i < cities.size(); i++) {
            if (cities.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    // helper method to print connections for a city
    public void printCityConnections(String cityName) {
        int cityIndex = getCityIndex(cityName);
        if (cityIndex == -1) {
            System.out.println("City not found: " + cityName);
            return;
        }

        System.out.println("\nConnections from " + cityName + ":");
        List<Edge> connections = adjacencyList.get(cityIndex);
        for (Edge edge : connections) {
            System.out.printf("  To %s: %.2f km\n",
                    cities.get(edge.destination).name,
                    edge.weight);
        }
    }

    // Dijkstra's algorithm implementation
    public void findShortestPath(String sourceCity, String destCity) {
        int source = getCityIndex(sourceCity);
        int destination = getCityIndex(destCity);

        if (source == -1 || destination == -1) {
            System.out.println("Source or destination city not found!");
            return;
        }

        // initialize distances and previous nodes
        double[] distances = new double[cities.size()];
        int[] previous = new int[cities.size()];
        boolean[] visited = new boolean[cities.size()];

        // initialize all distances to infinity except source
        Arrays.fill(distances, Double.MAX_VALUE);
        Arrays.fill(previous, -1);
        distances[source] = 0;

        // priority queue to get the minimum distance vertex
        PriorityQueue<Integer> queue = new PriorityQueue<>(
                (a, b) -> Double.compare(distances[a], distances[b])
        );
        queue.offer(source);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == destination) {
                break; // found the destination
            }

            if (visited[current]) {
                continue;
            }

            visited[current] = true;

            // check all neighbors of current vertex
            for (Edge edge : adjacencyList.getOrDefault(current, new ArrayList<>())) {
                if (visited[edge.destination]) {
                    continue;
                }

                double newDist = distances[current] + edge.weight;

                if (newDist < distances[edge.destination]) {
                    distances[edge.destination] = newDist;
                    previous[edge.destination] = current;
                    queue.offer(edge.destination);
                }
            }
        }

        // message in case there's no possible path (with current autonomy)
        if (distances[destination] == Double.MAX_VALUE) {
            System.out.println("No path exists between " + sourceCity + " and " + destCity);
            return;
        }

        // reconstruct and print the path
        List<Integer> path = new ArrayList<>();
        for (int at = destination; at != -1; at = previous[at]) {
            path.add(at);
        }
        Collections.reverse(path);

        System.out.println("\nShortest path from " + sourceCity + " to " + destCity + ":");
        System.out.printf("Total distance: %.2f km\n", distances[destination]);
        System.out.println("Route:");

        for (int i = 0; i < path.size(); i++) {
            City city = cities.get(path.get(i));
            System.out.print(city.name);
            if (i < path.size() - 1) {
                double segmentDistance = cities.get(path.get(i)).distanceTo(cities.get(path.get(i + 1)));
                System.out.printf(" -> (%.2f km) -> ", segmentDistance);
            }
        }
        System.out.println();
    }
}