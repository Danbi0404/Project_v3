/**
 * PaymentBean.java - 결제 정보 객체
 * 결제 및 구독 정보를 저장하고 관리합니다.
 * 관련 클래스: PaymentController, PaymentService, PaymentRepository, PaymentMapper
 */
package project.beans;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentBean {
    // 결제 로그 기본 정보
    private int payment_log_key;
    private int user_key;

    // 구독 기간 정보
    private String subscribe_start_time;
    private String subscribe_end_time;

    // 결제 상세 정보
    private String payment_date;
    private String payment_method;
    private int amount;
    private String merchant_uid;
    private String imp_uid;
    private String status; // 결제 상태 (success, failed, cancelled 등)
}