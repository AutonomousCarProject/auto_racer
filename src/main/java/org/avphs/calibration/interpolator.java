package org.avphs.calibration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class Interpolator {
	//number of terms in the desired polynomial
	int vars = 3;
	//degree of polynomial
	int degree = 1;
	//number of bots
	int size = 200;
	//number of trials
	int trials = 5000;
	//best bot currently
	bot bestbot;
	//bots
	bot[] bots = new bot[size];
	//number of input sets / output sets
	int n;// = 400;
	//input array
	double[][] input;
	//output array
	double[] output;
	Random ran = new Random();
	//mutate amount
	double m = 1;

	public Interpolator(double[] x, double[] y, int maxError) {
		double[] arr = new double[x.length];
		interp(x, arr, y, maxError);
	}
	public Interpolator(double[] x, double[] y, double[] z, int maxError) {
		interp(x, y, z, maxError);

		/*while(true) {
			System.out.print(":");
			String[] inp = in.nextLine().split(" ");
			String command = inp[0];
			if(command.equals("help")) {
				help();
			} else if(command.equals("init")) {
				initializebots();
			} else if(command.equals("run")) {
				run(Integer.parseInt(inp[1]));
			} else if(command.equals("query")) {
				double qx = Double.parseDouble(inp[1]);
				double qy = Double.parseDouble(inp[2]);
				double res = query(qx,qy);
				System.out.println("evaluating at "+qx+","+qy+"...");
				System.out.println(res);
			} else if(command.equals("addlayer")) {
				addlayer();
			} else if(command.equals("display")) {
				System.out.println("interpolation 3d");
				System.out.println("current degree: "+degree);
				System.out.println("current number of terms: "+vars);
				System.out.println("coefficient order: ");
				String co = "1 ";
				for(int i=1;i<=degree;i++) {
					for(int j=0;j<=i;j++) {
						co = co + "x^"+j+"*y^"+(i-j)+" ";
					}
				}
				System.out.println(co);
				System.out.println("number on the end is error");
				System.out.println("best bot: ");
				System.out.println(bestbot);
			} else if(command.equals("save")) {
				System.out.println("saving...");
				save();
			} else if(command.equals("exit")) {
				System.out.println("terminating program...");
				break;
			} else {
				System.out.println("invalid command");
			}
		}
		in.close();*/
	}


	public void interp(double[] x, double[] y, double[] z, int maxError){
		//INPUT
		n = x.length;
		input = new double[n][2];
		output = new double[n];
		for(int i=0;i<n;i++) {
			//X INPUT
			input[i][0] = x[i];
			//Y INPUT
			input[i][1] = y[i];
			//Z INPUT / OUTPUT
			output[i] = z[i];
		}
		//Scanner in = new Scanner(System.in);
		help();
		initializebots();
		double err = maxError + 100;
		int iteration = 0;
		while (err > maxError && iteration < 100){
			addlayer();
			err = bestbot.score;
			iteration++;
		}

		if(err > maxError){
			System.out.println("Could not create a good curve fit!");
		}
	}


	public void help() {
		System.out.println("help - display commands help");
		System.out.println("init - initialize bots randomly");
		System.out.println("run n - run n trials");
		System.out.println("query x y - evaluate bot at x,y");
		System.out.println("addlayer - add layer to polynomial");
		System.out.println("display - display info");
		System.out.println("save - save 640 x 640 array to file");
		System.out.println("exit - quit program");
	}
	//initialize the bots randomly
	public void initializebots() {
		for(int i=0;i<size;i++) {
			double[] temp = new double[vars];
			for(int j=0;j<vars;j++) {
				temp[j] = Math.random()*2-1;
			}
			bots[i] = new bot(temp);
		}
		System.out.println("bots initialized");
	}

	public double query(double x) {
		return query(x, 0);
	}

	public double query(double a, double b) {
		double[] pass = pass(a,b);
		double res = 0;
		for(int i=0;i<vars;i++) {
			res+=bestbot.w[i]*pass[i];
		}
		return res;
	}
	//run
	public void run(int numtrials) throws IOException {
		System.out.println("running "+numtrials+" trials...");
		for(int trial=0;trial<trials;trial++) {
			double[][] pass = new double[n][vars];
			double[] target = new double[n];
			//polynomial
			for(int i=0;i<n;i++) {
				double a = input[i][0];
				double b = input[i][1];
				double[] temppass = pass(a,b);
				for(int j=0;j<vars;j++) {
					pass[i][j] = temppass[j];
				}
				target[i] = output[i];
			}
			//evaluate bots
			for(int i=0;i<size;i++) {
				bot c = bots[i];
				c.value2(pass, target);
			}
			//sort bots
			Arrays.sort(bots);
			bestbot = bots[0];
			//mutate bottom 75% of bots
			for(int i=size/4;i<size;i++) {
				for(int j=0;j<vars;j++) {
					bots[i].w[j] = bestbot.w[j];
				}
				int index = ran.nextInt(vars);
				bots[i].w[index] = bestbot.w[index]+(Math.random()*2*m-m);
			}
			//decrease mutate amount
			//disabled
			//if(trial%500==0) m*=0.999;
		}
		System.out.println(numtrials+" trials completed");
		System.out.println("best bot: ");
		System.out.println(bestbot);
	}
	public double[] pass(double a, double b) {
		double[] ret = new double[vars];
		ret[0] = 1;
		int index = 1;
		for(int deg=1;deg<=degree;deg++) {
			for(int j=0;j<=deg;j++) {
				ret[index] = Math.pow(a, j)*Math.pow(b, deg-j);
				index++;
			}
		}
		if(index!=vars) System.out.println("UHOH");
		return ret;
	}
	public void addlayer() {
		System.out.println("adding layer...");
		//System.out.println("WARNING: NOT IMPLEMENTED YET");
		degree++;
		vars+=(degree+1);
		for(bot c:bots) {
			c.increasevars();
		}
		System.out.println("new degree: "+degree);
		System.out.println("terms: "+vars);
		System.out.println("added layer");
	}
	public static void main(String[] args) throws IOException {
		//dummy input;
		//subject to change
//		double[] x = {0,0,0,1,1,1,2,2,2};
//		double[] y = {0,1,2,0,1,2,0,1,2};
//		double[] z = {0,1,2,1,2,3,2,3,4};
		double[] x = new double[200];
		double[] y = new double[200];
		double[] z = new double[200];
		for(int i=0;i<200;i++) {
			x[i] = Math.random()*10-5;
			y[i] = Math.random()*10-5;
			z[i] = function(x[i],y[i]);
		}
		new Interpolator(x,y,z, 5);
	}
	public static double function(double a, double b) {
		return 1+a*a*a+b*b*b+a*b;
	}
	public void save() throws IOException {
		//print to file "interpolationoutput.txt"
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("interpolationoutput.txt")));
		for(int i=0;i<640;i++) {
			for(int j=0;j<640;j++) {
//				double[] pass = new double[vars];
//				pass[0] = 1;
//				pass[1] = i;
//				pass[2] = j;
//				pass[3] = i*i;
//				pass[4] = j*j;
//				pass[5] = i*j;
//				pass[6] = i*i*i;
//				pass[7] = i*i*j;
//				pass[8] = i*j*j;
//				pass[9] = j*j*j;
				double[] pass = pass(i,j);
				double ret = 0;
				for(int k=0;k<vars;k++) {
					ret += pass[k] * bestbot.w[k];
				}
				out.print(ret+" ");
			}
			out.println();
		}
		out.close();
		System.out.println("saved to file");
	}
	//error formula
	public double error(double o, double e) {
		return ((o-e)*(o-e));
	}
	//class bot
	class bot implements Comparable<bot> {
		double[] w = new double[vars];
		double score;
		public bot(double[] a) {
			for(int i=0;i<vars;i++) {
				w[i] = a[i];
			}
		}
		public void increasevars() {
			double[] nw = new double[vars];
			Arrays.fill(nw, 0);
			for(int i=0;i<w.length;i++) {
				nw[i] = w[i];
			}
			w = nw;
		}
		public double value(double[] a, double b) {
			double ret = 0;
			for(int i=0;i<vars;i++) {
				ret+=a[i]*w[i];
			}
			score = Math.abs(b-ret);
			return ret;
		}
		public double value2(double[][] a, double[] b) {
			double total = a.length;
			double ret = 0;
			for(int i=0;i<total;i++) {
				double sum = 0;
				for(int j=0;j<vars;j++) {
					sum+=a[i][j]*w[j];
				}
				ret += error(sum,b[i]);
			}
			score = ret;
			return ret;
		}
		public String toString() {
			String ret = "";
			for(int i=0;i<vars;i++) {
				ret+=w[i]+" ";
			}
			ret+=score;
			return ret;
		}
		@Override
		public int compareTo(bot o) {
			return Double.compare(score, o.score);
		}
	}
}