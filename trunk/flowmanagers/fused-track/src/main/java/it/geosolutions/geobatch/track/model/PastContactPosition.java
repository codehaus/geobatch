/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.track.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

@Entity(name = "PastContactPosition")
@Table(name = "PAST_CONTACT_POSITION", schema = "public")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "past_contact")
@NamedQueries({
	@NamedQuery(name="findPastContactPositionByPeriod", query="select pcp from PastContactPosition pcp " +
			"where pcp.contact.contactId=:contactId and pcp.time>=:timeStamp")
})
public class PastContactPosition implements Serializable {

	private static final long serialVersionUID = -1595692090863316356L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private long pastContactId;
	
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
	
	@ManyToOne
	private Contact contact;
	

	public PastContactPosition() {

	}

	/**
	 * @return the id
	 */
	public long getPastContactId() {
		return this.pastContactId;
	}

	/**
	 * @param id the id to set
	 */
	public void setPastContactId(long pastContactId) {
		this.pastContactId = pastContactId;
	}
	
	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param cog the cog to set
	 */
	public void setCog(double cog) {
		this.cog = cog;
	}

	/**
	 * @return the course over ground
	 */
	public double getCog() {
		return cog;
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
		return position;
	}

	/**
	 * @param course the course to set
	 */
	public void setCourse(LineString course) {
		this.course = course;
	}

	/**
	 * @return the course
	 */
	public LineString getCourse() {
		return course;
	}

	/**
	 * @param the contact
	 */
	public Contact getContact(){
		return this.contact;
	}
	
	/**
	 * @param contact the contact to set
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toStr = "[ ID : ";
		toStr = toStr.concat(Long.valueOf(this.getPastContactId()).toString());
		toStr = toStr.concat(" - TIME : ");
		toStr = toStr.concat(Long.valueOf(this.getTime().getTime()).toString());
		toStr = toStr.concat(" - COG : ");
		toStr = toStr.concat(Double.valueOf(this.getCog()).toString());
		toStr = toStr.concat(" - LOCATION : ");
		toStr = toStr.concat(this.getCourse().getGeometryType().toString());
		toStr = toStr.concat(" - COURSE : ");
		toStr = toStr.concat(this.getCourse().getGeometryType().toString());
		toStr = " ]";
		
		return toStr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Long.valueOf(this.pastContactId) == null) ? 
				0 : Long.valueOf(this.pastContactId).hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PastContactPosition other = (PastContactPosition) obj;
		if (Long.valueOf(this.pastContactId) == null) {
			if (Long.valueOf(other.getPastContactId()) != null)
				return false;
		} else if (!Long.valueOf(this.pastContactId).equals(Long.valueOf(other.getPastContactId())))
			return false;
		return true;
	}
}
