package org.avphs.driving;

import org.avphs.util.VectorPoint;

public class Turn extends RoadData{
	private float radius;
	private float angle;

	public Turn(float radius, float angle, VectorPoint pos) {
		this.radius = radius;
		this.angle = angle;
	}
}
