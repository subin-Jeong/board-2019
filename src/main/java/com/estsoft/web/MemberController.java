package com.estsoft.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.domain.api.Member;
import com.estsoft.domain.oauth.OAuthClientDetails;
import com.estsoft.repository.api.MemberRepository;
import com.estsoft.repository.oauth.OAuthClientRepository;
import com.estsoft.service.ClientTokenService;
import com.estsoft.util.ApiUtils;

@Controller
@RequestMapping("/member")
@Transactional
@PropertySource("classpath:/application.properties")
public class MemberController {

	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private OAuthClientRepository oAuthClientRepository;
	
	@Autowired
	private ClientTokenService clientTokenService;
	
	@Autowired
	private Environment environment;
	
	/**
	 * 회원가입 페이지
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/register")
	public String register() {
		return "member/register";
	}
	
	
	/**
	 * 로그인 페이지
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String errorType, Model model) {
		
		// 로그인 에러 시
		if(ApiUtils.isNotNullString(errorType)) {
			switch(errorType) {
				
				// 로그인 실패
				case "1" : 
					model.addAttribute("errorMsg", environment.getProperty("security.error.message.type1"));
					break;
					
				// 토큰 인증 실패
				case "2" :
					model.addAttribute("errorMsg", environment.getProperty("security.error.message.type2"));
					break;
					
				// 로그인 실패 메시지로 반환	
				default : model.addAttribute("errorMsg", environment.getProperty("security.error.message.type1"));
			
			}
		}
		
		return "member/login";
	}
	
	/**
	 * 회원가입
	 * @param member
	 * @return 등록된 Member Entity
	 */
	@PostMapping("/register")
	@ResponseBody
	public Member register(@RequestBody Member member) {
		
		member.setPassword(new BCryptPasswordEncoder().encode(member.getPassword()));
		
		// OAuth2 Server Client 등록
		OAuthClientDetails oauthClient = setOAuth2Client();
		
		if(oauthClient != null) {
		
			// 회원 테이블에 연관 client_id 저장
			
			member.setClientId(oauthClient.getClientId());
			return memberRepository.save(member);
			
		} else {
			
			return new Member();
		}
		
	}
	
	/**
	 * 이메일 중복확인
	 * @param email
	 * @return 이메일로 확인된 회원정보
	 */
	@PostMapping("/email")
	@ResponseBody
	public int checkEmail(@RequestBody Map<String, String> data) {
		
		String email = data.get("email");
		
		if(ApiUtils.isNotNullString(email)) {
			return memberRepository.countByEmailIgnoreCase(email);
		} else {
			return 1;
		}
		
	}
	
	
	/**
	 * 회원 이메일, 이름 정보 가져오기
	 * @return Map<이메일, 이름>
	 */
	@GetMapping("/list")
	@ResponseBody
	public List<Map<String, String>> list() {
		return memberRepository.findAllEmailAndName();
	}
	
	
	/**
	 * 수동 Access Token 갱신 (refresh_token 이용)
	 * @param principal
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	@PostMapping("/refresh")
	@ResponseBody
	public String refresh(Principal principal, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
		
		String email = principal.getName();
		if(ApiUtils.isNotNullString(email)) {
			
			JSONObject authInfo = clientTokenService.refreshOAuth2Token(email);
			if(authInfo.has("access_token")) {
				
				// Token 유효
				// 기존 access_token 쿠키를 비유효 처리 후, 새로 부여
				Cookie[] oldCookies = request.getCookies();
				for(Cookie cookie : oldCookies) {
					
					if(cookie.getName().equals("access_token")) {
						
						Cookie removeCookie = new Cookie(cookie.getName(), null);
						removeCookie.setPath("/");
						removeCookie.setMaxAge(0);
						response.addCookie(removeCookie);
					}
					
				}
				
				// 새로운 쿠키 발급
				Cookie addCookie = clientTokenService.makeAccessTokenCookie(authInfo);
				response.addCookie(addCookie);
				
				// 추후 access_token 재발급을 위해 refresh_token 저장
				if(authInfo.has("refresh_token")) {
					
					String refreshToken = authInfo.getString("refresh_token");
					memberRepository.updateRefreshTokenByEmail(email, refreshToken);
				
				}
				
				// refresh_token 으로 새로운 access_token 발급 성공
				return HttpStatus.OK.toString();
				
			} else if(authInfo.has("error")) {
				if(authInfo.getString("error_description").contains("expired")) {
					
					// refresh_token 만료, access_token 재발급 불가
					return HttpStatus.NO_CONTENT.toString();
					
				}
			}
			return HttpStatus.BAD_REQUEST.toString();
		}
		return HttpStatus.UNAUTHORIZED.toString();
	}
	
	/**
	 * 로그아웃
	 * @param request
	 * @param response
	 * @return 로그인 페이지
	 */
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response, Principal principal) {
		
		String email = principal.getName();
		
		// refresh_token 초기화
		if(ApiUtils.isNotNullString(email)) {
			memberRepository.updateRefreshTokenByEmail(email, null);
		}
		
		// 로그인 세션 종료 및 토큰 삭제
		request.getSession(false);
		
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			
			for(Cookie cookie : cookies) {
				
				if(cookie.getName().equals("access_token")) {
					
					Cookie removeCookie = new Cookie(cookie.getName(), null);
					removeCookie.setPath("/");
					removeCookie.setMaxAge(0);
					response.addCookie(removeCookie);
				}
				
			}
			
		}
		
		return "/member/login";
	}
	
	/**
	 * OAuth2 Server Client 등록
	 * @return OAuthClientDetails
	 */
	private OAuthClientDetails setOAuth2Client() {
		
		OAuthClientDetails oauthClient = new OAuthClientDetails();

		// client_id : 새로 발급
		int newId = oAuthClientRepository.findMaxId() + 1;
		String clientId = "client_" + newId;
		String clientPassword = new BCryptPasswordEncoder().encode(environment.getProperty("security.oauth2.client.client-secret"));
		String scope = environment.getProperty("security.oauth2.client.scope");
		String grantType = environment.getProperty("security.oauth2.client.grant-type");
		int autoApprove = Integer.parseInt(environment.getProperty("security.oauth2.client.auto-approve-scopes"));
		int accessTokenValidity = Integer.parseInt(environment.getProperty("security.oauth2.client.access-token-validity-seconds"));
		int refreshTokenValidity = Integer.parseInt(environment.getProperty("security.oauth2.client.refresh-token-validity-seconds"));
				
		// 기본 설정
		oauthClient.setCreated(new Date());
		oauthClient.setClientId(clientId);
		oauthClient.setClientSecret(clientPassword);
		oauthClient.setScope(scope);
		oauthClient.setAuthorizedGrantTypes(grantType);
		oauthClient.setAutoapprove(autoApprove);
		oauthClient.setAccessTokenValidity(accessTokenValidity);
		oauthClient.setRefreshTokenValidity(refreshTokenValidity);
		
		return oAuthClientRepository.save(oauthClient);
		
	}
	
}
