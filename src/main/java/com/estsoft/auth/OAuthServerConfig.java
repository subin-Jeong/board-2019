package com.estsoft.auth;

import java.util.Map;

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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


/**
 * OAuth2 JWT Token �߱��� ���� ���� ����
 */
@Configuration
@EnableAuthorizationServer
public class OAuthServerConfig extends AuthorizationServerConfigurerAdapter {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	
    @Autowired
    private AuthenticationManager authenticationManager;

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
    public TokenServices tokenServices() {
    	
    	// tokenStore ������ �ش� ��ū�� ��ȿ���� Ȯ��
    	TokenServices tokenServices = new TokenServices();
    	tokenServices.setTokenStore(tokenStore());
    	tokenServices.setSupportRefreshToken(true);
    	
        return tokenServices;
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
    	
    	//DefaultAccessTokenConverter act = new DefaultAccessTokenConverter();
    	//JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        
    	final JWTAccessTokenCoverter converter = new JWTAccessTokenCoverter();
    	converter.setSigningKey("estsoft-board");
        
        return converter;
        
    }

    @Bean
    public PasswordEncoder oAuthPasswordEncoder() {
        return new BCryptPasswordEncoder(4);       
    }

    
    /**
     * OAuth2 Create Token
     * ������ Token ������ DataSource �� �����ؼ� DB�� ���� ��, JWT ��ū���� ��ȯ�Ͽ� ���
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	//endpoints.tokenStore(tokenStore()).accessTokenConverter(accessTokenConverter()).authenticationManager(authenticationManager).userDetailsService(userDetailsServiceImpl);
    	//endpoints.tokenStore(tokenStore()).accessTokenConverter(accessTokenConverter()).authenticationManager(authenticationManager).userDetailsService(userDetailsServiceImpl);
    	endpoints.tokenServices(tokenServices()).tokenStore(tokenStore()).authenticationManager(authenticationManager);
    
    }
    
    /**
     * OAuth2 Access Permit
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    	
    	// ���� ������ /oauth/check_token ���� �߱޵� ��ū Ȯ���� ����
    	// client_secret BCrypt ���ڵ�
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
            // access_token ��ȿ�Ⱓ : 3��
            .accessTokenValiditySeconds(180)
            // refresh_token ��ȿ�Ⱓ : 10��
            .refreshTokenValiditySeconds(600);
		*/
    }

    
}