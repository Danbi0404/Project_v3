/**
 * user_login.js - 로그인 관련 스크립트
 * 관련 파일: login.html, user_login.css
 *
 * 로그인 처리 및 소셜 로그인 기능을 담당합니다.
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 폼 초기화
    init_login_form();

    // URL 파라미터 확인
    check_url_parameters();
});

// ===== 로그인 폼 초기화 =====
function init_login_form() {
    const login_form = document.querySelector('form');

    if (login_form) {
        login_form.addEventListener('submit', function(e) {
            e.preventDefault();

            const user_id = document.getElementById('login_user_id').value;
            const user_pw = document.getElementById('login_user_pw').value;

            // 간단한 유효성 검사
            if (!user_id || !user_pw) {
                show_message('아이디와 비밀번호를 모두 입력해주세요.', 'error', login_form);
                return;
            }

            // 폼 제출
            login_form.submit();
        });
    }
}

// ===== URL 파라미터 확인 =====
function check_url_parameters() {
    const url_params = new URLSearchParams(window.location.search);

    // 로그인 에러 파라미터가 있는 경우
    if (url_params.get('loginError') === 'true') {
        const login_form = document.querySelector('form');

        if (login_form) {
            show_message('아이디 또는 비밀번호를 확인해주세요.', 'error', login_form);
        }

        // URL에서 파라미터 제거
        history.replaceState(null, '', window.location.pathname);
    }
}

// ===== 소셜 로그인 함수 =====

/**
 * 네이버 로그인 처리
 */
function login_with_naver() {
    // 서버에서 네이버 로그인 URL 가져오기
    fetch('/user/social/login/urls')
        .then(response => response.json())
        .then(data => {
            const naver_url = data.naverUrl;

            // 팝업 창 열기
            const width = 500;
            const height = 600;
            const left = (window.innerWidth - width) / 2;
            const top = (window.innerHeight - height) / 2;

            const naver_popup = window.open(
                naver_url,
                'naverLogin',
                `width=${width},height=${height},left=${left},top=${top}`
            );

            // 팝업 창 닫힘 감지를 위한 인터벌
            const check_popup = setInterval(() => {
                if (naver_popup.closed) {
                    clearInterval(check_popup);
                    // 로그인 성공 여부 확인
                    check_login_status();
                }
            }, 500);
        })
        .catch(error => {
            console.error('네이버 로그인 URL 가져오기 실패:', error);
            alert('네이버 로그인을 시작할 수 없습니다. 다시 시도해주세요.');
        });
}

/**
 * 카카오 로그인 처리
 */
function login_with_kakao() {
    // 서버에서 카카오 로그인 URL 가져오기
    fetch('/user/social/login/urls')
        .then(response => response.json())
        .then(data => {
            const kakao_url = data.kakaoUrl + "&prompt=login"; //socialloginservice 와 동일하게.

            // 팝업 창 열기
            const width = 500;
            const height = 600;
            const left = (window.innerWidth - width) / 2;
            const top = (window.innerHeight - height) / 2;

            window.open(
                kakao_url,
                'kakaoLogin',
                `width=${width},height=${height},left=${left},top=${top}`
            );

            // 팝업 창 닫힘 감지를 위한 인터벌(필요없음, 컨트롤러 콜백에서 처리)
            /*const check_popup = setInterval(() => {
                if (kakao_popup.closed) {
                    clearInterval(check_popup);
                    // 로그인 성공 여부 확인
                    check_login_status();
                }
            }, 500);*/
        })
        .catch(error => {
            console.error('카카오 로그인 URL 가져오기 실패:', error);
            alert('카카오 로그인을 시작할 수 없습니다. 다시 시도해주세요.');
        });
}

/**
 * 로그인 상태 확인
 */
function check_login_status() {
    fetch('/user/check-login-status')
        .then(response => response.json())
        .then(data => {
            if (data.loggedIn) {
                // 로그인 성공 - 메인 페이지로 이동
                window.location.href = '/main';
            }
        })
        .catch(error => {
            console.error('로그인 상태 확인 실패:', error);
        });
}

// 전역 함수로 노출
window.loginWithNaver = login_with_naver;
window.loginWithKakao = login_with_kakao;