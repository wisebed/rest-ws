package eu.wisebed.restws.dto;

import eu.wisebed.api.rs.ConfidentialReservationData;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ConfidentialReservationDataList {

	List<ConfidentialReservationData> reservations;

	public ConfidentialReservationDataList(List<ConfidentialReservationData> reservations) {
		this.reservations = reservations;
	}

}
