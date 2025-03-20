/**
 * HomeController.java - 홈 컨트롤러
 * 루트 경로("/") 요청을 메인 페이지로 리다이렉트합니다.
 */
package project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String index() {
        return "redirect:/main";
    }
}