package com.estsoft.service;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.estsoft.domain.Member;
import com.estsoft.domain.MemberRole;
import com.estsoft.domain.dto.CustomGrantedAuthority;
import com.estsoft.domain.dto.CustomUserDetails;
import com.estsoft.repository.MemberRepository;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
/*
	@Autowired
	private MemberRepository memberRepository;
	
	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {	
		
		System.out.println("로그인" + username);
		
		
		//Member member = memberRepository.findByEmail(username);
		//System.out.println(new SecurityMember(member).toString());
		//return new SecurityMember(member);
		
		
		Member member = memberRepository.findByEmail(username);
		if (member != null) {
			CustomUserDetails customUserDetails = new CustomUserDetails();
			customUserDetails.setUserName(member.getEmail());
			customUserDetails.setPassword(member.getPassword());
			Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
			for (MemberRole role : member.getRoles()) {
				authorities.add(new CustomGrantedAuthority(role.getRoleName()));
			}
			customUserDetails.setGrantedAuthorities(authorities);
			return customUserDetails;
		}
		throw new UsernameNotFoundException(username);
		
    }

*/
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private ClientTokenService clientTokenService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		System.out.println("로그인" + username);
		
		Member member = memberRepository.findByEmail(username);
		
		if(member != null) {
			
			// 로그인된 정보 담기
			CustomUserDetails customUserDetails = new CustomUserDetails();
			customUserDetails.setUserName(member.getEmail());
			customUserDetails.setPassword(member.getPassword());
			
			// 로그인한 회원의 권한 목록
			Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
			for (MemberRole role : member.getRoles()) {
				roles.add(new CustomGrantedAuthority(role.getRole().getName()));
			}
			customUserDetails.setGrantedAuthorities(roles);
			
			System.out.println(customUserDetails.toString());
			
			// Oauth2 를 통한 JWT Token 획득
			//clientTokenService.getOAuth2Token();
			
			
			return customUserDetails;
		}
		
		throw new UsernameNotFoundException(username);
	}
	
}
