package dota2graph;

public class Dota2GraphEdge {

	private int weight;
	private Dota2GraphNode next;
	
	
	public Dota2GraphEdge(int newWeight, Dota2GraphNode newNext) {
		weight = newWeight;
		next = newNext;
	}
	
	public boolean equals (Object obj){
		// if the two objects are equal in reference, they are equal
	    if (this == obj) {
	      return true;
	    } else if (obj instanceof Dota2GraphEdge) {
	    	Dota2GraphEdge edge = (Dota2GraphEdge) obj;
	      if (this.weight == edge.getWeight() && this.next.equals(edge.getNext()))
	     {
	        return true;
	      } else {
	        return false;
	      }
	    } else {
	      return false;
	    }
	}
	
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public Dota2GraphNode getNext() {
		return next;
	}
	public void setNext(Dota2GraphNode next) {
		this.next = next;
	}
	
	
	
	
}
