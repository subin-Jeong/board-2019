package com.estsoft.oauth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OAUTH_ACCESS_TOKEN")
public class OAuthAccessToken {

	@Id
	@Column(name = "AUTHENTICATION_ID")
	private String authenticationId;
	
	@Column(name = "TOKEN_ID")
	private String tokenId;
	
	@Lob
	@Column(name = "TOKEN", columnDefinition = "MEDIUMBLOB")
	private byte[] token;
	
	@Column(name = "USER_NAME")
	private String userName;
	
	@Column(name = "CLIENT_ID")
	private String clientId;
	
	@Lob
	@Column(name = "AUTHENTICATION", columnDefinition = "MEDIUMBLOB")
	private byte[] authentication;
	
	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;

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

	public byte[] getAuthentication() {
		return authentication;
	}

	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}