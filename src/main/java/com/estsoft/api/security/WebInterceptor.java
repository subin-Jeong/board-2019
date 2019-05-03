package com.estsoft.api.security;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

import com.estsoft.api.repository.MemberRepository;
import com.estsoft.api.security.service.ClientTokenService;
import com.estsoft.oauth.domain.CustomUserDetails;
import com.estsoft.util.ApiUtils;

/**
 * �Խ��� ���� �̿� �� ���� ���� Ȯ���� ���� ���ͼ��� ���
 * @author JSB
 */
@Component
public class WebInterceptor implements HandlerInterceptor {

	// Log
	private Logger log = LoggerFactory.getLogger(WebInterceptor.class);
		
	@Autowired
	private ClientTokenService clientTokenService;
	
	@Autowired
	private MemberRepository memberRepository;
	
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

		// ���� ��� ����
		JSONObject tokenInfo = null;
		
		// Access Token ��߱� �ʿ� ����
		boolean isNecessaryToRefresh = false;
		
		// Request method, path, header
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("method", request.getMethod());
		requestMap.put("path", request.getRequestURI());
		requestMap.put("header", request.getHeader("x-requested-with"));
		
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
		
		log.info("[REQUEST METHOD] " + requestMap.get("method"));
		log.info("[REQUEST PATH] " + requestMap.get("path"));
		log.info("[REQUEST HEADER] " + requestMap.get("header"));
		log.info("[CHECK ACCESS TOKEN][SUCCESS] " + accessToken);
		
		// ��Ű���� Access Token Ȯ��
		if(ApiUtils.isNotNullString(accessToken)) {
			
			// Oauth2 �� ���� Access Token ȹ��
			tokenInfo = clientTokenService.checkOAuth2Token(accessToken);
			
		} else if(ApiUtils.isNotNullString(email)) {
			
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
				// ������ ��ȯ �ÿ��� �˸�ó��
				if(checkAlert(requestMap)) {
					
					response.setContentType("text/html; charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.println("<script> alert('���� �ð��� ����Ǿ� �ڵ� �ð� ���� �Ǿ����ϴ�.'); </script>");
					out.flush();
					
				}
				
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
		if(ApiUtils.isNotNullString(email)) {
			memberRepository.updateRefreshTokenByEmail(email, null);
		}
		
		// ����ڿ��� �˸�
		// ������ ��ȯ�ÿ��� �˸� �޽����� �ѷ��ְ�, �ƴ� ��� �����޽����� ����
		if(checkAlert(requestMap)) {
			
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script> "
					  + "	alert('���� �ð��� ����Ǿ����ϴ�. ��α��� �Ͻñ� �ٶ��ϴ�.'); "
					  + "	location.href = '/member/login'; "
					  + "</script>");
			out.flush();
			
		} else {

			// ������ �������� �ٿ�ε�� ���
			String path = requestMap.get("path");
			if(ApiUtils.isNotNullString(path) && path.contains("/download")) {
				return true;
			}
			
			response.sendError(500);
		}
		
		return false;
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
		
		if(ApiUtils.isNotNullString(email)) {

			// �α��ε� ���� ���� ���
			if(expireTime != null && modelAndView != null) {	
				
				String name = memberRepository.findNameByEmail(email);

				modelAndView.addObject("email", email);
				modelAndView.addObject("userName", name);
				modelAndView.addObject("expireTime", new SimpleDateFormat("HH:mm:ss").format(expireTime));
				
			}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}

	
	/**
	 * ����� �˸��� ���� Request Ȯ��
	 * @param path
	 * @return boolean
	 */
	private boolean checkAlert(Map<String, String> requestMap) {

		String method = requestMap.get("method");
		String path = requestMap.get("path");
		String header = requestMap.get("header");
		
		// ������ ��ȯ�� �̷������ �ʴ� ���(ajax) ���� �޽����� ����
		if(ApiUtils.isNotNullString(method) && method.equals("GET")) {

			// ������ ��  ÷������ �ٿ�ε�� ���
			if(ApiUtils.isNotNullString(path) && path.contains("/download")) {
				return false;
			}
			
			// ����� �˸� ���� ���
			// ������ ��ȯ�� �̷������ �ʴ� ���(ajax) ���� �޽����� ����
			String ajaxHeaderValue = "XMLHttpRequest";
			if(ApiUtils.isNotNullString(header) && header.equals(ajaxHeaderValue)) {
				log.info("[EXCLUDE PATH] " + path);
				return false;
			}
			
		} else {
			return false;
		}
		
		return true;
	}
	
}
