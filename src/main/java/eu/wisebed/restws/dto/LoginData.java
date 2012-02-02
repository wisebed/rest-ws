package eu.wisebed.restws.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.wisebed.api.snaa.AuthenticationTriple;

@XmlRootElement
public class LoginData {
	public List<AuthenticationTriple> authenticationData;
}