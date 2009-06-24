package org.dancingbear.graphbrowser.layout.hypergraph;

public class CartesianCoordinate {

	private double abscissa;
	private double ordinate;

	public CartesianCoordinate(double abscissa, double ordinate) {
		super();
		this.abscissa = abscissa;
		this.ordinate = ordinate;
	}

	public double getAbscissa() {
		return abscissa;
	}

	public void setAbscissa(double abscissa) {
		this.abscissa = abscissa;
	}

	public double getOrdinate() {
		return ordinate;
	}

	public void setOrdinate(double ordinate) {
		this.ordinate = ordinate;
	}

	public PolarCoordinate getPolar() {
		double radius =  Math.sqrt(abscissa*abscissa+ordinate*ordinate);
		double azimuth = Math.atan2(ordinate, abscissa);
	    //double azimuth =  Math.signum(abscissa) * Math.acos(abscissa/radius);
		return new PolarCoordinate(radius, azimuth);
	}
	
	public CartesianCoordinate translate(double x, double y) {
		return new CartesianCoordinate(abscissa+x, ordinate+y);	
	}

	public CartesianCoordinate scale(double i) {
		return new CartesianCoordinate(i*abscissa,i*ordinate);
	}
	
	

}
