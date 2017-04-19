/**
 * 
 */
package graph;

import java.util.HashMap;
import org.graphstream.graph.implementations.SingleGraph;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Stack;
import util.GraphLoader;


/**
 * @author Kathy Guajardo
 * 
 * CapGraph implements a graph using an Adjacency List.
 * It represents a social network graph, where edges represent friendships between people and  
 * the vertices which represent the people in the social network.   
 * 
 * The dynamic graph library, GraphStream, was included in this class for the sole purpose 
 * of creating a visualization of the graph as a SingleGraph object.   
 *
 */
public class CapGraph implements Graph {

	//CapGraph member variables
	private HashMap<Integer,CapVertex> adjList;
	private int numEdges;
	
	//Graph Cascader object
	private GraphCascader Cascader;
	
	//for visualization
	private SingleGraph graphView;
	
	//Coloring for nodes in Visualization
	//Nodes with B Property : Blue
	//Nodes with A Property : Red
	protected String styleSheet =
            "node {" +
            "	fill-color: black;" +
            "}" +
            "node.B {" +
            "	fill-color: blue;" +
            "}" +
            "node.A {" +
            "	fill-color: red;" +
            "}"
            ;
	
	/**
	* Create a new empty Graph
	* Constructor
	**/
	public CapGraph()
	{
		//create Adjacency List
		adjList = new HashMap<Integer,CapVertex>();
		numEdges = 0;

		//Visualization
		graphView = new SingleGraph("Cascade Graph");
		graphView.addAttribute("ui.stylesheet", styleSheet);
		graphView.addAttribute("ui.quality");
		
		//Cascader
		Cascader = new GraphCascader();
	}
	
	/**
	 * getMaxDegSeparation
	 * 
	 * This determines the maximum degree of separation for a given node, v,
	 *  and all the shortest paths to all other nodes in the graph.
	 */
	public int getMaxDegSeparation(int v){
		//Use BFS
		
		//Checks for bad input
		if (v<0)
			return -1;
		
		if (!adjList.containsKey(v))
			return -1;
		
		//Initialize
		int maxHops = 0;
		ArrayList<Integer> Queue = new ArrayList<Integer>();
		HashSet<Integer> Visited = new HashSet<Integer>();
		HashMap<Integer, Integer> HopMap = new HashMap<Integer, Integer>();
				
		//Start queue
		Queue.add(v);
		Visited.add(v);
		HopMap.put(v, 0);
		int curr = v;
				
		//Start Search
		while (!Queue.isEmpty())
		{
			curr = Queue.remove(0);
			//add all of curr's neighbors to the queue
			HashSet<Integer> neighbors = adjList.get(curr).getNeighbors();
					
			for (int n: neighbors){
				if (!Visited.contains(n)){
					Visited.add(n);
					Queue.add(n);
					int numHops = HopMap.get(curr) + 1;
					HopMap.put(n, numHops);
					if (numHops > maxHops)
						maxHops = numHops;
				}//if
			}// loop through neighbors
					
			//done looping through neighbors, go back up pull next node off of queue					
		}//while loop
		return maxHops;
	}
	
	public void RunCascade(){
		
		//Get large SCC from graph
		CapGraph LargeSCC = this.getLargestSCC();
		/*List<Graph> listSCCs = this.getSCCs();
		int maxSize = 0;
		CapGraph LargeSCC = new CapGraph();
		
		for (int index = 0; index < listSCCs.size(); index++)
		{
			CapGraph graph  = (CapGraph) listSCCs.get(index);
			int size = graph.getNumVertices();
			System.out.println("SCC graph index " + index + " number of vertices " + size);
			if (size > maxSize){
				System.out.println(" New max: " + size);
				maxSize = size;
				LargeSCC = graph;
			}
		}
		*/
		System.out.println("Large SCC is : " + LargeSCC.getNumVertices());
		
		//set properties of cascade:
		Cascader.seta(2);
		Cascader.setb(1);
		Cascader.setNumGenerations(50);
	
		//run cascade based on Closeness Centrality,
		// with 3 clusters of seeds
		// each cluster is 40% of neighbors of ranked node
		Cascader.RunCascade2HopDeg(LargeSCC, 2, 55);
	}
	
	public void DisplayGraph() {
		graphView.display();
	}
	
