package com.estsoft.config;

import javax.sql.DataSource;

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
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


/**
 * OAuth2 JWT Token 발급을 위한 권한 서버
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	
	
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private UserDetailsService userDetailsServiceImpl;
    

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
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
    public PasswordEncoder userPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }


    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	System.out.println("1111");
        endpoints.tokenStore(tokenStore()).accessTokenConverter(accessTokenConverter()).authenticationManager(authenticationManager).userDetailsService(userDetailsServiceImpl);
    	
    }
    
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    	
    	// 서버 내에서 /oauth/check_token 으로 발급된 토큰 확인이 가능
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("permitAll()");
        
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /*
    	JdbcClientDetailsService jdbcClientDetailsService = new JdbcClientDetailsService(dataSource);
        
        System.out.println("this-->" + jdbcClientDetailsService.listClientDetails().toString());
        clients.withClientDetails(jdbcClientDetailsService);
        */
    	
    	System.out.println("Inside AuthorizationServerConfiguration" + clientId);
        clients.inMemory()
            .withClient(clientId)
            .secret(clientSecret)
            .autoApprove(true)
            .resourceIds(clientId)
            .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit", "client_credentials")
            .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_ADMIN")
            .scopes("read", "write", "trust")
            .accessTokenValiditySeconds(120) //Access token is only valid for 2 minutes.
            .refreshTokenValiditySeconds(600); //Refresh token is only valid for 10 minutes.

    }

    
}