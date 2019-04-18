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
	
	// ���� ��� ����
	private JSONObject AUTH_INFO;
	
	// �α��ε� ����� ����
	private final Map<String, String> LOGIN_INFO = new HashMap<String, String>();
	
	
	// �α��� ���� Handler
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		
		LOGIN_INFO.put("username", request.getParameter("username"));
		LOGIN_INFO.put("password", request.getParameter("password"));
		
		// Oauth2 �� ���� JWT Token ȹ��
		AUTH_INFO = clientTokenService.getOAuth2Token(LOGIN_INFO);
		
		try {
			
			log.info("access_token : " + AUTH_INFO.getString("access_token"));
			log.info("token_type : " + AUTH_INFO.getString("token_type"));
			log.info("refresh_token : " + AUTH_INFO.getString("refresh_token"));
			log.info("expires_in : " + AUTH_INFO.getString("expires_in"));
			log.info("scope : " + AUTH_INFO.getString("scope"));
			
			if(AUTH_INFO.getString("access_token") != null) {
				
				// Token ��ȿ
				// ��Ű�� ��ū������ �߰�
				Cookie addCookie = new Cookie("access_token", AUTH_INFO.getString("access_token"));
				addCookie.setMaxAge(Integer.parseInt(AUTH_INFO.getString("expires_in")));
				
				// ��Ű �� ���������� ���� ���
				addCookie.setHttpOnly(true);
				
				response.addCookie(addCookie);
				
				// ���� access_token ��߱��� ���� refresh_token ����
				if(request.getParameter("username") != null && AUTH_INFO.has("refresh_token")) {
					
					String email = request.getParameter("username");
					String refreshToken = AUTH_INFO.getString("refresh_token");
					
					memberRepository.updateRefreshTokenByEmail(email, refreshToken);
				
				}
				
				response.sendRedirect("/board/list");
				
			} else {
				
				// Token ����ȿ
				// �α��� ������ �����ϰ� �α��� ����ó��
				authentication.setAuthenticated(false);
				request.getSession(false);
				
				// exception �� ���� ���� �޽��� �б�ó�� �ʿ�
				response.sendRedirect("/member/login?error=1");
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// �α��� ���� Handler
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		
		// exception �� ���� ���� �޽��� �б�ó�� �ʿ�
		response.sendRedirect("/member/login?error=1");
		
	}

}
