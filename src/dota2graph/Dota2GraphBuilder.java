/**
 * 
 */
package dota2graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author sam
 *
 */
public class Dota2GraphBuilder {

	Dota2Graph graph;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//argument processing
		if (args.length != 1) {
			System.out.println("You must provide at one file name");
			System.out.println("Example:");
			System.out.println("java Dota2GraphBuilder thisfile.log_parsed");

		}
		if (args.length == 1 && args[0].endsWith(".log_parsed")){
			Dota2GraphBuilder builder = new Dota2GraphBuilder();
			builder.createGraph(args[0]);
		}

	}
	
	public Dota2GraphBuilder(){
		graph = new Dota2Graph();
	}
	
	public void createGraph(String fileName) {
		File file = new File (fileName);
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseDota2Event(line);
			}
			System.out.println(graph);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void parseDota2Event(String line) {

		line = line.replace(".", "");
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
		String time = words.get(0);
		String p1Name = "";
		String p2Name = "";
		int weight = 0;
		
		if (line.contains("damage")){
			for (int i=2;i<words.indexOf("deals");i++){
				p1Name = p1Name + words.get(i) + " ";
			}
			p1Name = p1Name.trim();
			for (int i=words.indexOf("to")+1;i<words.indexOf("using");i++){
				p2Name = p2Name + words.get(i) + " ";
			}
			p2Name = p2Name.trim();
			weight = Integer.parseInt(
					words.get(words.indexOf("deals") + 1) );
		}
		if (line.contains("heals")){
			for (int i=2;i<words.indexOf("heals");i++){
				p1Name = p1Name + words.get(i) + " ";
			}
			p1Name = p1Name.trim();
			for (int i=words.indexOf("to")+1;i<words.indexOf("using");i++){
				p2Name = p2Name + words.get(i) + " ";
			}
			p2Name = p2Name.trim();
			weight = Integer.parseInt(
					words.get(words.indexOf("HP") - 1) );
		}
		if (line.contains("dies")){
			for (int i=2;i<words.indexOf("dies");i++){
				p2Name = p2Name + words.get(i) + " ";
			}
			p2Name = p2Name.trim();
			for (int i=words.indexOf("Killer:")+1;i<words.size();i++){
				p1Name = p1Name + words.get(i) + " ";
			}
			p1Name = p1Name.trim();
		}
		Dota2GraphNode p1Node = new Dota2GraphNode(p1Name);
		Dota2GraphNode p2Node = new Dota2GraphNode(p2Name);
		Dota2GraphEdge edge = new Dota2GraphEdge(weight, p2Node);
		graph.addNode(p1Node);
		graph.addNode(p2Node);
		graph.addEdge(p1Node, edge);
	}


}
