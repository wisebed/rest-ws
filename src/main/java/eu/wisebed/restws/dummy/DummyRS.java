package eu.wisebed.restws.dummy;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import eu.wisebed.api.rs.AuthorizationExceptionException;
import eu.wisebed.api.rs.ConfidentialReservationData;
import eu.wisebed.api.rs.GetReservations;
import eu.wisebed.api.rs.PublicReservationData;
import eu.wisebed.api.rs.RS;
import eu.wisebed.api.rs.RSExceptionException;
import eu.wisebed.api.rs.ReservervationConflictExceptionException;
import eu.wisebed.api.rs.ReservervationNotFoundExceptionException;
import eu.wisebed.api.rs.SecretAuthenticationKey;
import eu.wisebed.api.rs.SecretReservationKey;

public class DummyRS implements RS {

	@Override
	public void deleteReservation(List<SecretAuthenticationKey> arg0, List<SecretReservationKey> arg1) throws RSExceptionException,
			ReservervationNotFoundExceptionException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ConfidentialReservationData> getConfidentialReservations(List<SecretAuthenticationKey> arg0, GetReservations arg1)
			throws RSExceptionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ConfidentialReservationData> getReservation(List<SecretReservationKey> arg0) throws RSExceptionException,
			ReservervationNotFoundExceptionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PublicReservationData> getReservations(XMLGregorianCalendar arg0, XMLGregorianCalendar arg1) throws RSExceptionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SecretReservationKey> makeReservation(List<SecretAuthenticationKey> arg0, ConfidentialReservationData arg1)
			throws AuthorizationExceptionException, RSExceptionException, ReservervationConflictExceptionException {
		// TODO Auto-generated method stub
		return null;
	}

}
