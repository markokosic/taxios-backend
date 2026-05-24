package com.markokosic.minicrm.common.config.cors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
	@Bean
	public CorsConfigurationSource corsConfigurationSource(){
		CorsConfiguration config = new CorsConfiguration();
//		TODO change URL
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(List.of(
				"http://localhost:[*]",
				"http://127.0.0.1:[*]",
				"https://" + "taxi.mk0.me"
		));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET","POST","PUT", "PATCH", "DELETE", "OPTIONS"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}