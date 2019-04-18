package com.estsoft.domain.oauth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OAUTH_CLIENT_TOKEN")
public class OAuthClientToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "BIGINT UNSIGNED")
	private Integer id;
	
	@Column(name = "TOKEN_ID")
	private String tokenId;
	
	@Lob
	@Column(name = "TOKEN", columnDefinition = "MEDIUMBLOB")
	private byte[] token;
	
	@Column(name = "AUTHENTICATION_ID")
	private String authenticationId;
	
	@Column(name = "USER_NAME")
	private String userName;
	
	@Column(name = "CLIENT_ID")
	private String clientId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public String getAuthenticationId() {
		return authenticationId;
	}

	public void setAuthenticationId(String authenticationId) {
		this.authenticationId = authenticationId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	
}
