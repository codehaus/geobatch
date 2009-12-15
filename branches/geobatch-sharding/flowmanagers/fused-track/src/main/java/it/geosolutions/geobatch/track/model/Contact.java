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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */

@Entity(name = "Contact")
@Table(name = "CONTACT", schema = "public")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "contact")
public class Contact implements Serializable {

	private static final long serialVersionUID = 4856492695709986718L;

	@Id
	@Column(name = "ID")
	private long contactId;

	@Column(name = "LINK_CODE")
	private long link; 
	
	@Column(name = "TYPE_CODE")
	@Enumerated(EnumType.STRING)
	private ContactType contactType;
	
	@Embedded
	private ContactPosition contactPosition;
	
	@OneToMany(mappedBy="contact")
	private List<PastContactPosition> pastContactPosition = new ArrayList<PastContactPosition>();

	
	public Contact() {
		
	}

	/**
	 * @return the id
	 */
	public long getContactId() {
		return this.contactId;
	}

	/**
	 * @param id the id to set
	 */
	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

	/**
	 * @return the link
	 */
	public long getLink() {
		return link;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(long link) {
		this.link = link;
	}

	/**
	 * @return the current contact
	 */
	public ContactPosition getContactPosition(){
		return this.contactPosition;
	}
	
	/** 
	 * @param contact the contact to set 
	 */
	public void setContactPosition(ContactPosition contact){
		this.contactPosition = contact;
	}
	
	/**
	 * @return the past contact position
	 */
	public List<PastContactPosition> getPastContactPosition() {
		return this.pastContactPosition;
	}

	/**
	 * @return the past contact position to set
	 */
	public void setPastContactPosition(List<PastContactPosition> pastContactPosition) {
		this.pastContactPosition = pastContactPosition;
	}
	
	/**
	 * @return the contact type
	 */
	public ContactType getContactType(){
		return this.contactType;
	}
	
	/**
	 * @return the contact type to set
	 */
	public void setContactType(ContactType contactType){
		this.contactType = contactType;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toStr = "[ ID : ";
		toStr = toStr.concat(Long.valueOf(this.getContactId()).toString());
		toStr = toStr.concat(" - LINK_CODE : ");
		toStr = toStr.concat(Long.valueOf(this.link).toString());
		toStr = toStr.concat(" - TYPE_CODE : ");
		toStr = toStr.concat(this.contactType.toString());
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
		result = prime * result + ((Long.valueOf(this.contactId) == null) ? 
				0 : Long.valueOf(this.contactId).hashCode());
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
		Contact other = (Contact) obj;
		if (Long.valueOf(this.contactId) == null) {
			if (Long.valueOf(other.getContactId()) != null)
				return false;
		} else if (!Long.valueOf(this.contactId).equals(Long.valueOf(other.getContactId())))
			return false;
		return true;
	}
}
