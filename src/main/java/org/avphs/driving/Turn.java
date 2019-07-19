package org.avphs.driving;

public class Turn extends RoadData{

	private Curve c;
	private float[] center;
	public Turn(float startX, float startY, float endX, float endY, Curve c) {
		this.c = c;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		radius = c.getRadius();
	}

	public short getRadius(){
		return radius;
	}

	public float getCenterX(){
		return center[0];
	}

	public float getCenterY(){
		return center[1];
	}
}