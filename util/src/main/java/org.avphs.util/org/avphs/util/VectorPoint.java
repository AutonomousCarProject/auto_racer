package org.avphs.util;


/**
 * a datatype representing a floating point number pair (x,y)
 * 
 * @author Charlie
 * @version 1.0
 */
public class VectorPoint{
	
	/**
	 * the x component
	 */
	public final double x;
	/**
	 * the y component
	 */
	public final double y;
	/**
	 * magnitude of the vector
	 */
	public double length;

	/**
	 * constructing a new vector
	 * 
	 * @param x
	 *            the x component
	 * @param y
	 *            the y component
	 */
	public VectorPoint(double x, double y) {
		this.x = x;
		this.y = y;
		this.length = Math.hypot(x,y);
	}

	
	public VectorPoint add(VectorPoint v){
		return new VectorPoint(this.x+v.x,this.y+v.y);
	}
	
	public double dot(VectorPoint v){
		return this.x*v.x+this.y*v.y;
	}
	
	public double cross(VectorPoint v){
		return this.x*v.y-v.x*this.y;
	}
}

