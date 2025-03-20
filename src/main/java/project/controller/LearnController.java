package project.controller;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import project.beans.UserBean;

@Controller
@RequestMapping("/learn")
public class LearnController {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @GetMapping("/learn_page")
    public String learn_page(Model model) {
        // 로그인 상태 확인
        if (!loginUserBean.isLogin()) {
            return "redirect:/main?needLogin=true";
        }

        // 여기에 학습 데이터 로드 로직 추가 (나중에 서비스 레이어 구현 시)
        // model.addAttribute("learningData", learningService.getLearningData());

        return "learn/learn_page";
    }
}