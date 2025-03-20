package project.beans.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageBean {
    // 현재 페이지 번호
    private int currentPage;

    // 페이지당 게시글 수
    private int contentPerPage;

    // 페이지 그룹당 페이지 수
    private int pagesPerGroup;

    // 전체 게시글 수
    private int totalContent;

    // 전체 페이지 수
    private int totalPage;

    // 현재 페이지 그룹 시작 페이지
    private int pageGroupStart;

    // 현재 페이지 그룹 끝 페이지
    private int pageGroupEnd;

    // 이전 페이지 그룹이 있는지 여부
    private boolean prevPageGroup;

    // 다음 페이지 그룹이 있는지 여부
    private boolean nextPageGroup;

    // 시작 번호 (SQL에서 사용)
    private int startRow;

    // 종료 번호 (SQL에서 사용)
    private int endRow;

    // 기본 생성자
    public PageBean() {
        this(1, 10, 10, 0);
    }

    // 페이지 번호만 지정하는 생성자
    public PageBean(int currentPage) {
        this(currentPage, 10, 10, 0);
    }

    // 모든 값을 지정하는 생성자
    public PageBean(int currentPage, int contentPerPage, int pagesPerGroup, int totalContent) {
        this.currentPage = currentPage;
        this.contentPerPage = contentPerPage;
        this.pagesPerGroup = pagesPerGroup;
        this.totalContent = totalContent;

        // 페이징 계산 수행
        calculatePaging();
    }

    // 페이징 관련 값 계산 메서드
    public void calculatePaging() {
        // 전체 페이지 수 계산
        totalPage = (int) Math.ceil((double) totalContent / contentPerPage);

        // 현재 페이지가 범위를 벗어나면 조정
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > totalPage && totalPage > 0) {
            currentPage = totalPage;
        }

        // 현재 페이지 그룹 계산
        int currentGroup = (int) Math.ceil((double) currentPage / pagesPerGroup);

        // 페이지 그룹의 시작, 끝 페이지 계산
        pageGroupStart = (currentGroup - 1) * pagesPerGroup + 1;
        pageGroupEnd = currentGroup * pagesPerGroup;

        // 마지막 그룹 처리
        if (pageGroupEnd > totalPage) {
            pageGroupEnd = totalPage;
        }

        // 이전, 다음 페이지 그룹 여부
        prevPageGroup = pageGroupStart > 1;
        nextPageGroup = pageGroupEnd < totalPage;

        // SQL 쿼리용 시작, 끝 행 계산 (Oracle 페이징용)
        startRow = (currentPage - 1) * contentPerPage + 1;
        endRow = currentPage * contentPerPage;
    }

    // 총 게시글 수 설정 및 페이징 재계산
    public void setTotalContent(int totalContent) {
        this.totalContent = totalContent;
        calculatePaging();
    }
}