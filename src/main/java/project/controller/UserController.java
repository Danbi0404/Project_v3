package project.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.beans.UserBean;
import project.mapper.UserMapper;
import project.service.PaymentService;
import project.service.SocialLoginService;
import project.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SocialLoginService socialLoginService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserBean userBean;

    /**
     * 로그인 페이지 호출
     */
    @GetMapping("/login")
    public String login(@ModelAttribute("LoginUser") UserBean user) {
        return "user/login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login_pro")
    public String login_pro(@ModelAttribute("LoginUser") UserBean user, Model model) {

        //관리자 로그인으로 접근시 관리자 로그인 페이지 호출.
        if (user.getUser_id().equals("admin") && user.getUser_pw().equals("admin")) {

            return "admin/admin_login";
        }

        // 로그인 시도 전 유효성 검사
        if (userService.validate_login_user(user)) {
            if (userService.login(user)) {
                return "redirect:/main";
            }
        }

        // 로그인 실패 시 메시지와 함께 로그인 페이지로 리다이렉트
        return "redirect:/user/login?loginError=true";
    }

    /**
     * 로그인 상태 확인 API
     */
    @GetMapping("/check-login-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> check_login_status() {
        Map<String, Object> response = new HashMap<>();
        response.put("loggedIn", loginUserBean.isLogin());

        if (loginUserBean.isLogin()) {
            response.put("userId", loginUserBean.getUser_id());
            response.put("userName", loginUserBean.getUser_name());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * ID 중복 확인 API
     */
    @GetMapping("/check-id")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> check_id(@RequestParam String id) {
        UserBean user = new UserBean();
        user.setUser_id(id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", userService.check_id(user));

        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 확인 API
     */
    @GetMapping("/check-nickname")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> check_nickname(@RequestParam String nickname) {
        UserBean user = new UserBean();
        user.setUser_nickname(nickname);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", userService.check_nickname(user));

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 확인 API
     */
    @PostMapping("/verify-password")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyPassword(@RequestBody Map<String, String> request) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        String password = request.get("password");

        // 비밀번호 확인
        UserBean checkBean = new UserBean();
        checkBean.setUser_id(loginUserBean.getUser_id());
        checkBean.setUser_pw(password);

        boolean isValid = userService.checkPassword(checkBean);

        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * 회원가입 페이지 호출
     */
    @GetMapping("/join")
    public String join(@ModelAttribute("JoinUser") UserBean user) {
        return "user/join";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/join_pro")
    public String join_pro(@ModelAttribute("JoinUser") UserBean user) {
        // 모든 필드 유효성 검사를 서비스 레이어로 위임
        if (userService.validate_and_prepare_user(user)) {
            // ID와 닉네임 모두 중복 확인
            boolean id_available = userService.check_id(user);
            boolean nickname_available = userService.check_nickname(user);

            if (id_available && nickname_available) {
                userService.join(user);
                return "redirect:/main?joinSuccess=true";
            } else if (!id_available) {
                return "redirect:/user/join?joinError=duplicate-id";
            } else {
                return "redirect:/user/join?joinError=duplicate-nickname";
            }
        }

        return "redirect:/user/join?joinError=invalid";
    }

    /**
     * 마이페이지 호출
     */
    @GetMapping("/mypage")
    public String mypage(Model model) {

        // 구독 정보를 다시 확인하여 모델에 추가(payment.html과 같이 구독정보 확인하여 내려보내기)
        boolean isSubscribed = paymentService.hasActiveSubscription(loginUserBean.getUser_key());

        if(isSubscribed) {
            Map<String, Object> subscriptionInfo = paymentService.getActiveSubscriptionInfo(loginUserBean.getUser_key());
            model.addAttribute("subscriptionInfo", subscriptionInfo);
            model.addAttribute("isSubscribed", true);
        } else {
            model.addAttribute("isSubscribed", false);
        }

        return "user/mypage";
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout() {

        // 네이버 로그인 사용자인 경우 토큰 삭제
        if ("naver".equals(loginUserBean.getSocial_provider()) && loginUserBean.getNaverAccessToken() != null) {
            socialLoginService.delete_naver_token(loginUserBean.getNaverAccessToken());
        }

        // 로그인 상태 초기화
        userService.logout();
        return "redirect:/main";
    }

    /**
     * 프로필 수정 페이지 호출
     */
    @GetMapping("/profile/edit")
    public String profileEdit(Model model) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        return "user/profile-edit";
    }

    /**
     * 프로필 업데이트 처리
     */
    @PostMapping("/profile/update")
    public String profileUpdate(
            @ModelAttribute("updateUserBean") UserBean updateUserBean,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 현재 로그인한 사용자의 키 설정
        updateUserBean.setUser_key(loginUserBean.getUser_key());

        // 현재 이미지 정보 설정 (변경되지 않는 경우를 위해)
        updateUserBean.setUser_image(loginUserBean.getUser_image());

        // 서비스 호출하여 유효성 검사 및 업데이트
        String errorCode = userService.validateAndUpdateUserProfile(updateUserBean, profileImage);

        if (errorCode == null) {
            // 세션 정보도 업데이트
            loginUserBean.setUser_name(updateUserBean.getUser_name());
            loginUserBean.setUser_nickname(updateUserBean.getUser_nickname());
            loginUserBean.setUser_image(updateUserBean.getUser_image());

            return "redirect:/user/mypage?updateSuccess=true";
        } else {
            return "redirect:/user/profile/edit?error=" + errorCode;
        }
    }

    /**
     * 비밀번호 변경 처리
     */
    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute("passwordForm") UserBean passwordForm) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 사용자 ID와 키 설정
        passwordForm.setUser_id(loginUserBean.getUser_id());
        passwordForm.setUser_key(loginUserBean.getUser_key());

        // 서비스 호출하여 비밀번호 변경
        String errorCode = userService.validateAndChangePassword(passwordForm);

        if (errorCode == null) {
            return "redirect:/user/mypage?pwChangeSuccess=true";
        } else {
            return "redirect:/user/profile/edit?error=" + errorCode;
        }
    }

    /**
     * 아이디/비밀번호 찾기 페이지 호출
     */
    @GetMapping("/find-account")
    public String findAccount(@RequestParam(required = false) String tab) {
        return "user/find-account";
    }

    /**
     * 아이디 찾기 처리
     */
    @PostMapping("/find/id")
    public String findId(
            @RequestParam("user_name") String userName,
            Model model) {

        // 간단한 유효성 검사
        if (userName.isEmpty()) {
            return "redirect:/user/find-account?result=error";
        }

        // 아이디 찾기 서비스 호출 (이름만으로 찾기)
        UserBean foundUser = userService.findUserIdByName(userName);

        if (foundUser != null) {
            // 찾은 아이디 정보를 모델에 추가
            model.addAttribute("foundUser", foundUser);
            model.addAttribute("findIdSuccess", true);
            return "user/find-account-result";
        } else {
            return "redirect:/user/find-account?result=not-found";
        }
    }
}