	/**
	 * addVertex
	 * Parameters: int num = vertex id number
	 * 
	 * Adds a vertex onto adjList Map
	 */
	@Override
	public void addVertex(int num) {
		//only add a vertex if it is not already in the Graph
		if (!adjList.containsKey(num)){
			//create vertex
			CapVertex vertex = new CapVertex(num);
			
			//add it to the adjacency list
			adjList.put(num,  vertex);
			
			//visualization - add node to visualization as default B
			String node = Integer.toString(num);
			graphView.addNode(node);
			graphView.getNode(node).addAttribute("ui.class", "B");
		}		

	}
	
	public CapVertex getVertex(int id){
		if (!adjList.containsKey(id))
				return null;
		
		return adjList.get(id);
	}
	

	
	/**
	 * addEdge
	 * Parameters: int from, int to
	 * 
	 * Adds edge from Vertex with id from, to Vertex with id to
	 * Which means adding a neighbor (vertex to) to vertex from 
	 **/
	@Override
	public void addEdge(int from, int to) {

		//Error checking Cases:
		// check that from and to are both vertices in Graph
		if (!adjList.containsKey(from) || !adjList.containsKey(to))
		{//Can't add edge because both vertices are not in graph"
			//System.out.println("Error adding edge, 1 or both vertices ( " + from + ",  "+ to + ") are not in graph!");
			return;
		}
		
		//if Edge already exists don't add again
		if (adjList.get(from).getNeighbors().contains(to))
			return;
		
		// Add neighbor to vertex from
		adjList.get(from).addNeighbor(to);
		numEdges++;
				
		//Visualization
		//only add edge in one direction, don't add if already an edge in other direction
		// Edge Name = vertex from id + "to" + vertex to id
		if (!adjList.get(to).getNeighbors().contains(from)){
					
			String edgeName = Integer.toString(from)+ "to"+ Integer.toString(to);
			graphView.addEdge(edgeName,Integer.toString(from), Integer.toString(to) );
		}		

	}

	/** 
	 * getEgonet
	 * parameters: center - the center of the ego net
	 * return value:  returns Graph of egonet
	 * Egonet will include all neighbor nodes of the center node with those edges, plus
	 * 	any edges between neighbor nodes.
	 * 
	 * If center node value is not contained in the graph, return an empty Graph
	 * 
	 * Question? Don't include incoming neighbors to center if there is not an
	 * outgoing edge to that neighbor?
	 */
	@Override
	public Graph getEgonet(int center) {
		Graph egonet = new CapGraph();
		
		//check to see if center is a vertex in CapGraph
		if (!adjList.containsKey(center)){
			//not in the graph, return empty graph!
			return egonet;
		}
		
		//add center node
		egonet.addVertex(center);
		
		//add neighbor nodes and edges to neighbors from center
		HashSet<Integer> neighbors = getNeighbors(center);
		for (Integer n: neighbors){
			egonet.addVertex(n);
			egonet.addEdge(center, n);
			
		}
		 //add neighbors edges within graph
		for (Integer n: neighbors){
			HashSet<Integer> neighborEdges = getNeighbors(n);
			for (Integer edge: neighborEdges){
				//will only add edge if both to and from in graph
				egonet.addEdge(n,edge);	
			}
		}
		return egonet;
	}

	/**
	 * getSCCs()
	 * no parameters
	 * 
	 * Return value: returns a list of Strongly Connected Componenets subgraphs
	 * found within CapGraph
	 * 
	 * Algorithm: 3 steps
	 * 1.Depth First Search (DFS) of CapGraph G, keeping track of order vertices finish
	 * 2. Transpose G to G1
	 * 3. DFS(G1) exploring in reverse oder of finish time from step 1
	 * 
	 */
	@Override
	public List<Graph> getSCCs() {
		List<Graph> SCCList; 
		
		Stack<Integer> vertices = new Stack<Integer>();
		Stack<Integer> finished; 
		
		//Stores list of SCC Nodes containing all the vertices in each SCC
		List<Stack<Integer>>  SCCNodesList = new ArrayList<Stack<Integer>>();;
		boolean getSCCNodes = true;
		
		vertices.addAll(adjList.keySet());
		
		//Step 1 DFS of G
		finished = DFS(this,vertices, !getSCCNodes, SCCNodesList );
		//System.out.println("Step 1 finsihed list: " + finished.toString());
		
		//Step 2 Transpose G to G1
		CapGraph G1 = transposeGraph();
		//System.out.println("Step 2 transpose Graph: ");
		//G1.PrintAdjacencyList();
		
		//Step 3 DFS of G1 using finished vertices order from step 1
		DFS(G1,finished, getSCCNodes,SCCNodesList);
		
		//Step 4 Create List of SCC Graphs from SCC nodes list
		//loop through SCC lists and make a list of Graphs from them
		SCCList = CreateGraphList(SCCNodesList);
		
		return SCCList;
	}
	
