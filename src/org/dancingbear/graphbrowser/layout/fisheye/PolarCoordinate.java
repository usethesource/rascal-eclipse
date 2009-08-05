package org.dancingbear.graphbrowser.layout.fisheye;

public class PolarCoordinate {

	private double radius;
	private double azimuth;

	public PolarCoordinate(double radius, double azimuth) {
		super();
		this.radius = radius;
		this.azimuth = azimuth;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	public CartesianCoordinate getCartesian() {
		return new CartesianCoordinate(radius*Math.cos(azimuth), radius*Math.sin(azimuth));
	}
	
	public PolarCoordinate getHyperbolic(double factor) {
		return new PolarCoordinate(500*Math.log(factor*radius+1),azimuth);
	}

}
