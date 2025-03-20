/**
 * PaymentController.java - 결제 관련 컨트롤러
 * 구독 결제 처리 및 결제 내역 조회 기능을 담당합니다.
 * URL 맵핑:
 * - /payment: 결제 페이지
 * - /payment/verify/{imp_uid}: 결제 검증 API
 * - /payment/history: 결제 내역 조회 API
 */
package project.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.beans.PaymentBean;
import project.beans.UserBean;
import project.service.PaymentService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    private IamportClient client;

    @Value("${IAMPORT_REST_API_KEY}")
    private String apiKey;

    @Value("${IAMPORT_REST_API_SECRET}")
    private String secretKey;

    /**
     * 결제 클라이언트 초기화
     */
    @PostConstruct
    public void init() {
        this.client = new IamportClient(apiKey, secretKey);
    }

    /**
     * 결제 검증 API
     */
    @PostMapping("/verify/{imp_uid}")
    @ResponseBody
    public ResponseEntity<?> verify_payment(
            @PathVariable String imp_uid,
            @RequestBody Map<String, Object> requestData) throws IamportResponseException, IOException {

        // 포트원 API를 통해 결제 정보 조회
        IamportResponse<Payment> payment = client.paymentByImpUid(imp_uid);
        log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse().getMerchantUid());

        // 결제 금액 검증
        int amount = ((Number)requestData.get("amount")).intValue();
        if(payment.getResponse().getAmount().intValue() != amount) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "결제 금액이 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 결제 정보 DB 저장
        PaymentBean paymentBean = new PaymentBean();
        paymentBean.setUser_key(loginUserBean.getUser_key());
        paymentBean.setAmount(amount);

        // 날짜 형식 로깅
        log.info("받은 시작 날짜: {}", requestData.get("start_date"));
        log.info("받은 종료 날짜: {}", requestData.get("end_date"));
        log.info("결제 날짜: {}", payment.getResponse().getPaidAt());

        paymentBean.setSubscribe_start_time((String)requestData.get("start_date"));
        paymentBean.setSubscribe_end_time((String)requestData.get("end_date"));

        // 결제일은 현재 시스템 날짜로 사용
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd");
        paymentBean.setPayment_date(sdf.format(new java.util.Date()));

        paymentBean.setPayment_method((String)requestData.get("method"));
        paymentBean.setMerchant_uid(payment.getResponse().getMerchantUid());
        paymentBean.setImp_uid(imp_uid);
        paymentBean.setStatus("success");

        paymentService.insert_payment(paymentBean);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", payment.getResponse());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 페이지 호출
     */
    @GetMapping("")
    public String payment(Model model) {
        // 로그인 여부 확인
        if(!loginUserBean.isLogin()) {
            return "redirect:/user/login?needLogin=true";
        }

        // 사용자의 현재 구독 상태 확인
        boolean isSubscribed = paymentService.hasActiveSubscription(loginUserBean.getUser_key());

        if(isSubscribed) {//구독한 내역이 있다면
            // 구독 정보 가져오기
            Map<String, Object> subscriptionInfo = paymentService.getActiveSubscriptionInfo(loginUserBean.getUser_key());
            model.addAttribute("subscriptionInfo", subscriptionInfo);
            model.addAttribute("isSubscribed", true);

        } else {//아직 구독한 내역이 없다면
            model.addAttribute("isSubscribed", false);
        }

        return "payment/payment";
    }

    /**
     * 결제 내역 조회 API
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> get_payment_history() {
        // 로그인 여부 확인
        if(!loginUserBean.isLogin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인이 필요한 서비스입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 결제 내역 조회
        Map<String, Object> result = paymentService.get_user_payment_history(loginUserBean.getUser_key());
        return ResponseEntity.ok(result);
    }
}