	/**
	 * DFS - performs Depth First Searches until all nodes have been visited.
	 * @param G : Graph to perform the DFS on
	 * @param verticeOrder:  Order of Vertices to perform DFS
	 * @param getSCCList :  Is DFS resulting in finding SCCs?  true/false
	 * @param SCCnodes:  If getSCCList is true, returns a list of all SCCs in graphs,
	 *                   where each item in list is a list of nodes in an SCC
	 * @return: The reverse order the vertices in G were found in the DFS
	 */
	private Stack<Integer> DFS(CapGraph G, Stack<Integer> verticeOrder, boolean getSCCList, 
			List<Stack<Integer>> SCCnodes ){
		
		//Initialization of visted and vertices lists
		HashSet<Integer> visited = new HashSet<Integer>();
		Stack<Integer> finished = new Stack<Integer>();
				
		// visit all the vertices 
		while (!verticeOrder.empty()){
			int v = verticeOrder.pop();
			if (!visited.contains(v)){
				//Each call to DFSVisit will add to finish an SCC on Step 3
				//Need to know if storing SCC or not
				DFSVisit(G, v,visited, finished);
				if (getSCCList){
					//pull all nodes off of finished list from this 
					//DFSVisit call- these are a list of vertices for a SCC
					//and clear out finished for next call
					//System.out.println("SCC Node:  " + finished.toString());
					SCCnodes.add(finished);
					finished = new Stack<Integer>();
				}
			}
		}
		
		return finished;
	}
	
	/**
	 * DFSVisit : Traverses all possible paths in DFS from vertex passed in until all nodes 
	 *            from the given vertex that can be reached using DFS have been visited
	 * @param G : G is the graph to perform the DFS on
	 * @param vertex: Vertex to perform DFS from
	 * @param visited:  List of nodes visited in DFS
	 * @param finished:  Stack of nodes in the order they are finished with DFS
	 */
	private void DFSVisit(CapGraph G, int vertex, HashSet<Integer> visited, Stack<Integer> finished){
		
		//add vertex to visited
		visited.add(vertex);
		
		//get neighbors of vertex
		HashSet<Integer> neighbors = G.getNeighbors(vertex);
		
		for (Integer n: neighbors)
		{
			if (!visited.contains(n)){
				DFSVisit(G,n,visited, finished);
			}
		}
		finished.push(vertex);
		
		return;
		
	}
	
	/**
	 * CreateGraphList:  Creates the SCC graphs and puts them and SCCGraph list
	 * @param SCCNodesList:  A list containing the nodes contained in each SCC for the Graph
	 * @return: A list of the all the SCC graphs
	 */
	private List<Graph> CreateGraphList(List<Stack<Integer>> SCCNodesList){
		List<Graph> SCCList = new ArrayList<Graph>();
		CapGraph SCCGraph;
		for (int index =0; index < SCCNodesList.size(); index++){
			SCCGraph = new CapGraph();
			
			//add vertices of SCC Graph
			while (!SCCNodesList.get(index).isEmpty()){
				//loop through the list of nodes for 1 SCC
				// create Graph with those nodes and add
				//to SCCList
				int vertex = SCCNodesList.get(index).pop();
				SCCGraph.addVertex(vertex);	
			}//Get next Vertex
			
			//Add Edges of SCC Graph for each vertex in the graph
			Set<Integer> Vertices = SCCGraph.getVertices();
			for (Integer v: Vertices){
				HashSet<Integer> neighbors = this.getNeighbors(v);
				//loop through neighbors of vertex v
				//add neighbors if the neighbor is a vertex in SCC
				for (Integer n: neighbors){
					if (Vertices.contains(n)){
						SCCGraph.addEdge(v, n);
					}
				}//Get next neighbor

			}//Get next Vertex to add Edges
			
			//add SCC Graph to SCC Graph List
			SCCList.add(SCCGraph);
			
		}//Get next SCC Graph
		return SCCList;
	}
	

	/**
	 * TransposeGraph: Creates a transpose of the graph
	 * 
	 * return Value: A new Capgraph that is the transpose of the graph
	 */
	private CapGraph transposeGraph(){
		
		CapGraph transposedG = new CapGraph();
		
		for(Integer v: this.getVertices()){
			//add vertex to transposed Graph
			transposedG.addVertex(v);
			HashSet<Integer> neighbors = this.getNeighbors(v);
			for(Integer n: neighbors){
				//Add neighbor as vertex
				transposedG.addVertex(n);
				//Add edge from neighbor to v (opposite this CapGraph)
				transposedG.addEdge(n, v);
			}
		}
		return transposedG;
	}

	
	
