package com.estsoft.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * ȸ�� �α����� ���� Security ����
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = false)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecurityHandler securityHandler;
	
	@Autowired
    private UserDetailsService userDetailsServiceImpl;
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
    public PasswordEncoder userPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }
	
	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
		
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceImpl);
        authProvider.setPasswordEncoder(userPasswordEncoder());
        return authProvider;
        
    }
	  
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		// /login �� userDetailsServiceImpl ���� DB���� ���� ��ȸ�Ͽ� �α��εǵ��� ����
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Override 
	public void configure(WebSecurity web) {
		
		// ���� ���̺귯���� ���� ���� ���� ���
		web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
		
	}

	@Override 
	public void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
			.antMatchers("/member/**", "/oauth/**", "/css/**", "/img/**", "/js/**", "/vendor/**").permitAll()
			// ȸ������ ���
			.antMatchers("/member/getMember/**").permitAll()
			.anyRequest().authenticated()
			.and()
				.formLogin()
				.loginPage("/member/login")
				.loginProcessingUrl("/login")
				.successHandler(securityHandler)
				.failureHandler(securityHandler)
				.permitAll()
			.and()
				.logout()
				.permitAll()
			.and().exceptionHandling()
			.and().csrf().disable();
		  
	}
	
}
