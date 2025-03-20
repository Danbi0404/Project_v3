/**
 * payment.js - 결제 기능 스크립트
 * 관련 파일: payment.html, payment.css
 *
 * 결제 옵션 선택 및 결제 처리 기능을 담당합니다.
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 결제 옵션 선택 초기화
    init_payment_options();

    // 포트원 결제 모듈 초기화
    init_payment_module();
});

// ===== 결제 옵션 선택 초기화 =====
function init_payment_options() {
    const options = document.querySelectorAll('.payment-option');

    // 첫 번째 옵션을 기본으로 선택
    if (options.length > 0) {
        options[0].classList.add('selected');
    }

    // 각 옵션에 클릭 이벤트 연결
    options.forEach(option => {
        option.addEventListener('click', function() {
            // 금액과 기간 추출
            const amount = parseInt(this.dataset.amount);
            const period = parseInt(this.dataset.period);

            select_option(this, amount, period);
        });
    });
}

// ===== 포트원 결제 모듈 초기화 =====
function init_payment_module() {
    // 결제 객체 초기화
    var IMP = window.IMP;
    IMP.init("imp26546322");  // 포트원 상점 아이디

    // 버튼 클릭 이벤트 설정
    const kakao_pay_btn = document.getElementById('kakaoPayBtn');
    const toss_pay_btn = document.getElementById('tossPayBtn');

    if (kakao_pay_btn) {
        kakao_pay_btn.addEventListener('click', function() {
            request_pay('kakaopay');
        });
    }

    if (toss_pay_btn) {
        toss_pay_btn.addEventListener('click', function() {
            request_pay('tosspay');
        });
    }
}

// ===== 결제 옵션 선택 처리 =====
function select_option(element, amount, period) {
    // 모든 옵션에서 selected 클래스 제거
    document.querySelectorAll('.payment-option').forEach(option => {
        option.classList.remove('selected');
    });

    // 선택된 옵션에 selected 클래스 추가
    element.classList.add('selected');

    // 금액 입력 필드 업데이트
    const amount_input = document.getElementById('amountInput');
    if (amount_input) {
        amount_input.value = amount;
        // 데이터 속성에 기간 저장
        amount_input.dataset.period = period;
    }
}

// ===== 날짜를 오라클 호환 형식으로 포맷팅 (YYYY/MM/DD) =====
function format_date_for_oracle(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}/${month}/${day}`;
}

// ===== 결제 요청 처리 =====
function request_pay(payment_gateway) {
    // 포트원 객체 가져오기
    var IMP = window.IMP;
    if (!IMP) {
        alert('결제 모듈이 초기화되지 않았습니다.');
        return;
    }

    const amount_input = document.getElementById('amountInput');
    const amount = parseInt(amount_input.value);
    const period = parseInt(amount_input.dataset.period || 1);

    if (!amount || isNaN(amount)) {
        alert('유효한 금액을 입력해주세요.');
        return;
    }

    // 구독 유형 결정
    let subscription_type = `${period}개월 구독권`;

    // 선택된 구독 설명 가져오기
    const selected_option = document.querySelector('.payment-option.selected');
    const subscription_desc = selected_option ? selected_option.querySelector('.option-description').innerText : '';

    // PG 설정
    let pg_provider = '';
    if (payment_gateway === 'kakaopay') {
        pg_provider = "kakaopay.TC0ONETIME";
    } else if (payment_gateway === 'tosspay') {
        pg_provider = "tosspay";
    }

    // 구독 시작일과 종료일 계산
    const start_date = new Date();
    const end_date = new Date();
    end_date.setMonth(end_date.getMonth() + period);

    // 오라클 호환 날짜 형식 사용
    const start_date_str = format_date_for_oracle(start_date);
    const end_date_str = format_date_for_oracle(end_date);

    // 결제창 호출
    IMP.request_pay(
        {
            // 파라미터 값 설정
            pg: pg_provider,
            pay_method: "card",
            merchant_uid: "wordly_" + new Date().getTime(),  // 상점 고유 주문번호
            name: "WORDLY " + subscription_type,
            amount: amount,
            buyer_email: "guest@wordly.com",
            buyer_name: window.userName || "게스트",
            buyer_tel: "010-0000-0000",
            buyer_addr: "주소 정보 없음",
            buyer_postcode: "123-456",
        },
        function(rsp) {
            if (rsp.success) {
                // 결제 성공 시 서버에 결제 정보 저장 요청
                verify_payment(rsp, start_date_str, end_date_str, payment_gateway);
            } else {
                alert(`결제에 실패하였습니다. 에러 내용: ${rsp.error_msg}`);
            }
        }
    );
}

// ===== 결제 검증 처리 =====
function verify_payment(rsp, start_date_str, end_date_str, payment_gateway) {
    const amount_input = document.getElementById('amountInput');
    const amount = parseInt(amount_input.value);

    axios({
        url: `/payment/verify/${rsp.imp_uid}`,
        method: "post",
        headers: {"Content-Type": "application/json"},
        data: {
            imp_uid: rsp.imp_uid,
            merchant_uid: rsp.merchant_uid,
            amount: amount,
            start_date: start_date_str,
            end_date: end_date_str,
            method: payment_gateway
        }
    }).then((response) => {
        const data = response.data;
        if (data.success) {
            alert('결제가 완료되었습니다! 구독 기간: ' + amount_input.dataset.period + '개월');
            window.location.href = '/main';
        } else {
            alert('결제 검증에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
        }
    }).catch(error => {
        console.error("검증 요청 오류:", error);
        alert('결제는 성공했으나 검증 과정에서 오류가 발생했습니다.');
    });
}