	private CapGraph getLargestSCC(){
		//Get large SCC from graph
		List<Graph> listSCCs = this.getSCCs();
		int maxSize = 0;
		CapGraph LargeSCC = new CapGraph();
				
		for (int index = 0; index < listSCCs.size(); index++)
		{
			CapGraph graph  = (CapGraph) listSCCs.get(index);
			int size = graph.getNumVertices();
			//System.out.println("SCC graph index " + index + " number of vertices " + size);
			if (size > maxSize){
				System.out.println(" New max: " + size);
				maxSize = size;
				LargeSCC = graph;
			}
		}
		return LargeSCC;
	}
	
	/** (non-Javadoc)
	 * @see graph.Graph#exportGraph()
	 * 
	 * returns the adjacency List of graph, but only includes list of neighbors of 
	 * 	each vertex, not the CapVertex
	 * 
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		//will have to create this map
		HashMap<Integer,HashSet<Integer>> map = new HashMap<Integer, HashSet<Integer>>();
		
		for (int a: adjList.keySet()){
			map.put(a, adjList.get(a).getNeighbors());
		}
		return map;
	}
	
	
	
	/*
	 * getDegSeparation 
	 * Returns the shortest number of hops in a path from the start vertex to the 
	 * end vertex
	 */
	public int getDegSeparation(int start, int end)
	{
		//Checks for bad input
		if (start == end)
				return 0;
				
		if (start<0 || end<0)
				return -1;
				
		if (!adjList.containsKey(start))
				return -1;
		if(!adjList.containsKey(end))
				return -1;
		
		//Initialize
		boolean found = false;
		ArrayList<Integer> Queue = new ArrayList<Integer>();
		HashSet<Integer> Visited = new HashSet<Integer>();
		HashMap<Integer, Integer> ParentMap = new HashMap<Integer, Integer>();
		
		//Start queue
		Queue.add(start);
		Visited.add(start);
		int curr = start;
				
		//Start Search using BFS
		while (!Queue.isEmpty())
		{
			curr = Queue.remove(0);

			if (curr == end){
				found = true;
				break;
			}
			//add all of curr's neighbors to the queue
			HashSet<Integer> neighbors = adjList.get(curr).getNeighbors();
			
			for (int n: neighbors){
				if (!Visited.contains(n)){
					Visited.add(n);
					Queue.add(n);
					ParentMap.put(n, curr);
				}//if
			}// loop through neighbors
		}//while loop
		
		if (found){
			//get number of hops from path
			int node;
			node = ParentMap.get(curr);
			
			int num = 0;
			while (node != start){
				
				num++;
				//System.out.println("Node " + node+ " Num hops is: " + numHops);
				node = ParentMap.get(node);
			}
			return num;
		}
		return -1;
	}
	
	
	public int getNodeLeastDegreesOfSep(){
		//Find Max Degree of Separation for all nodes
		//Find node with the lowest Max Degree of Separation
		
		int minNode= -1;
		int minHops = Integer.MAX_VALUE;
		
		for (int n : getVertices() ){
			int deg = getMaxDegSeparation(n);
			if (deg< minHops){
				minHops = deg;
				minNode = n;
			}
		}
		System.out.println("Node with smalles max deg of  separation is : " + minNode+ 
				" with "+ minHops + "degrees of separation.");
		
		return minNode;
	}
	
	
	/**
	 * Getters and setters
	 * 
	 */
	
	public int getNumVertices()
	{
		return adjList.size();
	}
	
	public int getNumEdges(){
		
		return numEdges;
	}
	
	public HashSet<Integer> getNeighbors(int vertex){
		if (!adjList.containsKey(vertex))
			return null;
		
		return adjList.get(vertex).getNeighbors();
	}
	
	public Set<Integer> getVertices(){
		Set<Integer> vertices = new HashSet<Integer>();
		vertices = adjList.keySet();
		
		//return a copy
		return vertices;
	}
	
	public char getABProperty(int v){
		if (!adjList.containsKey(v))
			return '\0';
		
		//for a given vertex v, return the value of propertyAB
		return adjList.get(v).getPropertyAB();
	}
	
