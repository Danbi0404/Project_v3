package project.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.beans.CertificateFile;
import project.beans.GroupBean;
import project.beans.TutorBean;
import project.beans.UserBean;
import project.mapper.TutorMapper;
import project.repository.CertificateFileRepository;
import project.service.GroupService;
import project.service.TutorService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private TutorMapper tutorMapper;

    @Autowired
    private CertificateFileRepository certificateFileRepository;

    /**
     * 튜터 페이지 호출
     */
    @GetMapping("/tutor_page")
    public String tutor_page(Model model) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 사용자 상태에 따른 정보 설정
        String tutorStatus = tutorService.checkTutorStatus();
        model.addAttribute("tutorStatus", tutorStatus);

        // 승인된 튜터인 경우 대시보드로 리다이렉트
        if (tutorStatus.equals("approved")) {
            return "redirect:/tutor/dashboard";
        }

        // 신청 정보가 있는 경우 정보 추가
        if (!tutorStatus.equals("not_applied")) {
            TutorBean tutorInfo = tutorService.getCurrentUserTutorInfo();
            model.addAttribute("tutorInfo", tutorInfo);
        }

        return "tutor/tutor_page";
    }

    /**
     * 튜터 대시보드 페이지 호출
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 튜터 권한 확인
        if (!loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/tutor/tutor_page?error=not-tutor";
        }

        // 튜터 정보 조회 - 현재 로그인한 user_key로 조회
        TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());
        if (tutorInfo == null) {
            return "redirect:/tutor/tutor_page?error=no-tutor-info";
        }

        // 그룹 목록 조회
        List<GroupBean> groups = groupService.getGroupsByTutorKey(tutorInfo.getTutor_key());
        model.addAttribute("tutorInfo", tutorInfo);
        model.addAttribute("groupList", groups);
        model.addAttribute("hasGroups", !groups.isEmpty());

        return "tutor/tutor_dashboard";
    }

    /**
     * 튜터 신청 페이지 호출
     */
    @GetMapping("/tutor_join")
    public String tutor_join(Model model) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        String tutorStatus = tutorService.checkTutorStatus();
        TutorBean tutor = tutorMapper.getTutorByUserKey(loginUserBean.getUser_key());

        boolean isReapplying = false;

        // 신청했으나 거부당한 경우, 다시 신청하도록 이동
        if (tutor != null && "rejected".equals(tutor.getStatus())) {
            isReapplying = true;
            model.addAttribute("tutorInfo", tutor);

            // 기존 인증서 파일 목록 추가
            List<CertificateFile> existingFiles = certificateFileRepository.getFilesByTutorKey(tutor.getTutor_key());
            model.addAttribute("certificateFiles", existingFiles);
        }

        model.addAttribute("isReapplying", isReapplying);

        //신청했으나 거부당한 경우, 다시 신청하도록 이동(현재 중복 문제 발생)
        if (!tutorStatus.equals("not_applied") && (tutor != null && tutor.getStatus().equals("rejected"))) {
            return "tutor/tutor_join";
        }

        // 이미 신청했거나 튜터인 경우 메인 페이지로 리다이렉트
        if (!tutorStatus.equals("not_applied")) {
            return "redirect:/tutor/tutor_page?alreadyApplied=true";
        }

        return "tutor/tutor_join";
    }

    //튜터 신청처리
    @PostMapping("/tutor_join_pro")
    public String tutor_join_pro(
            @ModelAttribute TutorBean tutorBean,
            @RequestParam("profileImage") MultipartFile profileImage,
            @RequestParam(value = "certFiles", required = false) List<MultipartFile> certificateFiles,
            @RequestParam(value = "keepFiles", required = false) List<Integer> keepFileIds,
            @RequestParam(value = "keepProfileImage", required = false) Boolean keepProfileImage,
            @RequestParam(value = "existingProfileImage", required = false) String existingProfileImage
    ) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 파일 필수 검증 - 재신청 시 기존 파일 유지하는 경우 고려
        boolean hasExistingFiles = keepFileIds != null && !keepFileIds.isEmpty();
        boolean hasNewFiles = certificateFiles != null && certificateFiles.stream().anyMatch(file -> !file.isEmpty());
        boolean hasExistingProfileImage = keepProfileImage != null && keepProfileImage && existingProfileImage != null;

        // 프로필 이미지가 비어있고, 기존 이미지도 유지하지 않는 경우에만 에러
        if ((profileImage.isEmpty() && !hasExistingProfileImage) || (!hasExistingFiles && !hasNewFiles)) {
            return "redirect:/tutor/tutor_join?error=file-required";
        }

        // 튜터 신청 처리 - TutorService 수정 필요
        boolean success = tutorService.applyForTutor(tutorBean, profileImage, certificateFiles, keepFileIds, hasExistingProfileImage, existingProfileImage);

        if (success) {
            return "redirect:/tutor/tutor_page?applySuccess=true";
        } else {
            return "redirect:/tutor/tutor_join?error=apply-failed";
        }
    }

    /**
     * 언어 추가 신청 페이지 호출
     */
    @GetMapping("/add_language")
    public String addLanguagePage(Model model) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        // 튜터 정보 조회
        TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());
        if (tutorInfo == null) {
            return "redirect:/tutor/tutor_page?error=not-tutor";
        }

        // 현재 승인된 상태가 아니면 접근 불가
        if (!"approved".equals(tutorInfo.getStatus())) {
            return "redirect:/tutor/dashboard?error=not-approved";
        }

        model.addAttribute("tutorInfo", tutorInfo);
        model.addAttribute("currentLanguages", tutorInfo.getTeachLanguageList());

        return "tutor/tutor_add_language";
    }

    /**
     * 언어 추가 신청 처리
     */
    @PostMapping("/add_language_pro")
    public String addLanguagePro(
            @RequestParam("newLanguage") String newLanguage,
            @RequestParam(value = "certFiles", required = false) List<MultipartFile> certificateFiles) {

        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        // 튜터 정보 조회
        TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());
        if (tutorInfo == null) {
            return "redirect:/tutor/tutor_page?error=not-tutor";
        }

        // 인증서 파일 필수 검증
        if (certificateFiles == null || certificateFiles.isEmpty() || certificateFiles.get(0).isEmpty()) {
            return "redirect:/tutor/add_language?error=file-required";
        }

        // 언어 추가 신청
        boolean success = tutorService.applyAdditionalLanguage(tutorInfo, newLanguage, certificateFiles);

        if (success) {
            return "redirect:/tutor/dashboard?languageApplySuccess=true";
        } else {
            return "redirect:/tutor/add_language?error=apply-failed";
        }
    }

    /**
     * 인증서 파일 삭제 API
     */
    @PostMapping("/delete-certificate/{fileId}")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> deleteCertificate(@PathVariable int fileId) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        // 파일 소유자 확인 (보안을 위해)
        CertificateFile file = certificateFileRepository.getFileByKey(fileId);
        if (file == null) {
            return ResponseEntity.status(404).body(Map.of("success", false));
        }

        TutorBean tutor = tutorService.getTutorByKey(file.getTutor_key());
        if (tutor == null || tutor.getUser_key() != loginUserBean.getUser_key()) {
            return ResponseEntity.status(403).body(Map.of("success", false));
        }

        // 파일 삭제 처리는 실제로 수행하지 않음 (튜터 신청 승인 전까지는 데이터 유지)
        // 클라이언트 측에서만 제거하고, 최종 제출 시 처리

        return ResponseEntity.ok(Map.of("success", true));
    }
}