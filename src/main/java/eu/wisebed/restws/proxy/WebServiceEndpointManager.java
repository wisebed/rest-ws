package eu.wisebed.restws.proxy;

import javax.annotation.Nonnull;

import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;

public interface WebServiceEndpointManager {

	@Nonnull
	SNAA getSnaaEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;

	@Nonnull
	RS getRsEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;

	@Nonnull
	SessionManagement getSmEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;
}
