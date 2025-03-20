/**
 * util.js - 공통 유틸리티 기능
 * 전체 프로젝트에서 사용되는 유틸리티 함수들을 모아둔 파일입니다.
 */

// ===== 유효성 검증 함수 =====

/**
 * 문자열이 비어있지 않은지 확인
 * @param {string} value - 검사할 문자열
 * @return {boolean} 비어있지 않으면 true
 */
function is_not_empty(value) {
    return value !== null && value !== undefined && value.trim() !== '';
}

/**
 * 아이디 유효성 검증
 * @param {string} id - 검증할 아이디(이메일)
 * @return {object} 검증 결과와 메시지
 */
function validate_id(id) {
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return {
        is_valid: regex.test(id) && id.length <= 100,
        message: '올바른 형식의 이메일 주소를 입력해주세요.'
    };
}

/**
 * 비밀번호 유효성 검증 (8~12자 영문, 숫자)
 * @param {string} password - 검증할 비밀번호
 * @return {object} 검증 결과와 메시지
 */
function validate_password(password) {
    const regex = /^[a-zA-Z0-9]{8,12}$/;

    if (password.length < 8) {
        return {
            is_valid: false,
            message: '비밀번호는 최소 8자 이상이어야 합니다.'
        };
    }

    if (password.length > 12) {
        return {
            is_valid: false,
            message: '비밀번호는 최대 12자까지 가능합니다.'
        };
    }

    if (!regex.test(password)) {
        return {
            is_valid: false,
            message: '비밀번호는 영문과 숫자만 포함할 수 있습니다.'
        };
    }

    return {
        is_valid: true,
        message: '사용 가능한 비밀번호입니다.'
    };
}

/**
 * 닉네임 유효성 검증 (2~10자 한글, 영문, 숫자)
 * @param {string} nickname - 검증할 닉네임
 * @return {object} 검증 결과와 메시지
 */
function validate_nickname(nickname) {
    const regex = /^[가-힣a-zA-Z0-9]{2,10}$/;
    return {
        is_valid: regex.test(nickname),
        message: '2~10자의 한글, 영문, 숫자만 사용 가능합니다.'
    };
}

// ===== 메시지 표시 함수 =====

/**
 * 에러 메시지 또는 성공 메시지 표시
 * @param {string} message - 표시할 메시지
 * @param {string} type - 메시지 타입 (error 또는 success)
 * @param {HTMLElement} container - 메시지를 표시할 컨테이너
 */
function show_message(message, type, container) {
    // 기존 메시지 제거
    const existing_error = container.querySelector('.error-message');
    if (existing_error) existing_error.remove();

    const existing_success = container.querySelector('.success-message');
    if (existing_success) existing_success.remove();

    // 새 메시지 생성
    const message_element = document.createElement('div');
    message_element.className = type === 'error' ? 'error-message' : 'success-message';
    message_element.textContent = message;

    // 메시지 추가
    container.insertBefore(message_element, container.firstChild);

    // 성공 메시지는 5초 후 자동 제거
    if (type === 'success') {
        setTimeout(() => {
            if (message_element.parentNode) {
                message_element.remove();
            }
        }, 5000);
    }
}

// ===== API 호출 함수 =====

/**
 * ID 중복 체크 API 호출
 * @param {string} id - 체크할 아이디
 * @return {Promise} 중복 체크 결과
 */
async function check_id_duplicate(id) {
    try {
        const response = await fetch(`/user/check-id?id=${encodeURIComponent(id)}`);
        const data = await response.json();
        return {
            is_available: data.available,
            message: data.available ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.'
        };
    } catch (error) {
        console.error('ID 중복 검사 오류:', error);
        return {
            is_available: false,
            message: '중복 검사 중 오류가 발생했습니다.'
        };
    }
}

/**
 * 닉네임 중복 체크 API 호출
 * @param {string} nickname - 체크할 닉네임
 * @return {Promise} 중복 체크 결과
 */
async function check_nickname_duplicate(nickname) {
    try {
        const response = await fetch(`/user/check-nickname?nickname=${encodeURIComponent(nickname)}`);
        const data = await response.json();
        return {
            is_available: data.available,
            message: data.available ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.'
        };
    } catch (error) {
        console.error('닉네임 중복 검사 오류:', error);
        return {
            is_available: false,
            message: '중복 검사 중 오류가 발생했습니다.'
        };
    }
}

// ===== 날짜 관련 함수 =====

/**
 * 현재 날짜를 YYYY/MM/DD 형식으로 반환
 * @return {string} 포맷된 날짜 문자열
 */
function format_current_date() {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}/${month}/${day}`;
}

/**
 * 현재 날짜에 개월 수를 더한 날짜 반환
 * @param {number} months - 더할 개월 수
 * @return {string} YYYY/MM/DD 형식의 날짜
 */
function add_months_to_date(months) {
    const date = new Date();
    date.setMonth(date.getMonth() + months);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}/${month}/${day}`;
}