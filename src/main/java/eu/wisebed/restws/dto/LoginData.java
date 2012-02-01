package eu.wisebed.restws.dto;

import eu.wisebed.api.snaa.AuthenticationTriple;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class LoginData {
	public List<AuthenticationTriple> authenticationData;
}