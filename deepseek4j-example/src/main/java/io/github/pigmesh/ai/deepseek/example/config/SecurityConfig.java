package io.github.pigmesh.ai.deepseek.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Spring Security 配合流式接口的示例配置。
 *
 * <p>
 * 流式响应一旦提交（committed），Servlet 容器无法再 forward 到错误页面。这里将认证失败的入口点 设置为返回 401 状态码，避免出现 "Unable
 * to handle the Spring Security Exception because the response is already committed"
 * 这类异常。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests()
			.antMatchers("/secure/**")
				.authenticated()
				.anyRequest()
				.permitAll()
			.and()
			.httpBasic()
			.and()
			.exceptionHandling()
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
		// @formatter:on
		return http.build();
	}

}
