package com.estsoft.repository.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import com.estsoft.domain.api.Member;

@RepositoryRestResource
public interface MemberRepository extends JpaRepository<Member, Long> {
	
	// findByEmail
	Member findByEmail(String email);
	
	// countByEmailIgnoreCase
	int countByEmailIgnoreCase(String email);
	
	// findNameByEmail
	@Query("SELECT m.name FROM MEMBER m WHERE m.email = :email")
	String findNameByEmail(@Param("email") String email);
	
	// updateRefreshTokenByEmail
	@Modifying
	@Transactional
	@Query("UPDATE MEMBER m SET m.refreshToken = :refreshToken WHERE m.email = :email")
	int updateRefreshTokenByEmail(@Param("email") String email, @Param("refreshToken") String refreshToken);
	
	// findAllEmailAndName
	@Query("SELECT email, name FROM MEMBER")
	List<Member> findAllEmailAndName();

}
