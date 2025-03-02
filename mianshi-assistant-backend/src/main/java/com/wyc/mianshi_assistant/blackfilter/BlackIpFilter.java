package com.wyc.mianshi_assistant.blackfilter;

import com.wyc.mianshi_assistant.utils.NetUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * IP 黑名单过滤器
 */
@WebFilter(urlPatterns = "/*", filterName = "blackIpFilter")
public class BlackIpFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        // 根据请求获取 id 地址
        String ipAddress = NetUtils.getIpAddress(request);
        if(BlackIpUtils.isBlackIp(ipAddress)) {
            servletResponse.setContentType("text/html;charset=utf-8");
            servletResponse.getWriter().write("{\"errorCode\":\"-1\",\"errorMsg\":\"黑名单IP,禁止访问\"}");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
