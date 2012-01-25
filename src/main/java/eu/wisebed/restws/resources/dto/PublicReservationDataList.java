package eu.wisebed.restws.resources.dto;

import eu.wisebed.api.rs.PublicReservationData;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class PublicReservationDataList {

	List<PublicReservationData> reservations;

	public PublicReservationDataList(List<PublicReservationData> reservations) {
		this.reservations = reservations;
	}

}
