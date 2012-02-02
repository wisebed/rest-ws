package eu.wisebed.restws.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.api.snaa.SecretAuthenticationKey;
import eu.wisebed.restws.util.Base64Helper;
import eu.wisebed.restws.util.JSONHelper;

@XmlRootElement
public class SnaaSecretAuthenticationKeyList {

	public List<SecretAuthenticationKey> secretAuthenticationKeys;

	public SnaaSecretAuthenticationKeyList() {
	}

	public SnaaSecretAuthenticationKeyList(String json) {
		try {
			this.secretAuthenticationKeys = JSONHelper.fromJSON(Base64Helper.decode(json),
					SnaaSecretAuthenticationKeyList.class
			).secretAuthenticationKeys;
		} catch (Exception e) {
			this.secretAuthenticationKeys = null;
		}
	}

	public SnaaSecretAuthenticationKeyList(List<SecretAuthenticationKey> secretAuthenticationKeys) {
		this.secretAuthenticationKeys = secretAuthenticationKeys;
	}

}