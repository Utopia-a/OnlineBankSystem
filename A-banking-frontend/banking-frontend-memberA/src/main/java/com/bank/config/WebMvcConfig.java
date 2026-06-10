package com.bank.config;

import com.bank.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Spring MVC 全局配置
 * - CORS 跨域配置
 * - 拦截器注册
 * - 静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                // 拦截所有 API 请求
                .addPathPatterns("/api/**")
                // 放行登录、注册、验证码接口
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/auth/otp/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        // Swagger UI 资源放行
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径跳转到登录页
        registry.addRedirectViewController("/", "/login");
    }
}
