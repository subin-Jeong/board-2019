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
 * 게시판 서비스 이용 시 접근 권한 확인을 위한 인터셉터 등록
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
	
	// 로그인한 회원 정보
	private CustomUserDetails principal;
	
	
	/**
	 * 게시판 서비스 이용 시 모든 요청에 대해 Access Token 확인
	 * @return 토큰 확인 결과
	 * 
	 * 1. 쿠키에 만료되지 않은 Access Token 이 존재하는지 확인
	 * 2. 쿠키에서 Access Token 이 확인되지 않거나, Access Token 이 만료된 경우 Access Token 재발급
	 * 3-1. Access Token 이 있다면 Oauth2 서버에서 토큰 유효성 확인
	 * 3-2. Access Token 이 없다면 로그아웃 처리
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// 인증 결과 정보
		JSONObject tokenInfo = null;
		
		// Access Token 재발급 필요 여부
		boolean isNecessaryToRefresh = false;
		
		// Request method, path, header
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("method", request.getMethod());
		requestMap.put("path", request.getRequestURI());
		requestMap.put("header", request.getHeader("x-requested-with"));
		
		// 로그인한 회원 정보
		principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 

		// 로그인한 회원 정보 조회
		String email = "";
		if(principal != null) {			
			email = principal.getUsername();
			log.info("[CHECK LOGIN INFO][SUCCESS] " + email);
		}
		
		// 쿠키에 있는 access_token 값을 통해 토큰 확인
		String accessToken = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				
				// 만료기간이 남아있는 토큰을 사용하여 인증
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
		
		// 쿠키에서 Access Token 확인
		if(ApiUtils.isNotNullString(accessToken)) {
			
			// Oauth2 를 통한 Access Token 획득
			tokenInfo = clientTokenService.checkOAuth2Token(accessToken);
			
		} else if(ApiUtils.isNotNullString(email)) {
			
			// 로그인 정보는 있으나 토큰을 확인할 수 없음
			// Access Token 이 쿠키에서 만료되었거나 확인 불가능한 경우
			log.info("[CHECK ACCESS TOKEN][ERROR] no access token cookie");

			// Refresh Token 을 이용한 Access Token 재발급 시도
			isNecessaryToRefresh = true;
			tokenInfo = null;
			
		}

		// 인증된 토큰 정보 확인
		if(tokenInfo != null) {
			if(tokenInfo.has("error") && tokenInfo.has("error_description")) {
				
				// Access Token 만료
				// Refresh Token 을 이용한 Access Token 재발급 시도
				if(tokenInfo.getString("error_description").contains("expire")) {
					
					log.info("[CHECK ACCESS TOKEN][ERROR] token is expired");
					
					isNecessaryToRefresh = true;
					tokenInfo = null;
				}
			}
		}

		// Access Token Refresh
		if(isNecessaryToRefresh) {
			
			// 로그인 정보가 있는 경우에만 refresh
			JSONObject authInfo = clientTokenService.refreshOAuth2Token(email);
			
			// 재발급 성공
			if(authInfo.has("access_token")) {
				
				log.info("[REFRESH ACCESS TOKEN][SUCCESS]" + authInfo.getString("access_token"));
				
				// 토큰 확인을 위해 tokenInfo 에 저장
				tokenInfo = clientTokenService.checkOAuth2Token(authInfo.getString("access_token"));
				
				// 쿠키에 토큰정보를 추가
				Cookie addCookie = clientTokenService.makeAccessTokenCookie(authInfo);
				response.addCookie(addCookie);
				
				// 추후 access_token 재발급을 위해 refresh_token 저장
				if(authInfo.has("refresh_token")) {
					memberRepository.updateRefreshTokenByEmail(email, authInfo.getString("refresh_token"));
				}
				
				
				// 사용자에게 알림
				// 페이지 전환 시에만 알림처리
				if(checkAlert(requestMap)) {
					
					response.setContentType("text/html; charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.println("<script> alert('인증 시간이 만료되어 자동 시간 연장 되었습니다.'); </script>");
					out.flush();
					
				}
				
			} else {
				log.info("[REFRESH ACCESS TOKEN][FAIL]");
				tokenInfo = null;
			}
		}
		
		// 토큰 확인 정상적으로 완료
		if(tokenInfo != null) {
			if(tokenInfo.has("user_name") && tokenInfo.has("exp")) {
				
				principal.setExpireTime(Long.parseLong(tokenInfo.getString("exp")));
				
				// 정상 로그인
				return true;	
			}
		}
		
		// 로그아웃 처리
		// 1. 유효한 Access Token 을 로그인정보로도, 쿠키에서도 찾을 수 없음
		// 2. Refresh Token 이 만료되었거나 비유효한 토큰
		request.getSession(false);
		
		// refresh_token 초기화
		if(ApiUtils.isNotNullString(email)) {
			memberRepository.updateRefreshTokenByEmail(email, null);
		}
		
		// 사용자에게 알림
		// 페이지 전환시에는 알림 메시지를 뿌려주고, 아닌 경우 에러메시지로 전달
		if(checkAlert(requestMap)) {
			
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.println("<script> "
					  + "	alert('인증 시간이 만료되었습니다. 재로그인 하시기 바랍니다.'); "
					  + "	location.href = '/member/login'; "
					  + "</script>");
			out.flush();
			
		} else {

			// 페이지 내에서의 첨부파일 다운로드는 허용
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
		
		// 로그인한 유저 정보
		String email = "";
		Date expireTime = null;
		if(principal != null) {
			
			email = principal.getUsername();
			
			// Access Token Expiration
			expireTime = new Date(principal.getExpireTime() * 1000);
		}
		
		if(ApiUtils.isNotNullString(email)) {

			// 로그인된 유저 정보 담기
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
	 * 사용자 알림을 위한 Request 확인
	 * @param path
	 * @return boolean
	 */
	private boolean checkAlert(Map<String, String> requestMap) {

		String method = requestMap.get("method");
		String path = requestMap.get("path");
		String header = requestMap.get("header");
		
		// 페이지 전환이 이루어지지 않는 경우(ajax) 에러 메시지로 전달
		if(ApiUtils.isNotNullString(method) && method.equals("GET")) {

			// 페이지 내  첨부파일 다운로드는 허용
			if(ApiUtils.isNotNullString(path) && path.contains("/download")) {
				return false;
			}
			
			// 사용자 알림 제외 경로
			// 페이지 전환이 이루어지지 않는 경우(ajax) 에러 메시지로 전달
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
