import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * File writer for CSV data 1 x-axis array and 2 y-axis arrays
 * 
 * @author Isaac Addis
 * @author Ronan Konishi
 * 
 *         Modified by tedlin
 * 
 */

public class CSVFileWriter {

    private String m_name;
    private static ArrayList<Double> m_xAxisArray;
    private static ArrayList<Double> m_yAxisArray1;
    private static ArrayList<Double> m_yAxisArray2;
    private static ArrayList<Double> m_yAxisArray3;
    private static double m_finalValue;
    private static double m_dist;

    /**
     * File writer constructor
     * 
     * @param maxIterations
     * @param xAxisArray
     * @param yAxisArray1
     * @param yAxisArray2
     * @param finalValue
     * @param dist
     * 
     *            For use with motion profiling: xAxisArray = time yAxisArray1 =
     *            velocity yAxisArray2 = distance finalValue and dist for tolerance
     *            at the end
     * 
     */
    public CSVFileWriter(String name, ArrayList<Double> xAxisArray, ArrayList<Double> yAxisArray1,
	    ArrayList<Double> yAxisArray2, ArrayList<Double> yAxisArray3, double finalValue, double dist) {
	m_name = name;
	m_xAxisArray = xAxisArray;
	m_yAxisArray1 = yAxisArray1;
	m_yAxisArray2 = yAxisArray2;
	m_yAxisArray3 = yAxisArray3;
	m_finalValue = finalValue;
	m_dist = dist;
    }

    public void writeToFile() {
	int maxIterations = m_xAxisArray.size();
	try {
	    FileWriter writer = new FileWriter("generated_profiles/" + m_name);

	    // Headings
	    writer.append("Time");
	    writer.append(",");
	    writer.append("Velocity");
	    writer.append(",");
	    writer.append("Distance");
	    writer.append(",");
	    writer.append("Acceleration");
	    writer.append("\r");

	    // Start for loop
	    for (int j = 0; j <= maxIterations - 1; j++) {
		// Rows
		writer.append(String.valueOf(m_xAxisArray.get(j)));
		writer.append(",");
		writer.append(String.valueOf(m_yAxisArray1.get(j)));
		writer.append(",");
		writer.append(String.valueOf(m_yAxisArray2.get(j)));
		writer.append(",");
		writer.append(String.valueOf(m_yAxisArray3.get(j)));
		writer.append("\r");
	    }

	    if (m_yAxisArray2.get(maxIterations - 1) != m_dist) {
		writer.append(String.valueOf(m_finalValue));
		writer.append(",");
		writer.append("" + 0);
		writer.append(",");
		writer.append("" + m_dist);
		writer.append(",");
		writer.append("" + 0);
		writer.append("\r");
	    }

	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
