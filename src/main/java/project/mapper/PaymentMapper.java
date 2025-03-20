/**
 * PaymentMapper.java - 결제 관련 DB 매퍼 인터페이스
 * 결제 정보 관련 SQL 쿼리 매핑을 담당합니다.
 * 관련 클래스: PaymentRepository
 */
package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.PaymentBean;

import java.util.List;

@Mapper
public interface PaymentMapper {

    /**
     * 결제 로그 추가
     */
    @Insert("INSERT INTO payment_log (payment_log_key, user_key, subscribe_start_time, subscribe_end_time, " +
            "payment_date, payment_method, amount, merchant_uid, imp_uid, status) " +
            "VALUES (payment_log_seq.nextval, #{user_key}, " +
            "TO_DATE(#{subscribe_start_time}, 'YYYY/MM/DD'), TO_DATE(#{subscribe_end_time}, 'YYYY/MM/DD'), " +
            "TO_DATE(#{payment_date}, 'YYYY/MM/DD'), #{payment_method}, #{amount}, " +
            "#{merchant_uid, jdbcType=VARCHAR}, #{imp_uid, jdbcType=VARCHAR}, #{status, jdbcType=VARCHAR})")
    void insert_payment_log(PaymentBean paymentBean);

    /**
     * 결제 로그 단일 조회
     */
    @Select("SELECT payment_log_key, user_key, " +
            "to_char(subscribe_start_time, 'YYYY/MM/DD') subscribe_start_time, " +
            "to_char(subscribe_end_time, 'YYYY/MM/DD') subscribe_end_time, " +
            "to_char(payment_date, 'YYYY/MM/DD') payment_date, " +
            "payment_method, amount, merchant_uid, imp_uid, status " +
            "FROM payment_log WHERE payment_log_key = #{payment_log_key}")
    PaymentBean get_payment_log(int payment_log_key);

    /**
     * 사용자별 결제 로그 조회
     */
    @Select("SELECT payment_log_key, user_key, " +
            "to_char(subscribe_start_time, 'YYYY/MM/DD') subscribe_start_time, " +
            "to_char(subscribe_end_time, 'YYYY/MM/DD') subscribe_end_time, " +
            "to_char(payment_date, 'YYYY/MM/DD') payment_date, " +
            "payment_method, amount, merchant_uid, imp_uid, status " +
            "FROM payment_log WHERE user_key = #{user_key} ORDER BY payment_date DESC")
    List<PaymentBean> get_payment_logs_by_user_key(int user_key);

    /**
     * 전체 결제 로그 조회
     */
    @Select("SELECT payment_log_key, user_key, " +
            "to_char(subscribe_start_time, 'YYYY/MM/DD') subscribe_start_time, " +
            "to_char(subscribe_end_time, 'YYYY/MM/DD') subscribe_end_time, " +
            "to_char(payment_date, 'YYYY/MM/DD') payment_date, " +
            "payment_method, amount, merchant_uid, imp_uid, status " +
            "FROM payment_log ORDER BY payment_date DESC")
    List<PaymentBean> get_all_payment_logs();

    /**
     * 결제 로그 상태 업데이트
     */
    @Update("UPDATE payment_log SET status = #{status} " +
            "WHERE payment_log_key = #{payment_log_key}")
    void update_payment_status(PaymentBean paymentBean);
}