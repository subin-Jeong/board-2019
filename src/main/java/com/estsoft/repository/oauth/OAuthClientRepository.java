package com.estsoft.repository.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.estsoft.domain.oauth.OAuthClientDetails;

@RepositoryRestResource
public interface OAuthClientRepository extends JpaRepository<OAuthClientDetails, Long> {

	// findMaxId
	@Query("SELECT COALESCE(MAX(id), 0) FROM OAuthClientDetails")
	int findMaxId();
}
