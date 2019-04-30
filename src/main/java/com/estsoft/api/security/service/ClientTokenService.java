package com.estsoft.api.security.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.estsoft.api.domain.Member;
import com.estsoft.api.repository.MemberRepository;
import com.estsoft.api.security.SecurityConfig;


/**
 * 토큰 발급을 위한 사용자 API 클래스
 * @author JSB
 */
@Service
@Component
public class ClientTokenService {
	
	// Log
	private Logger log = LoggerFactory.getLogger(ClientTokenService.class);
	
	// OAuth2 client_secret
	@Value("${security.oauth2.client.client-secret}")
	private String CLIENT_SECRET;
	
	@Autowired
	private MemberRepository memberRepository;
	
	// 인증 결과 정보
	private JSONObject authInfo;
	
	// 토큰 인증 정보
	private JSONObject tokenInfo;
    

	/**
	 * 로그인 정보를 통해 OAuth2 Token 발급
	 * @param LOGIN_INFO
	 * @return 토큰정보
	 */
	public JSONObject getOAuth2Token(final Map<String, String> LOGIN_INFO) {

		// 로그인 정보
		final String userName = LOGIN_INFO.get("username");
		final String password = LOGIN_INFO.get("password");
		
		// 회원 정보 조회
		final Member member = memberRepository.findByEmail(userName);
		
		// access_token 요청 파라미터
		final String CLIENT_ID = member.getClientId();
		final String AUTH_HOST = "http://" + CLIENT_ID + ":" + CLIENT_SECRET + "@localhost:8080";
		final String tokenRequestUrl = AUTH_HOST + "/oauth/token";
		
		log.info("[REQUEST ACCESS TOKEN URL] " + tokenRequestUrl);

		
		// access_token 반환 StringBuffer
		final StringBuffer buffer;
		
		// 토큰 값 확인을 위한 필수 파라미터
		final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		
		postParams.add(new BasicNameValuePair("username", userName));
		postParams.add(new BasicNameValuePair("password", password));
		postParams.add(new BasicNameValuePair("grant_type", "password"));

		final HttpPost post = new HttpPost(tokenRequestUrl);

	    try {
	    	
	    	buffer = httpClientBuild("POST", post, null, postParams);
	    	log.info(buffer.toString());
  
	    	// 결과 데이터를 JSON 으로 담기
	    	authInfo = new JSONObject(buffer.toString());

	    } catch (JSONException e) {
			e.printStackTrace();
		} 
	    
	    return authInfo;
		
	}
	
	/**
	 * JWT OAuth2 Token 인증
	 * @param JWTToken
	 * @return 토큰 인증 결과
	 */
	public JSONObject checkOAuth2Token(final String JWTToken) {

		// access_token 인증 URL
		final String AUTH_HOST = "http://localhost:8080";
	    final String tokenRequestUrl = AUTH_HOST + "/oauth/check_token";
	    final HttpGet get = new HttpGet(tokenRequestUrl + "?token=" + JWTToken);
	    
	    log.info("[CHECK ACCESS TOKEN URL] " + tokenRequestUrl);
	    
	    // 인증 반환 StringBuffer
	    final StringBuffer buffer;
	    
	    try {
	    	
	    	buffer = httpClientBuild("GET", null, get, null);
	    	
	    	log.info(buffer.toString());
	      
	    	// 결과 데이터를 JSON 으로 담기
	    	tokenInfo = new JSONObject(buffer.toString());
	    	

	    } catch (JSONException e) {
	    	
			e.printStackTrace();
			
		}
	    
	    return tokenInfo;
		
	}
	
