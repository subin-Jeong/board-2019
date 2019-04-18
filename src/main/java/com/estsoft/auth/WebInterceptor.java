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
 * 게시판 서비스 이용 시 접근 권한 확인을 위한 인터셉터 등록
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
	
	// 인증 결과 정보
	private JSONObject TOKEN_INFO;
	
	// 로그인한 회원 정보
	private CustomUserDetails principal;
	
		
	/**
	 * 게시판 서비스 이용 시 모든 요청에 대해 JWT Token 확인
	 * @return 토큰 확인 결과
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		// 로그인한 회원 정보
		principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 
		
		String JWTToken = "";
		
		// 쿠키에 있는 JWT access_token 값을 통해 토큰 확인
		Cookie[] cookies = request.getCookies();
		
		if(cookies != null) {
			
			for(Cookie cookie : cookies) {
				
				// 만료기간이 남아있는 토큰을 사용하여 인증
				if(cookie.getName().equals("access_token") && cookie.getMaxAge() != 0) {
					
					JWTToken = cookie.getValue();
					break;
						
				}
				
			}
			
		}

		log.info("token is " + JWTToken);
		
		if(principal != null) {
			
			// 로그인한 회원 정보 조회
			String email = principal.getUsername();
			
			if(JWTToken != "") {
				
				// Oauth2 를 통한 JWT Token 획득
				TOKEN_INFO = clientTokenService.checkOAuth2Token(JWTToken);
					
				
			} else {
				
				// 토큰을 확인할 수 없음
				request.getSession(false);
				
				// refresh_token 초기화
				memberRepository.updateRefreshTokenByEmail(email, null);
				
				response.sendRedirect("/member/login?error=3");
				return true;
				
			}
		}

		// 토큰 확인 정상적으로 완료
		if(TOKEN_INFO != null) {
			
			if(TOKEN_INFO.has("user_name")) {
				
				log.info("login username : " + TOKEN_INFO.getString("user_name"));
				principal.setExpireTime(Long.parseLong(TOKEN_INFO.getString("exp")));
				
				// 정상 로그인
				return true;
				
			} else if(TOKEN_INFO.has("error")){
				
				log.info("token expired : " +  TOKEN_INFO.getString("error"));
				
				// 토큰 만료
				request.getSession(false);
				
				if(principal != null) {
					
					String email = principal.getUsername();

					// refresh_token 초기화
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
			
			
			// 로그인된 유저 정보 담기
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
