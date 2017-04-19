/**
 * 
 */
package graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;



/**
 * @author kguajardo
 * 
 * RankingNodes Class is an object that will rank Graph 
 * Nodes based on Centrality properties:
 * 
 * Graph type is undirected, unweighted
 * 
 * Degree Centrality: Number of degrees(neighbors) of a node
 * (maximum value is more central)
 * 
 * Degree Centrality 2 Hop: number of degrees of a node + number of degrees of all its neighbors
 * (maximum value is more central)
 * 
 * Closeness Centrality: The average distance (number of hops) from a node to all other nodes
 * (miniumum value is more central)
 *
 */
public class RankingNodes {
	
	// Lists of the top ranked nodes(node id's stored in list) based on centrality property
	private ArrayList<Integer> m_TopDegreeNodes;
	private ArrayList<Integer> m_TopDeg2HopNodes;
	private ArrayList<Integer> m_TopClosenessNodes;
	private ArrayList<Integer> m_1DegreeNodes;
	
	//this is a ranking based on combining these properties
	// criteria????
	//private ArrayList<Integer> m_TopCombinedCentNodes;
	
	//percent of graph Nodes that will be listed in top ranked list
	//i.e. 1 is 1 percent, 20 is 20%, valid values between 0 and 100
	private int m_percentTopNodes;
	
	private CapGraph m_GraphtoRank;
	
	public RankingNodes(/*CapGraph graph*/){
		//m_GraphtoRank = graph;
		
		m_TopDegreeNodes = new ArrayList<Integer>();
		m_TopDeg2HopNodes = new ArrayList<Integer>();
		m_TopClosenessNodes = new ArrayList<Integer>();
		//m_TopCombinedCentNodes = new ArrayList<Integer>();
		m_1DegreeNodes = new ArrayList<Integer>();
	}
	
	
	/**
	 * RankNodesDegree: loop through nodes and calculate and store Degrees, create list ranking
	 * 
	 * Rank in Descending Order
	 */
	public void RankNodesDegree(){
		
		
		for (int node: m_GraphtoRank.getVertices()){
			int deg = m_GraphtoRank.getNeighbors(node).size();
			
			//store value
			m_GraphtoRank.setDegrees(node, deg);
			
			//store all graphs with single degree (outer edge of graph)
			if (deg ==1){
				m_1DegreeNodes.add(node);
				
			}
			
			//Add to Ranking list
			BinarySearchDescending(m_TopDegreeNodes,node, deg, 1 );
		}
	}
	/**
	 * RankingNodesDeg2Hop: loop through nodes and calculate and store Deg2Hop, create List ranking
	 * 
	 * Rank in Descending Order
	 */
	public void RankNodesDeg2Hop(){
		
		
		for (int node: m_GraphtoRank.getVertices()){
			int deg2hop = CalcDeg2Hop(node);
			//System.out.println("Deg 2 hop for node " + node + " is " + deg2hop);
			
			//store value
			m_GraphtoRank.setDeg2Hop(node,deg2hop);
			
			//Insert into Ranking list
			BinarySearchDescending(m_TopDeg2HopNodes,node, deg2hop, 2);	
		}
	}
	/**
	 * RankNodesCloseness: loop through nodes and calculate and store Closeness, create list ranking
	 * 
	 * Rank in Ascending Order
	 */
	public void RankNodesCloseness(){
				
		//loop through graph nodes and calculate and store Closeness, create List ranking
		for (int node: m_GraphtoRank.getVertices()) {
			double closeness = CalcCloseness(node);
			
			System.out.println("Ranking Closeness, node: " + node);
			
			//store value
			m_GraphtoRank.setNodeCloseness(node, closeness);
			//System.out.println("Closeness value for node " + node + " is "+ closeness);
			
			//insert node into ranking if in top percent
			BinarySearchInsertAscending(m_TopClosenessNodes,node, closeness);
		}
		return;
	}
	
	/**
	 * CalcDeg2Hop: Calculate the total number of degrees of node in 2 hops (node + neighbors)
	 * @param node
	 * @return: return the number of degrees of node + the number of degrees of all node's neighbors
	 */
	private int CalcDeg2Hop(int node){
		
		if (!m_GraphtoRank.getVertices().contains(node)){
			//not a valid node id
			return -1;
		}
		
		int deg2Hop =0;
		HashSet<Integer> neighborsHop1;
				
		//Get 1st hop neighbor count
		neighborsHop1 = m_GraphtoRank.getNeighbors(node);
		deg2Hop += neighborsHop1.size();
				
		//Get 2nd hop neighbor count
		for (int v: neighborsHop1){
			deg2Hop += m_GraphtoRank.getNeighbors(v).size();	
		}	
		return deg2Hop;
	}
	
