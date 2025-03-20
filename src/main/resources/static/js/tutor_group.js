/**
 * tutor_group.js - 그룹 관리 관련 스크립트
 * 관련 파일: tutor_group_create.html, tutor_group_manage.html
 */

document.addEventListener('DOMContentLoaded', function() {
    // 그룹 관리 페이지 기능 초기화
    initGroupManagePage();
});

// ===== 그룹 관리 페이지 기능 초기화 =====
function initGroupManagePage() {
    // 모달 관련 초기화
    initRejectModal();
}

// ===== 거부 모달 초기화 =====
function initRejectModal() {
    const modal = document.getElementById('reject-modal');

    if (!modal) return;

    const closeButtons = modal.querySelectorAll('.close-modal');

    // 모달 닫기 버튼 이벤트
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            modal.style.display = 'none';
        });
    });

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });

    // 거부 폼 제출 전 검증
    const rejectForm = document.getElementById('reject-form');
    if (rejectForm) {
        rejectForm.addEventListener('submit', function(e) {
            const rejectReason = document.getElementById('rejectReason').value.trim();

            if (!rejectReason) {
                e.preventDefault();
                alert('거부 사유를 입력해주세요.');
            }
        });
    }
}

// ===== 거부 모달 표시 =====
function showRejectForm(requestKey) {
    const modal = document.getElementById('reject-modal');
    const rejectForm = document.getElementById('reject-form');
    const requestKeyInput = document.getElementById('reject-request-key');

    if (modal && rejectForm && requestKeyInput) {
        // 요청 키 설정
        requestKeyInput.value = requestKey;

        // 폼 액션 URL 업데이트
        rejectForm.action = `/group/reject/${requestKey}`;

        // 이전 입력 내용 초기화
        document.getElementById('rejectReason').value = '';

        // 모달 표시
        modal.style.display = 'block';
    }
}

// 전역 함수로 노출
window.showRejectForm = showRejectForm;