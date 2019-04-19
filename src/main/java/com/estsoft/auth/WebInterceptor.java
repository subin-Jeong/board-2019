package com.estsoft.auth;

import java.io.PrintWriter;
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
	private JSONObject tokenInfo;
	
	// �α����� ȸ�� ����
	private CustomUserDetails principal;
	
		
	/**
	 * �Խ��� ���� �̿� �� ��� ��û�� ���� Access Token Ȯ��
	 * @return ��ū Ȯ�� ���
	 * 
	 * 1. ��Ű�� ������� ���� Access Token �� �����ϴ��� Ȯ��
	 * 2. ��Ű���� Access Token �� Ȯ�ε��� �ʰų�, Access Token �� ����� ��� Access Token ��߱�
	 * 3-1. Access Token �� �ִٸ� Oauth2 �������� ��ū ��ȿ�� Ȯ��
	 * 3-2. Access Token �� ���ٸ� �α׾ƿ� ó��
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// Access Token ��߱� �ʿ� ����
		boolean isNecessaryToRefresh = false;
		
		// �α����� ȸ�� ����
		principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 

		// �α����� ȸ�� ���� ��ȸ
		String email = "";
		if(principal != null) {			
			email = principal.getUsername();
			log.info("[CHECK LOGIN INFO][SUCCESS] " + email);
		}
		
		// ��Ű�� �ִ� access_token ���� ���� ��ū Ȯ��
		String accessToken = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				
				// ����Ⱓ�� �����ִ� ��ū�� ����Ͽ� ����
				if(cookie.getName().equals("access_token") && cookie.getMaxAge() != 0) {
					accessToken = cookie.getValue();
					break;
				}
				
			}
		}
		
		log.info("[CHECK ACCESS TOKEN][SUCCESS] " + accessToken);
		
		// ��Ű���� Access Token Ȯ��
		if(!accessToken.equals("")) {
			
			// Oauth2 �� ���� Access Token ȹ��
			tokenInfo = clientTokenService.checkOAuth2Token(accessToken);
			
		} else if(!email.equals("")) {
			
			// �α��� ������ ������ ��ū�� Ȯ���� �� ����
			// Access Token �� ��Ű���� ����Ǿ��ų� Ȯ�� �Ұ����� ���
			log.info("[CHECK ACCESS TOKEN][ERROR] no access token cookie");

			// Refresh Token �� �̿��� Access Token ��߱� �õ�
			isNecessaryToRefresh = true;
			tokenInfo = null;
			
		}

		// ������ ��ū ���� Ȯ��
		if(tokenInfo != null) {
			if(tokenInfo.has("error") && tokenInfo.has("error_description")) {
				
				// Access Token ����
				// Refresh Token �� �̿��� Access Token ��߱� �õ�
				if(tokenInfo.getString("error_description").contains("expire")) {
					
					log.info("[CHECK ACCESS TOKEN][ERROR] token is expired");
					
					isNecessaryToRefresh = true;
					tokenInfo = null;
				}
			}
		}

		// Access Token Refresh
		if(isNecessaryToRefresh) {
			
			// �α��� ������ �ִ� ��쿡�� refresh
			JSONObject authInfo = clientTokenService.refreshOAuth2Token(email);
			
			// ��߱� ����
			if(authInfo.has("access_token")) {
				
				log.info("[REFRESH ACCESS TOKEN][SUCCESS]" + authInfo.getString("access_token"));
				
				// ��ū Ȯ���� ���� tokenInfo �� ����
				tokenInfo = clientTokenService.checkOAuth2Token(authInfo.getString("access_token"));
				
				// ��Ű�� ��ū������ �߰�
				Cookie addCookie = clientTokenService.makeAccessTokenCookie(authInfo);
				response.addCookie(addCookie);
				
				// ���� access_token ��߱��� ���� refresh_token ����
				if(authInfo.has("refresh_token")) {
					memberRepository.updateRefreshTokenByEmail(email, authInfo.getString("refresh_token"));
				}
				
				// ����ڿ��� �˸�
				response.setContentType("text/html; charset=UTF-8");
				PrintWriter out = response.getWriter();
				out.println("<script> alert('���� �ð��� ����Ǿ� �ڵ� �ð� ���� �Ǿ����ϴ�.'); </script>");
				out.flush();
				
			} else {
				log.info("[REFRESH ACCESS TOKEN][FAIL]");
				tokenInfo = null;
			}
		}
		
		// ��ū Ȯ�� ���������� �Ϸ�
		if(tokenInfo != null) {
			if(tokenInfo.has("user_name") && tokenInfo.has("exp")) {
				principal.setExpireTime(Long.parseLong(tokenInfo.getString("exp")));
				
				// ���� �α���
				return true;	
			}
		}
		
		// �α׾ƿ� ó��
		// 1. ��ȿ�� Access Token �� �α��������ε�, ��Ű������ ã�� �� ����
		// 2. Refresh Token �� ����Ǿ��ų� ����ȿ�� ��ū
		request.getSession(false);
		
		// refresh_token �ʱ�ȭ
		if(!email.equals("")) {
			memberRepository.updateRefreshTokenByEmail(email, null);
		}
		
		// ����ڿ��� �˸�
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<script> "
				  + "	alert('���� �ð��� ����Ǿ����ϴ�. ��α��� �Ͻñ� �ٶ��ϴ�.'); "
				  + "	location.href = '/member/login'; "
				  + "</script>");
		out.flush();
		
		return true;
	
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		
		// �α����� ���� ����
		String email = "";
		Date expireTime = null;
		if(principal != null) {
			
			email = principal.getUsername();
			
			// Access Token Expiration
			expireTime = new Date(principal.getExpireTime() * 1000);
		}
		
		if(!email.equals("")) {

			// �α��ε� ���� ���� ���
			if(expireTime != null && modelAndView != null) {	
				
				String name = memberRepository.findNameByEmail(email);
				
				modelAndView.addObject("userName", name);
				modelAndView.addObject("expireTime", new SimpleDateFormat("HH:mm:ss").format(expireTime));
				
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}

}
