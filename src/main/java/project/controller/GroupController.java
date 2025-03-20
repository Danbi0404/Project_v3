package project.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.beans.*;
import project.service.GroupService;
import project.service.TutorService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/group")
public class GroupController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private GroupService groupService;

    @Autowired
    private TutorService tutorService;

    /**
     * 그룹 생성 페이지
     */
    @GetMapping("/create")
    public String groupCreatePage(Model model, @RequestParam(required = false) String needLanguage) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        // 현재 로그인한 user_key로 튜터 정보 조회
        TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());
        if (tutorInfo == null) {
            return "redirect:/tutor/tutor_page?error=not-tutor";
        }

        model.addAttribute("tutorKey", tutorInfo.getTutor_key());

        // 필요한 언어가 있으면 모델에 추가
        if (needLanguage != null && !needLanguage.isEmpty()) {
            model.addAttribute("needLanguage", needLanguage);
        }

        // 튜터가 가르칠 수 있는 언어 목록 추가
        model.addAttribute("tutorLanguages", tutorInfo.getTeachLanguageList());

        return "tutor/tutor_group_create";
    }

    /**
     * 그룹 생성 처리
     */
    @PostMapping("/create_pro")
    public String createGroup(@ModelAttribute GroupBean groupBean) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        // 현재 로그인한 user_key로 튜터 정보 조회
        TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());
        if (tutorInfo == null) {
            return "redirect:/tutor/tutor_page?error=not-tutor";
        }

        // 가르칠 언어가 튜터의 가르칠 언어와 일치하는지 확인
        if (!tutorInfo.getTeach_language().equals(groupBean.getTeach_language())) {
            // 언어가 일치하지 않는 경우, 언어 추가 신청 페이지로 리다이렉트
            return "redirect:/group/create?needLanguage=" + groupBean.getTeach_language();
        }

        // 튜터 키 설정
        groupBean.setTutor_key(tutorInfo.getTutor_key());
        // 모집 글을 작성하지 않았으므로 기본값 null
        groupBean.setGroup_board_key(null);

        // 그룹 생성
        boolean success = groupService.createGroup(groupBean);

        if (success) {
            return "redirect:/tutor/dashboard?createSuccess=true";
        } else {
            return "redirect:/group/create?error=create-failed";
        }
    }

    /**
     * 그룹 신청 처리
     */
    @PostMapping("/join_group")
    public String joinGroup(@ModelAttribute GroupJoinRequestBean requestBean, Model model) {

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;
        requestBean.setUser_key(userKey);

        System.out.println(requestBean.getGroup_key());
        System.out.println(requestBean.getUser_key());
        System.out.println(requestBean.getRequest_message());

        //게시판에서 받아온 신청 정보로 데이터 생성
        if(groupService.checkExistingRequest(requestBean.getGroup_key(), requestBean.getUser_key())==0) {
            //중복된 신청이 아니면, 신청
            groupService.createJoinRequest(requestBean);
        }


        //게시판 홈으로 돌려보내기
        return "redirect:/board/wordly-talking/free";
    }

    /**
     * 그룹 관리 페이지
     */
    @GetMapping("/manage/{groupKey}")
    public String manageGroup(@PathVariable int groupKey, Model model) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        // 그룹 정보 조회
        GroupBean groupInfo = groupService.getGroupByKey(groupKey);
        if (groupInfo == null) {
            return "redirect:/tutor/dashboard?error=group-not-found";
        }

        // 가입 신청 목록 조회
        List<GroupJoinRequestBean> pendingRequests = groupService.getPendingRequestsByGroupKey(groupKey);
        //그룹에 가입된 멤버 정보 목록 조회
        List<GroupMemberBean> groupMemberList = groupService.getMembersByGroupKey(groupKey);

        model.addAttribute("groupInfo", groupInfo);
        model.addAttribute("groupMemberList", groupMemberList);
        model.addAttribute("pendingRequests", pendingRequests);

        return "tutor/tutor_group_manage";
    }

    /**
     * 가입 신청 승인 처리
     */
    @PostMapping("/approve/{requestKey}")
    public String approveRequest(@PathVariable int requestKey, @RequestParam int groupKey) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        boolean success = groupService.approveJoinRequest(requestKey);
        if (success) {
            return "redirect:/group/manage/" + groupKey + "?approveSuccess=true";
        } else {
            return "redirect:/group/manage/" + groupKey + "?error=approve-failed";
        }
    }

    /**
     * 가입 신청 거부 처리
     */
    @PostMapping("/reject/{requestKey}")
    public String rejectRequest(
            @PathVariable int requestKey,
            @RequestParam int groupKey,
            @RequestParam("rejectReason") String rejectReason
    ) {
        // 로그인 및 튜터 상태 확인
        if (!loginUserBean.isLogin() || !loginUserBean.getUser_type().equals("tutor")) {
            return "redirect:/main?accessDenied=true";
        }

        boolean success = groupService.rejectJoinRequest(requestKey, rejectReason);
        if (success) {
            return "redirect:/group/manage/" + groupKey + "?rejectSuccess=true";
        } else {
            return "redirect:/group/manage/" + groupKey + "?error=reject-failed";
        }
    }

    /**
     * 그룹 정보 API (AJAX용)
     */
    @GetMapping("/api/group-info/{groupKey}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGroupInfo(@PathVariable int groupKey) {
        GroupBean groupInfo = groupService.getGroupByKey(groupKey);

        Map<String, Object> response = new HashMap<>();
        if (groupInfo != null) {
            response.put("success", true);
            response.put("data", groupInfo);
        } else {
            response.put("success", false);
            response.put("message", "그룹 정보를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(response);
    }
}