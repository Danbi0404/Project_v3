/**
 * PaymentService.java - 결제 관련 서비스
 * 결제 처리 및 결제 내역 조회 기능을 담당합니다.
 * 관련 클래스: PaymentController, PaymentRepository, PaymentMapper
 */
package project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.beans.PaymentBean;
import project.repository.PaymentRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * 결제 정보 저장
     */
    public void insert_payment(PaymentBean paymentBean) {
        // 결제일이 없는 경우 현재 날짜 설정
        if (paymentBean.getPayment_date() == null || paymentBean.getPayment_date().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            paymentBean.setPayment_date(sdf.format(new Date()));
        }

        // 결제 상태가 없는 경우 기본값 설정
        if (paymentBean.getStatus() == null || paymentBean.getStatus().isEmpty()) {
            paymentBean.setStatus("success");
        }

        paymentRepository.insert_payment(paymentBean);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    public Map<String, Object> get_user_payment_history(int user_key) {
        Map<String, Object> result = new HashMap<>();

        // 사용자의 결제 내역 조회
        List<PaymentBean> payments = paymentRepository.get_payments_by_user(user_key);

        // 결과 맵 생성
        result.put("payments", payments);
        result.put("count", payments.size());

        return result;
    }

    /**
     * 사용자가 현재 활성화된 구독을 가지고 있는지 확인
     */
    public boolean hasActiveSubscription(int user_key) {
        // 현재 날짜
        String currentDate = format_current_date();

        // 사용자의 결제 내역 확인
        List<PaymentBean> payments = paymentRepository.get_payments_by_user(user_key);

        // 활성화된 구독이 있는지 확인
        for (PaymentBean payment : payments) {
            if (payment.getStatus().equals("success")) {
                // 현재 날짜가 구독 기간 내에 있는지 확인
                if (currentDate.compareTo(payment.getSubscribe_start_time()) >= 0 &&// 오늘 날짜가 시작일보다 이후거나 같을때
                        currentDate.compareTo(payment.getSubscribe_end_time()) <= 0) {// 오늘 날짜가 종료일보다 이전이거나 같을때
                    return true; // 활성 구독 있음
                }//compareto는 일종의 뺄셈 -> 2023/05/01 compareto 2023/05/02 => 결과는 음수(-1), 5월1일은 2일보다 이전
                //2023/05/01 compareto 2023/05/01 = 0 => 결과가 0으로 같은 날임.
                //2023/05/02 compareto 2023/05/01 = 1 => 결과가 양수, 5월2일은 5월1일보다 이후
            }
        }

        return false; // 활성 구독 없음
    }

    /**
     * 사용자의 활성화된 구독 정보 조회
     */
    public Map<String, Object> getActiveSubscriptionInfo(int user_key) {
        Map<String, Object> result = new HashMap<>();

        // 현재 날짜
        String currentDate = format_current_date();

        // 사용자의 결제 내역 확인
        List<PaymentBean> payments = paymentRepository.get_payments_by_user(user_key);

        // 가장 최근의 활성화된 구독 찾기
        PaymentBean activeSubscription = null;

        for (PaymentBean payment : payments) {
            if ("success".equals(payment.getStatus())) {
                // 현재 날짜가 구독 기간 내에 있는지 확인
                if (currentDate.compareTo(payment.getSubscribe_start_time()) >= 0 &&
                        currentDate.compareTo(payment.getSubscribe_end_time()) <= 0) {

                    // 가장 최근에 결제한 구독 선택
                    if (activeSubscription == null ||
                            payment.getPayment_date().compareTo(activeSubscription.getPayment_date()) > 0) {//
                        activeSubscription = payment;
                    }
                }
            }
        }

        if (activeSubscription != null) {
            result.put("startDate", activeSubscription.getSubscribe_start_time());
            result.put("endDate", activeSubscription.getSubscribe_end_time());
            result.put("paymentDate", activeSubscription.getPayment_date());
            result.put("amount", activeSubscription.getAmount());
            result.put("paymentMethod", activeSubscription.getPayment_method());
        }

        return result;
    }

    /**
     * 현재 날짜를 YYYY/MM/DD 형식으로 반환
     */
    public String format_current_date() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(new Date());
    }

    /**
     * 현재 날짜에 개월 수를 더한 날짜 반환
     */
    public String add_months_to_date(int months) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MONTH, months);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(calendar.getTime());
    }
}