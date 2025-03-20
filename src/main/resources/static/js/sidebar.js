/**
 * sidebar.js - 사이드바 관련 스크립트
 * 모든 페이지에서 공통적으로 사용되는 사이드바 기능을 담당합니다.
 */

document.addEventListener('DOMContentLoaded', function() {
    // 메뉴 아이콘 요소와 서브메뉴 요소 선택
    const menuIconContainer = document.querySelector('.menu-icon-container');
    const subMenu = document.querySelector('.sub-menu');

    if (menuIconContainer && subMenu) {
        // 초기 상태는 숨김
        subMenu.style.display = 'none';

        // 메뉴 아이콘 컨테이너에 호버 이벤트 추가
        menuIconContainer.addEventListener('mouseenter', function() {
            subMenu.style.display = 'block';
        });

        // 메뉴 아이콘 컨테이너에서 마우스가 떠날 때 이벤트
        menuIconContainer.addEventListener('mouseleave', function() {
            subMenu.style.display = 'none';
        });

        // 서브메뉴에도 호버 이벤트 추가 (서브메뉴 위에 있을 때 유지)
        subMenu.addEventListener('mouseenter', function() {
            subMenu.style.display = 'block';
        });

        // 서브메뉴에서 마우스가 떠날 때 이벤트
        subMenu.addEventListener('mouseleave', function() {
            subMenu.style.display = 'none';
        });
    }
});

    // 사이드바 아이템 클릭 효과
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('mousedown', function() {
            this.style.transform = 'scale(0.95)';
        });

        item.addEventListener('mouseup', function() {
            this.style.transform = 'scale(1)';
        });

        item.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });

    // 로그아웃 버튼 이벤트
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function() {
            if(typeof Kakao !== 'undefined' && Kakao.Auth) { // 카카오 인증이 있다면
                Kakao.Auth.logout(function() { // 카카오 세션, 쿠키 제거 함수
                    Kakao.init('${KAKAO_JAVA_KEY}');
                    Kakao.Auth.cleanup(); // 카카오 인증 초기화
                    window.location.href = '/user/logout';
                });
            } else {
                window.location.href = '/user/logout';
            }
        });
    }

    // 이벤트 카드 슬라이더 초기화
    initEventSlider();

    // 카드 닫기 버튼 기능
    const closeButtons = document.querySelectorAll('.close-button');
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const parentCard = this.closest('.event-card, .status-card');
            if (parentCard) {
                parentCard.style.display = 'none';
            }
        });
    });


/**
 * 이벤트 슬라이더 초기화
 */
function initEventSlider() {
    const eventData = [
        {
            title: "현재 이벤트",
            description: "WORDLY에서 진행 중인 이벤트 정보입니다."
        },
        {
            title: "신규 강좌 오픈",
            description: "새로운 언어 과정이 추가되었습니다. 지금 확인해보세요!"
        },
        {
            title: "튜터 모집 중",
            description: "WORDLY에서 함께할 언어 튜터를 모집합니다."
        }
    ];

    const eventCards = document.querySelectorAll('.event-card');

    eventCards.forEach(card => {
        const slidesContainer = card.querySelector('.carousel-slides');
        const leftArrow = card.querySelector('.carousel-arrow-left');
        const rightArrow = card.querySelector('.carousel-arrow-right');

        let currentSlide = 0;

        // 슬라이드 초기화
        if (slidesContainer) {
            slidesContainer.innerHTML = '';
            eventData.forEach(event => {
                const slide = document.createElement('div');
                slide.className = 'carousel-slide';
                slide.innerHTML = `
                    <h3>${event.title}</h3>
                    <p>${event.description}</p>
                `;
                slidesContainer.appendChild(slide);
            });

            updateSliderPosition();
        }

        // 좌우 화살표 이벤트 리스너
        if (leftArrow) {
            leftArrow.addEventListener('click', function(e) {
                e.stopPropagation();
                currentSlide = (currentSlide - 1 + eventData.length) % eventData.length;
                updateSliderPosition();
            });
        }

        if (rightArrow) {
            rightArrow.addEventListener('click', function(e) {
                e.stopPropagation();
                currentSlide = (currentSlide + 1) % eventData.length;
                updateSliderPosition();
            });
        }

        function updateSliderPosition() {
            if (slidesContainer) {
                slidesContainer.style.transform = `translateX(-${currentSlide * 100}%)`;
            }
        }
    });
}

// 사용자 프로필 드롭다운 기능
document.addEventListener('DOMContentLoaded', function() {
    const userProfile = document.querySelector('.user-profile');

    if (userProfile) {
        userProfile.addEventListener('click', function(e) {
            const dropdown = this.querySelector('.dropdown-menu');
            if (dropdown) {
                dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
                e.stopPropagation();
            }
        });

        // 문서 클릭 시 드롭다운 닫기
        document.addEventListener('click', function() {
            const dropdown = userProfile.querySelector('.dropdown-menu');
            if (dropdown) {
                dropdown.style.display = 'none';
            }
        });
    }
});