package org.avphs.driving;

public class Straight extends RoadData {
	private float straightLen;
	private float slope;
	private float b;
	private float negRecip;

	public Straight(float startX, float startY, float endX, float endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.radius = 0;

		slope = (endY-startY)/(endX-startX);
		b = (-slope * startX) + startY;
		straightLen = (float)Math.sqrt(Math.pow(endX - startX, 2.0) + Math.pow(endY - startY, 2.0));
		negRecip = ((float)-1)/slope;
	}

	public float getStraightLen(){
		return straightLen;
	}

	public float getSlope(){
		return slope;
	}

	public float getB(){
		return b;
	}

	public float getNegRecip(){
		return negRecip;
	}

	public float[] getStartingCoords(){
		float[] point = new float[2];
		point[0] = startX; point[1] = startY;
		return point;
	}
}
