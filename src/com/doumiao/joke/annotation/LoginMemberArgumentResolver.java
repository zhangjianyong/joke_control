package com.doumiao.joke.annotation;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.doumiao.joke.coder.DESCoder;
import com.doumiao.joke.lang.CookieUtils;
import com.doumiao.joke.schedule.Config;
import com.doumiao.joke.vo.Member;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

	@Resource
	private ObjectMapper objectMapper;
	
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
    	return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation annotation : annotations) {
            if (LoginMember.class.isInstance(annotation)) {
                HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
                String _user = CookieUtils.readCookie(request, "_user");
        		Member loginUser = null;
    			String charset = 
    					Config.get("system_charset","utf-8");
    			String key = Config.get("system_cookie_key","");
        		if (_user != null) {
        			try {
        				byte[] loginuser_c = DESCoder.decryptBASE64(_user
        						.getBytes(charset));
        				byte[] loginuser = DESCoder.decrypt(loginuser_c,
        						key.getBytes(charset));
        				loginUser = objectMapper.readValue(loginuser, Member.class);
        				loginUser.setNick(URLDecoder.decode(loginUser.getNick(),charset));
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
        		//loginUser = new Member();
        		//loginUser.setId(1);
        		return loginUser;
            }
        }
        return null;
    }
}