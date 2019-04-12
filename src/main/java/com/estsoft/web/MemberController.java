package com.estsoft.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.estsoft.domain.Member;
import com.estsoft.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/member")
public class MemberController {

	@Autowired
	private MemberRepository memberRepository;
	
	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	
	
	@GetMapping("/test")
	public String test() {
		return "Hello";
	}
	
	
	/**
	 * ȸ������ ������
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/register")
	public String register() {
		return "member/register";
	}
	
	/**
	 * �α��� ������
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/login")
	public String login() {
		return "member/login";
	}
	
	/**
	 * ȸ������
	 * @param member
	 * @return ��ϵ� Member Entity
	 */
	@PostMapping("/register")
	@ResponseBody
	public Member register(@RequestBody Member member) {
		member.setPassword(new BCryptPasswordEncoder().encode(member.getPassword()));
		return memberRepository.save(member);
	}
	
	/**
	 * �α���
	 * @param member
	 * @return �α����� Member Entity
	 */
	@PostMapping("/login")
	@ResponseBody
	public Member login(@RequestBody Member member) {
		return memberRepository.findByEmail(member.getEmail());
	}
	
	/**
	 * ȸ�� �������� Ȯ��
	 * @param auth
	 * @return ȸ�� ���� ����
	 */
	@GetMapping("/getMember")
	@ResponseBody
	@PreAuthorize("#oauth2.hasAnyScope('read')")
	public String getOAuth2Principal(OAuth2Authentication auth) {
		return "Acess Granted For " + auth.getName();
	}
	
	/**
	 * ȸ�� ���� Ȯ��
	 * @param email
	 * @return �̸��Ϸ� Ȯ�ε� ȸ������
	 */
	@PostMapping("/getMember")
	@ResponseBody
	public int getMember(@RequestBody Map<String, String> data) {
		
		String email = data.get("email");
		
		if(email != null && email != "") {
			
			System.out.println("EMAIL : " + email);
			System.out.println(memberRepository.countByEmailIgnoreCase(email));
			return memberRepository.countByEmailIgnoreCase(email);
		} else {
			return 1;
		}
		
	}
	
	
	/**
	 * OAuth2 Server ���� JWT Token �ޱ�
	 * @param user
	 * @return ��ȯ�� ������
	 * @throws JsonProcessingException 
	 */
	@PostMapping("/authorize")
	public String authoize(@RequestBody Member member) throws JsonProcessingException {
		
		System.out.println(member.toString());
		
		
		Map<String, String> data = new HashMap<>();
		
		data.put("client_id", clientId);
		data.put("client_secret", clientSecret);
		data.put("response_type", "code");
		data.put("username", member.getEmail());
		data.put("password", member.getPassword());
		data.put("grant_type", "password");
		
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(data);
		
		String msg = sendREST("http://localhost:8080/oauth/token", json);
		System.out.println("------------->" + msg);
		return "/board/list";
	}
	
	
	/**
	 * REST API ���� (JSON POST REQUEST)
	 * @param sendUrl
	 * @param jsonValue
	 * @return
	 * @throws IllegalStateException
	 */
	public static String sendREST(String sendUrl, String jsonValue) throws IllegalStateException {
		
		String inputLine = null;
		StringBuffer outResult = new StringBuffer();

		try {
			  
			URL url = new URL(sendUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			//conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Type", "x-www-form-urlencoded");
			conn.setRequestProperty("Accept-Charset", "UTF-8"); 
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			  
			OutputStream os = conn.getOutputStream();
			os.write(jsonValue.getBytes("UTF-8"));
			os.flush();
			
			// ���ϵ� ��� �б�
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			
			while ((inputLine = in.readLine()) != null) {
				outResult.append(inputLine);
			}
			
			conn.disconnect();   
			
		} catch(Exception e) {
			
		      e.printStackTrace();
		      
		}	
		  
		return outResult.toString();
	
	}
}
