import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * DO NOT COPY, MODIFY OR DISTRIBUTE
 *
 * - Created by Pride
 */
public class MetroGraph {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		System.out.print("Input a time: "); // User input (time)
		String inputTime = input.nextLine();

		System.out.print("\nInput a day: "); // User input (day)
		String inputDay = input.nextLine();

		Graph<Node, Edge> graph = new MetroGraph().generateGraph(inputTime, inputDay);

		if (!graph.getNodes().isEmpty()) {
			System.out.print("\nInput an origin: "); // User input (origin)
			String origin = input.nextLine();

			System.out.print("\nInput a destination: "); // User input (destination)
			String destination = input.nextLine();

			input.close();

			/*
			We will perform Dijkstra's algorithm to calculate the shortest path between two Nodes/stations.
			*/
			System.out.println("Shortest Path: " + graph.dijkstras(origin, destination));
			System.out.println("Shortest Walk Path: " + graph.shortestWalkPath(origin, destination));
		}
	}

	/**
	 * What we will do here is attempt to generate a graph given the user inputs of the certain time and day.
	 *
	 * We will store certain data so that we can use it when we create new nodes and edges (time, day, whether we are in rush hours and day type).
	 * We then scan the csv file so that we can enter data of the stations, trains available, conditions, and coordinates.
	 *
	 * Using the data that we accessed and inputted, we now assign them to new Node objects (which represent our stations),
	 * which then calculate the trains available according to our data.
	 *
	 * We can then create Edges simply by instantiating Edge objects that have been assigned from Nodes and to Nodes according to
	 * the trains at each station.
	 *
	 * Finally, we'll create a Graph object that takes in a Set of Nodes and a Set of Edges.
	 *
	 * @return our Graph
	 */
	public Graph<Node, Edge> generateGraph(String inputTime, String inputDay) {
		try {
			if (Time.validate(inputTime) && Day.validate(inputDay)) {
				int time = Integer.valueOf(inputTime);

				/*
				Here we'll read the csv file and access its data
				 */
				Scanner scanner;
				try {
					scanner = new Scanner(new File("mta_stations.csv"));
					scanner.nextLine();

					Graph<Node, Edge> graph = new Graph<>(new HashSet<>(), new HashSet<>());

					boolean rush = (time >= 700 && time <= 1000) || (time >= 1600 && time <= 2000); // Calculate rush hours

					while (scanner.hasNextLine()) {
						String[] split = scanner.nextLine().split(",");

						/*
						We'll just get purely numerical coordinates and ignore the unnecessary POINT(... stuff
						 */
						String coordinates = split[3].substring(split[3].indexOf('(') + 1, split[3].length() - 1);
						int separation = coordinates.indexOf(' ');

						double longitude = Double.valueOf(coordinates.substring(0, separation)), latitude = Double.valueOf(coordinates.substring(separation + 1));

						/*
						If we have multiple conditions, which we do, we will just store them all as one string, as we see
						in the 5th column of the csv file. Because I have split each column's values by the ',' character,
						this is necessary.
						 */
						StringBuilder build = new StringBuilder();

						for (int i = 5; i < split.length; i++) {
							build.append(split[i]);
							if (i < split.length - 1) build.append(",");
						}

						/*
						Instantiating our Nodes that we'll then store into our set.
						 */
						Node node = new Node(split[2], split[4].split("-"), longitude, latitude, build.toString()).applyFields(time, rush, Day.type(inputDay)).build();
						for (Node existing : graph.getNodes()) {
							if (existing.getStation().equals(node.getStation())) {
								node.getTrains().forEach(train -> existing.addTrain(train));
							}
						}
						graph.addNode(node);
					}
					scanner.close();

					/*
					We'll create some Edges. This runs quite inefficiently, but that's fine. We just need an accurate
					network of stations.

					TODO: make the graph somewhat smaller(?)
					*/
					for (Node from : graph.getNodes()) {
						for (Node to : graph.getNodes()) {
							for (Train train : to.getTrains()) {
								if (from.getTrains().contains(train)) {
									Edge edge = new Edge(from, to);

									graph.addEdge(edge);
									graph.addEdge(from, edge);
								}
							}
						}
					}
					return graph;
				} catch (Exception e) { e.printStackTrace(); }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Graph<>(new HashSet<>(), new HashSet<>());
	}

	/**
	 * This Graph object will store our Set of Nodes and Set of Edges by G = {V, E}
	 *
	 * @param <V> type V, vertices
	 * @param <E> type E, edges
	 */
	class Graph<V extends Node, E extends Edge> {
		private Set<V> nodes;
		private Set<E> edges;

		private Map<V, List<E>> graph;

		public Graph(Set<V> nodes, Set<E> edges) {
			this.nodes = nodes;
			this.edges = edges;
			this.graph = new HashMap<>();
		}

		/**
		 * This function runs Dijkstra's algorithm in order to find the shortest path
		 * between two stations. We run this algorithm in accordance to Edge weight,
		 * that represents distance in km. Thus, this algorithm attempts to find
		 * the shortest path by finding the shortest route to get from one
		 * station to the other by space metric.
		 *
		 * @param origin
		 * @param destination
		 * @return shortest Path
		 * @throws NoSuchElementException if inputted stations do not exist
		 */
		public Path dijkstras(String origin, String destination) throws NoSuchElementException {
			Node source = null, dest = null;
			for (V node : this.nodes) {
				if (source == null) {
					if (node.getStation().equals(origin)) {
						source = node;
					}
				}
				if (dest == null) {
					if (node.getStation().equals(destination)) {
						dest = node;
					}
				}
			}
			/*
			If the inputted origin or destination does not exist in our existing stations, we
			will throw an error;
			 */
			if (source == null || dest == null) {
				throw new NoSuchElementException("Invalid origin or destination station.");
			}
			this.resetVisits();

			/*
			Dijkstra's algorithm using Pair and PriorityQueue
			 */
			Map<Node, Pair<Path, Double>> tentative = new HashMap<>();
			Queue<Node> heap = new PriorityQueue<>((from, to) -> (int) from.distance(to));

			// Begin setting up tentative distances.
			for (V node : this.nodes) {
				tentative.put(node, Pair.of(new Path(node), Double.MAX_VALUE));
			}
			tentative.put(source, Pair.of(new Path(source), 0.0));
			heap.add(source);

			// Run the algorithm and take care of visited nodes.
			while (!heap.isEmpty()) {
				source = heap.poll();
				List<E> edges = this.graph.get(source);

				source.visit();

				for (Edge edge : edges) {
					Node to = edge.to();

					if (!to.visited()) {
						heap.add(to);

						double distance = tentative.get(source).getRight() + source.distance(to);

						if (distance < tentative.get(to).getRight()) {
							// We will override our old Path with a new one containing this new Node/station
							// at the head.
							Path path = new Path(tentative.get(source).getLeft()).addToPath(to);
							tentative.put(to, new Pair<>(path, distance));
						}
					}
				}
			}
			return tentative.get(dest).getLeft();
		}
		public Path dijkstras(Node origin, Node destination) {
			return dijkstras(origin.getStation(), destination.getStation());
		}

		/**
		 * In this definition of "shortest path", we will attempt to create a path using
		 * Dijkstra's algorithm that has the least amount of walks required (least transfers), if Dijkstra's path
		 * does not already provide the path with the least amount of walks.
		 *
		 * This is done by finding the most final stop that contains a train that is able to connect to
		 * a station from earlier in the path. This removes any duplicates, if found, and will
		 * attempt to create the shortest path with the shortest amount of walks.
		 *
		 * @return Path with least amount of walks
		 */
		public Path shortestWalkPath(String origin, String destination) {
			this.resetVisits();

			Path dijkstras = dijkstras(origin, destination);
			LinkedList<Node> path = dijkstras.path();

			int i = 0, j = -1, track = -1;
			Iterator<Node> outerItr = new Path<>(path).path().iterator();

			outer: while (outerItr.hasNext()) {
				Node next = outerItr.next();

				Iterator<Node> innerItr = new Path<>(path).path().iterator();

				while (innerItr.hasNext()) {
					track++;
					Node nextNext = innerItr.next();

					for (Train outerTrain : next.getTrains()) {
						for (Train innerTrain : nextNext.getTrains()) {
							if (outerTrain.train().equals(innerTrain.train())) {
								j = track;
							}
						}
					}
				}
				for (int remove = 0; remove < j - i - 1; remove++) {
					path.remove(i + 1);
				}
				track = -1;
				j = 0;
				i++;
			}
			return new Path(path);
		}

		public void resetVisits() {
			this.graph.keySet().forEach(node -> node.unvisit());
			this.nodes.forEach(node -> node.unvisit());
			this.edges.forEach(edge -> { edge.from().unvisit(); edge.to().unvisit(); });
		}

		/**
		 * Get our Set of Nodes
		 * @return set of Nodes
		 */
		public Set<V> getNodes() {
			return this.nodes;
		}

		/**
		 * Get our Set of Edges
		 * @return set of Edges
		 */
		public Set<E> getEdges() {
			return this.edges;
		}

		public void addNode(V node) {
			this.nodes.add(node);
		}
		public void addEdge(E edge) {
			this.edges.add(edge);
		}

		/**
		 * If the graph does not contain existing Node 'node', then add it to
		 * the graph, then add an edge. Otherwise, just add the edge.
		 *
		 * @param node
		 * @param edge
		 */
		public void addEdge(V node, E edge) {
			if (!this.graph.containsKey(node)) {
				this.graph.put(node, new ArrayList<>());
			}
			this.graph.get(node).add(edge);
		}

		/**
		 * This Path object represents a path that we want to establish between Nodes/stations. We represent
		 * this path using a LinkedList.
		 *
		 * @param <V>
		 */
		class Path<V extends Node> {
			// A LinkedList is a good way to represent a path because it is just one node to the next.
			private LinkedList<V> path = new LinkedList<>();

			/**
			 * If we already have some existing path, then we can just 'duplicate' it.
			 * @param path
			 */
			public Path(LinkedList<V> path) {
				path.iterator().forEachRemaining(e -> this.path.add(e));
			}
			public Path(Path<V> path) {
				this(path.path());
			}
			public Path(V node) {
				this.path.add(node);
			}
			public Path() { }

			public Path addToPath(V node) {
				return addToPath(node, 0.0);
			}
			public Path addToPath(V node, double timeTaken) {
				this.path.add(node);
				return this;
			}
			public LinkedList<V> path() {
				return this.path;
			}
			@Deprecated
			public V tail() {
				return this.path.getFirst();
			}
			@Deprecated
			public V head() {
				return this.path.getLast();
			}
			public V first() {
				return this.path.getFirst();
			}
			public V last() {
				return this.path.getLast();
			}

			@Override
			public boolean equals(Object object) {
				Iterator<V> fItr = ((Path) object).path().iterator();
				Iterator<V> sItr = ((Path) object).path().iterator();

				while (fItr.hasNext() && sItr.hasNext()) {
					if (!fItr.next().equals(sItr.next())) {
						return false;
					}
				}
				return true;
			}

			/**
			 * We will visually display the path, along with the trains that are readily available
			 * and required to take the route in this path.
			 *
			 * We will also crudely calculate the time it has taken to get to this station,
			 * by assumption that the train travels at 0.35 km / min, which is not entirely accurate,
			 * but the MTA is rather inconsistent, and it takes time to transfer between stations,
			 * so this will do relatively okay in providing some prediction of time it will take.
			 *
			 * @return the path along with the trains, time and stations required
			 */
			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				LinkedList<Node> path = new LinkedList<>(this.path);

				/*
				We will visually display the path, along with the trains that are readily available
				and required to take the route in this path.
				 */
				while (!path.isEmpty()) {
					Node next = path.poll();
					builder.append(next.getStation());

					Set<String> trains = new HashSet<>();

					if (path.peek() != null) {
						Node second = path.peek();

						next.getTrains().forEach(fromTrain -> second.getTrains().forEach(toTrain -> {
							if (fromTrain.train().equals(toTrain.train())) {
								switch (fromTrain.type()) {
									case LOCAL: trains.add("(" + fromTrain.train() + ")"); break;
									case EXPRESS: trains.add("<" + fromTrain.train() + ">"); break;
								}
							}
						}));

						for (String line : trains) {
							builder.append(" " + line);
						}
						builder.append(" [" + new DecimalFormat("0.00").format(Math.round(next.distance(second) / 0.35)) + " mins]");
						if (path.peek() != null) {
							builder.append(" -> ");
						}
					}
				}
				return builder.toString();
			}
		}
	}

	/**
	 * This Node object will store our station, coordinates and trains. Inside of this Node, we will
	 * calculate available trains and such.
	 */
	class Node implements Comparable<Node> {
		private String station;
		private double latitude, longitude;

		private int time;
		private boolean rush;
		private Day.DayType dayType;

		private String lineConditions;
		private Set<Train> trains;
		private Map<String, Train.TrainType> connections;
		private Optional<String> lastStop = Optional.empty(); // Assuming we do not indeed need this

		private boolean visited;

		public Node(String station, String[] connections, double latitude, double longitude, String lineConditions) {
			if (station.isEmpty()) {
				return;
			}
			this.station = station;
			this.latitude = latitude;
			this.longitude = longitude;
			this.lineConditions = lineConditions;

			this.connections = new HashMap<>();

			for (String connection : connections) {
				if (connection.contains("Express")) {
					this.connections.put(connection, Train.TrainType.EXPRESS);
				} else {
					this.connections.put(connection, Train.TrainType.LOCAL);
				}
			}
			this.trains = new HashSet<>();
		}

		/**
		 * Here we build our Node. The entire Node class is essentially a builder/factory
		 * object that we'll use a bunch of inputted values to build the final product.
		 *
		 * What we do here is we will apply certain conditions that we inputted earlier
		 * when instantiating new Node objects, and doing so, we'll be able to accurately
		 * add the trains that are available at the time and day.
		 *
		 * @return this Node
		 */
		public Node build() {
			String[] otherConditions = this.lineConditions.split(",");
			for (int i = 0; i < otherConditions.length; i++) {
				if (otherConditions[i].contains("to ")) {
					String beginString = otherConditions[i].substring(otherConditions[i].indexOf("to ") + 3);
					String stop;

					if (beginString.contains("-")) {
						stop = beginString.substring(0, beginString.indexOf('-'));
					} else {
						stop = beginString.substring(0, beginString.indexOf(" "));
					}
					this.lastStop = Optional.of(stop);
				}
				if (otherConditions[i].contains("exc nights") && (this.time >= 2000 || this.time <= 600)) {
					continue;
				}
				if (applyConditions(i, otherConditions, this.time, this.dayType, this.rush, (train, type, bound) -> {
					if (!train.isEmpty() || train.length() >= 1) {
						this.trains.add(new Train(train, type, bound));
					}
				}));
			}
			return this;
		}

		public Node applyFields(int time, boolean rush, Day.DayType dayType) {
			this.time = time;
			this.rush = rush;
			this.dayType = dayType;
			return this;
		}

		/**
		 * Here, we apply many conditions that we are given so that we can calculate the trains that
		 * are available at the time, day and whether or not we are in rush hour, as well as the
		 * bounds that the trains will take.
		 *
		 * Keyword: condition, here used to denote every train and condition from column 5.
		 * Ex. for Astor Pl, primary conditions are: "4 nights, 6-all times, 6 Express-weekdays AM southbound, PM northbound",
		 * each condition is split into 3: 4 nights, 6-all times, 6-Express + conditions in this condition.
		 *
		 * @param i
		 * @param otherConditions
		 * @param time
		 * @param dayType
		 * @param rush
		 * @param consumer
		 * @return True, if conditions are met for the train to be available, False otherwise.
		 */
		private boolean applyConditions(int i, String[] otherConditions, int time, Day.DayType dayType, boolean rush, TriConsumer<String, Train.TrainType, Train.Bound> consumer) {
			boolean day = time >= 700 && time < 2000, night = time >= 2000 || time < 700;
			String data = otherConditions[i];

			if (night && !data.contains("nights") && !data.contains("all times") && !data.contains("all other times") && !data.contains("Express-weekdays")) {
				return false;
			} else if (day && data.contains("nights") && !data.contains("all times") && !data.contains("all other times") && !data.contains("Express-weekdays")) {
				return false;
			} else if (data.contains("rush hours") && !rush) {
				return false;
			}
			boolean am = time >= 0 && time < 1200, pm = !am;
			Train.TrainType type = Train.TrainType.LOCAL;

			/*
			If we are in rush hour and if the current condition check is an Express train
			in rush hour, then we will assign the train type as Express.
			 */
			if (data.contains("Express-rush hours") && rush) {
				type = Train.TrainType.EXPRESS;
			}

			String train = "";
			for (String conn : this.connections.keySet()) {
				if (data.contains(conn)) {
					train = conn;
				}
			}

			/*
			If conditions are not met where the train runs at "all other times" (we compare it to
			previous trains and conditions) then we will not add this train to the set.
			 */
			if (data.contains("all other times")) {
				for (int j = i - 1; j >= 0; --j) {
					if (otherConditions[j].contains("all other times")) {
						break;
					}
					if (otherConditions[j].contains("nights") && night) {
						return false;
					} else if (otherConditions[j].contains("weekdays and evenings") && dayType == Day.DayType.WEEKDAY && night) {
						return false;
					} else if (otherConditions[j].contains("rush hours") && rush) {
						return false;
					}
				}
			}

			/*
			If it is rush hour and the conditions of this train skips during rush hour, then we will not
			add this train to the set.
			 */
			if (rush && i + 1 < otherConditions.length && otherConditions[i + 1].contains("skips rush hours")) {
				return false;
			}

			/*
			We will appropriately assign the bounds of this Express train according to time if this condition is that this
			train is an Express train and add it to the set.
			 */
			if (data.contains("Express-weekdays") && dayType == Day.DayType.WEEKDAY) {
				type = Train.TrainType.EXPRESS;

				if (data.contains("AM")) {
					if (am) {
						consumer.accept(train, type, getBound(data));
						return true;
					} else {
						if (otherConditions[i + 1].contains("PM")) {
							consumer.accept(train, type, getBound(otherConditions[i + 1]));
							return true;
						}
					}
				}
			} else if (data.contains("Express-weekdays") && dayType == Day.DayType.WEEKEND) {
				return false;
			}
			// We will otherwise add whatever Local train that is available and conditions have been met.
			consumer.accept(train, type, getBound(data));
			return true;
		}

		/**
		 * We convert Strings to Bounds by checking whether the String contains any
		 * type of cardinal bound.
		 *
		 * @param data String to check
		 * @return Bound enum if found, Bound None enum otherwise.
		 */
		private Train.Bound getBound(String data) {
			if (data.contains("northbound")) {
				return Train.Bound.NORTHBOUND;
			} else if (data.contains("southbound")) {
				return Train.Bound.SOUTHBOUND;
			} else if (data.contains("eastbound")) {
				return Train.Bound.EASTBOUND;
			} else if (data.contains("westbound")) {
				return Train.Bound.WESTBOUND;
			}
			return Train.Bound.NONE;
		}

		public String getStation() {
			return this.station;
		}
		public double getLongitude() {
			return this.longitude;
		}
		public double getLatitude() {
			return this.latitude;
		}
		public Set<Train> getTrains() {
			return this.trains;
		}
		public Optional<String> lastStop() {
			return this.lastStop;
		}
		public boolean visited() {
			return this.visited;
		}
		public void visit() {
			this.visited = true;
		}
		public void unvisit() {
			this.visited = false;
		}
		public void addTrain(Train train) {
			this.trains.add(train);
		}

		/**
		 * Using the Haversine formula, we can calculate the distance between 2 points of longitude and latitude
		 * and convert it to a metric that can be compared to. In this case, we will convert to kilometers.
		 *
		 * @param node second node (second point)
		 * @return distance between 2 points in km.
		 */
		public double distance(Node node) {
			double dlat = Math.toRadians(node.getLatitude() - this.latitude);
			double dlong = Math.toRadians(node.getLongitude() - this.longitude);

			double a = 0.5 - Math.cos(dlat) / 2 + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(node.getLatitude())) * (1 - Math.cos(dlong)) / 2;

			return 2 * 6371 * Math.asin(Math.sqrt(a));
		}

		@Override
		public boolean equals(Object object) {
			Node other = (Node) object;

			String thisLongitude = Double.toString(this.longitude), otherLongitude = Double.toString(other.getLongitude());
			String thisLatitude = Double.toString(this.latitude), otherLatitude = Double.toString(other.getLatitude());

			return this.station.equals(other.getStation()) && thisLongitude.equals(otherLongitude) && thisLatitude.equals(otherLatitude) && this.trains.containsAll(other.getTrains());
		}

		@Override
		public String toString() {
			return "Station: " + this.station + "\n" + "Longitude: " + this.longitude + "\n" + "Latitude: " + this.latitude + "\n";
		}

		@Override
		public int compareTo(Node to) {
			return (int) this.distance(to);
		}
	}

	/**
	 * We will be creating edges by having an Edge object to represent two nodes (from and to).
	 * The weight of the edge will be calculated using Node's own distance function between the
	 * two Nodes referenced in Edge.
	 */
	class Edge {
		private Node from, to;
		private double weight;

		public Edge(Node from, Node to) {
			this.from = from;
			this.to = to;
			this.weight = this.from.distance(this.to);
		}

		public Node from() {
			return this.from;
		}
		public Node to() {
			return this.to;
		}
		public double weight() {
			return this.weight;
		}
		public static Edge of(Set<Edge> edges, Node from, Node to) {
			for (Edge edge : edges) {
				if (edge.from().equals(from) && edge.to().equals(to)) {
					return edge;
				}
			}
			return null;
		}
	}

	/**
	 * Using a Java record (introduced in Java 16; thus this program should be built in Java 16 or higher), we can
	 * make simple objects that store only definitions, which is what the Train object should simply be in this program,
	 * less complications and more simplicity and elegance.
	 *
	 * @param train
	 * @param type
	 * @param bound (Under the assumption that bounds are the same for each line given a certain
	 * 				time, we will not have to worry much about bounds.)
	 */
	record Train(String train, TrainType type, Bound bound) {
		public enum TrainType { LOCAL, EXPRESS }
		public enum Bound { NORTHBOUND, SOUTHBOUND, EASTBOUND, WESTBOUND, NONE }

		public String train() { return this.train; }
		public TrainType type() { return this.type; }
		public Bound bound() { return this.bound; }
	}
}
