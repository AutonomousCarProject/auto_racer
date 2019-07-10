package org.avphs.driving;

public class Turn implements RoadData{
	private float radius;
	private float angle;
	private float startX;
	private float startY;
	private float endX;
	private float endY;

	public Turn(float startX, float startY, float endX, float endY, float radius, float angle) {
		this.radius = radius;
		this.angle = angle;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
}
