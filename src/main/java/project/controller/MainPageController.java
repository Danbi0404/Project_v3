/**
 * MainPageController.java - 메인 페이지 컨트롤러
 * 메인 페이지 요청을 처리합니다.
 */
package project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    /**
     * 메인 페이지 호출
     */
    @GetMapping("/main")
    public String main() {
        return "main";
    }
}