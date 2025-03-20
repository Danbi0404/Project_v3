package project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import project.beans.UserBean;

public class AuthInterceptor implements HandlerInterceptor {

    private UserBean loginUserBean;

    public AuthInterceptor(UserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 여부 확인
        if(!loginUserBean.isLogin()) {
            // 로그인되지 않은 경우 메인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/main?needLogin=true");
            return false;
        }

        return true;
    }
}