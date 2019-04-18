package com.estsoft.auth;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


/**
 * OAuth2 JWT Token 발급을 위한 권한 서버
 */
@Configuration
@EnableAuthorizationServer
public class OAuthServerConfig extends AuthorizationServerConfigurerAdapter {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsServiceImpl;
    
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    
    @Bean
    public TokenStore tokenStore() {
    	
    	return new JdbcTokenStore(dataSource);
        //return new JwtTokenStore(accessTokenConverter());
    }
    
    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
    	
    	// tokenStore 내에서 해당 토큰이 유효한지 확인
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        
        return defaultTokenServices;
    }
    

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
    	
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("estsoft-board");
        return converter;
        
    }


    @Bean
    public PasswordEncoder oAuthPasswordEncoder() {
        return new BCryptPasswordEncoder(4);       
    }

    
    /**
     * OAuth2 Create Token
     * 생성된 Token 정보를 DataSource 에 접근해서 DB에 저장 후, JWT 토큰으로 변환하여 사용
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	endpoints.tokenStore(tokenStore()).accessTokenConverter(accessTokenConverter()).authenticationManager(authenticationManager).userDetailsService(userDetailsServiceImpl); 
    
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
        
        log.info("Resource Clients >>>" + jdbcClientDetailsService.listClientDetails().toString());
        
        /*
        clients.inMemory()
            .withClient(clientId)
            .secret(clientSecret)
            .autoApprove(true)
            .resourceIds(clientId)
            .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit", "client_credentials")
            .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_ADMIN")
            .scopes("read", "write", "trust")
            // access_token 유효기간 : 3분
            .accessTokenValiditySeconds(180)
            // refresh_token 유효기간 : 10분
            .refreshTokenValiditySeconds(600);
		*/
    }

    
}