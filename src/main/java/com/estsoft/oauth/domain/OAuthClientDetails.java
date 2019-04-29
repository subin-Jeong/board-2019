package com.estsoft.oauth.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "OAUTH_CLIENT_DETAILS")
public class OAuthClientDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", columnDefinition = "BIGINT UNSIGNED")
	private Integer id;
	
	@Column(name = "CLIENT_ID")
	private String clientId;
	
	@Column(name = "CLIENT_NAME")
	private String clientName;
	
	@Column(name = "RESOURCE_IDS")
	private String resourceIds;
	
	@Column(name = "CLIENT_SECRET")
	private String clientSecret;
	
	@Column(name = "SCOPE")
	private String scope;
	
	@Column(name = "AUTHORIZED_GRANT_TYPES")
	private String authorizedGrantTypes;
	
	@Column(name = "WEB_SERVER_REDIRECT_URI")
	private String webServerRedirectUri;
	
	@Column(name = "AUTHORITIES")
	private String authorities;
	
	@Column(name = "ACCESS_TOKEN_VALIDITY", length = 11)
	private Integer accessTokenValidity;
	
	@Column(name = "REFRESH_TOKEN_VALIDITY", length = 11)
	private Integer refreshTokenValidity;
	
	@Column(name = "ADDITIONAL_INFORMATION", length = 4096)
	private String additionalInformation;
	
	@Column(name = "AUTOAPPROVE", columnDefinition = "TINYINT(4)")
	private Integer autoapprove;
	
	@Column(name = "UUID")
	private String uuid;
	
	@Column
	private Date created;
	
	@Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
	private Boolean enabled;
	
	@Transient
	private String[] scopes;
	
	@Transient
	private String[] grantTypes;
	
	@Transient
	private String ownerEmail;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAuthorizedGrantTypes() {
		return authorizedGrantTypes;
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public String getWebServerRedirectUri() {
		return webServerRedirectUri;
	}

	public void setWebServerRedirectUri(String webServerRedirectUri) {
		this.webServerRedirectUri = webServerRedirectUri;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public Integer getAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(Integer accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public Integer getRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(Integer refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public Integer getAutoapprove() {
		return autoapprove;
	}

	public void setAutoapprove(Integer autoapprove) {
		this.autoapprove = autoapprove;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String[] getScopes() {
		return scopes;
	}

	public void setScopes(String[] scopes) {
		this.scopes = scopes;
	}

	public String[] getGrantTypes() {
		return grantTypes;
	}

	public void setGrantTypes(String[] grantTypes) {
		this.grantTypes = grantTypes;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	
	
}
