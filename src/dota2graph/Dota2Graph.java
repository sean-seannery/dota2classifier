/**
 * 
 */
package dota2graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author sam
 *
 */
public class Dota2Graph {

	private HashMap<Dota2GraphNode, ArrayList<Dota2GraphEdge>> graphMap;
	private Date startTime;
	private Date endTime;
	
	public Dota2Graph(){
		graphMap = new HashMap<Dota2GraphNode, ArrayList<Dota2GraphEdge>>();
		startTime = null;
		endTime = null;
	}
	public Dota2Graph(Date newStartTime, Date newEndTime){
		graphMap = new HashMap<Dota2GraphNode, ArrayList<Dota2GraphEdge>>();
		startTime = newStartTime;
		endTime = newEndTime;
	}

	public void addNode(Dota2GraphNode newNode){
		if (!graphMap.containsKey(newNode)){
			graphMap.put(newNode, new ArrayList<Dota2GraphEdge>());
		}
		
	}
	
	public void addEdge(Dota2GraphNode source, Dota2GraphEdge edge){
		if (graphMap.containsKey(source)) {
			graphMap.get(source).add(edge);
		} else {	
			ArrayList<Dota2GraphEdge> edges =  new ArrayList<Dota2GraphEdge>();
			edges.add(edge);
			graphMap.put(source, edges);
		}
	}
	
	public int getEdgeCount() {
		int counter = 0;
		for (ArrayList<Dota2GraphEdge> edges : graphMap.values()) {
			counter += edges.size();
		}
		return counter;
	}
	
	public int getTotalEdgeWeight() {
		int counter = 0;
		for (ArrayList<Dota2GraphEdge> edges : graphMap.values()) {
			for (Dota2GraphEdge edge : edges) {
				counter += edge.getWeight();
			}
		}
		return counter;
	}
	
	public int getNodeCount() {
		return graphMap.keySet().size();
	}
	
	public String toString() {
		String output = "Dota2Graph{" + "\n";
		for (Dota2GraphNode node : graphMap.keySet() ) {
			output += "   " + node + graphMap.get(node).size() + "\n";
		}
		output +="}";
		return output;
	}
	
	public String calculateMVP() {
		int weight = 0;
		int maxWeight = 0;
		Dota2GraphNode maxNode = null;
		for (Dota2GraphNode node : graphMap.keySet() ) {
			weight = 0;
			for (Dota2GraphEdge edge : graphMap.get(node)) {
				
					weight += edge.getWeight();

			}
			if (weight >= maxWeight){
				maxWeight = weight;
				maxNode = node;
			}
			
		}
		
		return maxNode.getCharacterName();
	}
	
}
