package project.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.beans.CertificateFile;
import project.beans.TutorBean;
import project.beans.UserBean;
import project.mapper.UserMapper;
import project.repository.CertificateFileRepository;
import project.service.FileService;
import project.service.TutorService;
import project.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CertificateFileRepository certificateFileRepository;

    @GetMapping("/admin_page")
    public String adminPage(Model model) {
     return "admin/admin_page";
    }

    @PostMapping("/admin_login_pro")
    public String admin_login_pro(HttpServletRequest request) {
        String admin_pass = request.getParameter("admin_pass");

        if (admin_pass.equals("soldesk")) {
            // 관리자 세션을 직접 설정 (DB 조회 없이)
            loginUserBean.setUser_key(0);
            loginUserBean.setUser_id("admin");
            loginUserBean.setUser_name("관리자");
            loginUserBean.setUser_nickname("관리자");
            loginUserBean.setUser_type("admin");
            loginUserBean.setLogin(true);
            // 필요한 다른 필드도 설정

            return "admin/admin_page";
        } else {
            return "redirect:/main";
        }

    }

    /**
     * 관리자 전용 - 튜터 신청 목록에 상태 필터 추가
     */
    @GetMapping("/admin_tutor_list")
    public String admin_tutor_list(Model model, @RequestParam(required = false) String status) {
        // 관리자 권한 검증
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("admin")) {
            return "redirect:/main?accessDenied=true";
        }

        List<TutorBean> tutorList;

        // 상태별 목록 조회
        if (status != null && !status.isEmpty()) {
            tutorList = tutorService.getTutorsByStatus(status);
        } else {
            // 전체 튜터 목록 조회
            tutorList = tutorService.getAllTutors();
        }

        model.addAttribute("tutorList", tutorList);
        model.addAttribute("currentStatus", status != null ? status : "all");

        return "admin/admin_tutor_list";
    }


    /**
     * 관리자 전용 - 튜터 신청 상세 정보
     */
    @GetMapping("/admin_tutor_detail/{tutorKey}")
    public String admin_tutor_detail(@PathVariable int tutorKey, Model model) {
        // 관리자 권한 검증
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("admin")) {
            return "redirect:/main?accessDenied=true";
        }

        // 튜터 정보 조회
        TutorBean tutorInfo = tutorService.getTutorByKey(tutorKey);
        if (tutorInfo == null) {
            return "redirect:/admin/admin_tutor_list?error=not-found";
        }

        // 인증서 파일 목록 조회 - tutor_key 기준으로 조회
        List<CertificateFile> certificateFiles = certificateFileRepository.getFilesByTutorKey(tutorKey);

        model.addAttribute("tutorInfo", tutorInfo);
        model.addAttribute("certificateFiles", certificateFiles);
        return "admin/admin_tutor_detail";
    }

    /**
     * 관리자 전용 - 튜터 승인 처리
     */
    @PostMapping("/approve/{tutorKey}")
    public String approve_tutor(@PathVariable int tutorKey) {
        // 관리자 권한 검증
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("admin")) {
            return "redirect:/main?accessDenied=true";
        }

        // 튜터 정보 조회하여 상태 확인
        TutorBean tutorInfo = tutorService.getTutorByKey(tutorKey);
        if (tutorInfo == null) {
            return "redirect:/admin/admin_tutor_list?error=not-found";
        }

        boolean success;

        // 상태에 따라 분기 처리
        if ("reapply".equals(tutorInfo.getStatus())) {
            // 재신청 승인 처리 (기존 레코드 업데이트)
            success = tutorService.approveReapplyTutor(tutorKey);
        } else {
            // 일반 승인 처리
            success = tutorService.approveTutor(tutorKey);
        }

        if (success) {
            return "redirect:/admin/admin_tutor_list?approveSuccess=true";
        } else {
            return "redirect:/admin/admin_tutor_detail/" + tutorKey + "?error=approve-failed";
        }
    }

    /**
     * 추가 언어 신청 승인 처리
     */
    @PostMapping("/approve_language/{tutorKey}")
    public String approve_language(@PathVariable int tutorKey) {
        // 관리자 권한 검증
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("admin")) {
            return "redirect:/main?accessDenied=true";
        }

        boolean success = tutorService.approveAdditionalLanguage(tutorKey);
        if (success) {
            return "redirect:/admin/admin_tutor_list?approveSuccess=true";
        } else {
            return "redirect:/admin/admin_tutor_detail/" + tutorKey + "?error=approve-failed";
        }
    }

    /**
     * 관리자 전용 - 튜터 거부 처리 (거부 사유 포함)
     */
    @PostMapping("/reject/{tutorKey}")
    public String reject_tutor(
            @PathVariable int tutorKey,
            @RequestParam("rejectReason") String rejectReason
    ) {
        // 관리자 권한 검증
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("admin")) {
            return "redirect:/main?accessDenied=true";
        }

        boolean success = tutorService.rejectTutor(tutorKey, rejectReason);
        if (success) {
            return "redirect:/admin/admin_tutor_list?rejectSuccess=true";
        } else {
            return "redirect:/admin/admin_tutor_detail/" + tutorKey + "?error=reject-failed";
        }
    }

    /**
     * PDF 파일 열람 처리
     */
    @GetMapping("/view-pdf/{fileKey}")
    public ResponseEntity<byte[]> viewPDF(@PathVariable int fileKey) {
        try {
            // 파일 정보 조회
            CertificateFile certFile = certificateFileRepository.getFileByKey(fileKey);
            if (certFile == null) {
                return ResponseEntity.notFound().build();
            }

            // FileService를 통해 파일 읽기
            byte[] fileContent = fileService.readFile(certFile.getFile_path());

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("certificate.pdf").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
