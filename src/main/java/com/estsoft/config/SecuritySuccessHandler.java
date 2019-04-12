package com.estsoft.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.estsoft.service.ClientTokenService;

@Component
public class SecuritySuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private ClientTokenService clientTokenService;
	
	// 인증 결과 정보
	private JSONObject AUTH_INFO;
	
	// 로그인된 사용자 정보
	private final Map<String, String> LOGIN_INFO = new HashMap<String, String>();
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		
		LOGIN_INFO.put("username", request.getParameter("username"));
		LOGIN_INFO.put("password", request.getParameter("password"));
		
		// Oauth2 를 통한 JWT Token 획득
		AUTH_INFO = clientTokenService.getOAuth2Token(LOGIN_INFO);
		
		try {
			
			System.out.println("access_token : " + AUTH_INFO.getString("access_token"));
			System.out.println("token_type : " + AUTH_INFO.getString("token_type"));
			System.out.println("refresh_token : " + AUTH_INFO.getString("refresh_token"));
			System.out.println("expires_in : " + AUTH_INFO.getString("expires_in"));
			System.out.println("scope : " + AUTH_INFO.getString("scope"));
			System.out.println("jti : " + AUTH_INFO.getString("jti"));
			
			// Token 유효
			request.setAttribute("access_token", AUTH_INFO.getString("access_token"));
			request.getRequestDispatcher("/board/list");
			
			response.setHeader("access_token", AUTH_INFO.getString("access_token"));
			response.addHeader("access_token", AUTH_INFO.getString("access_token"));
			response.sendRedirect("/board/list");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Token 비유효
		// 기존 로그인 처리를 비유효 설정
		// 이부분 주석 풀면 로그인 비유효 처리됨 분기 처리 필요
		//authentication.setAuthenticated(false);
		//request.getSession(false);
	}

}
