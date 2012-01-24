package eu.wisebed.restws.resources;

public class RsResource {
	
	/**
	 * Deletes a single reservation.
	public void deleteReservation(List<SecretAuthenticationKey> authenticationData, List<SecretReservationKey> secretReservationKey)
	 * 
	 */

	/**
	 * Returns reservations for a time interval (including confidential information).
	public List<ConfidentialReservationData> getConfidentialReservations(List<SecretAuthenticationKey> secretAuthenticationKey, GetReservations period) 
	 */

	/** Returns data about a single reservation (including confidential information). 
	public List<ConfidentialReservationData> getReservation(List<SecretReservationKey> secretReservationKey) 
	 * */

	/** Returns reservations for a time interval (excluding confidential information). 
	public List<PublicReservationData> getReservations(XMLGregorianCalendar from, XMLGregorianCalendar to) 
	 * */

	/**
	 * Reserves a set of nodes for an interval of time. public List<SecretReservationKey>
	 * makeReservation(List<SecretAuthenticationKey> authenticationData, ConfidentialReservationData reservation)
	 * */

}
