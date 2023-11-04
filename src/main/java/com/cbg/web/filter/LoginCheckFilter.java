package com.cbg.web.filter;

import com.alibaba.fastjson.JSON;
import com.cbg.common.domain.R;
import com.cbg.web.config.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否通过登录进来 urlPatterns需要过滤的路径
 */
@Slf4j
@Configuration
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符/**
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //向下转型才能调用getRequestURI
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1,获取本次请求的URI
        String requestURI = request.getRequestURI();

//        log.info("拦截到请求：{}", requestURI);
        log.info("拦截到请求：" + requestURI);

        //2,判断请求的路径是否需要处理
        //定义不需要处理的的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                //静态资源
                "/backend/**",
                "/front/**",
                //移动端发送短信
                "/user/sendMsg",
                //移动端登录
                "/user/login"
        };
        boolean check = checkURI(urls, requestURI);

        //3,如果不需要处理，直接放行
        if (check) {
            log.info("本次请求 {} 不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4-1,判断登录状态，已经登录则放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，登录id为：{}", request.getSession().getAttribute("employee"));

            //设置线程池id
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        //4-2,判断移动端登录状态，已经登录则放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，登录id为：{}", request.getSession().getAttribute("user"));

            //设置线程池id
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        //或整合一块
//        if (check || request.getSession().getAttribute("employee") != null) {
//            log.info("本次请求 {} 不需要处理", requestURI);
//            filterChain.doFilter(request, response);
//            return;
//        }

        //5,如果未登录则返回登录结果，因为过滤器是void，要通过输出流形式向客户端页面响应数据
        log.info("用户未登录");
        //JSON.toJSONString 转JSON
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean checkURI(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match)
                return true;
        }
        return false;
    }
}
