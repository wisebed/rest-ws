package eu.wisebed.restws.dto;

import eu.wisebed.api.rs.SecretReservationKey;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SecretReservationKeyListRs {

	public List<SecretReservationKey> reservations;

	public SecretReservationKeyListRs(List<SecretReservationKey> reservations) {
		this.reservations = reservations;
	}

}
