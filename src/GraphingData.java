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
	public static Scanner scan = new Scanner(System.in);
	public static double distance;
	public static double a_max;
	public static double a_avg;
	public static double vmax;
	public static double clk;
	public static double jerk;
	public static int mode;
	
	/**
	 * Data arrays
	 */
	public static ArrayList<Double> time_data = new ArrayList<Double>();
	public static ArrayList<Double> velocity_data = new ArrayList<Double>();
	public static ArrayList<Double> distance_data = new ArrayList<Double>();
	public static ArrayList<Double> acceleration_data = new ArrayList<Double>();

	public static void main(String[] args) {
//		input();
		setTestingValues();
		
		String name = null;
		double final_time = 0;
		if (mode == 1) {
			final_time = scurveCalculations();
			name = "SCurveProfile.csv";
		} 
		else if (mode == 2) {
			final_time = trapezoidalCalculations();
			name = "TrapezoidalProfile.csv";
		} 
		System.out.println("Calculations finished");
		
		double finalValue = (double)Math.round(final_time * 1000) / 1000;
		CSVFileWriter csvFileWriter = new CSVFileWriter(name, time_data, velocity_data, distance_data, acceleration_data, finalValue, distance);
		csvFileWriter.writeToFile();
		System.out.println("Profile generated");
	}
	
	/**
	 * User input data
	 */
	public static void input() {
		System.out.println("Input desired distance");
		distance = scan.nextFloat();
		System.out.println("Input max acceleration");
		a_max = scan.nextFloat();
		System.out.println("Input mode (1 for pure s-curve, 2 for trapezoidal)");
		mode = scan.nextInt();
		if (mode == 1) {
			a_avg = 0.5 * a_max;
			jerk = (Math.pow(a_max, 2) * a_avg) / (vmax * (a_max - a_avg));
		} else if (mode == 2) {
			a_avg = a_max;
			jerk = 0;
		}
		System.out.println("Input max velocity");
		vmax = scan.nextFloat();
		System.out.println("Input clock speed");
		clk = scan.nextFloat();
		scan.close();
	}
	
	/**
	 * Set default testing parameters
	 */
	public static void setTestingValues() {
		scan.close();
		distance = 200;
		a_max = 5;
		mode = 1;
		a_avg = 2.5;
		vmax = 10;
		clk = 0.1;
		jerk = (Math.pow(a_max, 2) * a_avg) / (vmax * (a_max - a_avg));
		jerk = Math.pow(a_max, 2) / vmax;
	}

	/**
	 * 3 stages - accelerate, cruising, decelerate
	 * Loop through time and add data for each time interval to arrays
	 * Don't account for jerk
	 * 
	 * @return final time
	 */
	public static double trapezoidalCalculations() {
		double time;
		double x;
		double v = 0;
		
		boolean isTriangular = isTriangular();
		for (time = 0; time < (vmax/a_max); time += clk){
			time = roundTime(time);
			x = (0.5 * a_max * Math.pow(time, (double)2));
			x = round(x);
			v = a_max * time;
			v = round(v);
			addData(time, v, x, a_max);
		}
		double accelDist = distance_data.get(distance_data.size()-1);
		System.out.println("Distance to accelerate/decelerate: " + accelDist);
		if (isTriangular == false){
			for (time = vmax/a_max; time < (distance/vmax); time += clk){
				time = roundTime(time);
				x = (0.5 * (Math.pow(vmax, 2) / a_max)) + (vmax * (time - (vmax/a_max)));
				x = round(x);
				v = (vmax);
				v = round(v);
				addData(time, v, x, 0);
			}
		}
		double cruisingDist = distance_data.get(distance_data.size()-1) - accelDist;
		System.out.println("Cruising distance: " + cruisingDist);
		double end_of_second_stage = (vmax/a_max) + (distance/vmax);
		for (time = distance/vmax; time <= end_of_second_stage; time += clk){
			time = roundTime(time);
			x = (double)(distance - 0.5 * a_max * Math.pow((time-((vmax/a_max)+(distance/vmax))), 2));
			x = round(x);
			v = a_max * ((vmax/a_max)+(distance/vmax)-time);
			v = round(v);
			addData(time, v, x, a_max);
			if (v < 0)
				break;
		}
		System.out.println("Time to finish motion: " + time);
		return time;
	}
	
	/**
	 * Create S-curve for acceleration and deceleration (stages 1 and 3)
	 * Cruising is the same
	 * Loop through time and add data for each time interval to arrays
	 * Reduces jerk, which means that this takes longer than a trapezoidal profile
	 * No check for triangulation, default s-curve isn't a triangle
	 * 
	 * @return final time
	 */
	public static double scurveCalculations() {
		double time;
		double acceleration = 0;
		double x = 0;
		double v = 0;
				
		double tj;
		double ta;
		double tv;
		if (mode == 1) {
			tj = Math.pow((vmax/jerk), 0.5);
			ta = tj;
			tv = distance/vmax;
		} else {
			tj = a_max/jerk;
			ta = vmax/a_max;
			tv = distance/vmax;
		}
		
		double t1 = tj;
		System.out.println("t1: " + t1);
		double t2 = ta;
		System.out.println("t2: " + t2);
		double t3 = ta + tj;
		System.out.println("t3: " + t3);
		double t4 = tv;
		System.out.println("t4: " + t4);
		double t5 = tv + tj;
		System.out.println("t5: " + t5);
		double t6 = tv + ta;
		System.out.println("t6: " + t6);
		double t7 = tv + ta + tj;
		System.out.println("t7: " + t7);

		
		/**
		 * S-curve acceleration
		 */
		for (time = 0; time <= t1; time += clk) {
			time = roundTime(time);
			acceleration = jerk * time;
			acceleration = round(acceleration);
			v = (jerk * Math.pow(time, 2))/2;
			v = round(v);
			x = (jerk * Math.pow(time, 3))/6;
			x = round(x);
			addData(time, v, x, acceleration);
		}
		double v1 = v;
		double p1 = x;
		double a1 = acceleration;
		for (time = t1 + clk; time <= t2; time += clk) {
			time = roundTime(time);
			acceleration = a_max;
			acceleration = round(acceleration);
			v = ((Math.pow(a_max, 2) / (2 * jerk))) + a_max * (time - t1);
			v = v1 + (a1 * (time - t1));
			v = round(v);
			x = p1 + (v1 * (time - t1)) + (0.5 * a1 * Math.pow((time - t1), 2));
			x = round(x);
			addData(time, v, x, acceleration);
		}
		double v2 = v;
		double p2 = x;
		double a2 = acceleration;
		for (time = t2 + clk; time <= t3; time += clk) {
			time = roundTime(time);
			acceleration = a_max - (jerk * (time - t2));
			acceleration = round(acceleration);
			v = vmax - ((jerk * Math.pow((t3 - time), 2)) / 2);
			v = v2 + (a2 * (time - t2)) + (0.5 * -jerk * Math.pow((time - t2), 2));
			v = round(v);
			x = p2 + (v2 * (time - t2)) + (0.5 * a2 * Math.pow((time - t2), 2)) + (1/6 * -jerk * Math.pow((time - t2), 3));
			x = round(x);
			addData(time, v, x, acceleration);
		}
		double v3 = v;
		double p3 = x; 
		double distance_to_accelerate = (Math.pow(vmax, 2)) / (2 * a_avg);
		System.out.println("Distance to accelerate " + distance_to_accelerate);
		
		/**
		 * Cruising stage
		 */
		double cruising_distance = distance - (2 * distance_to_accelerate);
		System.out.println("Cruising distance: " + cruising_distance);
		double distance_after_second_stage = round(distance_to_accelerate + cruising_distance);
		System.out.println("Distance after second stage: " + distance_after_second_stage);
		
		for (time = t3 + clk; time < t4; time += clk){
			time = roundTime(time);
			acceleration = 0;
			x = p3 + v3 * (time - t3);
			x = round(x);
			v = (vmax);
			v = round(v);
			addData(time, v, x, acceleration);
		}
		double v4 = v;
		double p4 = x;
		
		/**
		 * S-curve deceleration
		 */
		double velocity_time_reference;
		for (time = t4; time <= t5; time += clk) {
			time = roundTime(time);
			velocity_time_reference = roundTime(t7 - time);
			acceleration = a_max - (jerk * (velocity_time_reference - t2));
			acceleration = -round(acceleration);
			v = v4 + (0.5 * -jerk * Math.pow((time - t4), 2));
			v = round(v);
			x = p4 + (v4 * (time - t4)) + (1/6 * -jerk * Math.pow((time - t4), 3));
			x = round(x);
			addData(time, v, x, acceleration);
		}
		double v5 = v;
		double p5 = x;
		double a5 = acceleration;
		for (time = t5 + clk; time <= t6; time += clk) {
			time = roundTime(time);
			velocity_time_reference = roundTime(t7 - time);
			acceleration = a_max;
			acceleration = -round(acceleration);
			v = v5 - (a_max * (time - t5));
			v = round(v);
			x = p5 + (v5 * (time - t5)) + (0.5 * a5 * Math.pow((time - t5), 2));
			x = round(x);
			addData(time, v, x, acceleration);
		}
		double v6 = v;
		double p6 = x;
		double a6 = acceleration;
		for (time = t6 + clk; time <= t7; time += clk) {
			time = roundTime(time);
			velocity_time_reference = roundTime(t7 - time);
			acceleration = jerk * velocity_time_reference;
			acceleration = -round(acceleration);
			v = v6 + (a6 * (time - t6)) + (0.5 * jerk * Math.pow((time - t6), 2));
			v = round(v);
			x = p6 + (v6 * (time - t6)) + (0.5 * a6 * Math.pow((time - t6), 2)) + (1/6 * jerk * Math.pow((time - t6), 3));
			x = round(x);
			addData(time, v, x, acceleration);
		}
		
		System.out.println("Time to finish motion: " + t7);
		return t7;
	}
	
	/**
	 * Adds data to array lists
	 * 
	 * @param time
	 * @param velocity
	 * @param distance
	 * @param acceleration
	 */
	public static void addData(double time, double v, double x, double acceleration) {
		time_data.add(time);
		velocity_data.add(v);
		distance_data.add(x);
		acceleration_data.add(acceleration);
	}
	
	/**
	 * Round a value to the hundred thousandths place
	 * 
	 * @param value
	 * @return rounded value
	 */
	public static double round(double value) {
		return (double)Math.round(value * 100000) / 100000;
	}
	
	/**
	 * Round a value to the thousandths place
	 * 
	 * @param value
	 * @return
	 */
	public static double roundTime(double value) {
		return (double)Math.round(value * 1000) / 1000;
	}
	
	/**
	 * Check if the maximum velocity can actually be reached
	 * If the maximum velocity can't be reached, adjust it to the final velocity that it can reach
	 * Maximum velocity can't be reached = triangular motion profile
	 */
	public static boolean isTriangular() {
		double mid = distance/2;
		double vFinal = Math.pow(2 * a_max * mid, 0.5);
		if (vFinal < vmax){
			vmax = vFinal;
			return true;
		} else {
			return false;
		}
	}
}
