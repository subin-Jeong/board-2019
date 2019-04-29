package com.estsoft.oauth;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;


/**
 * OAuth2 Access Token 발급을 위한 권한 서버
 * @author JSB
 * 
 * 1. Access Token : 발급 시 매번 만료시각이 새로 갱신, client_id 마다 1개씩 발급 가능
 * 2. Refresh Token : 첫 Access Token 발급 시 생성된 만료시각이 끝날 때 까지 유효, Access Token 이 재발급 되더라도 만료시각이 남아있다면 재사용
 */
@Configuration
@EnableAuthorizationServer
public class OAuthServerConfig extends AuthorizationServerConfigurerAdapter {

	// Log
	private Logger log = LoggerFactory.getLogger(OAuthServerConfig.class);
	
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    
    @Bean
    public TokenStore tokenStore() {
    	
    	// Token 정보 DB 관리
    	return new JdbcTokenStore(dataSource);
    	
    }
    
    @Bean
    @Primary
    public TokenServices tokenServices() {
    	
    	// tokenStore 내에서 해당 토큰이 유효한지 확인
    	TokenServices tokenServices = new TokenServices();
    	tokenServices.setTokenStore(tokenStore());
    	tokenServices.setSupportRefreshToken(true);
    	
        return tokenServices;
    }
    
    @Bean
    public PasswordEncoder oAuthPasswordEncoder() {
        return new BCryptPasswordEncoder(4);       
    }

    
    /**
     * OAuth2 Create Token
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	// tokenService 를 통해 커스텀된 access_token 생성
    	endpoints.tokenServices(tokenServices()).tokenStore(tokenStore()).authenticationManager(authenticationManager); 
    }
    
    /**
     * OAuth2 Access Permit
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    	
    	// 서버 내에서 /oauth/check_token 으로 발급된 토큰 확인이 가능
    	// client_secret BCrypt 인코딩
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()").passwordEncoder(oAuthPasswordEncoder());
        
    }


    /**
     * OAuth2 Client
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	
    	JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        clients.withClientDetails(jdbcClientDetailsService);
        
        log.info("[OAUTH2 CLIENT LIST] " + jdbcClientDetailsService.listClientDetails().toString());
    }

    
}