package com.estsoft.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.estsoft.domain.Member;

@RepositoryRestResource
public interface MemberRepository extends CrudRepository<Member, Long> {
	
	// findByEmail
	public Member findByEmail(String email);
	
	// countByEmailIgnoreCase
	public int countByEmailIgnoreCase(String email);

}
