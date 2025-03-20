/**
 * tutor_dashboard.js - 튜터 대시보드 관련 스크립트
 * 관련 파일: tutor_dashboard.html
 */

document.addEventListener('DOMContentLoaded', function() {
    // 탭 전환 기능 초기화
    initTabs();

    // 그룹 선택 변경 이벤트
    initGroupSelector();
});

// ===== 탭 전환 기능 초기화 =====
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    // 초기 탭 설정 (첫 번째 탭 활성화)
    tabButtons[0].classList.add('active');
    tabContents[0].style.display = 'block';

    // 탭 버튼 클릭 이벤트 리스너
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');

            // 모든 탭 비활성화
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.style.display = 'none');

            // 선택한 탭 활성화
            this.classList.add('active');
            document.getElementById(tabId).style.display = 'block';
        });
    });
}

// ===== 그룹 선택 변경 이벤트 =====
function initGroupSelector() {
    const groupSelector = document.getElementById('activity-group');
    const startClassButton = document.querySelector('.btn-start-class');

    if (groupSelector) {
        groupSelector.addEventListener('change', function() {
            if (this.value) {
                startClassButton.disabled = false;
            } else {
                startClassButton.disabled = true;
            }
        });
    }

    // 수업 시작 버튼 클릭 이벤트
    if (startClassButton) {
        startClassButton.addEventListener('click', function() {
            if (!this.disabled) {
                alert('화상 수업 기능은 아직 개발 중입니다.');
            }
        });
    }
}