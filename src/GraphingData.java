import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Trapezoidal motion profile & S-curve motion profile
 * 
 * @author tedfoodlin & vincentviloria
 *
 */

public class GraphingData {

	public static void main(String[] args) {
		double time;
		
		//given desired distance, max acceleration, max velocity, and clock speed
		Scanner scan = new Scanner(System.in);
		System.out.println("Input desired distance");
		double dist = scan.nextFloat();
		System.out.println("Input max acceleration");
		double amax = scan.nextFloat();
		System.out.println("Input max velocity");
		double vmax = scan.nextFloat();
		System.out.println("Input clock speed");
		double clk = scan.nextFloat();
		
		//initializing desired data for or s-curve motion profiles
		double x;    		// x = distance in y-axis
		double v = 0;	 		// v = velocity in y-axis 
		
		ArrayList<Double> timedata = new ArrayList<Double>();
		ArrayList<Double> velocitydata = new ArrayList<Double>();
		ArrayList<Double> distancedata = new ArrayList<Double>();
		
		//calculate data
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
		
		
		int catcher = 0;
		int i;
		//while (v*1000000 >= 0)
		for (i = 0; v*10000000 >= 0; i++){
			time = time+clk;
			x = (double)(dist - 0.5 * amax * Math.pow((time-((vmax/amax)+(dist/vmax))), 2));
			v = amax * ((vmax/amax)+(dist/vmax)-time);
			timedata.add(time);
			velocitydata.add(v);
			distancedata.add(x);
			System.out.println(v);
			catcher ++;
		}
		System.out.println(catcher); 
		
		//Code below based off of Isaac and Ronan's File writer for TBH
		//Initializes writing to files
		try {
			FileWriter writer = new FileWriter("CSV data file.txt");
			
			//Headings
			writer.append("Time");
			writer.append("\t");
			writer.append("Velocity");
			writer.append("\t");
			writer.append("Distance");
			writer.append("\r");
			
			int a = 0;
			
			//Start for loop
			for(int j = 0; j <= ((vmax/amax)+(dist/vmax))/clk + catcher; j++){
				
				//Rows
				writer.append(String.valueOf(timedata.get(j)));
				writer.append("\t");
				writer.append(" "+ String.valueOf(velocitydata.get(j)));
				writer.append("\t");
				writer.append(" "+ String.valueOf(distancedata.get(j)));
				writer.append("\r");
				a++;
				
				
			}
			
			writer.append(String.valueOf((double)Math.round((timedata.get(a-1)+clk)*1000) / 1000));
			writer.append("\t");
			writer.append(" "+ 0);
			writer.append("\t");
			writer.append(" "+ dist);
			writer.append("\r");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		scan.close();
	}

}
