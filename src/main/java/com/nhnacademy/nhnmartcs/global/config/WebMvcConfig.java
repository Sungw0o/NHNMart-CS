package com.nhnacademy.nhnmartcs.global.config;

import com.nhnacademy.nhnmartcs.global.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * Registers the LoginCheckInterceptor to apply to all request paths while excluding the login page, error page, and static resource paths.
     *
     * @param registry the InterceptorRegistry used to add and configure the interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/cs/login",
                        "/error",
                        "/css/**",
                        "/js/**"
                );
    }
}