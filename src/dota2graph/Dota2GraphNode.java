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

	private String characterName;
	private ArrayList<Dota2GraphEdge> edges;
	
	public Dota2GraphNode(String name){
		characterName = name;
		edges = new ArrayList<Dota2GraphEdge> ();
	}
	
	
	public boolean equals (Object obj){
		// if the two objects are equal in reference, they are equal
	    if (this == obj) {
	      return true;
	    } else if (obj instanceof Dota2GraphNode) {
	    	Dota2GraphNode node = (Dota2GraphNode) obj;
	      if ( this.characterName.equalsIgnoreCase(node.getCharacterName())
	    		 && this.edges.equals(node.getEdges()) ) {
	        return true;
	      } else {
	        return false;
	      }
	    } else {
	      return false;
	    }
	}
	public int hashCode() {
	    int hash = this.characterName.hashCode();
	    return hash;
	}
	
	//accessors

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
	
	public String toString() {
		return "(" + characterName + ")";	
	}

}

