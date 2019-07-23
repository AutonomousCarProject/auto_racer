package org.avphs.calibration;
public class interpolation_v2{
	
	
public static double poly_inter(double x_val[],double y_val[], double x) {
	if(x_val.length!=y_val.length) { //if there's an x value that doesn't have a y value, return null
		System.out.println("ERROR: X VALUE WITH NO Y");
		return 123456789;
	}
	if(x < x_val[0] || x > x_val[x_val.length-1]) { //if the selected x is outside of the domain (it's interpolation,) return
		System.out.println("ERROR: X OUTSIDE OF DOMAIN");
		return 123456789;
	}
	double ftally = 0; //end tally
	double temptally = 1; //tally of individual terms
	for(double i:x_val) {
		for(double j:x_val) {
			if(j!=i) { //if j==i, the function causes a divide by 0 (also doesn't work with formula)
				temptally *= (x-x_val[(int)j-1])/(x_val[(int)i-1]-x_val[(int)j-1]); //use Lagrange Polynomials for interpolation
			}
			else {
				continue;
			}
		}
		ftally += y_val[(int)i-1]*temptally; //multiply by y_val to complete the Lagrange Polynomial
		temptally = 1; //reset to temptally
	}
	
	
return ftally;	
}
	
public static void main(String[] args){
double x_val[] = {1, 2, 3}; //MAKE SURE THESE ARE IN ORDER FROM LEAST TO GREATEST
double y_val[] = {2, 4, 9};
double x = 3; //ENTER X TO PLUG IN HERE
double y = 0;

y = poly_inter(x_val,y_val, x);	
System.out.print (y);
}

}