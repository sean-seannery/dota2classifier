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

/**
 * @author sam
 * This class takes .log files and turns them into .log_parsed files
 * It filters out lines in the log files based on what words appear in them
 */
public class ReplayParser {

	private HashSet<String> bad_dictionary;


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
			ReplayParser parser = new ReplayParser();
			for (String filename : args){
				System.out.println("Processing: " + filename);
				parser.parse(filename, filename + "_parsed",filename + "_timed",0);
			}
		}
		//process .log files in a full directory. requires user to include '/' to work
		if (args.length == 1 && args[0].endsWith("/")){
			//get files in directory
			File folder = new File(args[0]);
			File[] listOfFiles = folder.listFiles();
			ReplayParser parser = new ReplayParser();
			//loop through files and process
			for (File filename : listOfFiles){
				if (filename.getName().endsWith(".log"))
				{
					System.out.println("Processing: " + filename.getName());
					parser.parse(filename.getAbsolutePath(), filename.getAbsolutePath() + "_parsed",filename.getAbsolutePath() + "_timed",0);

				}

			}
		}
		System.out.println("Done...");

	}

	public ReplayParser() {
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


	public void parse(String inputFile, String outputFile,String outputTimesFile,long timeInterval){
		File inFile = new File(inputFile);
		File outFile = new File(outputFile);
		File outTimesFile = new File(outputTimesFile);
		BufferedWriter writer = null;
		BufferedWriter timesWriter = null;
		BufferedReader reader = null;
		String line = null;
		int lastCount = 0;
		String lastTime = null;
		Calendar firstTimeCalender = GregorianCalendar.getInstance();
		Calendar lastTimeCalender = GregorianCalendar.getInstance();
		Calendar newTimeCalender = GregorianCalendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("mm:ss");
		int count = 0;
		int weight = 0;

		try {
			reader = new BufferedReader(new FileReader(inFile));
			writer = new BufferedWriter(new FileWriter(outFile));
			timesWriter = new BufferedWriter(new FileWriter(outTimesFile));
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
					ArrayList<String> wordsAL = new ArrayList<String>(Arrays.asList(line.split(" ")));
					Date date = null;
					try {
						date = dateFormat.parse(newTime);
						newTimeCalender.setTime(date);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (lastTime == null)
					{
						lastTime = newTime;
						firstTimeCalender.setTime(date);
						lastTimeCalender.setTime(date);
					}
					//System.out.println((lastTimeCalender.getTimeInMillis()	- newTimeCalender.getTimeInMillis()));
					if ((newTimeCalender.getTimeInMillis()	- lastTimeCalender.getTimeInMillis()) <= timeInterval)
					{
						if (line.contains("damage")){
							weight = Integer.parseInt(
									wordsAL.get(wordsAL.indexOf("deals") + 1) );
						}
						if (line.contains("heals")){
							weight = Integer.parseInt(
									wordsAL.get(wordsAL.indexOf("HP") - 1) );
						}
						lastCount++;
					}else 
					{
						timesWriter.write((lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000 + " " + weight + "\n");
						
						for(int i = (int) (lastTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000+1;i<(int) (newTimeCalender.getTimeInMillis()-firstTimeCalender.getTimeInMillis())/1000; i++)
						{
							timesWriter.write(i + " " + 0 + "\n");
						}
						
						count++;
						
						lastTimeCalender.setTime(date);
						lastCount = 1;
					}
					writer.write(line + "\n");
				}
			}
			System.out.println(count);
			reader.close();
			writer.close();
			timesWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
