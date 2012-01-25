package eu.wisebed.restws.dummy;

import eu.wisebed.api.rs.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.List;

public class DummyRS implements RS {

	@Override
	public void deleteReservation(List<SecretAuthenticationKey> arg0, List<SecretReservationKey> arg1)
			throws RSExceptionException,
			ReservervationNotFoundExceptionException {

	}

	@Override
	public List<ConfidentialReservationData> getConfidentialReservations(List<SecretAuthenticationKey> arg0,
																		 GetReservations arg1)
			throws RSExceptionException {

		return Arrays.asList(get());
	}

	@Override
	public List<ConfidentialReservationData> getReservation(List<SecretReservationKey> arg0)
			throws RSExceptionException,
			ReservervationNotFoundExceptionException {

		return Arrays.asList(get());
	}

	@Override
	public List<PublicReservationData> getReservations(XMLGregorianCalendar arg0, XMLGregorianCalendar arg1)
			throws RSExceptionException {

		return Arrays.asList((PublicReservationData) get());
	}

	@Override
	public List<SecretReservationKey> makeReservation(List<SecretAuthenticationKey> arg0,
													  ConfidentialReservationData arg1)
			throws AuthorizationExceptionException, RSExceptionException, ReservervationConflictExceptionException {

		SecretReservationKey srk = new SecretReservationKey();
		srk.setSecretReservationKey("key");
		srk.setUrnPrefix("urnprefix");

		return Arrays.asList(srk);
	}

	private ConfidentialReservationData get() {
		ConfidentialReservationData reservation = new ConfidentialReservationData();

		try {

			reservation.setFrom(DatatypeFactory.newInstance().newXMLGregorianCalendar());
			reservation.setTo(DatatypeFactory.newInstance().newXMLGregorianCalendar());
			reservation.setUserData("something");
			reservation.getNodeURNs().add("someurn1");
			reservation.getNodeURNs().add("someurn2");

			Data data = new Data();
			data.setSecretReservationKey("secret res key");
			data.setUrnPrefix("urnprefix:");
			data.setUsername("username");

			reservation.getData().add(data);

		} catch (DatatypeConfigurationException e) {
		}
		return reservation;
	}

}
