package project.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.beans.UserBean;
import project.repository.UserRepository;

import java.io.IOException;

@Service
public class UserService {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    /**
     * 회원가입 처리
     */
    public void join(UserBean joinUser) {

        if (joinUser.getSocial_provider()==null) {
            joinUser.setSocial_provider("normal");
        }//소셜 회원이 아닐 경우 기본값 normal 삽입

        // 기본 프로필 이미지 설정 (static 리소스 경로)
        joinUser.setUser_image("images/default_profile.png");

        userRepository.join(joinUser);
    }

    /**
     * 아이디 중복 검사
     */
    public boolean check_id(UserBean user) {
        return userRepository.check_id(user) == null;
    }

    /**
     * 닉네임 중복 검사
     */
    public boolean check_nickname(UserBean user) {
        return userRepository.check_nickname(user) == null;
    }

    /**
     * 로그인 검증
     */
    public boolean validate_login_user(UserBean user) {
        return user != null &&
                is_not_empty(user.getUser_id()) && // 여전히 이 메서드는 사용 가능하지만 의미는 이메일 확인으로 변경됨
                is_not_empty(user.getUser_pw());
    }

    /**
     * 사용자 정보 전체 유효성 검사 및 전처리
     */
    public boolean validate_and_prepare_user(UserBean user) {
        if (user == null) {
            System.out.println("사용자 정보가 null입니다.");
            return false;
        }

        // 모든 필수 필드 검증
        if (!validate_required_fields(user)) {
            System.out.println("필수 필드 검증 실패: " + user.getUser_id());
            return false;
        }

        // ID 검증
        if (!is_valid_id(user.getUser_id())) {
            System.out.println("ID 검증 실패: " + user.getUser_id());
            return false;
        }

        // 비밀번호 검증
        if (!is_valid_password(user.getUser_pw())) {
            System.out.println("PW 검증 실패: " + user.getUser_pw());
            return false;
        }

        // 이름 검증
        if (!is_valid_name(user.getUser_name())) {
            System.out.println("이름 검증 실패: " + user.getUser_name());
            return false;
        }

        // 닉네임 검증
        if (!is_valid_nickname(user.getUser_nickname())) {
            System.out.println("닉네임 검증 실패: " + user.getUser_nickname());
            return false;
        }

        return true;
    }

