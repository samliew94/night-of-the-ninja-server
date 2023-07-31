package com.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter extends OncePerRequestFilter {

    @Autowired
    SpamService spamService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equals("POST")) {
            if (spamService.isRequestSpam(request))
                return;
        }

        filterChain.doFilter(request, response);
    }
}

@Service
class SpamService {

    Map<String, Long> map = new ConcurrentHashMap<>();

    public boolean isRequestSpam(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Real-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        System.out.println(ip + " is sending POST request");

        if (map.containsKey(ip)) {

            long start = map.get(ip);
            long end = System.currentTimeMillis();
            long interval = end - start;
            if (interval <= 250L) {
                System.out.println(ip + " is spamming. Interval between last Request=" + (end - start));
                map.put(ip, end);
                return true;
            }

            map.put(ip, end);

        } else {
            map.put(ip, System.currentTimeMillis());
        }

        return false;

    }

}