	public void setNodeToA(int v){
		if (!adjList.containsKey(v))
			return;
		
		adjList.get(v).setPropertyABtoA();
		
		//visualization
		graphView.getNode(Integer.toString(v)).changeAttribute("ui.class", "A");
	}
	
	public void setNodeToB(int v){
		if (!adjList.containsKey(v))
			return;
		
		adjList.get(v).setPropertyABtoB();
		
		//visualization
		//graphView.getNode(Integer.toString(v)).removeAttribute("ui.class");
		graphView.getNode(Integer.toString(v)).changeAttribute("ui.class", "B");
		
	}

	public void setNodeCloseness(int node, double value){
		if (!adjList.containsKey(node))
			return;
		
		adjList.get(node).setClosenessCent(value);
	}
	public double getNodeCloseness(int node){
		if (!adjList.containsKey(node))
			return -1;
		
		return adjList.get(node).getClosenessCent();
	}
	
	public void setDeg2Hop(int node, int value){
		if (!adjList.containsKey(node))
			return;
		
		adjList.get(node).setDeg2Hop(value);
	}
	
	public int getDeg2Hop(int node){
		if (!adjList.containsKey(node))
			return -1;
		
		return adjList.get(node).getDeg2Hop();
	}
	
	public void setDegrees(int node, int value){
		if (!adjList.containsKey(node))
			return;
		
		adjList.get(node).setDegCent(value);
	}
	
	public int getDegrees(int node){
		if (!adjList.containsKey(node))
			return -1;
		return adjList.get(node).getDegCent();
	}
	
	public SingleGraph getGraphView(){
		return graphView;
	}
	
	
	/**
	 * Print out adjacency list
	 * @return the String
	 */
	public void PrintAdjacencyList() {
		
		
		//String s = "Adjacency list";
		//s += "( " + getNumVertices() + " Vertices, " + getNumEdges() + " Edges):";
		System.out.println("Adjacency List: ( " + getNumVertices() + " Vertices, " + getNumEdges() + " Edges):" );
		for (int v : adjList.keySet()) {
			String s = "";
			
			s += "\t"+v+": ";
			for (int w : adjList.get(v).getNeighbors()) {
				s += w+", ";
			}
			System.out.println(s);
		}
		return;
	}
	
	
	
	/**
	 * Use this method for testing
	 * 
	 */
	
