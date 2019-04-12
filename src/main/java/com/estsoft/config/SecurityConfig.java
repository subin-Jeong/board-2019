package com.estsoft.config;

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

	@Autowired
	private SecuritySuccessHandler securitySuccessHandler;
	
	@Autowired
    private UserDetailsService userDetailsServiceImpl;
	
	
	  
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		// /login �� userDetailsServiceImpl ���� DB���� ���� ��ȸ�Ͽ� �α��εǵ��� ����
		auth.authenticationProvider(authenticationProvider());
		//auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(userPasswordEncoder());
	}
	
	@Override 
	public void configure(WebSecurity web) {
		
		// ���� ���̺귯���� ���� ���� ���� ���
		web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
		
	}

	/*
	 * 
	 * @Override public void configure(WebSecurity web) {
	 * web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
	 * 
	 * // ����� �̺κ� ��� // web.ignoring().antMatchers("/board/**", "/reply/**",
	 * "/member/**");
	 * 
	 * }
	 * 
	 * 
	 * @Override protected void configure(HttpSecurity http) throws Exception {
	 * 
	 * // client_id ���� �κп����� csrf�� ������� ����
	 * //http.csrf().requireCsrfProtectionMatcher(new
	 * AntPathRequestMatcher("/**")).disable();
	 * 
	 * http.authorizeRequests().antMatchers("/").permitAll().antMatchers(
	 * "/member/getMember")
	 * .hasAnyRole("ADMIN").anyRequest().authenticated().and().formLogin()
	 * .permitAll().and().logout().permitAll();
	 * 
	 * http.csrf().disable();
	 * 
	 * }
	 */

	/*
	 * @Override protected void configure(HttpSecurity http) throws Exception {
	 * http.csrf().disable(); }
	 */

	
	  @Override 
	  public void configure(HttpSecurity http) throws Exception {
	  
		/*
		 * http .authorizeRequests() .and().csrf().disable();
		 * 
		 * 
		 * 
		 * System.out.println("�α��� ����??????"); http .authorizeRequests()
		 * .antMatchers("/board/**").authenticated()
		 * .antMatchers("/member/**").permitAll();
		 * 
		 * 
		 * 
		 * System.out.println("�α��� ����2"); http .authorizeRequests()
		 * .antMatchers("/member/**").permitAll()
		 * .antMatchers("/oauth/token").permitAll() .anyRequest().authenticated() .and()
		 * .formLogin() .loginPage("/member/login") .defaultSuccessUrl("/board/list")
		 * .and() .httpBasic(); //.and().csrf().disable();
		 * 
		 * 
		 * 
		 * http. anonymous().disable() .requestMatchers().antMatchers("/board/**")
		 * .and().authorizeRequests()
		 * .antMatchers("/board/**").access("hasRole('ADMIN')") .and() .formLogin()
		 * .loginPage("/member/login") .defaultSuccessUrl("/board/list")
		 * .and().exceptionHandling().accessDeniedHandler(new
		 * OAuth2AccessDeniedHandler()) .and().csrf().disable();
		 */
		  
		  // ȸ�����Խ� csrf ��ū �̻��
		  //http.csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/member/getMember/**")).disable();
		  
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
				//.successForwardUrl("/member/test")
				.defaultSuccessUrl("/board/list", true)
				.successHandler(securitySuccessHandler)
				.permitAll()
			.and()
				.logout()
				.permitAll()
			.and().exceptionHandling()
			.and().csrf().disable();
		  
	  }

	 
}
