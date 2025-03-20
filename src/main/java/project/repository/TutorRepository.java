package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.TutorBean;
import project.mapper.TutorMapper;

import java.util.List;

/**
 * TutorRepository.java - 튜터 관련 데이터 액세스 계층
 * 튜터 데이터 CRUD 작업을 처리합니다.
 */
@Repository
public class TutorRepository {

    @Autowired
    private TutorMapper tutorMapper;

    /**
     * 튜터 신청 등록
     */
    public void insertTutor(TutorBean tutorBean) {
        tutorMapper.insertTutor(tutorBean);
    }

    /**
     * 튜터 재신청 처리
     */
    public void updateTutorForReapply(TutorBean tutorBean) {
        tutorMapper.updateTutorForReapply(tutorBean);
    }

    /**
     * 사용자 키로 튜터 정보 조회
     */
    public TutorBean getTutorByUserKey(int userKey) {
        return tutorMapper.getTutorByUserKey(userKey);
    }

    /**
     * 튜터 키로 튜터 정보 조회
     */
    public TutorBean getTutorByKey(int tutorKey) {
        return tutorMapper.getTutorByKey(tutorKey);
    }

    /**
     * 모든 튜터 신청 목록 조회
     */
    public List<TutorBean> getAllTutors() {
        return tutorMapper.getAllTutors();
    }

    /**
     * 특정 상태의 튜터 신청 목록 조회
     */
    public List<TutorBean> getTutorsByStatus(String status) {
        return tutorMapper.getTutorsByStatus(status);
    }

    /**
     * 튜터 신청 상태 변경
     */
    public void updateStatus(int tutorKey, String status) {
        tutorMapper.updateStatus(tutorKey, status);
    }

    /**
     * 튜터 승인 처리
     */
    public void approveTutor(int tutorKey) {
        tutorMapper.approveTutor(tutorKey);
    }

    /**
     * 튜터 재신청 승인 처리
     */
    public void approveReapplyTutor(int tutorKey) {
        tutorMapper.approveReapplyTutor(tutorKey);
    }

    /**
     * 튜터 거부 처리
     */
    public void rejectTutor(int tutorKey, String rejectReason) {
        tutorMapper.rejectTutor(tutorKey, rejectReason);
    }


    /**
     * 튜터 언어 정보 업데이트
     */
    public void updateTutorLanguage(TutorBean tutorBean) {
        tutorMapper.updateTutorLanguage(tutorBean);
    }
}