package eu.wisebed.restws.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.api.rs.ConfidentialReservationData;

@XmlRootElement
public class ConfidentialReservationDataList {

	List<ConfidentialReservationData> reservations;

	public ConfidentialReservationDataList(List<ConfidentialReservationData> reservations) {
		this.reservations = reservations;
	}

}
