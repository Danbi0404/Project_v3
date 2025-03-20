/**
 * user_join.js - 회원가입 관련 스크립트
 * 관련 파일: join.html, user_join.css
 *
 * 회원가입 폼 유효성 검사 및 제출 처리를 담당합니다.
 */

// 타이핑 지연 타이머 변수
let id_timeout = null;
let nickname_timeout = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 회원가입 폼 초기화
    init_join_form();

    // 주소 검색 필드 이벤트 설정
    init_address_fields();

    // URL 파라미터 확인
    check_url_parameters();
});

// ===== 회원가입 폼 초기화 =====
function init_join_form() {
    const join_form = document.querySelector('form');

    if (!join_form) return;

    // ID 필드 유효성 검사
    const id_input = document.getElementById('join_user_id');
    if (id_input) {
        id_input.addEventListener('input', function() {
            const id = this.value.trim();

            // 이전 타이머 취소
            if (id_timeout) clearTimeout(id_timeout);

            if (id.length === 0) {
                show_field_message(this, '', false);
                return;
            }

            // 이메일 형식 검증 (아이디 검증 대신)
            const validation = validate_id(id);
            if (!validation.is_valid) {
                show_field_message(this, validation.message, true);
                return;
            }

            // 입력값이 유효한 경우에만 중복 검사 실행
            id_timeout = setTimeout(async () => {
                const result = await check_id_duplicate(id);
                show_field_message(id_input, result.message, !result.is_available);
            }, 500);
        });
    }

    // 비밀번호 필드 유효성 검사
    const pw_input = document.getElementById('join_user_pw');
    if (pw_input) {
        pw_input.addEventListener('input', function() {
            const password = this.value;

            if (password.length === 0) {
                show_field_message(this, '', false);
                return;
            }

            const validation = validate_password(password);
            show_field_message(this, validation.message, !validation.is_valid);
        });
    }

    // 이름 필드 유효성 검사
    const name_input = document.getElementById('join_user_name');
    if (name_input) {
        name_input.addEventListener('input', function() {
            const name = this.value.trim();

            if (name.length === 0) {
                show_field_message(this, '', false);
                return;
            }

            if (name.length < 2 || name.length > 15) {
                show_field_message(this, '2~15자의 이름을 입력해주세요.', true);
            } else {
                show_field_message(this, '', false);
            }
        });
    }

    // 닉네임 필드 유효성 검사
    const nickname_input = document.getElementById('join_user_nickname');
    if (nickname_input) {
        nickname_input.addEventListener('input', function() {
            const nickname = this.value.trim();

            // 이전 타이머 취소
            if (nickname_timeout) clearTimeout(nickname_timeout);

            if (nickname.length === 0) {
                show_field_message(this, '', false);
                return;
            }

            // 닉네임 형식 검증
            const validation = validate_nickname(nickname);
            if (!validation.is_valid) {
                show_field_message(this, validation.message, true);
                return;
            }

            // 입력값이 유효한 경우에만 중복 검사 실행
            nickname_timeout = setTimeout(async () => {
                const result = await check_nickname_duplicate(nickname);
                show_field_message(nickname_input, result.message, !result.is_available);
            }, 500);
        });
    }

    // 폼 제출 이벤트
    join_form.addEventListener('submit', async function(e) {
        e.preventDefault();

        // 필수 필드 검사
        const user_id = id_input.value.trim();
        const user_pw = pw_input.value.trim();
        const user_name = name_input.value.trim();
        const user_nickname = nickname_input.value.trim();
        if (!user_id || !user_pw || !user_name || !user_nickname) {
            show_message('모든 필수 항목을 입력해주세요.', 'error', join_form);
            return;
        }

        // ID 유효성 검사
        const id_validation = validate_id(user_id);
        if (!id_validation.is_valid) {
            show_field_message(id_input, id_validation.message, true);
            return;
        }

        // ID 중복 검사
        const id_check = await check_id_duplicate(user_id);
        if (!id_check.is_available) {
            show_field_message(id_input, id_check.message, true);
            return;
        }

        // 비밀번호 유효성 검사
        const pw_validation = validate_password(user_pw);
        if (!pw_validation.is_valid) {
            show_field_message(pw_input, pw_validation.message, true);
            return;
        }

        // 닉네임 유효성 검사
        const nickname_validation = validate_nickname(user_nickname);
        if (!nickname_validation.is_valid) {
            show_field_message(nickname_input, nickname_validation.message, true);
            return;
        }

        // 닉네임 중복 검사
        const nickname_check = await check_nickname_duplicate(user_nickname);
        if (!nickname_check.is_available) {
            show_field_message(nickname_input, nickname_check.message, true);
            return;
        }

        // 모든 검사 통과 - 폼 제출
        join_form.submit();
    });
}

// ===== URL 파라미터 확인 =====
function check_url_parameters() {
    const url_params = new URLSearchParams(window.location.search);
    const join_form = document.querySelector('form');

    // 회원가입 오류 파라미터가 있는 경우
    const join_error = url_params.get('joinError');
    if (join_error && join_form) {
        if (join_error === 'duplicate-id') {
            show_message('이미 사용중인 아이디입니다.', 'error', join_form);
        } else if (join_error === 'duplicate-nickname') {
            show_message('이미 사용중인 닉네임입니다.', 'error', join_form);
        } else {
            show_message('입력 정보를 확인해주세요.', 'error', join_form);
        }

        // URL에서 파라미터 제거
        history.replaceState(null, '', window.location.pathname);
    }

    // 회원가입 성공 파라미터가 있는 경우
    const join_success = url_params.get('joinSuccess');
    if (join_success === 'true' && join_form) {
        show_message('회원가입이 완료되었습니다.', 'success', join_form);

        // 3초 후 메인 페이지로 이동
        setTimeout(function() {
            window.location.href = '/';
        }, 3000);

        // URL에서 파라미터 제거
        history.replaceState(null, '', window.location.pathname);
    }
}

// ===== 유틸리티 함수 =====

/**
 * 입력 필드에 메시지 표시
 * @param {HTMLElement} input_element - 입력 필드 요소
 * @param {string} message - 표시할 메시지
 * @param {boolean} is_error - 에러 메시지 여부
 */
function show_field_message(input_element, message, is_error) {
    // 기존 메시지 요소 제거
    const existing_message = input_element.parentNode.querySelector('.validation-message');
    if (existing_message) {
        existing_message.remove();
    }

    // 메시지가 없으면 아무것도 표시하지 않음
    if (!message) return;

    // 새 메시지 요소 생성
    const message_element = document.createElement('span');
    message_element.className = `validation-message ${is_error ? 'error-message' : 'success-message'}`;
    message_element.textContent = message;

    // 입력 필드 아래에 메시지 삽입
    input_element.parentNode.appendChild(message_element);
}