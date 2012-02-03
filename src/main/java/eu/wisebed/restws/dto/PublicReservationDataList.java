package eu.wisebed.restws.dto;

import eu.wisebed.api.rs.PublicReservationData;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class PublicReservationDataList {

	public List<PublicReservationData> reservations;

	public PublicReservationDataList() {
	}

	public PublicReservationDataList(List<PublicReservationData> reservations) {
		this.reservations = reservations;
	}

}
