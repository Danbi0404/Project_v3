/**
 * admin.js - 관리자 페이지 관련 스크립트
 * 튜터 신청 관리 페이지의 기능을 담당합니다.
 */

document.addEventListener('DOMContentLoaded', function() {
    // 승인/거부 확인 메시지
    initConfirmActions();

    // 파일 링크 클릭 처리
    initFileLinks();

    // 거부 사유 폼 표시/숨김 처리
    const showRejectFormBtn = document.getElementById('showRejectForm');
    const rejectReasonForm = document.getElementById('rejectReasonForm');
    const cancelRejectBtn = document.getElementById('cancelReject');

    if (showRejectFormBtn && rejectReasonForm) {
        showRejectFormBtn.addEventListener('click', function() {
            // 거부 사유 폼 표시
            rejectReasonForm.style.display = 'block';
            // 버튼 숨김
            document.querySelector('.action-buttons').style.display = 'none';
        });
    }

    if (cancelRejectBtn && rejectReasonForm) {
        cancelRejectBtn.addEventListener('click', function() {
            // 거부 사유 폼 숨김
            rejectReasonForm.style.display = 'none';
            // 버튼 다시 표시
            document.querySelector('.action-buttons').style.display = 'flex';
        });
    }
});

//언어 추가에 대한 폼
if (document.getElementById('showLanguageRejectForm')) {
    document.getElementById('showLanguageRejectForm').addEventListener('click', function() {
        // 거부 사유 폼 표시
        document.getElementById('rejectReasonForm').style.display = 'block';
        // 버튼 숨김
        document.querySelector('.action-buttons').style.display = 'none';
    });
}

// 승인/거부 확인 창 표시
function initConfirmActions() {
    const approveForm = document.querySelector('form[action*="/approve/"]');
    const rejectForm = document.querySelector('form[action*="/reject/"]');

    if (approveForm) {
        approveForm.addEventListener('submit', function(e) {
            if (!confirm('이 튜터 신청을 승인하시겠습니까?')) {
                e.preventDefault();
            }
        });
    }

    if (rejectForm) {
        rejectForm.addEventListener('submit', function(e) {
            if (!confirm('이 튜터 신청을 거부하시겠습니까?')) {
                e.preventDefault();
            }
        });
    }
}

// PDF 파일 링크 클릭시 새 창에서 열기
function initFileLinks() {
    const fileLinks = document.querySelectorAll('.file-link');

    fileLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const url = this.getAttribute('href');
            if (url && url.endsWith('.pdf')) {
                e.preventDefault();
                window.open(url, '_blank', 'width=800,height=600');
            }
        });
    });
}