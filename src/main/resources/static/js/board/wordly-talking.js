/**
 * wordly-talking.js - 워들리 토킹 게시판 스크립트
 * 관련 파일: wordly-talking.html, board-common.css, wordly-talking.css
 */

document.addEventListener('DOMContentLoaded', function() {
    // 게시글 좋아요 기능 초기화
    initLikeButtons();

    // 메뉴 활성화 초기화
    initMenuHighlight();

    // 검색 기능 초기화
    initSearch();

    // 댓글 기능 초기화 (상세 페이지일 경우)
    if(document.querySelector('.comment-form')) {
        initCommentForm();
    }

    // 댓글 삭제 기능 초기화
    initCommentDeleteButtons();

    // 상대적 시간 표시 업데이트
    updateRelativeTimes();

    // 대댓글 폼 초기화 (상세 페이지일 경우)
    if(document.querySelector('.comment-list')) {
        initReplyForms();
    }
});

// ===== 게시글 좋아요 기능 초기화 =====
function initLikeButtons() {
    const likeButtons = document.querySelectorAll('.like-count');

    likeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();

            const boardKey = this.getAttribute('data-board-key');
            if(!boardKey) return;

            // AJAX 요청으로 좋아요 토글
            fetch('/board/api/like/toggle?boardKey=' + boardKey, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            })
                .then(response => {
                    if(response.status === 401) {
                        alert('로그인이 필요한 기능입니다.');
                        window.location.href = '/user/login';
                        throw new Error('로그인 필요');
                    }
                    return response.json();
                })
                .then(data => {
                    if(data.success) {
                        const heartIcon = this.querySelector('i');

                        if(data.isLiked) {
                            heartIcon.classList.remove('fi-rr-heart');
                            heartIcon.classList.add('fi-sr-heart');
                        } else {
                            heartIcon.classList.remove('fi-sr-heart');
                            heartIcon.classList.add('fi-rr-heart');
                        }

                        // 좋아요 수 업데이트
                        this.innerHTML = `<i class="${heartIcon.className}"></i> ${data.likeCount}`;
                    } else {
                        console.error('좋아요 처리 중 오류 발생:', data.message);
                    }
                })
                .catch(error => {
                    console.error('좋아요 요청 중 오류 발생:', error);
                });
        });
    });
}

function initMenuHighlight() {
    // URL 경로와 쿼리 파라미터 분석
    const pathParts = window.location.pathname.split('/');
    const searchParams = new URLSearchParams(window.location.search);

    // 기본 정보 설정
    let boardType = pathParts[2] || 'wordly-talking'; // 기본값: wordly-talking
    let category = pathParts[3] || 'free'; // 기본값: free (자유 게시판)

    // 검색 페이지인 경우 쿼리 파라미터에서 카테고리 가져오기
    if (pathParts[3] === 'search' && searchParams.has('category')) {
        category = searchParams.get('category');
    }

    // 메인 메뉴 활성화
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        const itemPath = item.getAttribute('href').split('/')[2];
        if (itemPath === boardType) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // 서브메뉴 활성화
    const submenuItems = document.querySelectorAll('.submenu-item');
    submenuItems.forEach(item => {
        const itemPath = item.getAttribute('href').split('/')[3];
        if (itemPath === category) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// ===== 검색 기능 초기화 =====
function initSearch() {
    const searchForm = document.querySelector('.search-box');
    if(!searchForm) return;

    const searchInput = searchForm.querySelector('input[type="text"]');
    const searchButton = searchForm.querySelector('.search-btn');
    // 히든 필드에서 카테고리 정보 가져오기
    const categoryInput = document.getElementById('searchCategory');

    searchButton.addEventListener('click', function(e) {
        e.preventDefault();
        performSearch(searchInput.value, categoryInput ? categoryInput.value : null);
    });

    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch(searchInput.value, categoryInput ? categoryInput.value : null);
        }
    });
}

// ===== 검색 실행 =====
function performSearch(query, category) {
    if (!query.trim()) {
        return;
    }

    // 현재 경로에서 게시판 타입 가져오기
    const pathParts = window.location.pathname.split('/');
    const boardType = pathParts[2] || 'wordly-talking';

    // 카테고리가 없으면 URL에서 유추
    if (!category) {
        if (pathParts.length > 3) {
            category = pathParts[3];
        } else {
            // 기본 카테고리 설정
            category = boardType === 'wordly-talking' ? 'free' : 'tutor';
        }
    }

    // 검색 결과 페이지로 리다이렉트
    window.location.href = `/board/${boardType}/search?q=${encodeURIComponent(query)}&category=${category}`;
}

