/**
 * 
 */
package dota2graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author sam
 *
 */
public class Dota2GraphBuilder {

	private Dota2Graph graph;
	private ArrayList<ArrayList<Integer>> timeWindows;
	private Date startTimeOffset = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//argument processing
		if (args.length != 2) {
			System.out.println("You must provide two file names");
			System.out.println("Example:");
			System.out.println("java Dota2GraphBuilder thisfile.log_parsed times.txt");

		}
		if (args.length == 2 && args[0].endsWith(".log_parsed")){
			Dota2GraphBuilder builder = new Dota2GraphBuilder();
			builder.readInTimeWindows(args[1]);
			for (ArrayList<Integer> dates : builder.getTimeWindows()){
				builder.createGraph(args[0],dates.get(0), dates.get(1));
				//System.out.println(builder.getGraph());
				System.out.println(builder.getGraph().calculateMVP());
				System.out.println();
			}
			
			System.out.println(builder.getKDAMVP(args[0].replace(".log_parsed", ".txt")));
			
		}
		//do all files in a directory
		if (args.length == 2 && args[0].endsWith("/")){
			File folder = new File(args[0]);
			File[] listOfFiles = folder.listFiles();
			int correct = 0;
			int total = 0;
		    for (File f : listOfFiles) {
		    	
		    	if (f.getName().endsWith(".log_parsed")) {
		    		total++;
		    		String mvp1 = "", mvp2 = "";
		    		Dota2GraphBuilder builder = new Dota2GraphBuilder();
					builder.readInTimeWindows(args[1]);
					for (ArrayList<Integer> dates : builder.getTimeWindows()){
						
						builder.createGraph(f.getAbsolutePath(),dates.get(0), dates.get(1));
						mvp1 = builder.getGraph().calculateMVP();
						System.out.println(mvp1);		
					}
					mvp2 = builder.getKDAMVP(f.getAbsolutePath().replace(".log_parsed", ".txt"));
					System.out.println(mvp2);
					System.out.println();
					
					if (mvp1.equals(mvp2)){
						correct++;
					}
		    	}
		    }
		    System.out.println("FINAL: " + correct + "/" + total);
		}

	}
	
	public void createGraph(String fileName, Integer startTime, Integer stopTime) {
		graph = new Dota2Graph();
		File file = new File (fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseDota2Event(line, startTime, stopTime);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void parseDota2Event(String line, Integer startTime, Integer stopTime) {
	
		line = line.replace(".", "");
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
		Date timed = null;
		Integer time = -1;
		try {
			timed = new SimpleDateFormat("HH:mm").parse(words.get(0));
			if (startTimeOffset == null){
				startTimeOffset = timed;
			}

			time = (int) ((timed.getTime() - startTimeOffset.getTime()) /  1000 / 60);

			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String p1Name = "";
		String p2Name = "";
		int weight = 0;
		if (time >= startTime && time <=stopTime)
		{
			
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
				//if you heal yourself that weight doesnt count.
				if (!p1Name.equals(p2Name)){
					weight = Integer.parseInt(
							words.get(words.indexOf("HP") - 1) );
				}
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
				weight = 1000 ;
			}
			Dota2GraphNode p1Node = new Dota2GraphNode(p1Name);
			Dota2GraphNode p2Node = new Dota2GraphNode(p2Name);
			Dota2GraphEdge edge = new Dota2GraphEdge(weight, p2Node, null);
			graph.addNode(p1Node);
			graph.addNode(p2Node);
			graph.addEdge(p1Node, edge);
		}
	}
	
	public ArrayList<ArrayList<Integer>> readInTimeWindows(String fileName) {
		ArrayList<ArrayList<Integer>> retVal = new ArrayList<ArrayList<Integer>>();
		File file = new File (fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				int time1 = Integer.parseInt(line.split(" ")[0]);;
				int time2 = Integer.parseInt(line.split(" ")[1]);
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(time1);
				temp.add(time2);
				retVal.add(temp);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.timeWindows = retVal;
		return retVal;
		
	}
	
	public String getKDAMVP(String fileName) {
		File file = new File (fileName);
		BufferedReader reader = null;
		String bestHero = "";
		double bestKDA=-100;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().contains("Players Info:")){
					//read in another line because the next line is blank
					line = reader.readLine();
					System.out.println(file.getName());
					while (! (line = reader.readLine()).trim().equals("")) {
						//System.out.println(line);

						int startIndex = line.indexOf("KDA: ") + 5;
						int endIndex = line.indexOf(" CS: ");;
						String[] KDAString = line.substring(startIndex, endIndex).split("/");
						double denominator = Double.parseDouble(KDAString[1]);
						if (denominator == 0){
							denominator = .01;
						}
						//double kda = Double.parseDouble(KDAString[0]) / denominator;
						double kda = Double.parseDouble(KDAString[0]) * 2 +  
								     Double.parseDouble(KDAString[1]) * -3 +
								     Double.parseDouble(KDAString[2]) * 1;
						
						if (kda > bestKDA){
							bestKDA = kda;
							startIndex = line.indexOf(": ") + 2;
							endIndex = line.indexOf(" KDA: ") - 1;
							bestHero = line.substring(startIndex, endIndex);
							
						}
						
					}
					break;
				}
				
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bestHero;
		
	}

	public Dota2Graph getGraph() {
		return graph;
	}

	public void setGraph(Dota2Graph graph) {
		this.graph = graph;
	}

	public ArrayList<ArrayList<Integer>> getTimeWindows() {
		return timeWindows;
	}

	public void setTimeWindows(ArrayList<ArrayList<Integer>> timeWindows) {
		this.timeWindows = timeWindows;
	}


}
