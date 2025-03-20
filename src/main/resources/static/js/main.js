/**
 * main.js - 메인 페이지 스크립트
 * 관련 파일: main.html, main.css
 *
 * 메인 페이지의 기능을 처리합니다:
 * - 로그인/회원가입 모달 처리
 * - 학습 콘텐츠 처리
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    // 로그인/회원가입 버튼 이벤트 처리
    init_auth_buttons();

    // 학습 콘텐츠 관련 기능 초기화
    init_learning_features();
});

// ===== 로그인/회원가입 버튼 초기화 =====
function init_auth_buttons() {
    const login_btn = document.getElementById('loginBtn');
    const join_btn = document.getElementById('joinBtn');

    if (login_btn) {
        login_btn.addEventListener('click', function () {
            window.location.href = '/user/login';
        });
    }

    if (join_btn) {
        join_btn.addEventListener('click', function () {
            window.location.href = '/user/join';
        });
    }
}

// ===== 학습 콘텐츠 관련 기능 초기화 =====
function init_learning_features() {
    const answer_key = document.querySelector('.answer-key');
    const nav_buttons = document.querySelectorAll('.nav-button');

    if (answer_key) {
        answer_key.addEventListener('click', function () {
            alert('정답 키를 확인합니다.');
        });
    }

    if (nav_buttons.length > 0) {
        nav_buttons[0].addEventListener('click', function () {
            console.log('이전 단어로 이동');
        });

        nav_buttons[1].addEventListener('click', function () {
            console.log('다음 단어로 이동');
        });
    }
}