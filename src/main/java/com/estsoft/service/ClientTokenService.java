package com.estsoft.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * 토큰 발급을 위한 사용자 API 클래스
 */
@Service
@Component
public class ClientTokenService {
	
	@Value("${security.oauth2.client.client-id}")
	private String CLIENT_ID;
	
	@Value("${security.oauth2.client.client-secret}")
	private String CLIENT_SECRET;
	
	private String REDIRECT_URI = "http://localhost:8080/oauth/token";
	private String code;
	
	// 인증 결과 정보
	private JSONObject AUTH_INFO;
    

	public JSONObject getOAuth2Token(final Map<String, String> LOGIN_INFO) {

		System.out.println("실행");
		
		final String AUTH_HOST = "http://" + CLIENT_ID + ":" + CLIENT_SECRET + "@localhost:8080";
	    final String tokenRequestUrl = AUTH_HOST + "/oauth/token";
	    final String userName = LOGIN_INFO.get("username");
	    final String password = LOGIN_INFO.get("password");

	    final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	    
	    
	    //postParams.add(new BasicNameValuePair("client_id", CLIENT_ID));
	    //postParams.add(new BasicNameValuePair("client_sercret", CLIENT_SECRET));
	    //postParams.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
	    //postParams.add(new BasicNameValuePair("code", code));
	    postParams.add(new BasicNameValuePair("username", userName));
	    postParams.add(new BasicNameValuePair("password", password));
	    postParams.add(new BasicNameValuePair("grant_type", "password"));
	    

	    final HttpClient client = HttpClientBuilder.create().build();
	    final HttpPost post = new HttpPost(tokenRequestUrl);

	    BufferedReader rd = null;
	    InputStreamReader isr = null;
	    try {
	      post.setEntity(new UrlEncodedFormEntity(postParams));

	      final HttpResponse response = client.execute(post);

	      final int responseCode = response.getStatusLine().getStatusCode();

	      System.out.println("\nSending 'POST' request to URL : " + tokenRequestUrl);
	      System.out.println("Post parameters : " + postParams);
	      System.out.println("Response Code : " + responseCode);

	      isr = new InputStreamReader(response.getEntity().getContent());
	      rd = new BufferedReader(isr);

	      final StringBuffer buffer = new StringBuffer();
	      String line;
	      while ((line = rd.readLine()) != null) {
	        buffer.append(line);
	      }

	      System.out.println(buffer.toString());
	      
	      // 결과 데이터를 JSON 으로 담기
	      AUTH_INFO = new JSONObject(buffer.toString());
		  System.out.println("result after JSON parse");
		  System.out.println(AUTH_INFO.getString("access_token"));

	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    } catch (ClientProtocolException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } catch (JSONException e) {
			e.printStackTrace();
		} finally {
	        // clear resources
	        if (rd != null) {
	            try {
	                rd.close();
	            } catch(Exception ignore) {
	            }
	        }
	        if (isr != null) {
	            try {
	                isr.close();
	            } catch(Exception ignore) {
	            }
	         }
	    }
	    
	    return AUTH_INFO;
		
	}
}