	/**
	 * BinarySearchDescending : Insert node into descending ranked list of top values if it is 
	 * 							in the top percent.  If value is in the top percent, use a binary
	 * 							search to determine the correct placement in the descending order
	 * 							list.
	 * 
	 * @param list : This is the ranking list to insert into.
	 * @param node : node id  of node being ranked
	 * @param value: Value of the property of the node id to rank.
	 * @param numHops: Used for to determine where to get lookup values, Degree or 2HopDegree
	 */
	private void BinarySearchDescending(ArrayList<Integer> list, int node, int value, int numHops){
		int MinListValue;
		
		if (list.isEmpty()){
			//if it's the first node to check, automatically makes it into the list
			list.add(node);
			return;
		}
		
		//Last value in the list in the minimum value, value must be greater than this to insert
		MinListValue = getDegreeValue(numHops, list.get(list.size()-1));
		
		//Ranking variables
		float percent =(float) m_percentTopNodes/100;
		int ListSize = (int) (m_GraphtoRank.getNumVertices() * percent);
		if (ListSize < 1){
			//store at a minimum the maximum value in list (1 value);
			ListSize =1;
		}
		boolean inserted = false;
		
		//Is top ranked?
		if (list.size() >= ListSize && value < MinListValue)
			//Not top ranked, don't insert
					return;
		
		//Insert into ranking list in descending order
		//System.out.println("Start binary search");
		int low, high, mid;
		high =0;
		low = list.size() - 1;
				
		while (!inserted){
			mid = high + ((low-high)/2);
			
			if (value == getDegreeValue(numHops,list.get(mid))){
				//nodes with same value, insert here
				list.add(mid, node);
				inserted = true;
			}
			if (value <  getDegreeValue(numHops, list.get(mid))){
				high = mid + 1;
				if (high>=list.size()){
					//insert at end, high value is greater than list size
					list.add(node);
					inserted = true;
				}
			
				//check to see if we went passed closeness value, if so insert here
				else if (value > getDegreeValue(numHops,list.get(high))){
					list.add(high, node);
					inserted = true;
				}
			}
			if(value >  getDegreeValue(numHops,list.get(mid))){
				low = mid - 1;
				//if low value is bigger than size of list, add at end
				if (low < 0){
					//add at end of list
					//System.out.println("low< 0, inserting at beginning");
					list.add(0,node);
					inserted = true;
				}
				//went passed closeness value, insert here
				else if(value <  getDegreeValue(numHops, list.get(low))){
					list.add(low+1, node);
					inserted = true;
				}
			}		
		}//while loop list insertion
		
		if (list.size()> ListSize){
			//list too big, remove last from list
			list.remove(ListSize);
		}
		MinListValue = getDegreeValue(numHops,list.get(list.size() - 1));	
		
		return;
				
	}
	
	/**
	 * getDegreeValue: Looks up degree or 2hopdegree value for node
	 * @param numHops: 1 to lookup degree value, 2 to loop up 2hopdegree value
	 * @param node: node to look up value for
	 * @return:  return degree or 2hopdegree value
	 */
	private int getDegreeValue(int numHops, int node){
		if (numHops == 1) 
			return m_GraphtoRank.getDegrees(node);
		else if (numHops == 2)
			return m_GraphtoRank.getDeg2Hop(node);
		
		return -1; //invalid numHops
			
	}
	
