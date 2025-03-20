package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.TutorBean;

import java.util.List;

/**
 * TutorMapper.java - 튜터 관련 DB 매퍼 인터페이스
 * 튜터 정보 관련 SQL 쿼리 매핑을 담당합니다.
 */
@Mapper
public interface TutorMapper {

    /**
     * 튜터 신청 등록 (여기서 시퀀스 값을 반환하도록 수정)
     */
    @Insert("INSERT INTO tutor (tutor_key, user_key, born_language, teach_language, " +
            "tutor_image, status, apply_date, create_date) " +
            "VALUES (tutor_seq.nextval, #{user_key}, #{born_language}, #{teach_language}, " +
            "#{tutor_image, jdbcType=VARCHAR}, " +
            "#{status}, SYSDATE, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "tutor_key", keyColumn = "tutor_key")
    void insertTutor(TutorBean tutorBean);

    /**
     * 튜터 재신청 처리
     */
    @Update("UPDATE tutor SET born_language = #{born_language}, teach_language = #{teach_language}, " +
            "tutor_image = #{tutor_image, jdbcType=VARCHAR}, status = 'reapply', apply_date = SYSDATE " +
            "WHERE tutor_key = #{tutor_key}")
    void updateTutorForReapply(TutorBean tutorBean);

    /**
     * 사용자 키로 튜터 정보 조회
     */
    @Select("SELECT * FROM tutor WHERE user_key = #{userKey}")
    TutorBean getTutorByUserKey(int userKey);

    /**
     * 튜터 키로 튜터 정보 조회
     */
    @Select("SELECT t.*, u.user_name, u.user_nickname FROM tutor t " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE t.tutor_key = #{tutorKey}")
    TutorBean getTutorByKey(int tutorKey);

    /**
     * 모든 튜터 신청 목록 조회
     */
    @Select("SELECT t.*, u.user_name, u.user_nickname FROM tutor t " +
            "JOIN users u ON t.user_key = u.user_key " +
            "ORDER BY t.apply_date DESC")
    List<TutorBean> getAllTutors();

    /**
     * 특정 상태의 튜터 신청 목록 조회
     */
    @Select("SELECT t.*, u.user_name, u.user_nickname FROM tutor t " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE t.status = #{status} " +
            "ORDER BY t.apply_date DESC")
    List<TutorBean> getTutorsByStatus(String status);

    /**
     * 튜터 신청 상태 변경
     */
    @Update("UPDATE tutor SET status = #{status} " +
            "WHERE tutor_key = #{tutorKey}")
    void updateStatus(@Param("tutorKey") int tutorKey, @Param("status") String status);

    /**
     * 튜터 승인 처리
     */
    @Update("UPDATE tutor SET status = 'approved', approve_date = SYSDATE " +
            "WHERE tutor_key = #{tutorKey}")
    void approveTutor(int tutorKey);

    /**
     * 튜터 재신청 승인 처리
     */
    @Update("UPDATE tutor SET status = 'approved', approve_date = SYSDATE " +
            "WHERE tutor_key = #{tutorKey}")
    void approveReapplyTutor(int tutorKey);

    /**
     * 튜터 언어 정보 업데이트
     */
    @Update("UPDATE tutor SET teach_language = #{teach_language}, status = #{status} " +
            "WHERE tutor_key = #{tutor_key}")
    void updateTutorLanguage(TutorBean tutorBean);

    /**
     * 튜터 거부 처리 (거부 사유 포함)
     */
    @Update("UPDATE tutor SET status = 'rejected', reject_reason = #{rejectReason} " +
            "WHERE tutor_key = #{tutorKey}")
    void rejectTutor(@Param("tutorKey") int tutorKey, @Param("rejectReason") String rejectReason);

}