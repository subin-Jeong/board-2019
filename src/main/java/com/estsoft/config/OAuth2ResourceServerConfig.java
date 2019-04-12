package com.estsoft.config;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;


/**
 * 발급된 OAuth2 JWT Token 을 확인하기 위한 서버
 */
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId(clientId).authenticationManager(authenticationManagerBean())
				.tokenExtractor(new TokenExtractor() {
					
					@Override
					public Authentication extract(HttpServletRequest request) {
						Enumeration<String> headers = request.getHeaders("Authorization");
						while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
							String value = headers.nextElement();
							if ((value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase()))) {
								String authHeaderValue = value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
								// Add this here for the auth details later. Would be better to change the
								// signature of this method.
								request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE,
										value.substring(0, OAuth2AccessToken.BEARER_TYPE.length()).trim());
								int commaIndex = authHeaderValue.indexOf(',');
								if (commaIndex > 0) {
									authHeaderValue = authHeaderValue.substring(0, commaIndex);
								}
								return new PreAuthenticatedAuthenticationToken(authHeaderValue, "");
							}
						}

						return null;
					}
				});
	}

	@Bean
	public ResourceServerTokenServices tokenService() {
		ResourceTokenService tokenServices = new ResourceTokenService();
		return tokenServices;
	}
	
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager();
		authenticationManager.setTokenServices(tokenService());
		return authenticationManager;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		
		/*
		System.out.println("로그인 시작??????");
		http
			.authorizeRequests()
			.antMatchers("/board/**").authenticated()
			.antMatchers("/member/**").permitAll();
		*/
		
		/*
    	System.out.println("로그인 시작2");
		http
		.authorizeRequests()
		.antMatchers("/**").permitAll()
		.antMatchers("/member/**").permitAll()
		.anyRequest().authenticated()
		.and()
		.formLogin()
		.loginPage("/member/login")
		.usernameParameter("email")
		.defaultSuccessUrl("/board/list")
		.and()
		.httpBasic();
		//.and().csrf().disable();
		*/
		
		// Spring Security 를 사용하고 있기 때문에 Header 부분이 충돌 나서 화면이 보이지 않는 경우
		http.headers().frameOptions().disable();
		
		http
		.authorizeRequests()
		.antMatchers("/member/**", "/oauth/**", "/css/**", "/img/**", "/js/**", "/vendor/**").permitAll()
		.anyRequest().authenticated()
		.and().exceptionHandling();
		//.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/member/login"));
	
	}
}
