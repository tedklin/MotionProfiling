import java.util.ArrayList;

/**
 * Trapezoidal motion profile & S-curve motion profile 
 * 
 * @author tedfoodlin
 *
 */

public class MPGenerator {
	
	// ClassicTrapezoidal and SCurve generate CSV files
	// TalonTrapezoidal generates 2D array for direct use with TalonControlMode MotionProfile
	public enum Mode {
		ClassicTrapezoidal, TalonTrapezoidal, SCurve
	}
	public static Mode mode = Mode.ClassicTrapezoidal;
	
	// Parameters: distance (rotations), acceleration (rot/min^2), maxVelocity (rpm), clk (ms)
	public static double distance = 30.93;
	public static double maxAccel = 51.56*3600;
	public static double maxDecel = -maxAccel;
	public static double a_avg = maxAccel;
	public static double maxVelocity = 1241;
	public static double clk = 10;
	public static double clkInMinutes = clk/60000;
	public static double jerk = 0;
	
	public static ArrayList<Double> time_data = new ArrayList<Double>();
	public static ArrayList<Double> velocity_data = new ArrayList<Double>();
	public static ArrayList<Double> distance_data = new ArrayList<Double>();
	public static ArrayList<Double> acceleration_data = new ArrayList<Double>();

	public static void main(String[] args) {
		System.out.println("Desired Distance: " + distance);
		System.out.println("Max Acceleration: " + maxAccel);
		System.out.println("Max Deceleration: " + maxDecel);
		System.out.println("Max Velocity: " + maxVelocity);
		System.out.println("Clock speed: " + clk);
		
		double finalValue = 0;
		if (mode == Mode.SCurve) {
			System.out.println("S-Curve Motion Profile");
			finalValue = (double)Math.round(scurveCalculations() * 1000) / 1000;
			CSVFileWriter csvFileWriter = new CSVFileWriter("SCurveProfile.csv", 
					time_data, velocity_data, distance_data, acceleration_data, finalValue, distance);
			csvFileWriter.writeToFile();
		} else if (mode == Mode.ClassicTrapezoidal) {
			System.out.println("Trapezoidal Motion Profile");
			finalValue = (double)Math.round(trapezoidalCalculations() * 1000) / 1000;
			CSVFileWriter csvFileWriter = new CSVFileWriter("TrapezoidalProfile.csv", 
					time_data, velocity_data, distance_data, acceleration_data, finalValue, distance);
			csvFileWriter.writeToFile();
		} else if (mode == Mode.TalonTrapezoidal) {
			System.out.println("Talon Trapezoidal Motion Profile");
			finalValue = (double)Math.round(trapezoidalCalculations() * 1000) / 1000;
			TalonMPLogger talonLogger = new TalonMPLogger(distance_data, velocity_data, clk, distance);
			talonLogger.logData();
		} else {
			System.out.println("Please select a valid mode");
		}
		System.out.println("Profile Generated");
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
		
		double accelTime = maxVelocity/maxAccel;
		System.out.println("Acceleration Time: " + accelTime);
		double cruiseTime = (distance - (accelTime * maxVelocity)) / maxVelocity;
		System.out.println("Cruise Time: " + cruiseTime);
		double accelAndCruiseTime = accelTime + cruiseTime;
		System.out.println("Acceleration + Cruise Time: " + accelAndCruiseTime);
		double decelTime = -maxVelocity/maxDecel;
		System.out.println("Deceleration Time: " + decelTime);
		double totalTime = accelTime + cruiseTime + decelTime;
		System.out.println("Expected End Time: " + totalTime);
		
		boolean triangular = isTriangular();
		for (time = 0; time < accelTime; time += clkInMinutes){
			x = (0.5 * maxAccel * Math.pow(time, (double)2));
			v = maxAccel * time;
			addData(time, v, x, maxAccel);
		}
		if (triangular == false){
			for (time = accelTime; time < accelAndCruiseTime; time += clkInMinutes){
				x = (0.5 * (Math.pow(maxVelocity, 2) / maxAccel)) + (maxVelocity * (time - (maxVelocity/maxAccel)));
				v = (maxVelocity);
				addData(time, v, x, 0);
			}
		}
		for (time = accelAndCruiseTime; time <= totalTime; time += clkInMinutes){
			x = (double)(distance + 0.5 * maxDecel * Math.pow((time-totalTime), 2));
			v = -maxAccel * time + (maxVelocity + maxAccel * accelAndCruiseTime);
			addData(time, v, x, maxDecel);
		}
		System.out.println("Actual time to finish motion: " + time);
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
		if (mode == Mode.SCurve) {
			tj = Math.pow((maxVelocity/jerk), 0.5);
			ta = tj;
			tv = distance/maxVelocity;
		} else {
			tj = maxAccel/jerk;
			ta = maxVelocity/maxAccel;
			tv = distance/maxVelocity;
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
			time = roundThousandths(time);
			acceleration = jerk * time;
			acceleration = roundHundredThousandths(acceleration);
			v = (jerk * Math.pow(time, 2))/2;
			v = roundHundredThousandths(v);
			x = (jerk * Math.pow(time, 3))/6;
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		double v1 = v;
		double p1 = x;
		double a1 = acceleration;
		for (time = t1 + clk; time <= t2; time += clk) {
			time = roundThousandths(time);
			acceleration = maxAccel;
			acceleration = roundHundredThousandths(acceleration);
			v = ((Math.pow(maxAccel, 2) / (2 * jerk))) + maxAccel * (time - t1);
			v = v1 + (a1 * (time - t1));
			v = roundHundredThousandths(v);
			x = p1 + (v1 * (time - t1)) + (0.5 * a1 * Math.pow((time - t1), 2));
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		double v2 = v;
		double p2 = x;
		double a2 = acceleration;
		for (time = t2 + clk; time <= t3; time += clk) {
			time = roundThousandths(time);
			acceleration = maxAccel - (jerk * (time - t2));
			acceleration = roundHundredThousandths(acceleration);
			v = maxVelocity - ((jerk * Math.pow((t3 - time), 2)) / 2);
			v = v2 + (a2 * (time - t2)) + (0.5 * -jerk * Math.pow((time - t2), 2));
			v = roundHundredThousandths(v);
			x = p2 + (v2 * (time - t2)) + (0.5 * a2 * Math.pow((time - t2), 2)) + (1/6 * -jerk * Math.pow((time - t2), 3));
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		double v3 = v;
		double p3 = x; 
		double distance_to_accelerate = (Math.pow(maxVelocity, 2)) / (2 * a_avg);
		System.out.println("Distance to accelerate " + distance_to_accelerate);
		
		/**
		 * Cruising stage
		 */
		double cruising_distance = distance - (2 * distance_to_accelerate);
		System.out.println("Cruising distance: " + cruising_distance);
		double distance_after_second_stage = roundHundredThousandths(distance_to_accelerate + cruising_distance);
		System.out.println("Distance after second stage: " + distance_after_second_stage);
		
		for (time = t3 + clk; time < t4; time += clk){
			time = roundThousandths(time);
			acceleration = 0;
			x = p3 + v3 * (time - t3);
			x = roundHundredThousandths(x);
			v = (maxVelocity);
			v = roundHundredThousandths(v);
			addData(time, v, x, acceleration);
		}
		double v4 = v;
		double p4 = x;
		
		/**
		 * S-curve deceleration
		 */
		double velocity_time_reference;
		for (time = t4; time <= t5; time += clk) {
			time = roundThousandths(time);
			velocity_time_reference = roundThousandths(t7 - time);
			acceleration = maxAccel - (jerk * (velocity_time_reference - t2));
			acceleration = -roundHundredThousandths(acceleration);
			v = v4 + (0.5 * -jerk * Math.pow((time - t4), 2));
			v = roundHundredThousandths(v);
			x = p4 + (v4 * (time - t4)) + (1/6 * -jerk * Math.pow((time - t4), 3));
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		double v5 = v;
		double p5 = x;
		double a5 = acceleration;
		for (time = t5 + clk; time <= t6; time += clk) {
			time = roundThousandths(time);
			velocity_time_reference = roundThousandths(t7 - time);
			acceleration = maxAccel;
			acceleration = -roundHundredThousandths(acceleration);
			v = v5 - (maxAccel * (time - t5));
			v = roundHundredThousandths(v);
			x = p5 + (v5 * (time - t5)) + (0.5 * a5 * Math.pow((time - t5), 2));
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		double v6 = v;
		double p6 = x;
		double a6 = acceleration;
		for (time = t6 + clk; time <= t7; time += clk) {
			time = roundThousandths(time);
			velocity_time_reference = roundThousandths(t7 - time);
			acceleration = jerk * velocity_time_reference;
			acceleration = -roundHundredThousandths(acceleration);
			v = v6 + (a6 * (time - t6)) + (0.5 * jerk * Math.pow((time - t6), 2));
			v = roundHundredThousandths(v);
			x = p6 + (v6 * (time - t6)) + (0.5 * a6 * Math.pow((time - t6), 2)) + (1/6 * jerk * Math.pow((time - t6), 3));
			x = roundHundredThousandths(x);
			addData(time, v, x, acceleration);
		}
		
		System.out.println("Time to finish motion: " + t7);
		return t7;
	}
	
	/**
	 * Add data to array lists
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
	
	public static double roundALot(double value) {
		return (double)Math.round(value * 1000000000) / 1000000000;
	}
	
	public static double roundHundredThousandths(double value) {
		return (double)Math.round(value * 100000) / 100000;
	}
	
	public static double roundThousandths(double value) {
		return (double)Math.round(value * 1000) / 1000;
	}
	
	/**
	 * Check if the maximum velocity can actually be reached
	 * If the maximum velocity can't be reached, adjust it to the final velocity that it can reach with some tolerance
	 * Maximum velocity can't be reached = triangular motion profile
	 */
	public static boolean isTriangular() {
		double mid = distance/2;
		double vFinal = Math.pow(2 * maxAccel * mid, 0.5);
		if (vFinal < maxVelocity){
			maxVelocity = vFinal - (maxVelocity/10);
			return true;
		} else {
			return false;
		}
	}
}
