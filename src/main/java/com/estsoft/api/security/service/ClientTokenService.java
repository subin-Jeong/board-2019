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
 * ��ū �߱��� ���� ����� API Ŭ����
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
	
	// ���� ��� ����
	private JSONObject authInfo;
	
	// ��ū ���� ����
	private JSONObject tokenInfo;
    

	/**
	 * �α��� ������ ���� OAuth2 Token �߱�
	 * @param LOGIN_INFO
	 * @return ��ū����
	 */
	public JSONObject getOAuth2Token(final Map<String, String> LOGIN_INFO) {

		// �α��� ����
		final String userName = LOGIN_INFO.get("username");
		final String password = LOGIN_INFO.get("password");
		
		// ȸ�� ���� ��ȸ
		final Member member = memberRepository.findByEmail(userName);
		
		// access_token ��û �Ķ����
		final String CLIENT_ID = member.getClientId();
		final String AUTH_HOST = "http://" + CLIENT_ID + ":" + CLIENT_SECRET + "@localhost:8080";
		final String tokenRequestUrl = AUTH_HOST + "/oauth/token";
		
		log.info("[REQUEST ACCESS TOKEN URL] " + tokenRequestUrl);

		
		// access_token ��ȯ StringBuffer
		final StringBuffer buffer;
		
		// ��ū �� Ȯ���� ���� �ʼ� �Ķ����
		final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		
		postParams.add(new BasicNameValuePair("username", userName));
		postParams.add(new BasicNameValuePair("password", password));
		postParams.add(new BasicNameValuePair("grant_type", "password"));

		final HttpPost post = new HttpPost(tokenRequestUrl);

	    try {
	    	
	    	buffer = httpClientBuild("POST", post, null, postParams);
	    	log.info(buffer.toString());
  
	    	// ��� �����͸� JSON ���� ���
	    	authInfo = new JSONObject(buffer.toString());

	    } catch (JSONException e) {
			e.printStackTrace();
		} 
	    
	    return authInfo;
		
	}
	
	/**
	 * JWT OAuth2 Token ����
	 * @param JWTToken
	 * @return ��ū ���� ���
	 */
	public JSONObject checkOAuth2Token(final String JWTToken) {

		// access_token ���� URL
		final String AUTH_HOST = "http://localhost:8080";
	    final String tokenRequestUrl = AUTH_HOST + "/oauth/check_token";
	    final HttpGet get = new HttpGet(tokenRequestUrl + "?token=" + JWTToken);
	    
	    log.info("[CHECK ACCESS TOKEN URL] " + tokenRequestUrl);
	    
	    // ���� ��ȯ StringBuffer
	    final StringBuffer buffer;
	    
	    try {
	    	
	    	buffer = httpClientBuild("GET", null, get, null);
	    	
	    	log.info(buffer.toString());
	      
	    	// ��� �����͸� JSON ���� ���
	    	tokenInfo = new JSONObject(buffer.toString());
	    	

	    } catch (JSONException e) {
	    	
			e.printStackTrace();
			
		}
	    
	    return tokenInfo;
		
	}
	
	/**
	 * refresh_token �� �̿��� OAuth2 access_token ��߱�
	 * @param LOGIN_INFO
	 * @return ��ū����
	 */
	public JSONObject refreshOAuth2Token(final String email) {

		// ȸ�� ���� ��ȸ
		final Member member = memberRepository.findByEmail(email);
	    
		// refresh_token ��û �Ķ����
		final String CLIENT_ID = member.getClientId();
		final String AUTH_HOST = "http://" + CLIENT_ID + ":" + CLIENT_SECRET + "@localhost:8080";
		final String tokenRequestUrl = AUTH_HOST + "/oauth/token";
	    
		log.info("[REFRESH ACCESS TOKEN URL] " + tokenRequestUrl);
		
		// refresh_token ��ȯ StringBuffer
		final StringBuffer buffer;
	    
	    // �߱޵� refresh_token �� �ִ� ���
	    if(member.getRefreshToken() != null) {
	    	
	    	// ��ū �� Ȯ���� ���� �ʼ� �Ķ����
	    	final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	    	
		    postParams.add(new BasicNameValuePair("username", member.getEmail()));
		    postParams.add(new BasicNameValuePair("password", member.getPassword()));
		    postParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
		    postParams.add(new BasicNameValuePair("refresh_token", member.getRefreshToken()));
		    
		    final HttpPost post = new HttpPost(tokenRequestUrl);

		    try {
		    	
		    	// OAuth2 �����κ��� ��ū���� Ȯ��
		    	buffer = httpClientBuild("POST", post, null, postParams);

		    	log.info(buffer.toString());
	  
		    	// ��� �����͸� JSON ���� ���
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
	 * ����� Access Token Cookie �����
	 * @param authInfo
	 * @return Access Token Cookie
	 * @throws JSONException
	 */
	public Cookie makeAccessTokenCookie(JSONObject authInfo) throws JSONException {
		
		log.info("[MAKE ACCESS TOKEN COOKIE START]");
		
		// Access Token Ȯ�� ����
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
	 * HttpClient ����� ���� �޼���
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
	    	
	    	// POST ��� ��û
		    if(requestType.equals("POST")) {
		    	
		    	// �Ķ���� ����
		    	post.setEntity(new UrlEncodedFormEntity(params));
		    	response = client.execute(post);

		    }
		    
		    // GET ��� ��û
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
