package org.avphs.calibration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class interpolation3d {
	//number of terms in the desired polynomial
	int vars = 10;
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
	public interpolation3d(double[] x, double[] y, double[] z) throws IOException {
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
		initializebots();
		run(trials);
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
	}
	//run
	public void run(int numtrials) throws IOException {
		for(int trial=0;trial<trials;trial++) {
			double[][] pass = new double[n][vars];
			double[] target = new double[n];
			//polynomial
			for(int i=0;i<n;i++) {
				double a = input[i][0];
				double b = input[i][1];
				pass[i][0] = 1;
				pass[i][1] = a;
				pass[i][2] = b;
				pass[i][3] = a*a;
				pass[i][4] = b*b;
				pass[i][5] = a*b;
				pass[i][6] = a*a*a;
				pass[i][7] = a*a*b;
				pass[i][8] = a*b*b;
				pass[i][9] = b*b*b;
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
			if(trial%500==0) m*=0.999;
		}
		System.out.println(bestbot);
		print();
	}
	public static void main(String[] args) throws IOException {
		//dummy input;
		//subject to change
		double[] x = {0,0,0,1,1,1,2,2,2};
		double[] y = {0,1,2,0,1,2,0,1,2};
		double[] z = {0,1,2,1,2,3,2,3,4};
		new interpolation3d(x,y,z);
	}
	public void print() throws IOException {
		//print to file "interpolationoutput.txt"
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("interpolationoutput.txt")));
		for(int i=0;i<640;i++) {
			for(int j=0;j<640;j++) {
				double[] pass = new double[vars];
				pass[0] = 1;
				pass[1] = i;
				pass[2] = j;
				pass[3] = i*i;
				pass[4] = j*j;
				pass[5] = i*j;
				pass[6] = i*i*i;
				pass[7] = i*i*j;
				pass[8] = i*j*j;
				pass[9] = j*j*j;
				double ret = 0;
				for(int k=0;k<vars;k++) {
					ret += pass[k] * bestbot.w[k];
				}
				out.print(ret+" ");
			}
			out.println();
		}
		out.close();
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