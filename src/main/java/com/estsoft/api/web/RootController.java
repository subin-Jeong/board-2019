package com.estsoft.api.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RootController {

	/**
	 * ������ ����
	 * @return �����̷�Ʈ �� �� ������
	 */
	@GetMapping("/")
	public String main() {
		return "/board/list";
	}
	
}
