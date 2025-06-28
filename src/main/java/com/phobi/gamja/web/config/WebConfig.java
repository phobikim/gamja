package com.phobi.gamja.web.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final UserAgentInterceptor userAgentInterceptor;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 또는 .allowedOrigins("https://phobi.me") 식으로 제한 가능
                .allowedHeaders("Authorization", "X-Requested-With", "Content-Type")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowCredentials(true); // 세션 쿠키 같이 보내려면 이거 꼭 필요!
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAgentInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/login",
                        "/api/signup",
                        "/api/admin/**",
                        "/api/session-check",
                        "/api/maintenance/**"
                );
    }
}
