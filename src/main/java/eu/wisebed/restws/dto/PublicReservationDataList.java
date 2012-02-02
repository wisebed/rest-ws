package eu.wisebed.restws.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.api.rs.PublicReservationData;

@XmlRootElement
public class PublicReservationDataList {

	List<PublicReservationData> reservations;

	public PublicReservationDataList(List<PublicReservationData> reservations) {
		this.reservations = reservations;
	}

}