    /**
     * 로그인 처리
     */
    public boolean login(UserBean user) {
        String user_key = userRepository.login(user);

        if (user_key != null && !user_key.isEmpty()) {
            UserBean user_info = userRepository.get_user(user);

            // 세션에 로그인 정보 저장 (email, address, phone 제거)
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
     * 회원정보 조회
     */
    public UserBean get_user(UserBean user) {
        return userRepository.get_user(user);
    }

    /**
     * 로그아웃 처리
     */
    public void logout() {
        loginUserBean.setUser_id(null);
        loginUserBean.setUser_name(null);
        loginUserBean.setUser_nickname(null);
        loginUserBean.setUser_type(null);
        loginUserBean.setUser_key(0);
        loginUserBean.setUser_money(0);
        loginUserBean.setUser_point(0);
        loginUserBean.setUser_bad_point(0);
        loginUserBean.setCreate_time(null);
        loginUserBean.setUser_image(null);
        loginUserBean.setLogin(false);
    }

    /**
     * 프로필 정보 유효성 검사 및 업데이트
     * @return 에러 코드 (성공 시 null)
     */
    public String validateAndUpdateUserProfile(UserBean userBean, MultipartFile profileImage) {
        // 기존 유효성 검사 (이름, 닉네임 등)
        if (is_empty(userBean.getUser_name())) {
            return "emptyName";
        }

        if (is_empty(userBean.getUser_nickname())) {
            return "emptyNickname";
        }

        // 닉네임 중복 검사 (현재 사용자 제외)
        if (!check_nickname_for_update(userBean)) {
            return "duplicateNickname";
        }

        try {
            // 프로필 이미지 업로드 처리
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    // 현재 사용자의 프로필 이미지 정보 가져오기
                    UserBean currentUser = userRepository.getUserByKey(userBean.getUser_key());

                    // 기존 이미지가 있고, 기본 이미지(images/로 시작)가 아닌 경우에만 삭제
                    if (currentUser != null && currentUser.getUser_image() != null
                            && !currentUser.getUser_image().startsWith("images/")) {//images는 프로젝트 내 이미지 경로이기 때문.
                        boolean deleted = fileService.deleteFile(currentUser.getUser_image());
                        System.out.println("기존 이미지 삭제 결과: " + deleted + ", 경로: " + currentUser.getUser_image());
                    }

                    // 새 이미지 저장
                    String imagePath = fileService.saveImage(profileImage);
                    userBean.setUser_image(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "imageUploadFailed";
                }
            }

            // 실제 DB 업데이트
            userRepository.updateUserProfile(userBean);
            return null; // 성공
        } catch (Exception e) {
            e.printStackTrace();
            return "updateFailed";
        }
    }

    /**
     * 현재 사용자를 제외한 닉네임 중복 검사
     */
    private boolean check_nickname_for_update(UserBean userBean) {
        // 현재 사용자의 닉네임과 동일하면 중복 검사 패스
        UserBean currentUser = userRepository.getUserByKey(userBean.getUser_key());
        if (currentUser != null && currentUser.getUser_nickname().equals(userBean.getUser_nickname())) {
            return true;
        }

        // 다른 사용자와 닉네임 중복 검사
        UserBean tempBean = new UserBean();
        tempBean.setUser_nickname(userBean.getUser_nickname());
        return userRepository.check_nickname(tempBean) == null;
    }

    /**
     * 비밀번호 변경 유효성 검사 및 처리
     * @param passwordForm 비밀번호 변경 정보가 담긴 UserBean 객체
     * @return 에러 코드 (성공 시 null)
     */
    public String validateAndChangePassword(UserBean passwordForm) {
        String currentPassword = passwordForm.getUser_pw();
        String newPassword = passwordForm.getUser_new_pw();
        String confirmPassword = passwordForm.getUser_check_pw();

        // 비밀번호 일치 확인
        if (!newPassword.equals(confirmPassword)) {
            return "passwordMismatch";
        }

        // 비밀번호 형식 검증
        if (!is_valid_password(newPassword)) {
            return "invalidPassword";
        }

        // 현재 비밀번호 확인
        UserBean checkBean = new UserBean();
        checkBean.setUser_id(passwordForm.getUser_id());
        checkBean.setUser_pw(currentPassword);

        if (!checkPassword(checkBean)) {
            return "wrongPassword";
        }

        try {
            // 비밀번호 변경
            userRepository.updatePassword(passwordForm.getUser_key(), newPassword);
            return null; // 성공
        } catch (Exception e) {
            e.printStackTrace();
            return "changeFailed";
        }
    }

    /**
     * 문자열이 비어있는지 확인
     */
    private boolean is_empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 비밀번호 확인
     */
    public boolean checkPassword(UserBean userBean) {
        // 현재 비밀번호 확인 로직
        String userKey = userRepository.login(userBean);
        return userKey != null && !userKey.isEmpty();
    }

    /**
     * 비밀번호 변경
     */
    public boolean changePassword(int userKey, String newPassword) {
        try {
            // 비밀번호 변경 로직
            userRepository.updatePassword(userKey, newPassword);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 이름으로 아이디 찾기
     */
    public UserBean findUserIdByName(String userName) {
        try {
            // 이름으로 사용자 찾기
            UserBean tempBean = new UserBean();
            tempBean.setUser_name(userName);

            return userRepository.findUserByName(tempBean);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== 유효성 검사 유틸리티 메서드 =====

    private boolean is_not_empty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean is_valid_id(String id) {
        return id != null &&
                id.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") &&
                id.length() <= 100;    }

    private boolean is_valid_password(String password) {
        return password != null && password.matches("^[a-zA-Z0-9]{8,12}$");
    }

    private boolean is_valid_name(String name) {
        return name != null && name.matches("^[가-힣a-zA-Z]{2,15}$");
    }

    private boolean is_valid_nickname(String nickname) {
        return nickname != null && nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$");
    }


    // 필수 필드 검증
    private boolean validate_required_fields(UserBean user) {
        return is_not_empty(user.getUser_id()) &&
                is_not_empty(user.getUser_pw()) &&
                is_not_empty(user.getUser_name()) &&
                is_not_empty(user.getUser_nickname());
    }
}