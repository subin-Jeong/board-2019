package com.estsoft.api.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RootController {

	/**
	 * 페이지 접속
	 * @return 리다이렉트 될 뷰 페이지
	 */
	@GetMapping("/")
	public String main() {
		return "/board/list";
	}
	
}
