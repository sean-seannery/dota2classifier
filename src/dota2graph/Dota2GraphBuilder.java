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
	private ArrayList<ArrayList<Date>> timeWindows;
	
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
			for (ArrayList<Date> dates : builder.getTimeWindows()){
				builder.createGraph(args[0],dates.get(0), dates.get(1));
				System.out.println(builder.getGraph());
				System.out.println(builder.getGraph().calculateMVP());
				System.out.println();
			}
			
			System.out.println(builder.getKDAMVP(args[0].replace(".log_parsed", ".txt")));
			
		}
		//do all files in a directory
		if (args.length == 2 && args[0].endsWith("/")){
			File folder = new File(args[0]);
			File[] listOfFiles = folder.listFiles();

		    for (File f : listOfFiles) {
		    	if (f.getName().endsWith(".log_parsed")) {
		    		Dota2GraphBuilder builder = new Dota2GraphBuilder();
					builder.readInTimeWindows(args[1]);
					for (ArrayList<Date> dates : builder.getTimeWindows()){
						
						builder.createGraph(f.getAbsolutePath(),dates.get(0), dates.get(1));
						System.out.println(builder.getGraph().calculateMVP());		
					}
					
					System.out.println("KDA:"+builder.getKDAMVP(f.getAbsolutePath().replace(".log_parsed", ".txt")));
					System.out.println();
		    	}
		    }

		}

	}
	
	public void createGraph(String fileName, Date startTime, Date stopTime) {
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
	
	
	private void parseDota2Event(String line, Date startTime, Date stopTime) {

		line = line.replace(".", "");
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
		Date time = null;
		try {
			time = new SimpleDateFormat("HH:mm").parse(words.get(0));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String p1Name = "";
		String p2Name = "";
		int weight = 0;
		if (time.compareTo(startTime)>= 0 && time.compareTo(stopTime)<= 0)
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
				weight = 900;
			}
			Dota2GraphNode p1Node = new Dota2GraphNode(p1Name);
			Dota2GraphNode p2Node = new Dota2GraphNode(p2Name);
			Dota2GraphEdge edge = new Dota2GraphEdge(weight, p2Node, time);
			graph.addNode(p1Node);
			graph.addNode(p2Node);
			graph.addEdge(p1Node, edge);
		}
	}
	
	public ArrayList<ArrayList<Date>> readInTimeWindows(String fileName) {
		ArrayList<ArrayList<Date>> retVal = new ArrayList<ArrayList<Date>>();
		File file = new File (fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				Date time1 = null;
				Date time2 = null;
				try {
					time1 = new SimpleDateFormat("HH:mm").parse(line.split(" ")[0]);
					time2 = new SimpleDateFormat("HH:mm").parse(line.split(" ")[1]);
					ArrayList<Date> temp = new ArrayList<Date>();
					temp.add(time1);
					temp.add(time2);
					retVal.add(temp);
				} catch (ParseException e) {
					e.printStackTrace();
				}
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
					while (! (line = reader.readLine()).trim().equals("")) {
						//System.out.println(line);

						int startIndex = line.indexOf("KDA: ") + 5;
						int endIndex = line.indexOf(" CS: ") - 1;
						String[] KDAString = line.substring(startIndex, endIndex).split("/");
						double denominator = Double.parseDouble(KDAString[1]);
						if (denominator == 0){
							denominator = .01;
						}
						double kda = Double.parseDouble(KDAString[0]) / denominator;
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
		return bestHero + " " + bestKDA;
		
	}

	public Dota2Graph getGraph() {
		return graph;
	}

	public void setGraph(Dota2Graph graph) {
		this.graph = graph;
	}

	public ArrayList<ArrayList<Date>> getTimeWindows() {
		return timeWindows;
	}

	public void setTimeWindows(ArrayList<ArrayList<Date>> timeWindows) {
		this.timeWindows = timeWindows;
	}


}
