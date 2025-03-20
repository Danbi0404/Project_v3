/**
 * PaymentRepository.java - 결제 관련 DB 접근 클래스
 * 결제 정보 관련 데이터베이스 작업을 담당합니다.
 * 관련 클래스: PaymentService, PaymentMapper
 */
package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.PaymentBean;
import project.mapper.PaymentMapper;

import java.util.List;

@Repository
public class PaymentRepository {

    @Autowired
    private PaymentMapper paymentMapper;

    /**
     * 결제 정보 저장
     */
    public void insert_payment(PaymentBean paymentBean) {
        paymentMapper.insert_payment_log(paymentBean);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    public List<PaymentBean> get_payments_by_user(int user_key) {
        return paymentMapper.get_payment_logs_by_user_key(user_key);
    }

    /**
     * 결제 정보 단일 조회
     */
    public PaymentBean get_payment(int payment_log_key) {
        return paymentMapper.get_payment_log(payment_log_key);
    }

    /**
     * 모든 결제 내역 조회
     */
    public List<PaymentBean> get_all_payments() {
        return paymentMapper.get_all_payment_logs();
    }
}