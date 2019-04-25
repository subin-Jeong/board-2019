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
	
	// 로그인 성공 Handler
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		// 인증 결과 정보
		JSONObject authInfo;
		
		// 로그인된 사용자 정보
		final Map<String, String> loginInfo = new HashMap<String, String>();
		
		
		// Oauth2 를 통한 Access Token 획득
		loginInfo.put("username", request.getParameter("username"));
		loginInfo.put("password", request.getParameter("password"));
		
		authInfo = clientTokenService.getOAuth2Token(loginInfo);
		
		try {
			
			// Access Token 토큰 발급 성공
			if(authInfo.has("access_token")) {

				log.info("[TOKEN VALUE] access_token : " + authInfo.getString("access_token"));
				log.info("[TOKEN VALUE] token_type : " + authInfo.getString("token_type"));
				log.info("[TOKEN VALUE] refresh_token : " + authInfo.getString("refresh_token"));
				log.info("[TOKEN VALUE] expires_in : " + authInfo.getString("expires_in"));
				log.info("[TOKEN VALUE] scope : " + authInfo.getString("scope"));				
				
				// Token 유효
				// 쿠키에 토큰정보를 추가
				Cookie addCookie = clientTokenService.makeAccessTokenCookie(authInfo);
				response.addCookie(addCookie);
				
				// 추후 access_token 재발급을 위해 refresh_token 저장
				if(request.getParameter("username") != null && authInfo.has("refresh_token")) {
					
					String email = request.getParameter("username");
					String refreshToken = authInfo.getString("refresh_token");
					
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
