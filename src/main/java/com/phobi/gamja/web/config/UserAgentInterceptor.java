package com.phobi.gamja.web.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class UserAgentInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false); // 세션이 없으면 null

        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "세션이 없습니다");
            return false;
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다");
            return false;
        }
        
        request.setAttribute("userId", userId);

        String sessionUserAgent = (String) session.getAttribute("userAgent");
        String currentUserAgent = request.getHeader("User-Agent");

        if (sessionUserAgent == null || currentUserAgent == null || !sessionUserAgent.equals(currentUserAgent)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User-Agent가 일치하지 않습니다");
            return false;
        }

        return true; // 검사 통과
    }
}
