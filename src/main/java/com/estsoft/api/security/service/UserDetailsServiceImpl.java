package com.estsoft.api.security.service;

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

import com.estsoft.api.domain.Member;
import com.estsoft.api.domain.MemberRole;
import com.estsoft.api.repository.MemberRepository;
import com.estsoft.oauth.domain.CustomGrantedAuthority;
import com.estsoft.oauth.domain.CustomUserDetails;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
	
	// Log
	private Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	
	@Autowired
	private MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		log.info("[LOGIN USERNAME] " + username);
		
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
			
			return customUserDetails;
		}
		
		throw new UsernameNotFoundException(username);
	}
	
}
