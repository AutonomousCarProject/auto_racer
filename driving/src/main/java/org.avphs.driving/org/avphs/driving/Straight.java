package org.avphs.driving;


public class Straight extends RoadData {
	private float straightLen;

	public Straight(float startX, float startY, float endX, float endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.radius = 0;


		straightLen = (float)Math.sqrt(Math.pow(endX - startX, 2.0) + Math.pow(endY - startY, 2.0));
	}

	public float getStraightLen(){
		return straightLen;
	}
}

