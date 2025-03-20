/**
 * UserMapper.java - 사용자 관련 DB 매퍼 인터페이스
 * 사용자 정보 관련 SQL 쿼리 매핑을 담당합니다.
 * 관련 클래스: UserRepository
 */
package project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;
import project.beans.UserBean;

@Mapper
public interface UserMapper {

    /**
     * 아이디 중복 확인
     */
    @Select("select user_id from users where user_id = #{user_id}")
    String check_id(UserBean user);

    /**
     * 닉네임 중복 확인
     */
    @Select("select user_nickname from users where user_nickname = #{user_nickname}")
    String check_nickname(UserBean user);

    /**
     * 로그인 확인
     */
    @Select("select user_key from users where user_id = #{user_id} and user_pw = #{user_pw}")
    String login(UserBean user);

    /**
     * 회원가입
     */
    @Insert("insert into users(user_key, user_id, user_pw, user_name, user_nickname, " +
            "user_money, user_point, user_type, user_bad_point, create_time, social_provider, user_image) " +
            "values (users_seq.nextval, #{user_id}, #{user_pw}, #{user_name}, #{user_nickname}, " +
            "0, 0, #{user_type}, 0, sysdate, #{social_provider, jdbcType=VARCHAR}, #{user_image, jdbcType=VARCHAR})")
    void join(UserBean joinUser);
    /**
     * 회원정보 조회
     */
    @Select("select user_key, user_id, user_pw, user_name, user_nickname, " +
            "user_money, user_point, user_type, user_bad_point, social_provider, user_image, " +
            "to_char(create_time, 'yyyy-mm-dd') create_time from users where user_id = #{user_id} and user_pw = #{user_pw}")
    UserBean get_user(UserBean user);

    /**
     * 사용자 키로 회원 정보 조회
     */
    @Select("select user_key, user_id, user_pw, user_name, user_nickname, " +
            "user_money, user_point, user_type, user_bad_point, social_provider, " +
            "to_char(create_time, 'yyyy-mm-dd') create_time from users where user_key = #{user_key}")
    UserBean getUserByKey(int userKey);

    /**
     * 사용자 유형 업데이트
     */
    @Update("UPDATE users SET user_type = #{userType} WHERE user_key = #{userKey}")
    void updateUserType(@Param("userKey") int userKey, @Param("userType") String userType);

    /**
     * 프로필 정보 업데이트
     */
    @Update("update users set user_name = #{user_name}, user_nickname = #{user_nickname}, user_image = #{user_image, jdbcType=VARCHAR} " +
            "where user_key = #{user_key}")
    void updateUserProfile(UserBean userBean);

    /**
     * 비밀번호 업데이트
     */
    @Update("update users set user_pw = #{param2} where user_key = #{param1}")
    void updatePassword(int userKey, String newPassword);

    /**
     * 이름으로 회원 찾기
     */
    @Select("select user_key, user_id, user_name, user_nickname, " +
            "to_char(create_time, 'yyyy-mm-dd') create_time " +
            "from users where user_name = #{user_name}")
    UserBean findUserByName(UserBean userBean);
}