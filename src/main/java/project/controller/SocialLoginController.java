/**
 * SocialLoginController.java - 소셜 로그인 컨트롤러
 * 네이버/카카오 소셜 로그인 처리를 담당합니다.
 * URL 맵핑:
 * - /user/social/urls: 소셜 로그인 URL 제공 API
 * - /login/code: 네이버 로그인 콜백
 * - /user/login/oauth2/code/kakao: 카카오 로그인 콜백
 * - /user/social/result: 소셜 로그인 결과 페이지
 */
package project.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import project.beans.UserBean;
import project.service.SocialLoginService;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SocialLoginController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private SocialLoginService socialLoginService;

    /**
     * 소셜 로그인 URL 정보 제공 API
     */
    @GetMapping("/user/social/login/urls")
    @ResponseBody
    public ResponseEntity<Map<String, String>> get_social_login_urls() {
        Map<String, String> urls = socialLoginService.get_social_login_urls();
        return ResponseEntity.ok(urls);
    }

    /**
     * 네이버 로그인 콜백 처리
     */
    @GetMapping("/login/code")
    public String naver_login_callback(
            @RequestParam(name = "code") String code,
            @RequestParam(name = "state", required = false) String state,
            Model model) {

        boolean loginSuccess = socialLoginService.process_naver_login(code, state != null ? state : "STATE_STRING");

        // Model에 로그인 결과 추가
        model.addAttribute("loginSuccess", loginSuccess);

        // 소셜 로그인 완료 페이지 반환
        return "user/social-login-complete";
    }
    /**
     * 카카오 로그인 콜백 처리
     */
    @GetMapping("/user/login/oauth2/code/kakao")
    public String kakao_login_callback(
            @RequestParam(name = "code") String code,
            Model model) {

        boolean loginSuccess = socialLoginService.process_kakao_login(code);

        // Model에 로그인 결과 추가
        model.addAttribute("loginSuccess", loginSuccess);

        // 소셜 로그인 완료 페이지 반환
        return "user/social-login-complete";
    }
}