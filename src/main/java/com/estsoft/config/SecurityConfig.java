package com.estsoft.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.estsoft.service.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		
		// 허용 경로
		/*
		web.ignoring().antMatchers("/member/register",
								   "/member/login",
								   "/css/**",
								   "/img/**",
								   "/js/**",
								   "/vendor/**");
	*/
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
/*
		http.authorizeRequests() 
			.antMatchers("/**").permitAll() 
		.and() 
			.formLogin()
			.loginPage("/member/login")
			.usernameParameter("email") 
			.passwordParameter("password") 
			.loginProcessingUrl("/member/login") 
			.defaultSuccessUrl("/board/list") 
			.permitAll() 
		.and() 
			.logout() 
			.logoutUrl("/member/logout") 
			.logoutSuccessUrl("/member/login") 
			.invalidateHttpSession(true) 
			.permitAll() 
		.and() 
			.csrf().disable();

	*/
	}
}