// ===== 댓글 작성 폼 초기화 =====
function initCommentForm() {
    const commentForm = document.querySelector('.comment-form');
    if (!commentForm) return;

    // 이미 초기화되었는지 확인
    if (commentForm.hasAttribute('data-initialized')) return;

    const commentText = commentForm.querySelector('textarea');
    const submitButton = commentForm.querySelector('button[type="submit"]');

    commentForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const boardKey = this.getAttribute('data-board-key');
        if(!boardKey || !commentText.value.trim()) return;

        // 폼 제출 중에는 버튼 비활성화
        submitButton.disabled = true;

        const commentData = {
            comment_board_key: boardKey,
            comment_text: commentText.value.trim()
        };

        // AJAX 요청으로 댓글 작성
        fetch('/board/api/comment/write', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(commentData)
        })
            .then(response => {
                if(response.status === 401) {
                    alert('로그인이 필요한 기능입니다.');
                    window.location.href = '/user/login';
                    throw new Error('로그인 필요');
                }
                if(response.status === 403) {
                    // 403 Forbidden 오류 처리 (새로 추가)
                    return response.json().then(data => {
                        alert(data.message || '권한이 없습니다.');
                        throw new Error('권한 없음');
                    });
                }
                return response.json();
            })
            .then(data => {
                if(data.success) {
                    // 댓글 목록 갱신
                    updateCommentList(data.commentList);

                    // 폼 초기화
                    commentText.value = '';
                } else {
                    console.error('댓글 작성 중 오류 발생:', data.message);
                    alert(data.message || '댓글 작성 중 오류가 발생했습니다.');
                }
            })
            .catch(error => {
                console.error('댓글 요청 중 오류 발생:', error);
            })
            .finally(() => {
                // 작업 완료 후 버튼 다시 활성화
                submitButton.disabled = false;
            });
    });

    // 초기화 완료 표시
    commentForm.setAttribute('data-initialized', 'true');
}

// ===== 대댓글 작성 폼 토글 =====
function toggleReplyForm(commentKey) {
    const replyForm = document.querySelector(`.reply-form[data-parent-key="${commentKey}"]`);

    if(replyForm.style.display === 'block') {
        replyForm.style.display = 'none';
    } else {
        // 다른 대댓글 폼 닫기
        document.querySelectorAll('.reply-form').forEach(form => {
            form.style.display = 'none';
        });
        replyForm.style.display = 'block';
    }
}

// ===== 대댓글 작성 처리 =====
function initReplyForms() {
    const replyForms = document.querySelectorAll('.reply-form');

    replyForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();

            const parentCommentKey = this.getAttribute('data-parent-key');
            const boardKey = this.getAttribute('data-board-key');
            const replyText = this.querySelector('textarea').value.trim();

            if(!parentCommentKey || !boardKey || !replyText) return;

            const submitButton = this.querySelector('button[type="submit"]');
            submitButton.disabled = true;

            const replyData = {
                comment_board_key: boardKey,
                parent_comment_key: parentCommentKey,
                comment_text: replyText
            };

            // AJAX 요청으로 대댓글 작성
            fetch('/board/api/comment/reply', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify(replyData)
            })
                .then(response => {
                    if(response.status === 401) {
                        alert('로그인이 필요한 기능입니다.');
                        window.location.href = '/user/login';
                        throw new Error('로그인 필요');
                    }
                    if(response.status === 403) {
                        // 403 Forbidden 오류 처리 (새로 추가)
                        return response.json().then(data => {
                            alert(data.message || '권한이 없습니다.');
                            throw new Error('권한 없음');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    if(data.success) {
                        // 댓글 목록 갱신
                        updateCommentList(data.commentList);

                        // 폼 초기화 및 닫기
                        this.querySelector('textarea').value = '';
                        this.style.display = 'none';
                    } else {
                        console.error('대댓글 작성 중 오류 발생:', data.message);
                        alert(data.message || '대댓글 작성 중 오류가 발생했습니다.');
                    }
                })
                .catch(error => {
                    console.error('대댓글 요청 중 오류 발생:', error);
                })
                .finally(() => {
                    submitButton.disabled = false;
                });
        });
    });
}

