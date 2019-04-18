package com.estsoft.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.estsoft.repository.api.MemberRepository;
import com.estsoft.service.ClientTokenService;

@Component
public class SecurityHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
			
	@Autowired
	private ClientTokenService clientTokenService;
	
	@Autowired
	private MemberRepository memberRepository;
	
	// 인증 결과 정보
	private JSONObject AUTH_INFO;
	
	// 로그인된 사용자 정보
	private final Map<String, String> LOGIN_INFO = new HashMap<String, String>();
	
	
	// 로그인 성공 Handler
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		
		LOGIN_INFO.put("username", request.getParameter("username"));
		LOGIN_INFO.put("password", request.getParameter("password"));
		
		// Oauth2 를 통한 JWT Token 획득
		AUTH_INFO = clientTokenService.getOAuth2Token(LOGIN_INFO);
		
		try {
			
			log.info("access_token : " + AUTH_INFO.getString("access_token"));
			log.info("token_type : " + AUTH_INFO.getString("token_type"));
			log.info("refresh_token : " + AUTH_INFO.getString("refresh_token"));
			log.info("expires_in : " + AUTH_INFO.getString("expires_in"));
			log.info("scope : " + AUTH_INFO.getString("scope"));
			
			if(AUTH_INFO.getString("access_token") != null) {
				
				// Token 유효
				// 쿠키에 토큰정보를 추가
				Cookie addCookie = new Cookie("access_token", AUTH_INFO.getString("access_token"));
				addCookie.setMaxAge(Integer.parseInt(AUTH_INFO.getString("expires_in")));
				
				// 쿠키 값 브라우저에서 접근 방어
				addCookie.setHttpOnly(true);
				
				response.addCookie(addCookie);
				
				// 추후 access_token 재발급을 위해 refresh_token 저장
				if(request.getParameter("username") != null && AUTH_INFO.has("refresh_token")) {
					
					String email = request.getParameter("username");
					String refreshToken = AUTH_INFO.getString("refresh_token");
					
					memberRepository.updateRefreshTokenByEmail(email, refreshToken);
				
				}
				
				response.sendRedirect("/board/list");
				
			} else {
				
				// Token 비유효
				// 로그인 세션을 종료하고 로그인 실패처리
				authentication.setAuthenticated(false);
				request.getSession(false);
				
				// exception 에 따른 실패 메시지 분기처리 필요
				response.sendRedirect("/member/login?error=1");
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 로그인 실패 Handler
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		
		// exception 에 따른 실패 메시지 분기처리 필요
		response.sendRedirect("/member/login?error=1");
		
	}

}
