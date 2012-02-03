package eu.wisebed.restws.dummy;

import eu.wisebed.api.snaa.*;

import java.util.LinkedList;
import java.util.List;

public class DummySnaa implements SNAA {

	@Override
	public List<SecretAuthenticationKey> authenticate(List<AuthenticationTriple> loginData)
			throws AuthenticationExceptionException,
			SNAAExceptionException {

		List<SecretAuthenticationKey> secretAuthKeys = new LinkedList<SecretAuthenticationKey>();

		SecretAuthenticationKey sak1 = new SecretAuthenticationKey();
		sak1.setSecretAuthenticationKey("verysecret1");
		sak1.setUrnPrefix("urn1");
		sak1.setUsername("user1");

		SecretAuthenticationKey sak2 = new SecretAuthenticationKey();
		sak2.setSecretAuthenticationKey("verysecret2");
		sak2.setUrnPrefix("urn2");
		sak2.setUsername("user2");

		secretAuthKeys.add(sak1);
		secretAuthKeys.add(sak2);

		return secretAuthKeys;
	}

	@Override
	public boolean isAuthorized(List<SecretAuthenticationKey> arg0, Action arg1) throws SNAAExceptionException {
		return true;
	}

}
