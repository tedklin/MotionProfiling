import java.util.Scanner;
import java.util.ArrayList;

/**
 * Trapezoidal motion profile & S-curve motion profile
 * 
 * @author tedfoodlin
 *
 */

public class GraphingData {
	
	/**
	 * Input data
	 */
	private static Scanner scan = new Scanner(System.in);
	private static double dist;
	private static double amax;
	private static double vmax;
	private static double clk;
	
	/**
	 * Desired data for motion profiles
	 * 
	 * time = x-axis
	 * x = distance in y-axis
	 * v = velocity in y-axis
	 */
	private static double time;
	private static double x;
	private static double v = 0;
	
	/**
	 * Data arrays
	 */
	private static ArrayList<Double> timedata = new ArrayList<Double>();
	private static ArrayList<Double> velocitydata = new ArrayList<Double>();
	private static ArrayList<Double> distancedata = new ArrayList<Double>();
	
	/**
	 * Extra variables to make file writing easier
	 */
	private static int catcher;
	private static int maxIterations;
	private static double finalValue;

	public static void main(String[] args) {
		input();
		calculateData();
		
		maxIterations = (int)(((vmax/amax)+(dist/vmax))/clk + catcher);
		finalValue = (double)Math.round((timedata.get(maxIterations-1)+clk)*1000) / 1000;
		CSVFileWriter csvFileWriter = new CSVFileWriter(maxIterations, timedata, velocitydata, distancedata, finalValue, dist);
		csvFileWriter.writeToFile();
	}
	
	/**
	 * Input desired distance, max acceleration, max velocity, clock speed
	 */
	public static void input() {
		System.out.println("Input desired distance");
		dist = scan.nextFloat();
		System.out.println("Input max acceleration");
		amax = scan.nextFloat();
		System.out.println("Input max velocity");
		vmax = scan.nextFloat();
		System.out.println("Input clock speed");
		clk = scan.nextFloat();
		scan.close();
	}
	
	/**
	 * 3 stages - accelerate, travel at constant velocity, decelerate (that's a funny word)
	 * Loops through time and adds data for each time interval to arrays
	 */
	public static void calculateData() {
		for (time = 0; time < vmax/amax; time = time + clk){
			time = (double)Math.round(time * 100000) / 100000;
			x = (0.5 * amax * Math.pow(time, (double)2));
			x = (double)Math.round(x * 100000) / 100000;
			v = amax * time;
			v = (double)Math.round(v * 100000) / 100000;
			timedata.add(time);
			velocitydata.add(v);
			distancedata.add(x);
		}
		for (time = vmax/amax; time < dist/vmax; time = time + clk){
			time = (double)Math.round(time * 1000) / 1000;
			x = (0.5 * (Math.pow(vmax, 2) / amax)) + (vmax * (time - (vmax/amax)));
			x = (double)Math.round(x * 100000) / 100000;
			v = (vmax);
			v = (double)Math.round(v * 100000) / 100000;
			timedata.add(time);
			velocitydata.add(v);
			distancedata.add(x);
		}
		for (time = dist/vmax; time <= (vmax/amax)+(dist/vmax); time = time + clk){
			time = (double)Math.round(time * 1000) / 1000;
			x = (double)(dist - 0.5 * amax * Math.pow((time-((vmax/amax)+(dist/vmax))), 2));
			x = (double)Math.round(x * 100000) / 100000;
			v = amax * ((vmax/amax)+(dist/vmax)-time);
			v = (double)Math.round(v * 100000) / 100000;
			timedata.add(time);
			velocitydata.add(v);
			distancedata.add(x);
			if (v < 0)
				break;
		}
		
		/**
		 * Extra stage to increase accuracy at the end of the third stage
		 */
		catcher = 0;
		//while (v*1000000 >= 0)
		for (int i = 0; v*10000000 >= 0; i++){
			time = time+clk;
			x = (double)(dist - 0.5 * amax * Math.pow((time-((vmax/amax)+(dist/vmax))), 2));
			v = amax * ((vmax/amax)+(dist/vmax)-time);
			timedata.add(time);
			velocitydata.add(v);
			distancedata.add(x);
			System.out.println(v);
			catcher ++;
		}
	}
}
