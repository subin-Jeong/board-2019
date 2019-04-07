package com.estsoft.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.estsoft.domain.Member;
import com.estsoft.repository.MemberRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private MemberRepository memberRepository;
	
	@Override
	public User loadUserByUsername(String email) {
		Member member = memberRepository.findByEmail(email);
		
		if(member == null) {
			throw new UsernameNotFoundException(email);
		}
		
		return new User(member.getEmail(), member.getPassword(), AuthorityUtils.createAuthorityList("USER"));
	}
}
