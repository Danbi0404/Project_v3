/**
 * SocialLoginService.java - 소셜 로그인 관련 서비스
 * 네이버/카카오 소셜 로그인 처리를 담당합니다.
 * 관련 클래스: SocialLoginController, UserRepository
 */
package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import project.beans.UserBean;
import project.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SocialLoginService {

    @Autowired
    private UserRepository userRepository;

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Value("${naver.client.id}")
    private String NAVER_CLIENT_ID;

    @Value("${naver.client.secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${naver.redirect.uri}")
    private String NAVER_REDIRECT_URI;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * 소셜 로그인 URL 정보 제공
     */
    public Map<String, String> get_social_login_urls() {
        Map<String, String> urls = new HashMap<>();

        // 네이버 로그인 URL 생성
        String naverUrl = "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + NAVER_CLIENT_ID +
                "&state=STATE_STRING" +
                "&redirect_uri=" + NAVER_REDIRECT_URI;

        // 카카오 로그인 URL 생성
        String kakaoUrl = "https://kauth.kakao.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + KAKAO_CLIENT_ID +
                "&redirect_uri=" + KAKAO_REDIRECT_URI +
                "&prompt=login";//로그인 파라미터로 url 요청시 항상 소셜 로그인 화면 호출

        urls.put("naverUrl", naverUrl);
        urls.put("kakaoUrl", kakaoUrl);

        return urls;
    }

    // 네이버 로그인 처리
    public boolean process_naver_login(String code, String state) {
        try {
            // 1. 액세스 토큰 획득
            String access_token = get_naver_token(code, state);
            // 토큰 저장
            loginUserBean.setNaverAccessToken(access_token);

            // 2. 사용자 정보 획득
            Map<String, String> user_info = get_naver_user_info(access_token);

            // 3. 사용자 정보로 DB 조회 또는 가입 처리
            UserBean user = new UserBean();
            // 네이버에서 받은 이메일이 있으면 그대로 사용, 없으면 임시 이메일 생성
            String email = user_info.get("email");
            if (email == null || email.isEmpty()) {
                email = "naver_" + user_info.get("id") + "@example.com";
            }
            user.setUser_id(email); // ID를 이메일로 사용
            user.setUser_pw("naver"); // 암호화 필요
            user.setUser_name(user_info.get("name"));
            user.setUser_nickname("naver_" + user_info.get("id"));
            user.setUser_type("normal");
            user.setSocial_provider("naver");

            // 기본 프로필 이미지 설정
            user.setUser_image("images/default_profile.png");

            // 회원 존재여부 확인 및 가입 처리
            if (userRepository.check_id(user) == null) {
                userRepository.join(user);
            }

            // 4. 로그인 처리
            return authenticate_with_social(email, "naver");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 카카오 로그인 처리
     */
    public boolean process_kakao_login(String code) {
        try {
            // 1. 액세스 토큰 획득
            String access_token = get_kakao_token(code);

            // 2. 사용자 정보 획득
            Map<String, String> user_info = get_kakao_user_info(access_token);

            // 3. 사용자 정보로 DB 조회 또는 가입 처리 (이메일, 전화번호, 주소 제거)
            UserBean user = new UserBean();
            // 카카오에서 받은 이메일이 있으면 그대로 사용, 없으면 임시 이메일 생성
            String email = user_info.get("email");
            if (email == null || email.isEmpty()) {
                email = "kakao_" + user_info.get("id") + "@example.com";
            }
            user.setUser_id(email); // ID를 이메일로 사용
            user.setUser_pw("kakao"); // 암호화 필요
            user.setUser_name(user_info.get("nickname"));
            user.setUser_nickname("kakao_" + user_info.get("id"));
            user.setUser_type("normal");
            user.setSocial_provider("kakao");

            // 기본 프로필 이미지 설정
            user.setUser_image("images/default_profile.png");

            System.out.println("카카오 : " + user.getUser_id() + user.getUser_name());

            // 회원 존재여부 확인 및 가입 처리
            if (userRepository.check_id(user) == null) {
                userRepository.join(user);
            }

            // 4. 로그인 처리
            return authenticate_with_social(email, "kakao");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 소셜 로그인 인증 처리
     */
    private boolean authenticate_with_social(String social_id, String provider) {
        UserBean user = new UserBean();
        user.setUser_id(social_id);
        user.setUser_pw(provider);  // 소셜 로그인은 비밀번호 실제 체크 안함

        UserBean user_info = userRepository.get_user(user);
        if (user_info != null) {
            // 세션에 로그인 정보 저장 (이메일, 전화번호, 주소 제거)
            loginUserBean.setUser_key(user_info.getUser_key());
            loginUserBean.setUser_id(user_info.getUser_id());
            loginUserBean.setUser_name(user_info.getUser_name());
            loginUserBean.setUser_nickname(user_info.getUser_nickname());
            loginUserBean.setUser_type(user_info.getUser_type());
            loginUserBean.setUser_money(user_info.getUser_money());
            loginUserBean.setUser_point(user_info.getUser_point());
            loginUserBean.setUser_bad_point(user_info.getUser_bad_point());
            loginUserBean.setCreate_time(user_info.getCreate_time());
            loginUserBean.setSocial_provider(user_info.getSocial_provider());
            loginUserBean.setUser_image(user_info.getUser_image());
            loginUserBean.setLogin(true);

            return true;
        }
        return false;
    }

    /**
     * 네이버 토큰 획득
     */
    private String get_naver_token(String code, String state) {
        String reqUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", NAVER_CLIENT_ID);
        params.add("client_secret", NAVER_CLIENT_SECRET);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        String responseBody = response.getBody();
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    /**
     * 네이버 토큰 삭제(로그아웃)
     */
    public void delete_naver_token(String accessToken) {
        String reqUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "delete");
        params.add("client_id", NAVER_CLIENT_ID);
        params.add("client_secret", NAVER_CLIENT_SECRET);
        params.add("access_token", accessToken); // 실제 액세스 토큰 사용
        params.add("service_provider", "NAVER");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 요청만 보내고 응답은 처리하지 않음
        restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        // 응답 결과를 확인하지 않고 메서드 종료
    }

    /**
     * 네이버 사용자 정보 획득
     */
    private Map<String, String> get_naver_user_info(String accessToken) {
        String reqUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        String responseBody = response.getBody();
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject profile = json.getAsJsonObject("response");

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", profile.get("id").getAsString());
        userInfo.put("name", profile.get("name").getAsString());

        // 이메일과 전화번호는 선택적으로 제공될 수 있음
        if (profile.has("email")) {
            userInfo.put("email", profile.get("email").getAsString());
        } else {
            userInfo.put("email", "no-email@example.com");
        }

        if (profile.has("mobile")) {
            userInfo.put("mobile", profile.get("mobile").getAsString());
        } else {
            userInfo.put("mobile", "");
        }

        return userInfo;
    }

    /**
     * 카카오 토큰 획득
     */
    private String get_kakao_token(String code) {
        String reqUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("redirect_uri", KAKAO_REDIRECT_URI);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        String responseBody = response.getBody();
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    /**
     * 카카오 사용자 정보 획득
     */
    private Map<String, String> get_kakao_user_info(String accessToken) {
        String reqUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        String responseBody = response.getBody();

        // 전체 JSON 내용 로깅
        System.out.println("Full Kakao User Info JSON: " + responseBody);

        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", json.get("id").getAsString());

        // 닉네임 정보
        if (json.has("properties")) {
            JsonObject properties = json.getAsJsonObject("properties");
            userInfo.put("nickname", properties.get("nickname").getAsString());
        } else {
            userInfo.put("nickname", "카카오사용자");
        }

        // 이메일 정보
        if (json.has("kakao_account")) {
            JsonObject account = json.getAsJsonObject("kakao_account");
            if (account.has("email") && !account.get("email").isJsonNull()) {
                userInfo.put("email", account.get("email").getAsString());
            } else {
                userInfo.put("email", "no-email@example.com");
            }
        } else {
            userInfo.put("email", "no-email@example.com");
        }

        return userInfo;
    }
}