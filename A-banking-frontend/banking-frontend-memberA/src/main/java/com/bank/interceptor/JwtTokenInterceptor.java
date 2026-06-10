package com.bank.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT Token 拦截器
 * 验证前端请求是否携带有效 Token（Token 实际验证由成员B的认证服务负责）
 * 本拦截器仅做基础检查，确保 Token 存在
 */
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("请求 [{}] 缺少有效的 Authorization Header", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                    {"code":401,"message":"未登录或Token已过期，请重新登录","data":null}
                    """);
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        log.debug("请求 [{}] 携带 Token，长度: {}", request.getRequestURI(), token.length());

        // 将 token 存入 request attribute，供 Client 层转发使用
        request.setAttribute("jwtToken", token);
        return true;
    }
}
