package org.avphs.driving;


public class Straight implements RoadData {
	float straightLen;
	private float startX;
	private float startY;
	private float endX;
	private float endY;

	public Straight(float startX, float startY, float endX, float endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;

		straightLen = (float)Math.sqrt(Math.pow(endX - startX, 2.0) + Math.pow(endY - startY, 2.0));
	}
}

