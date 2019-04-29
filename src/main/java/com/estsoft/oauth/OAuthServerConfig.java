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
 * OAuth2 Access Token �߱��� ���� ���� ����
 * @author JSB
 * 
 * 1. Access Token : �߱� �� �Ź� ����ð��� ���� ����, client_id ���� 1���� �߱� ����
 * 2. Refresh Token : ù Access Token �߱� �� ������ ����ð��� ���� �� ���� ��ȿ, Access Token �� ��߱� �Ǵ��� ����ð��� �����ִٸ� ����
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
    	
    	// Token ���� DB ����
    	return new JdbcTokenStore(dataSource);
    	
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
    public PasswordEncoder oAuthPasswordEncoder() {
        return new BCryptPasswordEncoder(4);       
    }

    
    /**
     * OAuth2 Create Token
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	// tokenService �� ���� Ŀ���ҵ� access_token ����
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
        
        log.info("[OAUTH2 CLIENT LIST] " + jdbcClientDetailsService.listClientDetails().toString());
    }

    
}