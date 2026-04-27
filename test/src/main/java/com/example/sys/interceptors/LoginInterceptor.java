package com.example.sys.interceptors;

import com.alibaba.fastjson2.JSON;
import com.example.sys.entity.User;
import com.example.sys.util.ThreadLocalUtil;
import com.example.sys.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // 静态资源直接放行
        if (requestURI.startsWith("/api/files/avatars/")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        // redis中的令牌验证
        try {
            Object obj = redisTemplate.opsForValue().get(token);
            // 放行
            if(obj != null){
                // 把用户信息保存到ThreadLocal
                User user = JSON.parseObject(JSON.toJSONString(obj), User.class);
                ThreadLocalUtil.set(user);
                return true;
            } else {
                response.setStatus(401);
                return false;
            }
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
