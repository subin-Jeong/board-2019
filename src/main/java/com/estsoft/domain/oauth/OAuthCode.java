package com.estsoft.domain.oauth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "OAUTH_CODE")
public class OAuthCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "BIGINT UNSIGNED")
	private Integer id;
	
	@Column(name = "CODE")
	private String code;
	
	@Lob
	@Column(name = "AUTHENTICATION", columnDefinition = "MEDIUMBLOB")
	private byte[] authentication;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public byte[] getAuthentication() {
		return authentication;
	}

	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
	}
	
	
}