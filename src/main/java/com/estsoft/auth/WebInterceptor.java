package com.estsoft.auth;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.estsoft.domain.oauth.CustomUserDetails;
import com.estsoft.repository.api.MemberRepository;
import com.estsoft.service.ClientTokenService;

/**
 * �Խ��� ���� �̿� �� ���� ���� Ȯ���� ���� ���ͼ��� ���
 * @author JSB
 */
@Component
public class WebInterceptor implements HandlerInterceptor {

	// Log
	private Logger log = LoggerFactory.getLogger(SecurityConfig.class);
		
	@Autowired
	private ClientTokenService clientTokenService;
	
	@Autowired
	private MemberRepository memberRepository;
	
	// ���� ��� ����
	private JSONObject TOKEN_INFO;
	
	// �α����� ȸ�� ����
	private CustomUserDetails principal;
	
		
	/**
	 * �Խ��� ���� �̿� �� ��� ��û�� ���� JWT Token Ȯ��
	 * @return ��ū Ȯ�� ���
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		// �α����� ȸ�� ����
		principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 
		
		String JWTToken = "";
		
		// ��Ű�� �ִ� JWT access_token ���� ���� ��ū Ȯ��
		Cookie[] cookies = request.getCookies();
		
		if(cookies != null) {
			
			for(Cookie cookie : cookies) {
				
				// ����Ⱓ�� �����ִ� ��ū�� ����Ͽ� ����
				if(cookie.getName().equals("access_token") && cookie.getMaxAge() != 0) {
					
					JWTToken = cookie.getValue();
					break;
						
				}
				
			}
			
		}

		log.info("token is " + JWTToken);
		
		if(principal != null) {
			
			// �α����� ȸ�� ���� ��ȸ
			String email = principal.getUsername();
			
			if(JWTToken != "") {
				
				// Oauth2 �� ���� JWT Token ȹ��
				TOKEN_INFO = clientTokenService.checkOAuth2Token(JWTToken);
					
				
			} else {
				
				// ��ū�� Ȯ���� �� ����
				request.getSession(false);
				
				// refresh_token �ʱ�ȭ
				memberRepository.updateRefreshTokenByEmail(email, null);
				
				response.sendRedirect("/member/login?error=3");
				return true;
				
			}
		}

		// ��ū Ȯ�� ���������� �Ϸ�
		if(TOKEN_INFO != null) {
			
			if(TOKEN_INFO.has("user_name")) {
				
				log.info("login username : " + TOKEN_INFO.getString("user_name"));
				principal.setExpireTime(Long.parseLong(TOKEN_INFO.getString("exp")));
				
				// ���� �α���
				return true;
				
			} else if(TOKEN_INFO.has("error")){
				
				log.info("token expired : " +  TOKEN_INFO.getString("error"));
				
				// ��ū ����
				request.getSession(false);
				
				if(principal != null) {
					
					String email = principal.getUsername();

					// refresh_token �ʱ�ȭ
					memberRepository.updateRefreshTokenByEmail(email, null);
				}
				
				response.sendRedirect("/member/login?error=3");
				return true;
			}
		}
			
		
		return false;
		
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		
		if(principal != null) {

			String email = principal.getUsername();
			Date expireTime = new Date(principal.getExpireTime() * 1000);
			
			
			// �α��ε� ���� ���� ���
			if(email != null && expireTime != null && modelAndView != null) {	
				
				String name = memberRepository.findNameByEmail(email);
				
				modelAndView.addObject("userName", name);
				modelAndView.addObject("expireTime", new SimpleDateFormat("HH:mm:ss").format(expireTime));
				
			}
			
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}

}