// ===== 댓글 목록 갱신 수정 =====
function updateCommentList(commentList) {
    const commentContainer = document.querySelector('.comment-list');
    if(!commentContainer) return;

    // 댓글 목록 비우기
    commentContainer.innerHTML = '';

    // 댓글이 없는 경우
    if(!commentList || commentList.length === 0) {
        commentContainer.innerHTML = '<div class="no-comments">작성된 댓글이 없습니다.</div>';
        return;
    }

    // 댓글 목록 생성
    commentList.forEach(comment => {
        const commentElement = document.createElement('div');
        commentElement.className = 'comment-item' + (comment.depth > 0 ? ' comment-reply' : '');
        commentElement.dataset.commentKey = comment.comment_key;

        if(comment.depth > 0) {
            commentElement.style.paddingLeft = '30px';
            commentElement.style.borderLeft = '2px solid #e5e5e5';
            commentElement.style.marginLeft = '20px';
        }

        const createdDate = new Date(comment.created_date);
        const formattedDate = `${createdDate.getFullYear()}-${(createdDate.getMonth()+1).toString().padStart(2, '0')}-${createdDate.getDate().toString().padStart(2, '0')} ${createdDate.getHours().toString().padStart(2, '0')}:${createdDate.getMinutes().toString().padStart(2, '0')}`;

        commentElement.innerHTML = `
            <div class="comment-header">
                <span class="comment-author">${comment.user_nickname || comment.user_name}</span>
                <span class="comment-date" data-date="${comment.created_date}">${formattedDate}</span>
            </div>
            <div class="comment-content">${comment.comment_text}</div>
            <div class="comment-actions">
                ${comment.depth === 0 ? `<button class="reply-comment-btn" onclick="toggleReplyForm(${comment.comment_key})">답글</button>` : ''}
                ${comment.is_author ? `<button class="delete-comment" data-comment-key="${comment.comment_key}" data-board-key="${comment.comment_board_key}">삭제</button>` : ''}
            </div>
            ${comment.depth === 0 ? `
            <div class="reply-form-container">
                <form class="reply-form" data-parent-key="${comment.comment_key}" data-board-key="${comment.comment_board_key}" style="display: none;">
                    <textarea class="reply-textarea" placeholder="답글을 작성해주세요." required></textarea>
                    <div style="text-align: right;">
                        <button type="button" class="cancel-reply-btn" onclick="toggleReplyForm(${comment.comment_key})">취소</button>
                        <button type="submit" class="reply-submit">답글 등록</button>
                    </div>
                </form>
            </div>` : ''}
        `;

        commentContainer.appendChild(commentElement);
    });

    // 삭제 버튼 이벤트 초기화
    initCommentDeleteButtons();

    // 대댓글 폼 이벤트 초기화
    initReplyForms();

    // 상대적 시간 표시 업데이트
    updateRelativeTimes();
}

// ===== 댓글 삭제 버튼 초기화 =====
function initCommentDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.delete-comment');

    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();

            if(!confirm('댓글을 삭제하시겠습니까?')) return;

            const commentKey = this.getAttribute('data-comment-key');
            const boardKey = this.getAttribute('data-board-key');

            if(!commentKey || !boardKey) return;

            const commentData = {
                comment_key: commentKey,
                comment_board_key: boardKey
            };

            // AJAX 요청으로 댓글 삭제
            fetch('/board/api/comment/delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify(commentData)
            })
                .then(response => {
                    if(response.status === 401) {
                        alert('로그인이 필요한 기능입니다.');
                        window.location.href = '/user/login';
                        throw new Error('로그인 필요');
                    }
                    return response.json();
                })
                .then(data => {
                    if(data.success) {
                        // 댓글 목록 갱신
                        updateCommentList(data.commentList);
                    } else {
                        console.error('댓글 삭제 중 오류 발생:', data.message);
                        alert('댓글 삭제 중 오류가 발생했습니다.');
                    }
                })
                .catch(error => {
                    console.error('댓글 삭제 요청 중 오류 발생:', error);
                });
        });
    });
}

// ===== 상대적 시간 표시 업데이트 =====
function updateRelativeTimes() {
    const timestamps = document.querySelectorAll('.post-timestamp, .comment-date');

    timestamps.forEach(timestamp => {
        const dateStr = timestamp.getAttribute('data-date');
        if(!dateStr) return;

        const date = new Date(dateStr);
        const now = new Date();
        const diff = Math.floor((now - date) / 1000); // 초 단위 차이

        let timeText = '';

        if(diff < 60) {
            timeText = `${diff}초 전`;
        } else if(diff < 3600) {
            timeText = `${Math.floor(diff / 60)}분 전`;
        } else if(diff < 86400) {
            timeText = `${Math.floor(diff / 3600)}시간 전`;
        } else if(diff < 604800) {
            timeText = `${Math.floor(diff / 86400)}일 전`;
        } else {
            const year = date.getFullYear();
            const month = (date.getMonth() + 1).toString().padStart(2, '0');
            const day = date.getDate().toString().padStart(2, '0');
            timeText = `${year}-${month}-${day}`;
        }

        timestamp.textContent = timeText;
    });
}

// 1분마다 상대적 시간 업데이트
setInterval(updateRelativeTimes, 60000);