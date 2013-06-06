package dota2changepoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ChangePointParser {

	public static int SEARCH_WINDOW = 20;
	public static int MAX_TRIES = 10;
	public static int EVENT_CUTOFF = 1;
	private ArrayList<Double> timeSeries;

	public ChangePointParser(String filename){
		timeSeries = new ArrayList<Double>();
		loadTimeSeries(filename);
	}


	public void loadTimeSeries(String filename){
		File file = new File(filename);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));

			String line = "";
			while ((line = reader.readLine()) != null) {
				if( ! line.trim().equals("")) {
					timeSeries.add(Integer.parseInt(line.split(" ")[0]), Double.parseDouble(line.split(" ")[1]));
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public ArrayList<Integer> getChangepointPeaks()
	{

		ArrayList<Integer> peaks = new ArrayList<Integer>();
		for (int i = 2; i < timeSeries.size() - 2; i++){

			if (timeSeries.get(i-2) < timeSeries.get(i) && timeSeries.get(i-1) < timeSeries.get(i)
					&& timeSeries.get(i) > timeSeries.get(i+1) && timeSeries.get(i+1) >= timeSeries.get(i+2)){

				if (timeSeries.get(i) > Math.exp(.0005*i)+300){
					peaks.add(i);
				}


			}

		}

		return peaks;

	}

	public void getChangepointWindows(ArrayList<Integer> input,String fileName)
	{
		File outFile = new File(fileName);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < input.size(); i++) 
		{
			int current = input.get(i);
			System.out.println("Doing Left: " + current);
			int left = getChangepointForOneSide(input,current,true);
			System.out.println("Doing Right: " + current);
			System.out.println("left: " + left);
			int right = getChangepointForOneSide(input,current,false);
			if (left > right)
				System.err.println("ERRORZ LEFT>RIGHT");
			if (current > right)
				System.err.println("ERRORZ CURRENT>RIGHT");
			//left should not be greater than right! or equal to
			//current should be greater than left
			//current should be lesser than right
			System.out.println("right " + right);
			try {
				writer.write(left + " " + right + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getChangepointForOneSide(ArrayList<Integer> input,int peak,boolean left)
	{
		int result = -1;
		int count = 1;
		do
		{
			//if (count>0)
			//	System.out.println("stuck here" + count);
			int window = (left) ? (peak-(SEARCH_WINDOW*count)) : (peak+(SEARCH_WINDOW*count));
			if (!left && (window < peak))
				System.err.println("ERRORZ RIGHT");
			if (left && (window > peak))
				System.err.println("ERRORZ LEFT");
			result = getChangepointWindowsRec(input,peak,window,0,left);
			count++;

		}while(result == -1);
		return result;

	}


	public int getChangepointWindowsRec(ArrayList<Integer> input,int peak, int window,int tries,boolean left)
	{

		double current = 0;
		try
		{
			current = timeSeries.get(window);
		}catch (IndexOutOfBoundsException e)
		{
			//System.out.println("thrown " + window);
			//tries++;
			//if (tries >= MAX_TRIES)
			//{
			//System.out.println("window increased");
			return (left) ? 0 : timeSeries.size()-1;//Increase window try again
			//}

			//if (left)
			//return getChangepointWindowsRec(input, peak,0,tries,left);
			//else
			//return getChangepointWindowsRec(input, peak,timeSeries.size()-1,tries,left);	
		}
		tries++;
		if (tries >= MAX_TRIES)
		{
			//System.out.println("window increased");
			return -1;//Increase window try again
		}
		if (current < EVENT_CUTOFF)//return window change this later.
			return window;
		//go left or right
		return getChangepointWindowsRec(input, peak, (peak+window)/2,tries,left);
	}

	public static void main(String[] args) 
	{
		ChangePointParser cpp=  new ChangePointParser("201395825.log_timed");
		ArrayList<Integer> peaks = cpp.getChangepointPeaks();
		if (peaks.size() == 0)
			System.err.println("NO PEAKS FOUND");
		cpp.getChangepointWindows(peaks,"201395825.txt_times");
	}

}
