/**
 * tutor_join.js - 튜터 신청 페이지 스크립트
 * 튜터 신청 폼의 파일 업로드 및 유효성 검사를 담당합니다.
 */

// 전역 변수로 선택된 파일 배열 관리
let selectedFiles = [];

document.addEventListener('DOMContentLoaded', function() {
    // 파일 업로드 초기화
    initFileUploads();

    // 폼 유효성 검사
    initFormValidation();

    // 기존 인증서 파일 처리 (재신청 시)
    if (window.isReapplying && window.existingCertificateFiles) {
        initExistingFiles();
    }
});

// 기존 인증서 파일 처리 함수
function initExistingFiles() {
    // 기존 파일 보기 버튼 이벤트 연결
    const viewButtons = document.querySelectorAll('.view-file-btn');
    viewButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const filePath = this.getAttribute('data-path');
            if (filePath) {
                window.open(filePath, '_blank');
            }
        });
    });

    // 기존 파일 삭제 버튼 이벤트 연결
    const removeButtons = document.querySelectorAll('.existing-file .remove-file-btn');
    removeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const fileId = this.getAttribute('data-id');
            const fileItem = document.getElementById('existing-file-' + fileId);

            if (fileItem) {
                // 화면에서 파일 항목 제거
                fileItem.remove();

                // keepFiles hidden input 제거
                const keepFilesInput = document.querySelector(`input[name="keepFiles"][value="${fileId}"]`);
                if (keepFilesInput) {
                    keepFilesInput.remove();
                }
            }
        });
    });
}

// 파일 업로드 설정
function initFileUploads() {
    // 프로필 이미지 미리보기
    const profileInput = document.getElementById('profile-image');
    const profilePreview = document.getElementById('profile-image-preview');

    if (profileInput && profilePreview) {
        profileInput.addEventListener('change', function() {
            // 파일 선택 여부 확인
            if (this.files && this.files[0]) {
                const file = this.files[0];

                // 파일 타입 검증
                if (!isImageFile(file)) {
                    alert('이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif)');
                    this.value = '';
                    if (!window.isReapplying || !document.querySelector('#profile-image-preview img')) {
                        profilePreview.innerHTML = '<i class="fi fi-rr-user"></i>';
                    }
                    return;
                }

                // 파일 크기 검증 (2MB)
                if (file.size > 2 * 1024 * 1024) {
                    alert('이미지 크기는 2MB를 초과할 수 없습니다.');
                    this.value = '';
                    if (!window.isReapplying || !document.querySelector('#profile-image-preview img')) {
                        profilePreview.innerHTML = '<i class="fi fi-rr-user"></i>';
                    }
                    return;
                }

                // 이미지 미리보기
                const reader = new FileReader();
                reader.onload = function(e) {
                    profilePreview.innerHTML = `<img src="${e.target.result}" alt="Profile Preview">`;
                };
                reader.readAsDataURL(file);

                // 새 이미지가 선택되면 기존 이미지 유지 필드 삭제
                const keepProfileImageField = document.querySelector('input[name="keepProfileImage"]');
                if (keepProfileImageField) {
                    keepProfileImageField.remove();
                }
                const existingProfileImageField = document.querySelector('input[name="existingProfileImage"]');
                if (existingProfileImageField) {
                    existingProfileImageField.remove();
                }
            }
        });
    }

    // PDF 파일 선택 처리
    const certificateInput = document.getElementById('certificate-file');
    const addPdfBtn = document.getElementById('add-pdf-btn');
    const pdfFilesList = document.getElementById('pdf-files-list');

    if (certificateInput && addPdfBtn && pdfFilesList) {
        // '파일 추가' 버튼 클릭 이벤트
        addPdfBtn.addEventListener('click', function() {
            if (certificateInput.files && certificateInput.files.length > 0) {
                const file = certificateInput.files[0];

                // 파일 타입 검증
                if (!isPdfFile(file)) {
                    alert('PDF 파일만 업로드 가능합니다: ' + file.name);
                    return;
                }

                // 파일 크기 검증 (5MB)
                if (file.size > 5 * 1024 * 1024) {
                    alert('PDF 파일 크기는 5MB를 초과할 수 없습니다: ' + file.name);
                    return;
                }

                // 중복 파일 검사
                if (selectedFiles.some(f => f.name === file.name)) {
                    alert('이미 추가된 파일입니다: ' + file.name);
                    return;
                }

                // 파일을 배열에 추가
                const newFile = {
                    file: file,
                    id: 'file_' + Date.now() // 고유 ID 생성
                };
                selectedFiles.push(newFile);

                // 파일 목록 UI 업데이트
                updateFilesList();

                // 파일 선택 필드 초기화
                certificateInput.value = '';
            } else {
                alert('파일을 선택해주세요.');
            }
        });

        // 파일 목록에서 삭제 이벤트 처리 (이벤트 위임 사용)
        pdfFilesList.addEventListener('click', function(e) {
            // 삭제 버튼 클릭 처리 (새 파일)
            if ((e.target.classList.contains('remove-file-btn') ||
                    e.target.closest('.remove-file-btn')) &&
                !e.target.closest('.existing-file')) { // 기존 파일이 아닌 경우만 처리

                const button = e.target.classList.contains('remove-file-btn') ?
                    e.target : e.target.closest('.remove-file-btn');
                const fileId = button.getAttribute('data-id');

                // 배열에서 해당 파일 제거
                selectedFiles = selectedFiles.filter(item => item.id !== fileId);

                // 파일 목록 UI 업데이트
                updateFilesList();
            }
        });
    }
}

