package com.estsoft.service;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.estsoft.auth.SecurityConfig;
import com.estsoft.domain.api.Member;
import com.estsoft.domain.api.MemberRole;
import com.estsoft.domain.oauth.CustomGrantedAuthority;
import com.estsoft.domain.oauth.CustomUserDetails;
import com.estsoft.repository.api.MemberRepository;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
	
	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Autowired
	private MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		log.info("login : " + username);
		
		Member member = memberRepository.findByEmail(username);
		
		if(member != null) {
			
			// �α��ε� ���� ���
			CustomUserDetails customUserDetails = new CustomUserDetails();
			customUserDetails.setUserName(member.getEmail());
			customUserDetails.setPassword(member.getPassword());
			
			// �α����� ȸ���� ���� ���
			Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
			for (MemberRole role : member.getRoles()) {
				roles.add(new CustomGrantedAuthority(role.getRole().getName()));
			}
			customUserDetails.setGrantedAuthorities(roles);
			
			log.info(customUserDetails.toString());
			
			// Oauth2 �� ���� JWT Token ȹ��
			//clientTokenService.getOAuth2Token();
			
			
			return customUserDetails;
		}
		
		throw new UsernameNotFoundException(username);
	}
	
}
