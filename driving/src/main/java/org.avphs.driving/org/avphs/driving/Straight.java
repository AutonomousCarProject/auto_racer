package org.avphs.driving;


public class Straight extends RoadData {
	float straightLen;

	public Straight(float startX, float startY, float endX, float endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;

		straightLen = (float)Math.sqrt((endX-startX)+(endY-startY));
	}
}

