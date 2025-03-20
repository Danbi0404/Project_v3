package project.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.beans.UserBean;
import project.interceptor.AuthInterceptor;
import project.interceptor.BoardInterceptor;
import project.interceptor.SidebarInterceptor;

@Configuration
public class WebContext implements WebMvcConfigurer {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Value("${file.upload.path}")
    private String fileUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resource/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        // 업로드 파일 접근 경로 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileUploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 사이드바 인터셉터
        SidebarInterceptor sidebarInterceptor = new SidebarInterceptor(loginUserBean);
        registry.addInterceptor(sidebarInterceptor).addPathPatterns("/**");

        // 인증 인터셉터
        AuthInterceptor authInterceptor = new AuthInterceptor(loginUserBean);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/user/profile", "/payment/**", "/learning/**", "/tutor/**")
                .excludePathPatterns("/user/login", "/user/join", "/user/login_pro", "/user/join_pro",
                        "/user/social/**", "/user/check-*", "/main", "/");
        // 게시판 인터셉터
        BoardInterceptor boardInterceptor = new BoardInterceptor(loginUserBean);
        registry.addInterceptor(boardInterceptor)
                .addPathPatterns("/board/*/write", "/board/*/modify/*", "/board/*/delete/*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://t1.kakaocdn.net")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}