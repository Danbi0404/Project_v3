package project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import project.beans.UserBean;

public class SidebarInterceptor implements HandlerInterceptor {

    private final UserBean loginUserBean;

    public SidebarInterceptor(UserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 로그인 상태를 request에 추가하여 사이드바에서 사용
        request.setAttribute("loginUserBean", loginUserBean);

        // 현재 메뉴 결정 (URL 기반)
        String path = request.getRequestURI();
        String currentMenu = "home"; // 기본값

        if(path.contains("/learning")) {
            currentMenu = "learning";
        } else if(path.contains("/tutor")) {
            currentMenu = "tutor";
        } else if(path.contains("/payment")) {
            currentMenu = "payment";
        } else if(path.contains("/user/profile")) {
            currentMenu = "profile";
        }

        request.setAttribute("currentMenu", currentMenu);

        return true;
    }
}