/**
 * UserBean.java - 사용자 정보 객체
 * 사용자의 계정 정보 및 로그인 상태를 관리합니다.
 * 관련 클래스: UserController, UserService, UserRepository, UserMapper
 */
package project.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBean {
    // 사용자 기본 정보
    private int user_key;
    private String user_id;
    private String user_pw;
    private String user_name;
    private String user_nickname;
    private String user_image;

/* email을 user_id로 받을 것.
    private String user_email;
    private String user_address;
    private String user_phone;
*/

    // 사용자 포인트/재화 정보
    private int user_money;
    private int user_point;
    private String user_type;
    private int user_bad_point;

    // 생성 시간 및 소셜 로그인 정보
    private String create_time;
    private String social_provider; // 소셜 로그인 제공자 (kakao, naver 등)

    // 로그인 상태 관리
    private boolean login;

    //네이버 토큰(카카오는 자체 삭제 메서드 제공하여 생략)
    private String naverAccessToken;

    // 비밀번호 변경 관련 필드
    private String user_new_pw;
    private String user_check_pw;

    // 생성자에서 로그인 상태 초기화
    public UserBean() {
        login = false;
    }

}