/**
 * profile-edit.js - 프로필 수정 페이지 스크립트
 * 관련 파일: profile-edit.html, profile-edit.css
 */

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 프로필 수정 폼 초기화
    initProfileEditForm();

    // URL 파라미터 확인
    checkUrlParameters();
});

// ===== 프로필 수정 폼 초기화 =====
function initProfileEditForm() {
    // 비밀번호 확인 폼
    const passwordConfirmForm = document.getElementById('password-confirm-form');
    if (passwordConfirmForm) {
        passwordConfirmForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const password = document.getElementById('current-password').value;

            if (!password) {
                showMessage(passwordConfirmForm, '비밀번호를 입력해주세요.', 'error');
                return;
            }

            // AJAX로 비밀번호 확인 요청
            fetch('/user/verify-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ password: password }),
            })
                .then(response => response.json())
                .then(data => {
                    if (data.valid) {
                        // 비밀번호 확인 성공
                        document.getElementById('password-confirm-section').style.display = 'none';
                        document.getElementById('profile-edit-section').style.display = 'block';
                    } else {
                        showMessage(passwordConfirmForm, '비밀번호가 일치하지 않습니다.', 'error');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showMessage(passwordConfirmForm, '서버 오류가 발생했습니다.', 'error');
                });
        });
    }

    // 프로필 수정 폼
    const profileEditForm = document.getElementById('profile-edit-form');
    if (profileEditForm) {
        profileEditForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // 기본 검증 (이메일, 주소, 전화번호 제외)
            const userName = document.getElementById('user-name').value;
            const userNickname = document.getElementById('user-nickname').value;

            if (!userName || !userNickname) {
                showMessage(profileEditForm, '모든 필수 항목을 입력해주세요.', 'error');
                return;
            }

            // 폼 제출
            this.submit();
        });
    }

    // 비밀번호 변경 버튼
    const changePasswordBtn = document.getElementById('change-password-btn');
    if (changePasswordBtn) {
        changePasswordBtn.addEventListener('click', function() {
            document.getElementById('profile-edit-section').style.display = 'none';
            document.getElementById('password-change-section').style.display = 'block';
        });
    }

    // 비밀번호 변경 취소 버튼
    const cancelPwChangeBtn = document.getElementById('cancel-pw-change');
    if (cancelPwChangeBtn) {
        cancelPwChangeBtn.addEventListener('click', function() {
            document.getElementById('password-change-section').style.display = 'none';
            document.getElementById('profile-edit-section').style.display = 'block';
        });
    }

    // 비밀번호 변경 폼
    const passwordChangeForm = document.getElementById('password-change-form');
    if (passwordChangeForm) {
        passwordChangeForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const currentPassword = document.getElementById('current-pw').value;
            const newPassword = document.getElementById('new-pw').value;
            const confirmPassword = document.getElementById('confirm-pw').value;

            // 기본 검증
            if (!currentPassword || !newPassword || !confirmPassword) {
                showMessage(passwordChangeForm, '모든 항목을 입력해주세요.', 'error');
                return;
            }

            // 비밀번호 일치 확인
            if (newPassword !== confirmPassword) {
                showMessage(passwordChangeForm, '새 비밀번호와 확인이 일치하지 않습니다.', 'error');
                return;
            }

            // 비밀번호 형식 검증
            const validation = validatePassword(newPassword);
            if (!validation.isValid) {
                showMessage(passwordChangeForm, validation.message, 'error');
                return;
            }

            // 폼 제출
            // 여기서는 예시로 페이지 이동만 구현
            alert('비밀번호가 성공적으로 변경되었습니다.');
            window.location.href = '/user/mypage';
        });
    }
}

// ===== URL 파라미터 확인 =====
function checkUrlParameters() {
    const urlParams = new URLSearchParams(window.location.search);

    // 프로필 업데이트 결과 확인
    const updateResult = urlParams.get('update');
    if (updateResult === 'success') {
        showMessage(document.querySelector('.profile-edit-container'), '프로필이 성공적으로 업데이트되었습니다.', 'success');
        history.replaceState(null, '', window.location.pathname);
    } else if (updateResult === 'error') {
        showMessage(document.querySelector('.profile-edit-container'), '프로필 업데이트 중 오류가 발생했습니다.', 'error');
        history.replaceState(null, '', window.location.pathname);
    }

    // 비밀번호 변경 결과 확인
    const pwChangeResult = urlParams.get('pwChange');
    if (pwChangeResult === 'success') {
        showMessage(document.querySelector('.profile-edit-container'), '비밀번호가 성공적으로 변경되었습니다.', 'success');
        history.replaceState(null, '', window.location.pathname);
    } else if (pwChangeResult === 'error') {
        showMessage(document.querySelector('.profile-edit-container'), '비밀번호 변경 중 오류가 발생했습니다.', 'error');
        history.replaceState(null, '', window.location.pathname);
    }
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
 * 비밀번호 유효성 검증
 */
function validatePassword(password) {
    if (password.length < 8) {
        return {
            isValid: false,
            message: '비밀번호는 최소 8자 이상이어야 합니다.'
        };
    }

    if (password.length > 12) {
        return {
            isValid: false,
            message: '비밀번호는 최대 12자까지 가능합니다.'
        };
    }

    if (!/^[a-zA-Z0-9]+$/.test(password)) {
        return {
            isValid: false,
            message: '비밀번호는 영문과 숫자만 포함할 수 있습니다.'
        };
    }

    return {
        isValid: true,
        message: ''
    };
}