package graph;

import java.util.ArrayList;
import java.util.HashSet;

import org.graphstream.graph.implementations.SingleGraph;

public class GraphCascader {
	
	//Reward for switching to a or b
	private int m_a;
	private int m_b;
	 
	//Graph to cascade
	private CapGraph m_GraphtoCascade;
	
	//Node rankings
	private RankingNodes m_RankGraph;
	
	//Number of Generations to run for Cascade
	private int m_NumGenerations;
	
	//Visualization
	private SingleGraph graphView;
	
	public GraphCascader(){
		//Constructor
		
		//default values for a and b: 1
		m_a = 1;
		m_b = 1;
		
		//Graph node ranking Initialization
		//ranking graph
		m_RankGraph = new RankingNodes();
				
		//set percent values to 10 percent - TODO set this somewhere
		m_RankGraph.setPercentNodesList(10);
		
		//set default value to 50
		m_NumGenerations = 50;	
	}
	
	/**
	 * willNodeChage: Determines if given node, v, will change to A
	 * based on m_a, m_b reward values and fraction of neighbors that
	 * have already switched to A 
	 * 
	 * @param v is node to check
	 * @return 	true if node should chnge to A
	 * 			false if node should not change to A
	 */
	private boolean willNodeChangetoA(int v){
		//set values of rewards a and b
		//a is reward if node switches to A
		//b is reward if node switches to B
		
		float p;
		int D;
		
		//D is the number of friends node v has
		HashSet<Integer> neighbors = m_GraphtoCascade.getNeighbors(v); 
		D = neighbors.size();
		int numA=0;
		
		//if D is zero then no p or p = 0
		//get p : percentage of friends that are A
		if (D != 0 ){
			for (int x: neighbors){
				if (m_GraphtoCascade.getABProperty(x) == 'A')
					numA++;
				
			}
			p = (float)numA/D;
		}
		else 
			p=0;
		
		if (p>= ((float)m_b/(m_a+ m_b)))
			return true;
		else
			return false;
		
	}
	
	/**
	 * SeedGraph: This will seed nodes to start the cascade.
	 * Seeding based on ranking list passed in
	 * Seeding occurs by using top nodes from the list and seeding a percentage of its neighbors
	 * The number of top nodes to seed around is passed in.
	 * @param RankedList
	 * @param numTopNodes = num of clusters around top nodes to seed
	 * @param pctNeighborsSeed = percent of neighbors of top nodes to seed.
	 */
	private void SeedGraph(ArrayList<Integer> RankedList, int numTopNodes, int pctNeighborsSeed){
		
		HashSet<Integer> SeededNodes = new HashSet<Integer>();
	
		
		//check that numTopNodes is not greater than list size
		// if it is, set it to list size.
				if (numTopNodes> RankedList.size()){
					numTopNodes = RankedList.size();
				}
		
		for (int index = 0; index < numTopNodes; index++){
			int counter =0;
			int node = RankedList.get(index);
			int numToSeed;
			HashSet<Integer> NodeNeighbors = m_GraphtoCascade.getNeighbors(node);
			numToSeed =  (NodeNeighbors.size() * pctNeighborsSeed)/100;
			
			//seed percent of neighbors passed in
			for (int neighbor : NodeNeighbors) {
				if (counter< numToSeed/* && !SeededNodes.contains(neighbor)*/ && (m_GraphtoCascade.getABProperty(neighbor) == 'B')){
					counter++;
					m_GraphtoCascade.setNodeToA(neighbor);
					SeededNodes.add(neighbor);
				}
				else if (counter >= numToSeed){
					break;
				}
			}
		}
		System.out.println("Total Nodes seeded = " + SeededNodes.size());
		return;
	}
	
	/**
	 * RunCascade: Runs a cascade through the graph of PropertyAB after it's been seeded.  Assumes 
	 * default value of B and checks to see which nodes will change to A.  Runs m_NumGenerations 
	 * or runs until an equilibrium is reached, whichever comes first.
	 */
	private void RunCascade(){
		
		//to start, check all nodes 
		ArrayList<Integer> NodesChanged = new ArrayList<Integer>(); 
		NodesChanged.addAll(m_GraphtoCascade.getVertices());
		
		int TotalNodesChanged =0;
		
		//Run NumGens number of generations
		int NumGens= 50;
		for (int gen=0; gen <NumGens; gen++){
			//for each generation keep track of nodes to check and  nodes that change
			// Use HashSet, so same nodes are not added more than once
			HashSet<Integer> NodesToCheck = new HashSet<Integer>();
			ArrayList<Integer> NodesToChange = new ArrayList<Integer>();
			//find nodes to check:
			//nodes that are neighbors of nodes that changed in last generation
			for (int index = 0; index<NodesChanged.size(); index++){
				//add all neighbors of each node that changed if they are 
				NodesToCheck.addAll(m_GraphtoCascade.getVertex(NodesChanged.get(index)).getNeighbors());
			}
				
			//Calculate p for nodes whose neigbhors have changed in last generation
			for (int node: NodesToCheck){
				
				//only change the nodes that are not already change to A
				if(m_GraphtoCascade.getABProperty(node) != 'A' && willNodeChangetoA(node) )
					NodesToChange.add(node);
			}
			
			
			// Set Property to A for node if p>(b/(a+b))
			// Keep of list of nodes that get changed for next generation
			for(int n = 0; n < NodesToChange.size(); n ++){
				m_GraphtoCascade.setNodeToA(NodesToChange.get(n));
				//System.out.println("Changed node " + NodesToChange.get(n) + " to A.");
				TotalNodesChanged++;
			}
			sleep();
			
			//if nothing changed, reached equilibrium, stop running generations
			if(NodesToChange.size() == 0){
				System.out.println("Equilibrium reached, total nodes changed: "+ TotalNodesChanged + 
						" in " + gen + "generations.");
				
				break;
			}
			else{
			//update NodesChanged for next generation
				NodesChanged.clear();
				NodesChanged.addAll(NodesToChange);
			}
		}
		System.out.println("Finished. Total nodes changed: "+ TotalNodesChanged);
		return;
		
	}

