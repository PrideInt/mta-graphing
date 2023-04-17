# MTA Network Graphing

## FUNCTION

The function of this program is to find the *shortest path from one station to another* by construction of a graph of nodes/vertices and edges. This shortest path represents the most desired route for an individual to take on the MTA subway (given any conditions).

The user and program must take into the account the *time and day* as well as the *origin station* and *destination station* within this program prior to generating the graph. Depending on the circumstances (user inputs), the graph can take on different stations/nodes that are available given the time and day. 

The program thus accepts user inputs via terminal or console, generates a graph, and within that graph, performs Dijkstra’s shortest path-finding algorithm in order to find the shortest path between one station to another (described by distance in kilometer metric), as well as the time and trains necessary to complete the route.

## GRAPH CONSTRUCT

The **graph** is a visual description of the network of stations described, where **nodes** represent stations and **edges** represent the connection between each station; the **weight** of an edge represents the distance between *station from* and *station to*. The **path** in this program is represented as a subgraph of the constructed main graph as a tree with a branching factor of 1, or a linked list. If no such direct path (path in which one singular walk is required) exists, then one must transfer stations (meaning there exists more than one node within the path, and thus > 1 number of walks). 

Within the ***Node*** object, the trains that are available given the user input *time and day* are calculated and then stored in a Set, and is described in further detail under the **OBJECTS** section. For example, if the user inputs 2300 for *time* and Saturday for *day*, and the trains availability conditions are described as “*6-all times, 6-Express weekdays, 4-nights only*”, then only 6 (local) and 4 trains will be available and stored, and this availability is calculated.

***Edge*** creation is simpler. Only three *for loops* are required, in which nodes are generated first prior to looping. Within the for loops, each station is compared with every other station to check whether or not it is transferable; if it is then we will create an edge.

## OBJECTS

### Graph:
> The ***Graph*** object holds a Set of ***Nodes*** and a Set of ***Edges*** to represent graph notation, G = {V, E}.

> Additionally, an *adjacency list* is defined within the ***Graph*** object to represent the graph itself. This is the chosen method of graph representation due to the sheer size of an adjacency matrix, if constructed (holding a size of > 200,000 elements, and in the consideration of the project, will be considered sparse).

### Path:
> The ***Path*** object holds a LinkedList reference that stores ***Nodes***. This represents the route and transferable stations (if any).

### Node:
> The ***Node*** object stores code and data that represent an individual station and its trains that are available that time and day.

> *applyConditions* apply conditions in which a train can be stored within the *trains* Set (representing trains that are available to perform an algorithm on). This is done by String manipulation, in which the program detects certain keywords, and with the condition of those keywords, either applies certain attributes such as local or express or cardinal bounds, or downright excludes it from becoming stored. 

> *distance* calculates the distance—between the ***Node*** in question and the parameter ***Node***—using the Haversine formula given two longitude and latitude points, and converts it into a metric of choice more calulatable and describable in space (the program converts it into kilometers rather than nautical miles).

### Edge:
> Using the definition of an edge, e = {v1, v2}, where v1 = *from* and v2 = *to*, the ***Edge*** object stores data and references that the program uses to manipulate from ***Nodes*** (v1) and to ***Nodes*** (v2).

> The *weight* of this edge is the calculated distance between *from* and *to*.

### Train:
> The ***Train*** object is a Java record, a special type of object that stores only definitions of data.

> This object is used to simplify object creation, definition and manipulation as the program only requires attributes of the object, such as *train type* (***TrainType*** enum), *bounds* (***Bound*** enum) and *line* (line number).

### Pair:
> The ***Pair*** object represents an object that is able to hold two objects of similar, different or either type within its reference. 

> This object is used primarily to store a pair of objects within one.

### Tuple:
> The ***Tuple*** object represents an object that is able to hold three objects of similar, different or either type within its reference. This is just like ***Pair***, except a ***Tuple*** can hold three rather than two.

> This object is used primarily to store three objects within one.

### Time:
> ***Time*** is a more of a utility class, where public static functions can be called to perform a variety of operations, such as validating the user input time.

### Day: 
> ***Day***, like ***Time*** is a utility class that provides the program with functions to work with days.

### Triconsumer:
> The ***Triconsumer*** interface (like Java’s ***Consumer*** and ***Biconsumer***) holds functional code that takes in objects of three types and returns nothing.

> This is used to run code upon objects that are accepted into the consumer.

## PATH-FINDING ALGORITHM

This program runs Dijkstra’s algorithm when a graph is constructed. This algorithm attempts to find the shortest path in the graph given edge weights, the weights in question are represented by distance in kilometers between two stations (where they are represented by nodes). Therefore, the shortest path will be the path in which all edge weights within the path add up to < *tentative distance*.

In another scenario, however, if the program is written to simply find any “shortest path” in the graph without regard for distance, then one must assume that all edge weights are equal in value, then run Dijkstra’s once more (which is actually just Breadth First Search).

During the running of the algorithm, a ***Path*** is created and overridden (with a new ***Path*** containing a new station at its head) if tentative distance conditions are met. Calculation of transfer time is done when returning the result of the ***Path*** by *toString*, where a raw assumption is made in which 0.35 km in distance between stations translates to 1 single minute.

## SOFTWARE DISCUSSION

Despite many changes in the structure of the code, the intentions had changed very little. At first, the program was intended to topologically sort the graph in order to ease the algorithmic process, but found it unnecessary and Dijkstra’s algorithm was enough. Furthermore, the graph structure had originally been intended to be defined as an adjacency matrix so that matrix manipulation can be utilized, especially when calculating the minimum number of walks necessary for the program, but this idea was disregarded as it was viewed as too dense and would require far more computation than what an adjacency list would need (despite the slight overhead issue when dealing with an adjacency list). Lastly, the cardinal bounds as well as the final stop Optional were disregarded, as an assumption was made of the direction of the edges that deemed it little importance.

Lastly, the choices made during the development of this software had seen many specific handling during the coding process. It had to be assumed that few things within the project required definitions provided only by the programmer. Moreover, the data that had to be accessed needed to be closely analyzed to notice variations within certain stations that would largely affect the construction of the graph, and thus the path and objective of the program if not handled. 

From the conceptualization to the actual software development of the project, it required continuous development into the process to handle, optimize and improve the usability and performance of the program.
