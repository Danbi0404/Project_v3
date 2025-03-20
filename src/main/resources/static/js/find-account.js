/**
 * find-account.js - 아이디/비밀번호 찾기 페이지 스크립트
 * 관련 파일: find-account.html, find-account.css
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 탭 전환 이벤트 초기화
    initTabs();

    // 아이디 찾기 폼 이벤트 초기화
    initFindIdForm();

    // 비밀번호 찾기 폼 이벤트 초기화
    initFindPwForm();

    // URL 파라미터 확인
    checkUrlParameters();
});

// ===== 탭 전환 기능 초기화 =====
function initTabs() {
    const tabItems = document.querySelectorAll('.tab-item');

    tabItems.forEach(item => {
        item.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            showTab(tabId);
        });
    });
}

// ===== 탭 표시 함수 =====
function showTab(tabId) {
    // 모든 탭 내용 숨기기
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.style.display = 'none';
    });

    // 모든 탭 헤더에서 active 클래스 제거
    const tabItems = document.querySelectorAll('.tab-item');
    tabItems.forEach(item => {
        item.classList.remove('active');
    });

    // 선택한 탭 표시
    document.getElementById(tabId + '-tab').style.display = 'block';

    // 선택한 탭 헤더에 active 클래스 추가
    document.querySelector(`.tab-item[data-tab="${tabId}"]`).classList.add('active');

    // 결과 컨테이너 초기화
    document.getElementById('find-id-result').style.display = 'none';
    document.getElementById('find-pw-result').style.display = 'none';
}

// ===== 아이디 찾기 폼 초기화 =====
function initFindIdForm() {
    const findIdForm = document.getElementById('find-id-form');

    if (findIdForm) {
        findIdForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const name = document.getElementById('name-for-id').value.trim();
            const email = document.getElementById('email-for-id').value.trim();

            // 기본 유효성 검사
            if (!name || !email) {
                showMessage(findIdForm, '모든 필드를 입력해주세요.', 'error');
                return;
            }

            // 이메일 형식 검사
            if (!validateEmail(email)) {
                showMessage(findIdForm, '유효한 이메일 주소를 입력해주세요.', 'error');
                return;
            }

            // 아이디 찾기 요청 (실제로는 서버에 AJAX 요청)
            // 여기서는 예시로 가짜 응답을 사용
            findIdByNameAndEmail(name, email)
                .then(result => {
                    if (result.success) {
                        // 결과 표시
                        document.getElementById('found-id').textContent = result.userId;
                        document.getElementById('register-date').textContent = result.registerDate;
                        document.getElementById('find-id-result').style.display = 'block';
                    } else {
                        showMessage(findIdForm, '입력하신 정보와 일치하는 계정이 없습니다.', 'error');
                    }
                })
                .catch(error => {
                    showMessage(findIdForm, '아이디 찾기 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
                });
        });
    }
}

// ===== 비밀번호 찾기 폼 초기화 =====
function initFindPwForm() {
    const findPwForm = document.getElementById('find-pw-form');

    if (findPwForm) {
        findPwForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const userId = document.getElementById('id-for-pw').value.trim();
            const email = document.getElementById('email-for-pw').value.trim();

            // 기본 유효성 검사
            if (!userId || !email) {
                showMessage(findPwForm, '모든 필드를 입력해주세요.', 'error');
                return;
            }

            // 이메일 형식 검사
            if (!validateEmail(email)) {
                showMessage(findPwForm, '유효한 이메일 주소를 입력해주세요.', 'error');
                return;
            }

            // 비밀번호 찾기 요청 (실제로는 서버에 AJAX 요청)
            // 여기서는 예시로 가짜 응답을 사용
            requestPasswordReset(userId, email)
                .then(result => {
                    if (result.success) {
                        // 결과 표시
                        document.getElementById('email-sent').textContent = email;
                        document.getElementById('find-pw-result').style.display = 'block';
                    } else {
                        showMessage(findPwForm, '입력하신 정보와 일치하는 계정이 없습니다.', 'error');
                    }
                })
                .catch(error => {
                    showMessage(findPwForm, '비밀번호 찾기 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
                });
        });
    }
}

// ===== URL 파라미터 확인 =====
function checkUrlParameters() {
    const urlParams = new URLSearchParams(window.location.search);

    // 탭 선택 파라미터
    const tab = urlParams.get('tab');
    if (tab === 'password') {
        showTab('find-pw');
    } else {
        showTab('find-id');
    }

    // 결과 파라미터
    const result = urlParams.get('result');
    if (result === 'not-found') {
        if (tab === 'password') {
            showMessage(document.getElementById('find-pw-form'), '입력하신 정보와 일치하는 계정이 없습니다.', 'error');
        } else {
            showMessage(document.getElementById('find-id-form'), '입력하신 정보와 일치하는 계정이 없습니다.', 'error');
        }
    } else if (result === 'error') {
        if (tab === 'password') {
            showMessage(document.getElementById('find-pw-form'), '서비스 오류가 발생했습니다. 다시 시도해주세요.', 'error');
        } else {
            showMessage(document.getElementById('find-id-form'), '서비스 오류가 발생했습니다. 다시 시도해주세요.', 'error');
        }
    } else if (result === 'sent') {
        showMessage(document.getElementById('find-pw-form'), '비밀번호 재설정 이메일이 발송되었습니다.', 'success');
    }
}

// ===== 가짜 API 함수 (실제로는 서버에 요청하는 함수) =====

/**
 * 아이디 찾기 API 호출 (가짜)
 */
function findIdByNameAndEmail(name, email) {
    return new Promise((resolve, reject) => {
        // 실제로는 서버에 AJAX 요청
        // 여기서는 예시로 가짜 응답 사용
        setTimeout(() => {
            // 성공 예시 (실제로는 서버 응답에 따라 달라짐)
            resolve({
                success: true,
                userId: 'user' + Math.floor(Math.random() * 1000),
                registerDate: '2023-01-01'
            });

            // 실패 예시
            // resolve({ success: false, message: '일치하는 정보가 없습니다.' });
        }, 1000);
    });
}

/**
 * 비밀번호 재설정 요청 API 호출 (가짜)
 */
function requestPasswordReset(userId, email) {
    return new Promise((resolve, reject) => {
        // 실제로는 서버에 AJAX 요청
        // 여기서는 예시로 가짜 응답 사용
        setTimeout(() => {
            // 성공 예시 (실제로는 서버 응답에 따라 달라짐)
            resolve({
                success: true,
                message: '이메일 발송 완료'
            });

            // 실패 예시
            // resolve({ success: false, message: '일치하는 정보가 없습니다.' });
        }, 1000);
    });
}

// ===== 유틸리티 함수 =====

/**
 * 메시지 표시
 */
function showMessage(container, message, type) {
    // 기존 메시지 제거
    const existingMessage = container.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }

    // 새 메시지 생성
    const messageElement = document.createElement('div');
    messageElement.className = `message ${type}-message`;
    messageElement.textContent = message;

    // 컨테이너의 첫 번째 자식으로 삽입
    container.insertBefore(messageElement, container.firstChild);

    // 일정 시간 후 자동 제거
    setTimeout(() => {
        if (messageElement.parentNode) {
            messageElement.remove();
        }
    }, 5000);
}

/**
 * 이메일 유효성 검증
 */
function validateEmail(email) {
    const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return regex.test(email);
}

// 글로벌 함수로 노출
window.showTab = showTab;