package project.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.beans.CertificateFile;
import project.beans.TutorBean;
import project.beans.UserBean;
import project.repository.CertificateFileRepository;
import project.repository.TutorRepository;
import project.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TutorService.java - 튜터 관련 서비스
 * 튜터 신청, 조회, 상태 관리 등의 비즈니스 로직을 처리합니다.
 */
@Service
public class TutorService {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateFileRepository certificateFileRepository;

    @Autowired
    private FileService fileService;

    // 튜터 신청 처리
    public boolean applyForTutor(
            TutorBean tutorBean,
            MultipartFile profileImage,
            List<MultipartFile> certificateFiles,
            List<Integer> keepFileIds,
            boolean hasExistingProfileImage,
            String existingProfileImage) {
        try {
            // 기존 튜터 정보 조회
            TutorBean existingTutor = tutorRepository.getTutorByUserKey(loginUserBean.getUser_key());

            // 재신청 여부 확인
            boolean isReapply = existingTutor != null &&
                    existingTutor.getStatus().equals("rejected");

            // 사용자 키 설정
            tutorBean.setUser_key(loginUserBean.getUser_key());

            // 프로필 이미지 처리
            if (!profileImage.isEmpty()) {
                // 기존 프로필 이미지가 있고 재신청이면 물리적 파일 삭제
                if (isReapply && existingTutor.getTutor_image() != null) {
                    fileService.deleteFile(existingTutor.getTutor_image());
                }

                // 새 프로필 이미지 업로드 처리
                String profileFileName = fileService.saveImage(profileImage);
                tutorBean.setTutor_image(profileFileName);
            } else if (hasExistingProfileImage) {
                // 기존 프로필 이미지 유지
                tutorBean.setTutor_image(existingProfileImage);
            }

            // 재신청 또는 첫 신청 처리
            if (isReapply) {
                // 이전 튜터 키 유지
                tutorBean.setTutor_key(existingTutor.getTutor_key());
                tutorBean.setStatus("pending");
                tutorBean.setReject_reason(null);
                tutorBean.setApply_date(new Date());

                tutorRepository.updateTutorForReapply(tutorBean);
            } else {
                tutorBean.setStatus("pending");
                tutorRepository.insertTutor(tutorBean);
            }

            int tutorKey = tutorBean.getTutor_key();

            // 재신청이면서 기존 파일 일부 삭제 처리
            if (isReapply) {
                List<CertificateFile> existingFiles = certificateFileRepository.getFilesByTutorKey(tutorKey);

                for (CertificateFile file : existingFiles) {
                    // keepFileIds에 없는 파일은 물리적으로 삭제
                    if (keepFileIds == null || !keepFileIds.contains(file.getFile_key())) {
                        // 물리적 파일 삭제
                        fileService.deleteFile(file.getFile_path());
                        // DB에서 파일 정보 삭제
                        certificateFileRepository.deleteFile(file.getFile_key());
                    }
                }
            }

            // 새 인증서 파일 업로드 (기존 코드 유지)
            if (certificateFiles != null) {
                List<MultipartFile> nonEmptyFiles = certificateFiles.stream()
                        .filter(file -> !file.isEmpty())
                        .toList();

                if (!nonEmptyFiles.isEmpty()) {
                    List<CertificateFile> savedFiles = fileService.saveCertificates(
                            nonEmptyFiles, tutorKey, loginUserBean.getUser_id());

                    for (CertificateFile file : savedFiles) {
                        certificateFileRepository.insertFile(file);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 튜터 재신청 승인 처리
     */
    public boolean approveReapplyTutor(int tutorKey) {
        try {
            // 튜터 정보 조회
            TutorBean tutor = tutorRepository.getTutorByKey(tutorKey);
            if (tutor == null) {
                return false;
            }

            // 재신청 승인 처리 (상태 업데이트)
            tutorRepository.approveReapplyTutor(tutorKey);

            // 사용자 유형을 tutor로 변경 (이미 tutor일 수도 있지만 안전하게 업데이트)
            userRepository.updateUserType(tutor.getUser_key(), "tutor");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 현재 로그인 사용자의 튜터 정보 조회
    public TutorBean getCurrentUserTutorInfo() {
        if (loginUserBean.isLogin()) {
            TutorBean tutor = tutorRepository.getTutorByUserKey(loginUserBean.getUser_key());
            if (tutor != null) {
                // 인증서 파일 정보도 함께 조회
                List<CertificateFile> files = certificateFileRepository.getFilesByTutorKey(tutor.getTutor_key());
                tutor.setCertificateFiles(files);
            }
            return tutor;
        }
        return null;
    }

    // 유저 키로 튜터 정보 조회
    public TutorBean getTutorByUserKey(int userKey) {
        TutorBean tutor = tutorRepository.getTutorByUserKey(userKey);
        if (tutor != null) {
            // 인증서 파일 목록 조회
            List<CertificateFile> files = certificateFileRepository.getFilesByUserKey(userKey);
            tutor.setCertificateFiles(files);
        }
        return tutor;
    }

    // 튜터 키로 튜터 정보 조회
    public TutorBean getTutorByKey(int tutorKey) {
        TutorBean tutor = tutorRepository.getTutorByKey(tutorKey);
        if (tutor != null) {
            // 인증서 파일 목록 조회
            List<CertificateFile> files = certificateFileRepository.getFilesByTutorKey(tutorKey);
            tutor.setCertificateFiles(files);
        }
        return tutor;
    }


    // 모든 튜터 신청 목록 조회 (관리자용)
    public List<TutorBean> getAllTutors() {
        return tutorRepository.getAllTutors();
    }

    // 특정 상태의 튜터 신청 목록 조회 (관리자용)
    public List<TutorBean> getTutorsByStatus(String status) {
        return tutorRepository.getTutorsByStatus(status);
    }

    // 튜터 신청 승인
    public boolean approveTutor(int tutorKey) {
        try {
            // 승인 처리
            tutorRepository.approveTutor(tutorKey);

            // 튜터 정보 조회
            TutorBean tutor = tutorRepository.getTutorByKey(tutorKey);

            // 사용자 유형을 tutor로 변경
            userRepository.updateUserType(tutor.getUser_key(), "tutor");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 튜터 거부 처리 - 파일은 삭제하지 않고 상태만 변경
    public boolean rejectTutor(int tutorKey, String rejectReason) {
        try {
            // 거부 사유 저장 및 상태 변경만 수행
            tutorRepository.rejectTutor(tutorKey, rejectReason);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 튜터 신청 상태 확인
    public String checkTutorStatus() {
        if (!loginUserBean.isLogin()) {
            return "not_logged_in";
        }

        TutorBean tutor = tutorRepository.getTutorByUserKey(loginUserBean.getUser_key());

        if (tutor == null) {
            return "not_applied";
        }

        return tutor.getStatus();
    }

    /**
     * 튜터에 가르칠 언어 추가 신청
     */
    public boolean applyAdditionalLanguage(TutorBean tutorBean, String newLanguage, List<MultipartFile> certificateFiles) {
        try {
            // 기존 튜터 정보 조회
            TutorBean existingTutor = tutorRepository.getTutorByKey(tutorBean.getTutor_key());
            if (existingTutor == null) {
                return false;
            }

            // 이미 해당 언어가 있는지 확인
            if (existingTutor.hasTeachLanguage(newLanguage)) {
                return false;
            }

            // 상태를 language_pending으로 변경하고 언어 추가
            existingTutor.addTeachLanguage(newLanguage);
            existingTutor.setStatus("language_pending");

            // 업데이트 적용
            tutorRepository.updateTutorLanguage(existingTutor);

            // 인증서 파일 저장
            int tutorKey = existingTutor.getTutor_key();
            if (certificateFiles != null && !certificateFiles.isEmpty()) {
                for (MultipartFile file : certificateFiles) {
                    if (!file.isEmpty()) {
                        // 파일 저장 로직
                        String filePath = fileService.saveCertificate(file);

                        CertificateFile certFile = new CertificateFile();
                        certFile.setTutor_key(tutorKey);
                        certFile.setFile_path(filePath);
                        certFile.setOriginal_filename(file.getOriginalFilename());

                        certificateFileRepository.insertFile(certFile);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 추가 언어 신청 승인
     */
    public boolean approveAdditionalLanguage(int tutorKey) {
        try {
            // 튜터 정보 조회
            TutorBean tutor = tutorRepository.getTutorByKey(tutorKey);
            if (tutor == null || !"language_pending".equals(tutor.getStatus())) {
                return false;
            }

            // 상태를 approved로 변경
            tutor.setStatus("approved");
            tutorRepository.updateStatus(tutorKey, "approved");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 유틸리티 메소드
    private boolean is_empty(String value) {
        return value == null || value.trim().isEmpty();
    }
}