	/**
	 * BinarySearchAscending : Insert node into ascending ranked list of top values if it is 
	 * 							in the top percent.  If value is in the top percent, use a binary
	 * 							search to determine the correct placement in the ascending order
	 * 							list.
	 * 
	 * @param list : This is the ranking list to insert into.
	 * @param node : node id  of node being ranked
	 * @param value: Value of the property of the node id to rank.
	 * 
	 */
	private void BinarySearchInsertAscending(ArrayList<Integer> list, int node, double value ){
		
		double MaxListValue;
		
		if (list.isEmpty()){
			//If first node to be ranked, automatically gets added to list
			list.add(node);
			return;
		}
		
		MaxListValue = m_GraphtoRank.getNodeCloseness(list.get(list.size()-1));
		
		//Ranking variables
		float percent =(float) m_percentTopNodes/100;
		int ListSize = (int) (m_GraphtoRank.getNumVertices() * percent);
		if (ListSize < 1){
			//store at a minimum the maximum value in list (1 value);
			ListSize =1;
		}
		
		boolean inserted = false;
		
		//Is top ranked?
		if (list.size() >= ListSize && value > MaxListValue){
			//No it's not, don't insert
			return;
		}
		

		//Insert in list in ascending order order - inserting node id not CC value
		//Binary search for insertion		
		int low, high, mid;
		low =0;
		high = list.size() - 1;
				
		while (!inserted){
			mid = low + ((high-low)/2);
			
			if (value == m_GraphtoRank.getNodeCloseness(list.get(mid))){
				//nodes with same value, insert here
				list.add(mid, node);
				inserted = true;
			}
			if (value <  m_GraphtoRank.getNodeCloseness(list.get(mid))){
				high = mid - 1;
				if (high<0){
					//insert at beginning
					list.add(0,node);
					inserted = true;
				}
			
				//check to see if we went passed closeness value, if so insert here
				else if (value > m_GraphtoRank.getNodeCloseness(list.get(high))){
					list.add(high+1, node);
					inserted = true;
				}
			}
			if(value >  m_GraphtoRank.getNodeCloseness(list.get(mid))){
					
				low = mid + 1;
				//if low value is bigger than size of list, add at end
				if (low>= list.size()){
					//add at end of list
					list.add(node);
					inserted = true;
				}
				//went passed closeness value, insert here
				else if(value <  m_GraphtoRank.getNodeCloseness(list.get(low))){
					list.add(low, node);
					inserted = true;
				}
			
			}
		}//while loop list insertion
		
		if (list.size()> ListSize){
			//list too big, remove last from list
			list.remove(ListSize);
		}
		MaxListValue = m_GraphtoRank.getNodeCloseness(list.get(list.size() - 1));	
		
		return;
	}
	
	/**
	 * CalcCloseness: Calculates the closeness value for give node.
	 * 					Closeness = avg path length to all other nodes from given node
	 * @param node
	 * @return: value of Closeness for node
	 */
	private double CalcCloseness(int node){
		
		//Checks for bad input
		if (!m_GraphtoRank.getVertices().contains(node)){
			//node value not in graph return
			//To do errors
			return -1;
		}
				
		double cc;
		//Use BFS
		
		//Initialize
		int numPaths =0;
		int TotalHops=0;
		ArrayList<Integer> Queue = new ArrayList<Integer>();
		HashSet<Integer> Visited = new HashSet<Integer>();
		HashMap<Integer, Integer> HopMap = new HashMap<Integer, Integer>();
		
		//Start queue
		Queue.add(node);
		Visited.add(node);
		HopMap.put(node, 0);

		int curr = node;

		//Start Search
		while (!Queue.isEmpty())
		{
			curr = Queue.remove(0);
			//System.out.println("Curr is : "+ curr);
			//add all of curr's neighbors to the queue
			HashSet<Integer> neighbors = m_GraphtoRank.getNeighbors(curr);
			
			for (int n: neighbors){
				if (!Visited.contains(n)){
					Visited.add(n);
					Queue.add(n);
					int numHops = HopMap.get(curr) + 1;
					HopMap.put(n, numHops);
					TotalHops += numHops;
					numPaths++;
					
				}//if
			}// loop through neighbors
		}//while loop
	
		cc = (float)TotalHops/numPaths;
		
		return cc;
	}
	
	
	/**
	 * 
	 * getters and setters
	 * 
	 */
	
	public ArrayList<Integer> getTopDegList(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(m_TopDegreeNodes);

		return list;
	}
	
	public ArrayList<Integer> getTopDeg2HopList(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(m_TopDeg2HopNodes);

		return list;
	}
	
	public ArrayList<Integer> getTopClosenessList(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(m_TopClosenessNodes);

		return list;
	}
	
/*	public ArrayList<Integer> getTopCombinedList(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(m_TopCombinedCentNodes);

		return list;
	}*/
	
	public ArrayList<Integer> get1DegreeNodesList(){
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(m_1DegreeNodes);

		return list;
	}
	
	public void setPercentNodesList(int percent){
		
		if (percent<0 || percent > 100)
			//invalid value
			return;
		
		m_percentTopNodes = percent;
		return;
		
	}
	
	public void setGraph(CapGraph graph){
		m_GraphtoRank = graph;
	}
	
	
	

}
