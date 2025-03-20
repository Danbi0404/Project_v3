package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.board.*;
import project.mapper.BoardMapper;

import java.util.List;

@Repository
public class BoardRepository {

    @Autowired
    private BoardMapper boardMapper;

    // 게시판 정보 목록 가져오기
    public List<BoardInfoBean> getBoardInfoList() {
        return boardMapper.getBoardInfoList();
    }

    // 게시글 목록 가져오기 (페이징 없음)
    public List<BoardBean> getBoardList(String board_info_type) {
        return boardMapper.getBoardList(board_info_type);
    }

    // 게시글 목록 가져오기 (페이징)
    public List<BoardBean> getBoardListWithPaging(String board_info_type, PageBean pageBean) {
        // 전체 게시글 수 조회
        int totalCount = boardMapper.getTotalBoardCount(board_info_type);

        // 페이지 정보 업데이트
        pageBean.setTotalContent(totalCount);

        // 게시글 목록 조회
        return boardMapper.getBoardListWithPaging(board_info_type,
                pageBean.getStartRow(),
                pageBean.getEndRow());
    }

    // 특정 게시판 정보 가져오기 (타입으로)
    public BoardInfoBean getBoardInfoByType(String board_info_type) {
        return boardMapper.getBoardInfoByType(board_info_type);
    }

    // 특정 게시판 정보 가져오기 (키로)
    public BoardInfoBean getBoardInfoByKey(int board_info_key) {
        return boardMapper.getBoardInfoByKey(board_info_key);
    }

    // 게시글 상세 정보 가져오기
    public BoardBean getBoardDetail(int board_key) {
        return boardMapper.getBoardDetail(board_key);
    }

    // 게시글 작성
    public void writeBoard(BoardBean boardBean) {
        boardMapper.writeBoard(boardBean);
    }

    // 게시글 수정
    public void updateBoard(BoardBean boardBean) {
        boardMapper.updateBoard(boardBean);
    }

    // 게시글 삭제
    public void deleteBoard(BoardBean boardBean) {
        boardMapper.deleteBoard(boardBean);
    }

    // 댓글 목록 가져오기
    public List<BoardCommentBean> getBoardCommentList(int board_key) {
        return boardMapper.getBoardCommentList(board_key);
    }

    // 댓글 작성
    public void writeComment(BoardCommentBean boardCommentBean) {
        boardMapper.writeComment(boardCommentBean);
    }

    // 댓글 키로 특정 댓글 정보 가져오기
    public BoardCommentBean getCommentByKey(int comment_key) {
        return boardMapper.getCommentByKey(comment_key);
    }

    // 대댓글 작성
    public void writeReply(BoardCommentBean boardCommentBean) {
        boardMapper.writeReply(boardCommentBean);
    }

    // 댓글 삭제
    public void deleteComment(BoardCommentBean boardCommentBean) {
        boardMapper.deleteComment(boardCommentBean);
    }

    // 좋아요 추가
    public void addLike(BoardLikeBean boardLikeBean) {
        boardMapper.addLike(boardLikeBean);
    }

    // 좋아요 삭제
    public void removeLike(BoardLikeBean boardLikeBean) {
        boardMapper.removeLike(boardLikeBean);
    }

    // 게시글 좋아요 수 조회
    public int getLikeCount(int board_key) {
        return boardMapper.getLikeCount(board_key);
    }

    // 사용자의 게시글 좋아요 여부 조회
    public boolean getUserLikeStatus(int board_key, int user_key) {
        return boardMapper.getUserLikeStatus(board_key, user_key) > 0;
    }

    // 게시글 검색 (페이징 없음)
    public List<BoardBean> searchBoard(String keyword, String board_info_type) {
        return boardMapper.searchBoard(keyword, board_info_type);
    }

    // 게시글 검색 (페이징)
    public List<BoardBean> searchBoardWithPaging(String keyword, String board_info_type, PageBean pageBean) {
        // 전체 검색 결과 수 조회
        int totalCount = boardMapper.getTotalSearchCount(keyword, board_info_type);

        // 페이지 정보 업데이트
        pageBean.setTotalContent(totalCount);

        // 검색 결과 조회
        return boardMapper.searchBoardWithPaging(keyword,
                board_info_type,
                pageBean.getStartRow(),
                pageBean.getEndRow());
    }

    // 게시글 댓글 수 조회
    public int getCommentCount(int board_key) {
        return boardMapper.getCommentCount(board_key);
    }

    // 전체 게시글 수 조회
    public int getTotalBoardCount(String board_info_type) {
        return boardMapper.getTotalBoardCount(board_info_type);
    }

    // 검색 결과 전체 개수
    public int getTotalSearchCount(String keyword, String board_info_type) {
        return boardMapper.getTotalSearchCount(keyword, board_info_type);
    }

}