	/**
	 * RunCascadeCloseness : This runs a cascade on the graph using the ranking of nodes 
	 * based on Closeness Centrality (i.e. avg distance from node to every other node).
	 * @param numAreasToSeed - the number of clusters to seed.
	 * @param percentNeighborsToSeed - percentage of neighbors of ranked node to seed for
	 * 									each cluster.
	 */
	public void RunCascadeCloseness(CapGraph graph, int numAreasToSeed, int percentNeighborsToSeed ){
		 
		//Initialization
		Initialize(graph);
		
		//Rank Nodes based on Closeness Centrality
		m_RankGraph.RankNodesCloseness();
		
		//Display initial Graph before seeding
		DisplayGraph();
		sleep();
		
		//seed graph based on Closeness rankings
		SeedGraph(m_RankGraph.getTopClosenessList(),numAreasToSeed,percentNeighborsToSeed);
		sleep();
		
		//start Cascade
		RunCascade();
	}
	
	/**
	 * RunCascadeDegrees : This runs a cascade on the graph using the ranking of nodes 
	 * based on Degrees.
	 * @param numAreasToSeed - the number of clusters to seed.
	 * @param percentNeighborsToSeed - percentage of neighbors of ranked node to seed for
	 * 									each cluster.
	 */
	public void RunCascadeDegrees(CapGraph graph, int numAreasToSeed, int percentNeighborsToSeed ){
		 
		//Initialization
		Initialize(graph);
		
		//Rank Nodes based on Closeness Centrality
		m_RankGraph.RankNodesDegree();
		
		//Display initial Graph before seeding
		DisplayGraph();
		sleep();
		
		//seed graph based on Closeness rankings
		SeedGraph(m_RankGraph.getTopDegList(),numAreasToSeed,percentNeighborsToSeed);
		sleep();
		
		//start Cascade
		RunCascade();
	}
	
	/**
	 * RunCascade2HopDeg : This runs a cascade on the graph using the ranking of nodes 
	 * based on  2 Hop Degrees.
	 * @param numAreasToSeed - the number of clusters to seed.
	 * @param percentNeighborsToSeed - percentage of neighbors of ranked node to seed for
	 * 									each cluster.
	 */
	public void RunCascade2HopDeg(CapGraph graph, int numAreasToSeed, int percentNeighborsToSeed ){
		 
		//Initialization
		Initialize(graph);
		
		//Rank Nodes based on Closeness Centrality
		m_RankGraph.RankNodesDeg2Hop();
		
		//Display initial Graph before seeding
		DisplayGraph();
		sleep();
		
		//seed graph based on Closeness rankings
		SeedGraph(m_RankGraph.getTopDeg2HopList(),numAreasToSeed,percentNeighborsToSeed);
		sleep();
		
		//start Cascade
		RunCascade();
	}
	
	private void Initialize(CapGraph graph){
		//Initialization
		m_GraphtoCascade = graph;
		graphView = graph.getGraphView();
		m_RankGraph.setGraph(graph);
		
	}
	
	private void DisplayGraph() {
	
		graphView.display();
	}

	private void sleep() {
		//time delay, used for graph display, so each generation change can be viewed
		try { Thread.sleep(1000); } catch (Exception e) {}
	}

	
	//getters and setters
	public void seta(int a){
		if (a<0)
			//bad input - todo error handling
			return;
		m_a = a;
		return;
	}
	
	public void setb(int b){
		if (b<0)
			//bad input - todo error handling
			return;
		m_b = b;
		return;
	}
	
	public void setNumGenerations(int num){
		if (num<0)
			//bad input
			return;
		
		m_NumGenerations = num;
	}
	
	public int getNumGenerations(){
		return m_NumGenerations;
	}

}
