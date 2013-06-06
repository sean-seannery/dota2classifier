/**
 * 
 */
package dota2parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import dota2changepoint.ChangePointParser;

/**
 * @author sam
 * This class takes .log files and turns them into .log_parsed files
 * It filters out lines in the log files based on what words appear in them
 */
public class ReplayParserWeight {

	private HashSet<String> bad_dictionary;
	private static int WINDOW_SIZE = 10;

	/**
	 * @param args takes in either a file name or a directory to process
	 */
	public static void main(String[] args) {
		//argument processing
		if (args.length == 0) {
			System.out.println("You must provide at least one file name or directory");
			System.out.println("Example:");
			System.out.println("java ReplayParser thisfile.log    OR   java ReplayParser /home/test/");

		}
		if (args.length >= 1 && args[0].endsWith(".log")){
			ReplayParserWeight parser = new ReplayParserWeight();
			for (String filename : args){
				System.out.println("Processing: " + filename);
				parser.parse(filename, filename + "_parsed",filename + "_timed",filename+ "_death",0);
				ChangePointParser cpp =  new ChangePointParser(filename + "_timed");
				ArrayList<Integer> peaks = cpp.getChangepointPeaks();
				if (peaks.size() == 0)
					System.err.println("NO PEAKS FOUND");
				cpp.getChangepointWindows(peaks,filename + "_windows");
				
				
			}
		}
		//process .log files in a full directory. requires user to include '/' to work
		if (args.length == 1 && args[0].endsWith("/")){
			//get files in directory
			File folder = new File(args[0]);
			File[] listOfFiles = folder.listFiles();
			ReplayParserWeight parser = new ReplayParserWeight();
			//loop through files and process
			for (File filename : listOfFiles){
				if (filename.getName().endsWith(".log"))
				{
					System.out.println("Processing: " + filename.getName());
					parser.parse(filename.getAbsolutePath(), filename.getAbsolutePath() + "_parsed",filename.getAbsolutePath() + "_timed",filename.getAbsolutePath() + "_death",0);
					ChangePointParser cpp =  new ChangePointParser(filename + "_timed");
					ArrayList<Integer> peaks = cpp.getChangepointPeaks();
					if (peaks.size() == 0)
						System.err.println("NO PEAKS FOUND");
					cpp.getChangepointWindows(peaks,filename + "_windows");
				}

			}
		}
		System.out.println("Done...");

	}

	public ReplayParserWeight() {
		//load hash map dictionaries
		bad_dictionary = new HashSet<String>();		
		File file = new File("badwords.txt");
		BufferedReader reader = null;

		try{
			reader = new BufferedReader(new FileReader(file));
			String word = null;
			while ((word = reader.readLine()) != null) {
				bad_dictionary.add(word);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public void parse(String inputFile, String outputFile,String outputTimesFile,String deathTimesFile,long timeInterval){
		File inFile = new File(inputFile);
		File outFile = new File(outputFile);
		File outTimesFile = new File(outputTimesFile);
		File deathTimesFilePointer = new File(deathTimesFile);
		BufferedWriter writer = null;
		BufferedWriter deathWriter = null;
		BufferedWriter timesWriter = null;
		BufferedReader reader = null;
		String line = null;
		int lastCount = 0;
		String lastTime = null;
		long newTimeMs = 0;
		long lastTimeMs = 0;
		Calendar firstTimeCalender = GregorianCalendar.getInstance();
		Calendar lastTimeCalender = GregorianCalendar.getInstance();
		Calendar newTimeCalender = GregorianCalendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("mm:ss");
		int count = 0;
		ArrayList<Integer> timeSeries = new ArrayList<Integer>();
		Queue<Double> queue = new LinkedList<Double>();

		try {
			reader = new BufferedReader(new FileReader(inFile));
			writer = new BufferedWriter(new FileWriter(outFile));
			timesWriter = new BufferedWriter(new FileWriter(outTimesFile));
			deathWriter = new BufferedWriter(new FileWriter(deathTimesFilePointer));
			while ((line = reader.readLine()) != null) {
				boolean ignore_line = false;
				line = line.replace(".", "");
				String[] words = line.split(" ");
				//check inputfile line for bad words, if it has, dont write out line
				for (String word : words){
					if (bad_dictionary.contains(word.toLowerCase())) {
						ignore_line = true;
						break;
					}
				}
				if (!ignore_line) {
					//write to output file if line doesnt contain bad words
					if (!outFile.exists()) {
						outFile.createNewFile();
					}

					String newTime = words[0];
					double weight = 0;
					
					
					ArrayList<String> wordsAL = new ArrayList<String>(Arrays.asList(line.split(" ")));
					Date date = null;
					
					
					try {
						date = dateFormat.parse(newTime);
						newTimeCalender.setTime(date);
						newTimeMs = (newTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000; 
						if (line.contains("dies"))
						{
							deathWriter.write(newTimeMs + " " + 1+ "\n");
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (lastTime == null)
					{
						lastTime = newTime;
						firstTimeCalender.setTime(date);
						lastTimeCalender.setTime(date);
						lastTimeMs = (lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000; 

					}
					//System.out.println((lastTimeCalender.getTimeInMillis()	- newTimeCalender.getTimeInMillis()));
					if (newTimeMs - lastTimeMs <= timeInterval)
					{
						
						if (line.contains("damage")){
							lastCount += Integer.parseInt(
									wordsAL.get(wordsAL.indexOf("deals") + 1) );
						}
						if (line.contains("heals")){
							lastCount += Integer.parseInt(
									wordsAL.get(wordsAL.indexOf("HP") - 1) );
						}
						/*
						for (int i = (int) lastTimeMs; i < lastTimeMs+timeInterval; i++) {
							try
							{
								int  current = timeSeries.get(i);
								current++;
								timeSeries.set(i, current);
							} catch(IndexOutOfBoundsException e)
							{
								timeSeries.add(1);
							}
						}*/
						
						//lastCount++;
					}
					else 
					{
						
						weight = calculateWeightInWindow(queue,lastCount);
						timesWriter.write((lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000 + " " + weight + "\n");
						
						for(int i = (int) (lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000+1;i<(int) (newTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000; i++)
						{
							weight = calculateWeightInWindow(queue,0);
							timesWriter.write(i + " " + weight + "\n");
						}
						count++;
						lastTimeCalender.setTime(date);
						lastTimeMs = (lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000; 
						lastCount = 0;
					}
					
					writer.write(line + "\n");
				}
			}
			System.out.println(count);
			
			
			reader.close();
			writer.close();
			deathWriter.close();
			timesWriter.close();
			Process proc =Runtime.getRuntime().exec("Rscript pelt.R "+ outTimesFile + " " +deathTimesFilePointer); 
			InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
             line = null;
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            int exitVal = proc.waitFor();
            System.out.println("Process exitValue: " + exitVal);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double calculateWeightInWindow(Queue queue, int count)
	{
		double weight = 0;
		
		ArrayList<Double> weightsInWindow = new ArrayList<Double>(queue);
		for (int i = 0; i < weightsInWindow.size(); i++) 
		{
			weight += ((double)(i+1)/WINDOW_SIZE)*weightsInWindow.get(i);
		}
		weight += count;
		queue.offer((double) count);
		if (queue.size()>WINDOW_SIZE)
		{
			queue.poll();
		}
		return weight;
		
	}


}
