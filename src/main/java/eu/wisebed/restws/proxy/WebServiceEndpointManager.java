package eu.wisebed.restws.proxy;

import eu.wisebed.api.rs.RS;
import eu.wisebed.api.sm.SessionManagement;
import eu.wisebed.api.snaa.SNAA;

import javax.annotation.Nonnull;

public interface WebServiceEndpointManager {

	@Nonnull
	SNAA getSnaaEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;

	@Nonnull
	RS getRsEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;

	@Nonnull
	SessionManagement getSmEndpoint(@Nonnull String testbedId) throws UnknownTestbedIdException;
}