	/**
	 * refresh_token 을 이용한 OAuth2 access_token 재발급
	 * @param LOGIN_INFO
	 * @return 토큰정보
	 */
	public JSONObject refreshOAuth2Token(final String email) {

		// 회원 정보 조회
		final Member member = memberRepository.findByEmail(email);
	    
		// refresh_token 요청 파라미터
		final String CLIENT_ID = member.getClientId();
		final String AUTH_HOST = "http://" + CLIENT_ID + ":" + CLIENT_SECRET + "@localhost:8080";
		final String tokenRequestUrl = AUTH_HOST + "/oauth/token";
	    
		log.info("[REFRESH ACCESS TOKEN URL] " + tokenRequestUrl);
		
		// refresh_token 반환 StringBuffer
		final StringBuffer buffer;
	    
	    // 발급된 refresh_token 이 있는 경우
	    if(member.getRefreshToken() != null) {
	    	
	    	// 토큰 값 확인을 위한 필수 파라미터
	    	final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	    	
		    postParams.add(new BasicNameValuePair("username", member.getEmail()));
		    postParams.add(new BasicNameValuePair("password", member.getPassword()));
		    postParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
		    postParams.add(new BasicNameValuePair("refresh_token", member.getRefreshToken()));
		    
		    final HttpPost post = new HttpPost(tokenRequestUrl);

		    try {
		    	
		    	// OAuth2 서버로부터 토큰정보 확인
		    	buffer = httpClientBuild("POST", post, null, postParams);

		    	log.info(buffer.toString());
	  
		    	// 결과 데이터를 JSON 으로 담기
		    	authInfo = new JSONObject(buffer.toString());

		    } catch (JSONException e) {
		    	
				e.printStackTrace();
				
			}
		    
		    log.info("[REFRESH ACCESS TOKEN END]");
		    return authInfo;
			
	    }
	    
	    return new JSONObject();
	    
	}
	
	/**
	 * 사용자 Access Token Cookie 만들기
	 * @param authInfo
	 * @return Access Token Cookie
	 * @throws JSONException
	 */
	public Cookie makeAccessTokenCookie(JSONObject authInfo) throws JSONException {
		
		log.info("[MAKE ACCESS TOKEN COOKIE START]");
		
		// Access Token 확인 가능
		if(authInfo.has("access_token")) {
			
			Cookie cookie = new Cookie("access_token", authInfo.getString("access_token"));
			cookie.setMaxAge(Integer.parseInt(authInfo.getString("expires_in")));
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			
			log.info("[MAKE ACCESS TOKEN COOKIE SUCCESS]");
			
			return cookie;
			
		} else {
		
			log.info("[MAKE ACCESS TOKEN COOKIE ERROR]");
			
			return new Cookie("error", "");
		}		
	}
	
	/**
	 * HttpClient 사용을 위한 메서드
	 * @param requestType
	 * @param post
	 * @param get
	 * @param params
	 * @return StringBuffer
	 */
	private StringBuffer httpClientBuild(String requestType, HttpPost post, HttpGet get, List<NameValuePair> params) {

	    final HttpClient client = HttpClientBuilder.create().build();

	    BufferedReader rd = null;
	    InputStreamReader isr = null;
	    HttpResponse response = null;
	    StringBuffer buffer = new StringBuffer();
	    
	    try {
	    	
	    	// POST 방식 요청
		    if(requestType.equals("POST")) {
		    	
		    	// 파라미터 설정
		    	post.setEntity(new UrlEncodedFormEntity(params));
		    	response = client.execute(post);

		    }
		    
		    // GET 방식 요청
		    if(requestType.equals("GET")) {
		    	response = client.execute(get);
		    }
		    
	    	isr = new InputStreamReader(response.getEntity().getContent());
	    	rd = new BufferedReader(isr);

	    	String line;
	    	while ((line = rd.readLine()) != null) {
	    		buffer.append(line);
	    	}

	    	return buffer;

	    } catch(UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } catch(ClientProtocolException e) {
	      e.printStackTrace();
	    } catch(IOException e) {
	      e.printStackTrace();
	    } finally {
	        // clear resources
	        if (rd != null) {
	            try {
	                rd.close();
	            } catch(Exception ignore) {}
	        }
	        if (isr != null) {
	            try {
	                isr.close();
	            } catch(Exception ignore) {}
	         }
	    }
	    
		return buffer;
	}
	
}