	public static void main(String[] args){
		
		
		CapGraph FBGraph = new CapGraph();
		
		
		GraphLoader.loadGraph(FBGraph, "data/facebook_1000.txt");
		System.out.println("FB Vertices "+ FBGraph.getNumVertices() + ", Edges "
		+ FBGraph.getNumEdges());
		FBGraph.RunCascade();
		
		/*//Add vertices to testGraph - example from class
		
		CapGraph testGraph = new CapGraph();
		testGraph.addVertex(18);
		testGraph.addVertex(23);
		testGraph.addVertex(25);
		testGraph.addVertex(32);
		testGraph.addVertex(44);
		testGraph.addVertex(50);
		testGraph.addVertex(65);
		
		//Add Edges - make all edges go both ways like friends
		testGraph.addEdge(18,23);
		testGraph.addEdge(18,  44);
		testGraph.addEdge(18, 65);
		testGraph.addEdge(23,  18);
		testGraph.addEdge(23, 25);
		//add
		testGraph.addEdge(23, 65);
		testGraph.addEdge(44, 18);
		
		testGraph.addEdge(25, 23);
		testGraph.addEdge(25, 65);
		testGraph.addEdge(44, 32);
		testGraph.addEdge(44, 50);
		//add Edge
		testGraph.addEdge(44, 18);
		testGraph.addEdge(44,65);
		
		testGraph.addEdge(50,  32);
		//add Edge
		testGraph.addEdge(50,  44);
		
		testGraph.addEdge(65, 23);
		//add edges
		testGraph.addEdge(65,  18);
		testGraph.addEdge(65,  25);
		testGraph.addEdge(65, 44);
		testGraph.addEdge(32, 50);
		testGraph.addEdge(32, 44);
		
		
		//testGraph.getDegreeCentrality2Hops(65);
		
		
		
		//try adding a bad Edge
	/*	testGraph.addEdge(18,  99);
		testGraph.addEdge(0, 7);
		testGraph.addEdge(88, 32);
		*/
		//add same edge twice
		//testGraph.addEdge(65, 23);
		
		//Check graph
		/*System.out.println("Number of Vertices: " + testGraph.getNumVertices() + " Number of Edges:  "+ testGraph.getNumEdges());
		
		testGraph.PrintAdjacencyList();*/
		//testGraph.getNodeLeastDegreesOfSep();
		//testGraph.getClosenessCentrality(65);
		
		//testGraph.DisplayGraph();
		
		//test transpose Graph
		//testGraph.transposeGraph(testGraph);
		
		//test SCC
		/*List<Graph> SCCList = testGraph.getSCCs();
		System.out.println("List of SCCs found in Graph");
		for (int index=0; index< SCCList.size(); index++){
			CapGraph SCCGraph = (CapGraph)SCCList.get(index);
			SCCGraph.PrintAdjacencyList();
		}
		
		//Test degrees of separation
		int sep = testGraph.getDegSeparation(25, 50);
		System.out.println("The Degrees of Separation between 65 and 50 are: " + sep);
		
		System.out.println("The maximum dos for vertex 23 is : " + testGraph.getMaxDegSeparation(23));
		
		//set one node to A
		//testGraph.setNodeToA(25);
		//testGraph.setNodeToA(18);
		/*boolean change = testGraph.willNodeChangetoA(23);
		
		if (change)
			System.out.println("Change node to A");
		else
			System.out.println("Node doesn't change to A");*/
		
		//testGraph.RunCascade();
	
		
		//testGraph.FindNodeWithMostNeighbors();
		
		/*CapGraph egoNet = (CapGraph) testGraph.getEgonet(65);
		System.out.println("Egonet for node 65");
		egoNet.PrintAdjacencyList();*/
		
		//Test using GraphLoader class
		
		
		/*int v = FBGraph.FindNodeWithMostNeighbors();
		int x = FBGraph.getMaxDegSeparation(v);
		
		
		System.out.println("Max Degrees of separation of node " + v+ "is : "+ x);
		FBGraph.getNodeLeastDegreesOfSep();
		FBGraph.getClosenessCentrality(v);
		int v2 = FBGraph.CalculateCentralityValues();
		FBGraph.getMaxDegSeparation(v2);
		
		//seed cascade
		HashSet<Integer> neighbors = FBGraph.getNeighbors(v);
		int counter = 0;
		for (int node : neighbors){
			counter ++;
			
			if (counter > (neighbors.size()))
				break;
			else
				FBGraph.setNodeToA(node);
			
		}
		System.out.println(counter + " nodes seeded in Graph.");
		//seed more
			/*	neighbors = FBGraph.getNeighbors(v2);
				counter = 0;
				for (int node : neighbors){
					counter ++;
					
					if (counter > (neighbors.size()))
						break;
					else
						FBGraph.setNodeToA(node);
					
				}
				System.out.println(counter + " nodes seeded in Graph.");
		*/
		//FBGraph.RunCascade();
		//FBGraph.DisplayGraph();
		
		/*//FBGraph.PrintAdjacencyList();
		//egoNet =(CapGraph) FBGraph.getEgonet(4);
		//egoNet.PrintAdjacencyList();
		//Can't print it out for large FB data!  Too big?  Figure this out or write to a file?
		//System.out.println(FBGraph.adjacencyString());
		
		//HashMap<Integer,HashSet<Integer>> data = FBGraph.exportGraph();
		
		//GraphLoader.loadGraph(TwitterGraph, "data/twitter_higgs.txt");
		
		//TwitterGraph.PrintAdjacencyList();
		//System.out.println("Twitter Vertices "+ TwitterGraph.getNumVertices() + ", Edges "+ TwitterGraph.getNumEdges());
		
		*/
		
		
		
		/*GraphLoader.loadGraph(FBGraph, "data/facebook_UCSD.txt");
		System.out.println("FB Vertices "+ FBGraph.getNumVertices() + ", Edges "+ FBGraph.getNumEdges());

		FBGraph.PrintAdjacencyList();
		//list number of neighbors
		//for(int index =0; index < FBGraph.getNumVertices())
		//look at SCC's in Fb data
		SCCList = FBGraph.getSCCs();
		System.out.println("Number of SCCs found in Facebook graph: " + SCCList.size());
		for (int index = 0; index < SCCList.size(); index++)
		{
			CapGraph scc = (CapGraph)SCCList.get(index);
			if (scc.getNumVertices()> 1){
				System.out.println("SCC number : " + index + 
					" Number of vertices in Graph: "+ scc.getNumVertices());
			}
		}
		
		int max = FBGraph.getMaxDegSeparation(792);
		
		System.out.println("Max Deg of Separation for 792 is: " + max);*/
		
	}

}
