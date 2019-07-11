package org.avphs.driving;

public class Turn extends RoadData{

	public Turn(float startX, float startY, float endX, float endY, short radius) {
		this.radius = radius;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
}