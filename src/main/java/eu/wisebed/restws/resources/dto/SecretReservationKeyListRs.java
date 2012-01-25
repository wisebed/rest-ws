package eu.wisebed.restws.resources.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.api.rs.SecretReservationKey;

@XmlRootElement
public class SecretReservationKeyListRs {
	public List<SecretReservationKey> reservations;

	public SecretReservationKeyListRs(List<SecretReservationKey> reservations) {
		this.reservations = reservations;
	}

}
