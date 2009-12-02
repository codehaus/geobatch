package it.geosolutions.geobatch.track.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

@Embeddable
public class ContactPosition {

	@Column(name = "TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	
	@Column(name = "COG")
	private double cog;
	
	@Type(type = "org.hibernatespatial.GeometryUserType")
	@Column(name = "position", nullable = true)
	private Point position;
	
	@Type(type = "org.hibernatespatial.GeometryUserType")
	@Column(name = "the_geom", nullable = true)
	private LineString course;

	public ContactPosition() {
		super();
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return this.time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return the course
	 */
	public LineString getCourse() {
		return this.course;
	}

	/**
	 * @param course the course to set
	 */
	public void setCourse(LineString course) {
		this.course = course;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * @return the position
	 */
	public Point getPosition() {
		return this.position;
	}

	/**
	 * @param cog the course over ground to set
	 */
	public void setCog(double cog) {
		this.cog = cog;
	}

	/**
	 * @return the course over ground
	 */
	public double getCog() {
		return this.cog;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toStr = "[ TIME : ";
		toStr = toStr.concat(Long.valueOf(this.getTime().getTime()).toString());
		toStr = toStr.concat(" - COG : ");
		toStr = toStr.concat(Double.valueOf(this.getCog()).toString());
		toStr = toStr.concat(" - LOCATION : ");
		toStr = toStr.concat(this.getPosition().getGeometryType().toString());
		toStr = toStr.concat(" - COURSE : ");
		toStr = toStr.concat(this.getCourse().getGeometryType().toString());
		toStr = " ]";
		
		return toStr;
	}
}