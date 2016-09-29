import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * File writer for CSV data
 * 1 x-axis array and 2 y-axis arrays
 * 
 * @author tedfoodlin
 * Based off of file writer by isaacaddis (Isaac Addis) and lostsamurai (Ronan Konishi)
 */
public class CSVFileWriter {
	
	private static double m_maxIterations;
	private static ArrayList<Double>  m_xAxisArray;
	private static ArrayList<Double>  m_yAxisArray1;
	private static ArrayList<Double>  m_yAxisArray2;
	private static double m_finalValue;
	private static double m_dist;
	
	public CSVFileWriter(double maxIterations, ArrayList<Double> xAxisArray, ArrayList<Double> yAxisArray1, ArrayList<Double> yAxisArray2, double finalValue, double dist) {
		m_maxIterations = maxIterations;
		m_xAxisArray = xAxisArray;
		m_yAxisArray1 = yAxisArray1;
		m_yAxisArray2 = yAxisArray2;
		m_finalValue = finalValue;
		m_dist = dist;
	}
	
	public void writeToFile(){
		try {
			FileWriter writer = new FileWriter("CSV data file.txt");
			
			//Headings
			writer.append("Time");
			writer.append("\t");
			writer.append("Velocity");
			writer.append("\t");
			writer.append("Distance");
			writer.append("\r");
			
			//Start for loop
			for(int j = 0; j <= m_maxIterations; j++){
				//Rows
				writer.append(String.valueOf(m_xAxisArray.get(j)));
				writer.append("\t");
				writer.append(" "+ String.valueOf(m_yAxisArray1.get(j)));
				writer.append("\t");
				writer.append(" "+ String.valueOf(m_yAxisArray2.get(j)));
				writer.append("\r");
			}
			
			writer.append(String.valueOf(m_finalValue));
			writer.append("\t");
			writer.append(" "+ 0);
			writer.append("\t");
			writer.append(" "+ m_dist);
			writer.append("\r");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
