/**
 * 
 */
package dota2graph;

import java.util.ArrayList;

/**
 * @author sam
 *
 */
public class Dota2GraphNode {

	private int playerID;
	private String characterName;
	private ArrayList<Dota2GraphEdge> edges;
	
	public Dota2GraphNode(int id, String name){
		playerID = id;
		characterName = name;
		edges = null;
	}
	
	
	public boolean equals (Object obj){
		// if the two objects are equal in reference, they are equal
	    if (this == obj) {
	      return true;
	    } else if (obj instanceof Dota2GraphNode) {
	    	Dota2GraphNode node = (Dota2GraphNode) obj;
	      if (this.playerID == node.getPlayerID() && this.characterName.equalsIgnoreCase(node.getCharacterName())
	    		 && this.edges.equals(node.getEdges()) ) {
	        return true;
	      } else {
	        return false;
	      }
	    } else {
	      return false;
	    }
	}
	
	
	//accessors
	public int getPlayerID() {
		return playerID;
	}
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
	public String getCharacterName() {
		return characterName;
	}
	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}
	public ArrayList<Dota2GraphEdge> getEdges() {
		return edges;
	}
	public void setEdges(ArrayList<Dota2GraphEdge> edges) {
		this.edges = edges;
	}

}

