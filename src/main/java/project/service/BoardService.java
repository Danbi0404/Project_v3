package project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.beans.board.*;
import project.repository.BoardRepository;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 게시판 정보 목록 가져오기
    public List<BoardInfoBean> getBoardInfoList() {
        return boardRepository.getBoardInfoList();
    }

    // 게시글 목록 가져오기 (페이징 없음)
    public List<BoardBean> getBoardList(String board_info_type, int current_user_key) {
        List<BoardBean> boardList = boardRepository.getBoardList(board_info_type);

        // 각 게시글의 좋아요 수, 댓글 수, 사용자 좋아요 여부 설정
        for(BoardBean board : boardList) {
            board.setLike_count(boardRepository.getLikeCount(board.getBoard_key()));
            board.setComment_count(boardRepository.getCommentCount(board.getBoard_key()));
            if(current_user_key > 0) {
                board.setUser_like(boardRepository.getUserLikeStatus(board.getBoard_key(), current_user_key));
            }
        }

        return boardList;
    }

    // 전체 게시글 수 조회
    public int getTotalBoardCount(String board_info_type) {
        return boardRepository.getTotalBoardCount(board_info_type);
    }

    // 검색 결과 전체 개수
    public int getTotalSearchCount(String keyword, String board_info_type) {
        return boardRepository.getTotalSearchCount(keyword, board_info_type);
    }

// 게시글 목록 가져오기 (페이징)
public List<BoardBean> getBoardListWithPaging(String board_info_type, int current_user_key, PageBean pageBean) {
    List<BoardBean> boardList = boardRepository.getBoardListWithPaging(board_info_type, pageBean);

    // 각 게시글의 좋아요 수, 댓글 수, 사용자 좋아요 여부 설정
    for(BoardBean board : boardList) {
        board.setLike_count(boardRepository.getLikeCount(board.getBoard_key()));
        board.setComment_count(boardRepository.getCommentCount(board.getBoard_key()));
        if(current_user_key > 0) {
            board.setUser_like(boardRepository.getUserLikeStatus(board.getBoard_key(), current_user_key));
        }
    }

    return boardList;
}

// 특정 게시판 정보 가져오기 (타입으로)
public BoardInfoBean getBoardInfoByType(String board_info_type) {
    return boardRepository.getBoardInfoByType(board_info_type);
}

// 특정 게시판 정보 가져오기 (키로)
public BoardInfoBean getBoardInfoByKey(int board_info_key) {
    return boardRepository.getBoardInfoByKey(board_info_key);
}

// 게시글 상세 정보 가져오기 (좋아요 수, 댓글 수 포함)
public BoardBean getBoardDetail(int board_key, int current_user_key) {
    BoardBean boardBean = boardRepository.getBoardDetail(board_key);

    if(boardBean != null) {
        boardBean.setLike_count(boardRepository.getLikeCount(board_key));
        boardBean.setComment_count(boardRepository.getCommentCount(board_key));
        if(current_user_key > 0) {
            boardBean.setUser_like(boardRepository.getUserLikeStatus(board_key, current_user_key));
        }
    }

    return boardBean;
}

// 게시글 작성
public void writeBoard(BoardBean boardBean) {
    boardRepository.writeBoard(boardBean);
}

// 게시글 수정
public void updateBoard(BoardBean boardBean) {
    boardRepository.updateBoard(boardBean);
}

// 게시글 삭제
public void deleteBoard(BoardBean boardBean) {
    boardRepository.deleteBoard(boardBean);
}

// 댓글 목록 가져오기
public List<BoardCommentBean> getBoardCommentList(int board_key) {
    return boardRepository.getBoardCommentList(board_key);
}

// 댓글 작성
public void writeComment(BoardCommentBean boardCommentBean) {
    boardRepository.writeComment(boardCommentBean);
}

// 대댓글 작성
public void writeReply(BoardCommentBean boardCommentBean) {
    // 부모 댓글 정보 가져오기
    BoardCommentBean parentComment = boardRepository.getCommentByKey(boardCommentBean.getParent_comment_key());

    // 부모 댓글이 이미 대댓글인 경우 (depth > 0), 최상위 부모로 설정
    if (parentComment != null && parentComment.getParent_comment_key() != null) {
        boardCommentBean.setParent_comment_key(parentComment.getParent_comment_key());
    }

    // depth 설정 (1로 고정 - 2단계 이상의 중첩 대댓글은 허용하지 않음)
    boardCommentBean.setDepth(1);

    // 대댓글 작성
    boardRepository.writeReply(boardCommentBean);
}

// 댓글 삭제
public void deleteComment(BoardCommentBean boardCommentBean) {
    boardRepository.deleteComment(boardCommentBean);
}

// 좋아요 토글 (있으면 삭제, 없으면 추가)
public boolean toggleLike(int board_key, int user_key) {
    BoardLikeBean boardLikeBean = new BoardLikeBean();
    boardLikeBean.setBoard_key(board_key);
    boardLikeBean.setUser_key(user_key);

    boolean currentStatus = boardRepository.getUserLikeStatus(board_key, user_key);

    if(currentStatus) {
        // 좋아요가 이미 있으면 삭제
        boardRepository.removeLike(boardLikeBean);
        return false;
    } else {
        // 좋아요가 없으면 추가
        boardRepository.addLike(boardLikeBean);
        return true;
    }
}

// 게시글 검색 (페이징 없음)
public List<BoardBean> searchBoard(String keyword, String board_info_type, int current_user_key) {
    List<BoardBean> boardList = boardRepository.searchBoard(keyword, board_info_type);

    // 각 게시글의 좋아요 수, 댓글 수, 사용자 좋아요 여부 설정
    for(BoardBean board : boardList) {
        board.setLike_count(boardRepository.getLikeCount(board.getBoard_key()));
        board.setComment_count(boardRepository.getCommentCount(board.getBoard_key()));
        if(current_user_key > 0) {
            board.setUser_like(boardRepository.getUserLikeStatus(board.getBoard_key(), current_user_key));
        }
    }

    return boardList;
}

// 게시글 검색 (페이징)
public List<BoardBean> searchBoardWithPaging(String keyword, String board_info_type, int current_user_key, PageBean pageBean) {
    List<BoardBean> boardList = boardRepository.searchBoardWithPaging(keyword, board_info_type, pageBean);

        // 각 게시글의 좋아요 수, 댓글 수, 사용자 좋아요 여부 설정
        for(BoardBean board : boardList) {
            board.setLike_count(boardRepository.getLikeCount(board.getBoard_key()));
            board.setComment_count(boardRepository.getCommentCount(board.getBoard_key()));
            if(current_user_key > 0) {
                board.setUser_like(boardRepository.getUserLikeStatus(board.getBoard_key(), current_user_key));
            }
        }

        return boardList;
    }

}
