package project.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.beans.*;
import project.beans.board.BoardBean;
import project.beans.board.BoardCommentBean;
import project.beans.board.BoardInfoBean;
import project.beans.board.PageBean;
import project.repository.CertificateFileRepository;
import project.service.BoardService;
import project.service.FileService;
import project.service.GroupService;
import project.service.TutorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CertificateFileRepository certificateFileRepository;

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;
    @Autowired
    private GroupService groupService;

    // 기본 게시판 페이지 리다이렉트
    @GetMapping("")
    public String boardMain() {
        // 기본값으로 워들리 토킹 게시판으로 리다이렉트
        return "redirect:/board/wordly-talking";
    }

    // 워들리 토킹 게시판
    @GetMapping("/wordly-talking")
    public String wordlyTalkingMain(Model model) {
        // 기본 카테고리 (자유 게시판)로 리다이렉트
        return "redirect:/board/wordly-talking/free";
    }

    // 워들리 토킹 - 카테고리 별 (페이징 추가)
    @GetMapping("/wordly-talking/{category}")
    public String wordlyTalkingCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            HttpServletRequest request) {

        model.addAttribute("currentMenu", "board");
        model.addAttribute("category", category);
        model.addAttribute("currentBoardType", "wordly-talking");

        // 카테고리에 따른 게시판 정보 가져오기
        BoardInfoBean boardInfoBean = boardService.getBoardInfoByType("wordly-" + category);
        model.addAttribute("boardInfo", boardInfoBean);

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 페이징 객체 생성 (현재 페이지, 한 페이지당 게시글 수, 페이지 그룹 사이즈)
        PageBean pageBean = new PageBean(page, 10, 5, 0);

        // 게시글 목록 가져오기 (페이징 적용)
        List<BoardBean> boardList = boardService.getBoardListWithPaging("wordly-" + category, userKey, pageBean);

        model.addAttribute("boardList", boardList);
        model.addAttribute("pageBean", pageBean);

        return "board/wordly-talking/index";
    }

    // 게시글 상세 페이지 (워들리 토킹)
    @GetMapping("/wordly-talking/detail/{postId}")
    public String wordlyTalkingDetail(@PathVariable int postId, Model model) {
        model.addAttribute("currentMenu", "board");
        model.addAttribute("currentBoardType", "wordly-talking");

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 게시글 상세 정보 가져오기
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);
        model.addAttribute("boardBean", boardBean);

        // 댓글 목록 가져오기
        List<BoardCommentBean> commentList = boardService.getBoardCommentList(postId);
        model.addAttribute("commentList", commentList);

        //학생 모집 게시판일 경우, 튜터 정보 담아서 보냄.
        if (boardBean.getBoard_info_key()==4) {

            int tutor_userKey =   boardBean.getUser_key();
            TutorBean tutorInfo = tutorService.getTutorByKey(tutor_userKey);
            List<CertificateFile> tutorFiles = certificateFileRepository.getFilesByTutorKey(tutor_userKey);

            //게시글 키와 그룹의 외래키가 일치하는 그룹이 있으면 가져옴
            if ((groupService.getGroupsByGroupBoardKey(boardBean.getBoard_key(), tutorInfo.getTutor_key()))!= null) {

                GroupBean groupBean = groupService.getGroupByGroupBoardKey(boardBean.getBoard_key());
                GroupJoinRequestBean requestBean = new GroupJoinRequestBean();
                int checkApply = groupService.checkExistingRequest(groupBean.getGroup_key(), loginUserBean.getUser_key());

                requestBean.setGroup_key(groupBean.getGroup_key()); //해당 그룹의 그룹키를 담고 신청 객체 내려보냄.

                model.addAttribute("groupBean", groupBean); //사용자가 볼 그룹 객체
                model.addAttribute("requestBean", requestBean);//사용자가 그룹 신청할 경우 값을 담을 신청 객체
                model.addAttribute("checkApply", checkApply);//사용자가 그룹에 신청 했는지 확인(중복 방지)

            }

            model.addAttribute("tutorInfo", tutorInfo); // 튜터 정보
            model.addAttribute("tutorFiles", tutorFiles); // 튜터 자격증, 이미지 파일 등

        }//학생 모집 게시글 로직

        return "board/wordly-talking/detail";
    }

    /**
     * PDF 파일 열람 처리(추가됨)
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

    /**
     * 워들리 토킹 게시글 작성 페이지
     */
    @GetMapping("/wordly-talking/write")
    public String wordlyTalkingWriteForm(@RequestParam(required = false) String category, Model model) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        model.addAttribute("currentMenu", "board");
        model.addAttribute("category", category);

        // 게시판 정보 목록 가져오기 (워들리 토킹 관련 게시판만)
        List<BoardInfoBean> boardInfoList = boardService.getBoardInfoList();

        //로그인 유저가 튜터가 아닌 경우 학생 모집 게시글은 작성하지 못하게 함.
        if (!(loginUserBean.getUser_type().equals("tutor"))) {
            boardInfoList.removeIf(boardInfoBean -> boardInfoBean.getBoard_info_key()==4);
        }

        model.addAttribute("boardInfoList", boardInfoList);

        //로그인 유저가 튜터, 학생 모집 게시글을 작성할 경우, 튜터 정보를 내려보냄.
        if (loginUserBean.getUser_type().equals("tutor")) {

            TutorBean tutorInfo = tutorService.getTutorByUserKey(loginUserBean.getUser_key());

            // 튜터가 그룹을 생성하면 리스트에 담아 보내고, 없으면 비워서 내려보냄(게시글 없는(null) 객체만 담아옴)
            if (groupService.getGroupsByGroupBoardKey(null,tutorInfo.getTutor_key()) != null) {
                List<GroupBean> groupList = groupService.getGroupsByGroupBoardKey(null,tutorInfo.getTutor_key());



                model.addAttribute("groupList", groupList);
            } else {
                model.addAttribute("groupList", null);
            }

            model.addAttribute("tutorInfo", tutorInfo);

        }

        // 새 게시글 객체
        BoardBean boardBean = new BoardBean();
        model.addAttribute("boardBean", boardBean);

        return "board/wordly-talking/write";
    }

    /**
     * 게시글 작성 처리
     */
    @PostMapping("/wordly-talking/write_pro")
    public String wordlyTalkingWritePro(@ModelAttribute("boardBean") BoardBean boardBean,
                                        @RequestParam(value = "selectedGroup", required = false) Integer selectedGroup) {//학생 모집 게시글의 경우, 튜터의 그룹 값 받아옴
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 사용자 키 설정
        boardBean.setUser_key(loginUserBean.getUser_key());

        // 게시글 작성
        boardService.writeBoard(boardBean);

        System.out.println(selectedGroup);

        // 선택된 그룹이 있는 경우 처리
        if (selectedGroup != null) {//selectedGroup은 글 작성때 선택한 그룹의 기본키
            // 그룹과 연결된 게시글 값 있는지 확인 후, 업데이트 진행
            GroupBean group = groupService.getGroupByKey(selectedGroup);
             if (group.getGroup_board_key() == null) {
                 group.setGroup_board_key(boardBean.getBoard_key());

                 System.out.println(boardBean.getBoard_key());
                 System.out.println(group.getGroup_board_key());

                 groupService.updateGroupBoardKey(group);
             }

        }

        // 게시판 목록 페이지로 리다이렉트
        String category = "";
        BoardInfoBean boardInfoBean = boardService.getBoardInfoByKey(boardBean.getBoard_info_key());
        if(boardInfoBean != null) {
            // 예: wordly-free에서 free만 추출
            category = boardInfoBean.getBoard_info_type().replace("wordly-", "");
        }

        return "redirect:/board/wordly-talking/" + category;
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/wordly-talking/modify/{postId}")
    public String wordlyTalkingModifyForm(@PathVariable int postId, Model model) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        model.addAttribute("currentMenu", "board");

        // 게시글 정보 가져오기
        int userKey = loginUserBean.getUser_key();
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);

        // 작성자 확인
        if(boardBean.getUser_key() != userKey) {
            return "redirect:/board/wordly-talking";
        }

        model.addAttribute("boardBean", boardBean);

        return "board/wordly-talking/modify";
    }

    /**
     * 게시글 수정 처리
     */
    @PostMapping("/wordly-talking/modify_pro")
    public String wordlyTalkingModifyPro(@ModelAttribute("boardBean") BoardBean boardBean) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 사용자 키 설정
        boardBean.setUser_key(loginUserBean.getUser_key());

        // 게시글 수정
        boardService.updateBoard(boardBean);

        return "redirect:/board/wordly-talking/detail/" + boardBean.getBoard_key();
    }

    /**
     * 게시글 삭제 처리
     */
    @GetMapping("/wordly-talking/delete/{postId}")
    public String wordlyTalkingDelete(@PathVariable int postId) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 게시글 정보 가져오기
        int userKey = loginUserBean.getUser_key();
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);

        // 작성자 확인
        if(boardBean.getUser_key() != userKey) {
            return "redirect:/board/wordly-talking";
        }

        // 게시글 삭제
        boardBean.setUser_key(userKey);
        boardService.deleteBoard(boardBean);

        // 게시판 카테고리 추출
        String category = boardBean.getBoard_info_type().replace("wordly-", "");

        return "redirect:/board/wordly-talking/" + category;
    }

    /**
     * 워들리 토킹 검색 결과 페이지 (페이징 추가)
     */
    @GetMapping("/wordly-talking/search")
    public String wordlyTalkingSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "free") String category,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        model.addAttribute("currentMenu", "board");
        model.addAttribute("searchQuery", q);
        model.addAttribute("category", category);
        model.addAttribute("currentBoardType", "wordly-talking");

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 페이징 객체 생성
        PageBean pageBean = new PageBean(page, 10, 5, 0);

        // 검색 결과 가져오기 (페이징 적용)
        List<BoardBean> searchResults = boardService.searchBoardWithPaging(q, "wordly-" + category, userKey, pageBean);

        model.addAttribute("boardList", searchResults);
        model.addAttribute("pageBean", pageBean);

        return "board/wordly-talking/search";
    }

    /**
     * 댓글 작성 AJAX 처리 - 수정
     */
    @PostMapping("/api/comment/write")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> writeComment(@RequestBody BoardCommentBean commentBean) {
        Map<String, Object> result = new HashMap<>();

        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        // 사용자 키 설정
        commentBean.setComment_user_key(loginUserBean.getUser_key());

        try {
            // 게시글 정보 가져오기
            BoardBean boardBean = boardService.getBoardDetail(commentBean.getComment_board_key(), loginUserBean.getUser_key());

            // 비즈니스 게시판 댓글 권한 체크 (새로 추가)
            if(boardBean != null && "connect-tutor".equals(boardBean.getBoard_info_type())) {
                boolean isAuthor = boardBean.getUser_key() == loginUserBean.getUser_key();
                boolean isTutor = "tutor".equals(loginUserBean.getUser_type());

                if(!isAuthor && !isTutor) {
                    result.put("success", false);
                    result.put("message", "비즈니스 게시판의 댓글은 작성자와 튜터만 작성할 수 있습니다.");
                    return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
                }
            }

            // 댓글 작성
            boardService.writeComment(commentBean);

            // 댓글 목록 다시 가져오기
            List<BoardCommentBean> commentList = boardService.getBoardCommentList(commentBean.getComment_board_key());

            result.put("success", true);
            result.put("commentList", commentList);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "댓글 작성 중 오류가 발생했습니다.");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 댓글 삭제 AJAX 처리
     */
    @PostMapping("/api/comment/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComment(@RequestBody BoardCommentBean commentBean) {
        Map<String, Object> result = new HashMap<>();

        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        // 사용자 키 설정
        commentBean.setComment_user_key(loginUserBean.getUser_key());

        try {
            // 댓글 삭제
            boardService.deleteComment(commentBean);

            // 댓글 목록 다시 가져오기
            List<BoardCommentBean> commentList = boardService.getBoardCommentList(commentBean.getComment_board_key());

            result.put("success", true);
            result.put("commentList", commentList);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "댓글 삭제 중 오류가 발생했습니다.");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 좋아요 토글 AJAX 처리
     */
    @PostMapping("/api/like/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestParam int boardKey) {
        Map<String, Object> result = new HashMap<>();

        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        try {
            // 좋아요 토글
            boolean isLiked = boardService.toggleLike(boardKey, loginUserBean.getUser_key());

            // 좋아요 수 가져오기
            int likeCount = boardService.getBoardDetail(boardKey, loginUserBean.getUser_key()).getLike_count();

            result.put("success", true);
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "좋아요 처리 중 오류가 발생했습니다.");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 대댓글 작성 AJAX 처리 - 수정
     */
    @PostMapping("/api/comment/reply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> writeReply(@RequestBody BoardCommentBean commentBean) {
        Map<String, Object> result = new HashMap<>();

        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        // 사용자 키 설정
        commentBean.setComment_user_key(loginUserBean.getUser_key());

        try {
            // 게시글 정보 가져오기
            BoardBean boardBean = boardService.getBoardDetail(commentBean.getComment_board_key(), loginUserBean.getUser_key());

            // 비즈니스 게시판 댓글 권한 체크 (새로 추가)
            if(boardBean != null && "connect-tutor".equals(boardBean.getBoard_info_type())) {
                boolean isAuthor = boardBean.getUser_key() == loginUserBean.getUser_key();
                boolean isTutor = "tutor".equals(loginUserBean.getUser_type());

                if(!isAuthor && !isTutor) {
                    result.put("success", false);
                    result.put("message", "비즈니스 게시판의 댓글은 작성자와 튜터만 작성할 수 있습니다.");
                    return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
                }
            }

            // 대댓글 작성
            boardService.writeReply(commentBean);

            // 댓글 목록 다시 가져오기
            List<BoardCommentBean> commentList = boardService.getBoardCommentList(commentBean.getComment_board_key());

            result.put("success", true);
            result.put("commentList", commentList);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "대댓글 작성 중 오류가 발생했습니다.");
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 커넥트 게시판 메인 페이지
     */
    @GetMapping("/connect")
    public String connectMain(Model model) {
        model.addAttribute("currentMenu", "board");
        return "redirect:/board/connect/tutor";
    }

    /**
     * 커넥트 카테고리별 페이지 (페이징 추가)
     */
    @GetMapping("/connect/{category}")
    public String connectCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        model.addAttribute("currentMenu", "board");
        model.addAttribute("category", category);
        model.addAttribute("currentBoardType", "connect");

        // 카테고리에 따른 게시판 정보 가져오기
        BoardInfoBean boardInfoBean = boardService.getBoardInfoByType("connect-" + category);
        model.addAttribute("boardInfo", boardInfoBean);

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 페이징 객체 생성
        PageBean pageBean = new PageBean(page, 10, 5, 0);

        // 게시글 목록 가져오기 (페이징 적용)
        List<BoardBean> boardList = boardService.getBoardListWithPaging("connect-" + category, userKey, pageBean);

        model.addAttribute("boardList", boardList);
        model.addAttribute("pageBean", pageBean);

        return "board/connect/index";
    }

    /**
     * 커넥트 게시글 상세 페이지
     */
    @GetMapping("/connect/detail/{postId}")
    public String connectDetail(@PathVariable int postId, Model model) {
        model.addAttribute("currentMenu", "board");

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 게시글 상세 정보 가져오기
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);
        model.addAttribute("boardBean", boardBean);

        // 댓글 목록 가져오기
        List<BoardCommentBean> commentList = boardService.getBoardCommentList(postId);
        model.addAttribute("commentList", commentList);

        return "board/connect/detail";
    }

    /**
     * 커넥트 게시글 작성 페이지
     */
    @GetMapping("/connect/write")
    public String connectWriteForm(@RequestParam(required = false) String category, Model model) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        model.addAttribute("currentMenu", "board");
        model.addAttribute("category", category);

        // 게시판 정보 목록 가져오기 (커넥트 관련 게시판만)
        List<BoardInfoBean> boardInfoList = boardService.getBoardInfoList();
        model.addAttribute("boardInfoList", boardInfoList);

        // 새 게시글 객체
        BoardBean boardBean = new BoardBean();
        model.addAttribute("boardBean", boardBean);

        return "board/connect/write";
    }

    /**
     * 커넥트 게시글 작성 처리
     */
    @PostMapping("/connect/write_pro")
    public String connectWritePro(@ModelAttribute("boardBean") BoardBean boardBean) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 사용자 키 설정
        boardBean.setUser_key(loginUserBean.getUser_key());

        // 게시글 작성
        boardService.writeBoard(boardBean);

        // 게시판 정보 가져오기
        BoardInfoBean boardInfoBean = boardService.getBoardInfoByKey(boardBean.getBoard_info_key());

        // 카테고리 결정
        String category = "tutor"; // 기본값 설정

        if(boardInfoBean != null) {
            // connect- 접두사 제거
            if(boardInfoBean.getBoard_info_type().startsWith("connect-")) {
                category = boardInfoBean.getBoard_info_type().replace("connect-", "");
            }
        }

        return "redirect:/board/connect/" + category;
    }

    /**
     * 커넥트 게시글 수정 페이지
     */
    @GetMapping("/connect/modify/{postId}")
    public String connectModifyForm(@PathVariable int postId, Model model) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        model.addAttribute("currentMenu", "board");

        // 게시글 정보 가져오기
        int userKey = loginUserBean.getUser_key();
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);

        // 작성자 확인
        if(boardBean.getUser_key() != userKey) {
            return "redirect:/board/connect";
        }

        model.addAttribute("boardBean", boardBean);

        return "board/connect/modify";
    }

    /**
     * 커넥트 게시글 수정 처리
     */
    @PostMapping("/connect/modify_pro")
    public String connectModifyPro(@ModelAttribute("boardBean") BoardBean boardBean) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 사용자 키 설정
        boardBean.setUser_key(loginUserBean.getUser_key());

        // 게시글 수정
        boardService.updateBoard(boardBean);

        return "redirect:/board/connect/detail/" + boardBean.getBoard_key();
    }

    /**
     * 커넥트 게시글 삭제 처리
     */
    @GetMapping("/connect/delete/{postId}")
    public String connectDelete(@PathVariable int postId) {
        // 로그인 체크
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login";
        }

        // 게시글 정보 가져오기
        int userKey = loginUserBean.getUser_key();
        BoardBean boardBean = boardService.getBoardDetail(postId, userKey);

        // 작성자 확인
        if(boardBean.getUser_key() != userKey) {
            return "redirect:/board/connect";
        }

        // 게시글 삭제
        boardBean.setUser_key(userKey);
        boardService.deleteBoard(boardBean);

        // 게시판 카테고리 추출
        String category = boardBean.getBoard_info_type().replace("connect-", "");

        return "redirect:/board/connect/" + category;
    }

    /**
     * 커넥트 검색 결과 페이지 (페이징 추가)
     */
    @GetMapping("/connect/search")
    public String connectSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "tutor") String category,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        model.addAttribute("currentMenu", "board");
        model.addAttribute("searchQuery", q);
        model.addAttribute("category", category);
        model.addAttribute("currentBoardType", "connect");

        // 로그인한 사용자 키 (로그인하지 않았으면 0)
        int userKey = loginUserBean.isLogin() ? loginUserBean.getUser_key() : 0;

        // 페이징 객체 생성
        PageBean pageBean = new PageBean(page, 10, 5, 0);

        // 검색 결과 가져오기 (페이징 적용)
        List<BoardBean> searchResults = boardService.searchBoardWithPaging(q, "connect-" + category, userKey, pageBean);

        model.addAttribute("boardList", searchResults);
        model.addAttribute("pageBean", pageBean);

        return "board/connect/search";
    }

    /**
     * 테스트용 게시글 대량 생성 API
     * 사용 예: /board/api/generate-test-posts?type=wordly-free&count=100&userKey=1
     */
    @PostMapping("/api/generate-test-posts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateTestPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "3") int userKey) {

        Map<String, Object> result = new HashMap<>();

        try {
            // board_info_key 가져오기
            BoardInfoBean boardInfoBean = boardService.getBoardInfoByType(type);
            if(boardInfoBean == null) {
                result.put("success", false);
                result.put("message", "존재하지 않는 게시판 타입입니다: " + type);
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }

            // 테스트 게시글 생성
            List<BoardBean> generatedPosts = new ArrayList<>();
            for(int i = 1; i <= count; i++) {
                BoardBean post = new BoardBean();
                post.setBoard_info_key(boardInfoBean.getBoard_info_key());
                post.setUser_key(userKey);
                post.setBoard_title("테스트 게시글 #" + i + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                post.setBoard_text("이 게시글은 페이징 테스트를 위해 자동 생성된 게시글입니다.\n\n"
                        + "게시판 타입: " + type + "\n"
                        + "생성 시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n"
                        + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam auctor, nunc eget ultricies ultrices, "
                        + "nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl. Nullam auctor, nunc eget ultricies ultrices, "
                        + "nisl nisl aliquam nisl, eget aliquam nisl nisl sit amet nisl.");

                boardService.writeBoard(post);
                generatedPosts.add(post);
            }

            result.put("success", true);
            result.put("message", count + "개의 테스트 게시글이 생성되었습니다.");
            result.put("type", type);
            result.put("boardInfoKey", boardInfoBean.getBoard_info_key());
            result.put("count", count);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "게시글 생성 중 오류 발생: " + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}