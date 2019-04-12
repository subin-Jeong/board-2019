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
 * 회원 로그인을 위한 Security 설정
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
		
		// /login 시 userDetailsServiceImpl 통해 DB에서 정보 조회하여 로그인되도록 지정
		auth.authenticationProvider(authenticationProvider());
		//auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(userPasswordEncoder());
	}
	
	@Override 
	public void configure(WebSecurity web) {
		
		// 정적 라이브러리에 대한 접근 권한 허용
		web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
		
	}

	/*
	 * 
	 * @Override public void configure(WebSecurity web) {
	 * web.ignoring().antMatchers("/css/**", "/img/**", "/js/**", "/vendor/**");
	 * 
	 * // 리뷰시 이부분 사용 // web.ignoring().antMatchers("/board/**", "/reply/**",
	 * "/member/**");
	 * 
	 * }
	 * 
	 * 
	 * @Override protected void configure(HttpSecurity http) throws Exception {
	 * 
	 * // client_id 인증 부분에서는 csrf를 사용하지 않음
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
		 * System.out.println("로그인 시작??????"); http .authorizeRequests()
		 * .antMatchers("/board/**").authenticated()
		 * .antMatchers("/member/**").permitAll();
		 * 
		 * 
		 * 
		 * System.out.println("로그인 시작2"); http .authorizeRequests()
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
		  
		  // 회원가입시 csrf 토큰 미사용
		  //http.csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/member/getMember/**")).disable();
		  
		  http
			.authorizeRequests()
				.antMatchers("/member/**", "/oauth/**", "/css/**", "/img/**", "/js/**", "/vendor/**").permitAll()
				// 회원가입 경로
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
