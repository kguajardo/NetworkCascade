package graph;
import java.util.HashSet;

/**
 * 
 * @author kguajardo
 * 
 * CapVertex: 	Represents a node in the graph.  Keeps a list of all neighbors 
 * 				or edges for each node. Also stores the property of the node 
 * 				for the cascade (default set to “B” and cascade to “A”), centrality
 * 				values for the nodes used to help in seeding the cascade: degree 
 * 				centrality for 1 hop and 2 hops as well as closeness centrality.
 *
 */

public class CapVertex {
	//member variables of vertex
	
	//the id of this vertex (also stored in adjlist as lookup in CapGraph - do I need it here?
	private int m_vertexID;
	
	//PropertyAB may be set to A or B, default is B
	private char m_PropertyAB;
	
	//m_neighbors is a list of vertex id's for the outgoing neighbors of this vertex
	private HashSet<Integer> m_neighbors;
	
	//For node ranking, centrality values for node
	private int m_DegCent;
	private int m_DegCent2Hop;
	private double m_ClosenessCent;
	
	
	public CapVertex(int id){
		
		m_vertexID = id;
		
		//set Property to default value B
		setPropertyABtoB();
	
		//create neighbor list
		m_neighbors = new HashSet<Integer>();
		
	}
	
	public int getVertexID(){
		return m_vertexID;
	}
	
	public void addNeighbor(int node){
		
		//return unique edge id for visualization tool
		m_neighbors.add(node);
	}
	
	public HashSet<Integer> getNeighbors(){
		//return a copy
		HashSet<Integer> neighbors = new HashSet<Integer>();
		neighbors.addAll(m_neighbors);
		
		return neighbors;
	}
	
	public char getPropertyAB()
	{
		return m_PropertyAB;
	}
	
	public void setPropertyABtoA(){
		m_PropertyAB = 'A';
	}
	
	public void setPropertyABtoB(){
		m_PropertyAB = 'B';
	}

	public int getDegCent(){
		return m_DegCent;
	}
	
	public void setDegCent(int degCent){
		m_DegCent = degCent;
	}
	
	public int getDeg2Hop(){
		return m_DegCent2Hop;
	}

	public void setDeg2Hop(int degCent2){
		m_DegCent2Hop = degCent2;
	}
	
	public double getClosenessCent(){
		return m_ClosenessCent;
	}

	public void setClosenessCent(double closeCent){
		m_ClosenessCent = closeCent;
	}
}
