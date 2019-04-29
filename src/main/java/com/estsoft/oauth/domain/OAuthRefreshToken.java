package com.estsoft.oauth.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OAUTH_REFRESH_TOKEN")
public class OAuthRefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "BIGINT UNSIGNED")
	private Integer id;
	
	@Column(name = "TOKEN_ID")
	private String tokenId;
	
	@Lob
	@Column(name = "TOKEN", columnDefinition = "MEDIUMBLOB")
	private byte[] token;
	
	@Lob
	@Column(name = "AUTHENTICATION", columnDefinition = "MEDIUMBLOB")
	private byte[] authentication;

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

	public byte[] getAuthentication() {
		return authentication;
	}

	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
	}
}
