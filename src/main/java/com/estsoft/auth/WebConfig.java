package com.estsoft.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.estsoft.util.HTMLCharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web API ��� �� �ʿ� ��� ����
 * @author JSB
 * 
 * 1. Access Token Check Interceptor 
 * 2. XSS ���� Filter
 * 3. ÷������ �ܺ� ���ε� ��� ����
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private HandlerInterceptor interceptor;
	
	@Bean
    public HttpMessageConverter escapingConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());

        MappingJackson2HttpMessageConverter escapingConverter =
                new MappingJackson2HttpMessageConverter();
        escapingConverter.setObjectMapper(objectMapper);

        return escapingConverter;
    }

	/**
	 * �Խ��� ���� �� Access Token Ȯ��
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(interceptor)
				.addPathPatterns("/board/**", "/reply/**")
				.excludePathPatterns("/member/**", "/oauth/**", "/css/**", "/img/**", "/js/**", "/vendor/**");
		
	}
	
	/**
	 * XSS ���� Filter
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		
		converters.add(escapingConverter());
	}
	
	/**
	 * ÷������ �ܺ� ���ε� ��� ����
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		registry.addResourceHandler("/upload/**").addResourceLocations("file:///C:/upload/");
		
	}

	
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {}

	@Override
	public void addFormatters(FormatterRegistry registry) {}

	@Override
	public void addCorsMappings(CorsRegistry registry) {}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {}

	
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {}

	@Override
	public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {}

	@Override
	public Validator getValidator() {
		return null;
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return null;
	}

}
