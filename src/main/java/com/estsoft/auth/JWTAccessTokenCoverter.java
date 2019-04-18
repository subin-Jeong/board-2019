package com.estsoft.auth;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

/**
 * OAuth2 Access Token Customize
 * @author JSB
 */
@PropertySource("classpath:/application.properties")
public class JWTAccessTokenCoverter extends JwtAccessTokenConverter {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Autowired
	private Environment environment;
		
	public static final String TOKEN_TYPE = "token_type";
	public static final String REFRESH_TOKEN_ID = "rti";

	private JsonParser objectMapper = JsonParserFactory.create();
	
	/**
	 * Access Token 초기 발급 시 발급된 Refresh Token 영속화 
	 * 1. Refresh Token 이 Access Token 의 Id 에 의존하지 않고 유지되도록 변경
	 * 2. Refresh Token 을 통해 Access Token 을 재발급 하는 경우 Refresh Token 의 만료기간을 단위기간만큼 더 증가
	 */
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		
		log.info("[New Access Token Value] " + accessToken.getValue());
		log.info("[New Access Token Expiration] " + accessToken.getExpiration());
		
		DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);
		
		Map<String, Object> info = new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());
		String tokenId = result.getValue();
		
		if (!info.containsKey(TOKEN_ID)) {
			
			info.put(TOKEN_ID, tokenId);
			
		} else {
			
			tokenId = (String) info.get(TOKEN_ID);
			
		}
		
		result.setAdditionalInformation(info);
		
		OAuth2RefreshToken refreshToken = result.getRefreshToken();
		
		if (refreshToken != null) {
			
			DefaultOAuth2AccessToken encodedRefreshToken = new DefaultOAuth2AccessToken(accessToken);
			encodedRefreshToken.setValue(refreshToken.getValue());
			
			// Refresh tokens do not expire unless explicitly of the right type
			encodedRefreshToken.setExpiration(null);
			try {
				
				Map<String, Object> claims = objectMapper.parseMap(JwtHelper.decode(refreshToken.getValue()).getClaims());
				
				if (claims.containsKey(TOKEN_ID)) {
					encodedRefreshToken.setValue(claims.get(TOKEN_ID).toString());
				}
				
			} catch (IllegalArgumentException e) {}
			
			Map<String, Object> refreshTokenInfo = new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());
			
			info.put(REFRESH_TOKEN_ID, encodedRefreshToken.getValue());
			result.setAdditionalInformation(info);
			refreshTokenInfo.put(TOKEN_ID, encodedRefreshToken.getValue());
			refreshTokenInfo.put(TOKEN_TYPE, "refresh_token");
			encodedRefreshToken.setAdditionalInformation(refreshTokenInfo);
			
			DefaultOAuth2RefreshToken token = new DefaultOAuth2RefreshToken(encode(encodedRefreshToken, authentication));
			if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
				
				// 기존 만료 시각
				Date preExpiration = ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration();
				
				// Refresh Token 의 만료기간을 단위기간만큼 증가
		        int refreshTokenValidity = Integer.parseInt(environment.getProperty("security.oauth2.client.refresh-token-validity-seconds"));
		        Calendar cal = Calendar.getInstance();
		        cal.setTime(preExpiration);
		        cal.add(Calendar.SECOND, refreshTokenValidity);
		        
		        // 새로운 만료 시각
		        Date expiration = cal.getTime();

				log.info("[REFRESH TOKEN PRE EXPIRE] " + preExpiration.toString());
				log.info("[REFRESH TOKEN NOW EXPIRE] " + expiration.toString());
				
				// Refresh Token 에 적용
				encodedRefreshToken.setExpiration(expiration);
				token = new DefaultExpiringOAuth2RefreshToken(encode(encodedRefreshToken, authentication), expiration);
				
			}
			
			result.setRefreshToken(token);
			result.setValue(encode(result, authentication));
			
		} else {
			
			result.setValue(encode(result, authentication));
			
		}
		
		return result;
	}

	public boolean isRefreshToken(OAuth2AccessToken token) {
		
		return token.getAdditionalInformation().containsKey(TOKEN_TYPE);
		
	}
}