// PDF 파일 목록 업데이트
function updateFilesList() {
    const pdfFilesList = document.getElementById('pdf-files-list');
    const certificateFilesContainer = document.getElementById('certificate-files-container');

    if (!pdfFilesList || !certificateFilesContainer) return;

    // 새 파일 목록 업데이트 (기존 파일은 그대로 둠)
    const existingFilesHTML = pdfFilesList.querySelectorAll('.existing-file');

    // 현재 추가된 새 파일 항목 제거
    const newFileItems = pdfFilesList.querySelectorAll('.file-item:not(.existing-file)');
    newFileItems.forEach(item => item.remove());

    // certificateFilesContainer 초기화
    certificateFilesContainer.innerHTML = '';

    // 각 새 파일에 대한 UI 요소 생성
    selectedFiles.forEach((item, index) => {
        // 파일 목록 UI 업데이트
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';
        fileItem.id = item.id;
        fileItem.innerHTML = `
            <i class="fi fi-rr-document"></i>
            <span class="file-name">${item.file.name}</span>
            <button type="button" class="remove-file-btn" data-id="${item.id}">
                <i class="fi fi-rr-cross-small"></i>
            </button>
        `;
        pdfFilesList.appendChild(fileItem);

        // 폼 제출용 hidden input 생성
        const input = document.createElement('input');
        input.type = 'file';
        input.name = 'certFiles';
        input.style.display = 'none';

        // 파일 데이터 복사
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(item.file);
        input.files = dataTransfer.files;

        certificateFilesContainer.appendChild(input);
    });
}

// 폼 유효성 검사
function initFormValidation() {
    const form = document.querySelector('form[action*="/tutor_join_pro"]');

    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();

            // 유효성 검사
            // 프로필 이미지 확인
            const profileInput = document.getElementById('profile-image');
            const isProfileEmpty = profileInput.files.length === 0;
            const hasExistingProfileImage = window.isReapplying && document.querySelector('#profile-image-preview img');

            // 프로필 이미지가 새로 선택되지 않았고, 기존 이미지도 없는 경우
            if (isProfileEmpty && !hasExistingProfileImage) {
                alert('프로필 이미지를 선택해주세요.');
                return;
            }

            // 인증서 파일 확인 (새 파일 또는 기존 파일 중 하나라도 있는지)
            const hasNewFiles = selectedFiles.length > 0;
            const existingFilesCount = document.querySelectorAll('.existing-file').length;

            if (!hasNewFiles && existingFilesCount === 0) {
                alert('자격증 PDF 파일을 하나 이상 추가해주세요.');
                return;
            }

            // 언어 선택 확인
            const bornLanguage = document.getElementById('born-language');
            if (bornLanguage && bornLanguage.value === '') {
                alert('모국어를 선택해주세요.');
                return;
            }

            const teachLanguage = document.getElementById('teach-language');
            if (teachLanguage && teachLanguage.value === '') {
                alert('가르칠 언어를 선택해주세요.');
                return;
            }

            // 모든 검증 통과 시 폼 제출
            form.submit();
        });
    }
}

// 이미지 파일 확인
function isImageFile(file) {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    return allowedTypes.includes(file.type);
}

// PDF 파일 확인
function isPdfFile(file) {
    return file.type === 'application/pdf';
}

// 이미지 미리보기 함수
function previewImage(input, previewId) {
    const preview = document.getElementById(previewId);

    if (input.files && input.files[0]) {
        const reader = new FileReader();

        reader.onload = function(e) {
            preview.innerHTML = `<img src="${e.target.result}" alt="Profile Preview">`;
        }

        reader.readAsDataURL(input.files[0]);
    } else {
        // 이미지가 선택되지 않았고, 재신청 중이 아니면 기본 아이콘 표시
        if (!window.isReapplying || !document.querySelector('#profile-image-preview img')) {
            preview.innerHTML = '<i class="fi fi-rr-user"></i>';
        }
    }
}