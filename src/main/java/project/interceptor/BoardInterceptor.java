package project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import project.beans.UserBean;

public class BoardInterceptor implements HandlerInterceptor {

    private final UserBean loginUserBean;

    public BoardInterceptor(UserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 상태가 아니면 로그인 페이지로 리다이렉트
        if(!loginUserBean.isLogin()) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/user/login");
            return false;
        }

        return true;
    }
}