package com.estsoft.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * WhiteLabel Error Page
 * @author JSB
 */
@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

	@RequestMapping("/error")
	public String handleError() {
		return "error";
	}
	
	@Override
	public String getErrorPath() {
		return "/error";
	}
	
}
