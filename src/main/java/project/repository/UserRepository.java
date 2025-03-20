package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.UserBean;
import project.mapper.UserMapper;

@Repository
public class UserRepository {

    @Autowired
    private UserMapper userMapper;

    /**
     * 회원가입
     */
    public void join(UserBean joinUser){
        userMapper.join(joinUser);
    }

    /**
     * 아이디 중복 확인
     */
    public String check_id(UserBean user) {
        return userMapper.check_id(user);
    }

    /**
     * 닉네임 중복 확인
     */
    public String check_nickname(UserBean user) {
        return userMapper.check_nickname(user);
    }

    /**
     * 로그인 확인
     */
    public String login(UserBean user) {
        return userMapper.login(user);
    }

    /**
     * 회원정보 조회
     */
    public UserBean get_user(UserBean user) {
        return userMapper.get_user(user);
    }

    /**
     * 사용자 키로 회원 정보 조회
     */
    public UserBean getUserByKey(int userKey) {
        return userMapper.getUserByKey(userKey);
    }

    /**
     * 사용자 유형 업데이트
     */
    public void updateUserType(int userKey, String userType) {
        userMapper.updateUserType(userKey, userType);
    }

    /**
     * 회원 프로필 업데이트
     */
    public void updateUserProfile(UserBean userBean) {
        userMapper.updateUserProfile(userBean);
    }

    /**
     * 비밀번호 업데이트
     */
    public void updatePassword(int userKey, String newPassword) {
        userMapper.updatePassword(userKey, newPassword);
    }

    /**
     * 이름과 이메일로 회원 찾기
     */
    public UserBean findUserByName(UserBean userBean) {
        return userMapper.findUserByName(userBean);
    }
}