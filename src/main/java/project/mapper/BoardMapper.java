package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.board.BoardBean;
import project.beans.board.BoardCommentBean;
import project.beans.board.BoardInfoBean;
import project.beans.board.BoardLikeBean;

import java.util.List;

@Mapper
public interface BoardMapper {

    /**
     * 게시판 정보 목록 가져오기
     */
    @Select("SELECT board_info_key, board_info_name, board_info_type FROM board_info ORDER BY board_info_key")
    List<BoardInfoBean> getBoardInfoList();

    /**
     * 특정 게시판 정보 가져오기 (타입으로)
     */
    @Select("SELECT board_info_key, board_info_name, board_info_type FROM board_info WHERE board_info_type = #{board_info_type}")
    BoardInfoBean getBoardInfoByType(String board_info_type);

    /**
     * 특정 게시판 정보 가져오기 (키로)
     */
    @Select("SELECT board_info_key, board_info_name, board_info_type FROM board_info WHERE board_info_key = #{board_info_key}")
    BoardInfoBean getBoardInfoByKey(int board_info_key);

    /**
     * 게시글 목록 가져오기 (페이징 없음)
     */
    @Select("SELECT b.board_key, b.board_info_key, b.user_key, b.board_title, b.board_text, " +
            "TO_CHAR(b.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bi.board_info_name " +
            "FROM board b " +
            "JOIN users u ON b.user_key = u.user_key " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE bi.board_info_type = #{board_info_type} " +
            "ORDER BY b.board_key DESC")
    List<BoardBean> getBoardList(String board_info_type);

    /**
     * 게시글 목록 가져오기 (페이징)
     */
    @Select("SELECT * FROM (" +
            "SELECT ROWNUM AS rnum, A.* FROM (" +
            "SELECT b.board_key, b.board_info_key, b.user_key, b.board_title, b.board_text, " +
            "TO_CHAR(b.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bi.board_info_name " +
            "FROM board b " +
            "JOIN users u ON b.user_key = u.user_key " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE bi.board_info_type = #{board_info_type} " +
            "ORDER BY b.board_key DESC" +
            ") A WHERE ROWNUM <= #{endRow}" +
            ") WHERE rnum >= #{startRow}")
    List<BoardBean> getBoardListWithPaging(String board_info_type, int startRow, int endRow);

    /**
     * 특정 게시판의 전체 게시글 수 조회
     */
    @Select("SELECT COUNT(*) FROM board b " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE bi.board_info_type = #{board_info_type}")
    int getTotalBoardCount(String board_info_type);

    /**
     * 게시글 상세 정보 가져오기
     */
    @Select("SELECT b.board_key, b.board_info_key, b.user_key, b.board_title, b.board_text, " +
            "TO_CHAR(b.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bi.board_info_name, bi.board_info_type " +
            "FROM board b " +
            "JOIN users u ON b.user_key = u.user_key " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE b.board_key = #{board_key}")
    BoardBean getBoardDetail(int board_key);

    /**
     * 게시글 작성
     */
    @Insert("INSERT INTO board (board_key, board_info_key, user_key, board_title, board_text, created_date) " +
            "VALUES (board_seq.nextval, #{board_info_key}, #{user_key}, #{board_title}, #{board_text}, SYSDATE)")
    @SelectKey(statement = "SELECT board_seq.currval FROM dual", keyProperty = "board_key", before = false, resultType = int.class)
    void writeBoard(BoardBean boardBean);//before = false : 삽입 후에, 키를 반환 받음

    /**
     * 게시글 수정
     */
    @Update("UPDATE board SET board_title = #{board_title}, board_text = #{board_text} " +
            "WHERE board_key = #{board_key} AND user_key = #{user_key}")
    void updateBoard(BoardBean boardBean);

    /**
     * 게시글 삭제
     */
    @Delete("DELETE FROM board WHERE board_key = #{board_key} AND user_key = #{user_key}")
    void deleteBoard(BoardBean boardBean);

    /**
     * 댓글 목록 가져오기 (대댓글 포함)
     */
    @Select("SELECT bc.comment_key, bc.comment_board_key, bc.comment_user_key, bc.comment_text, " +
            "TO_CHAR(bc.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bc.parent_comment_key, bc.depth, " +
            "CASE WHEN (SELECT COUNT(*) FROM board_comment WHERE parent_comment_key = bc.comment_key) > 0 THEN 1 ELSE 0 END AS has_replies " +
            "FROM board_comment bc " +
            "JOIN users u ON bc.comment_user_key = u.user_key " +
            "WHERE bc.comment_board_key = #{board_key} " +
            "ORDER BY CASE WHEN bc.parent_comment_key IS NULL THEN bc.comment_key ELSE bc.parent_comment_key END, " +
            "bc.depth, bc.comment_key")
    List<BoardCommentBean> getBoardCommentList(int board_key);

    /**
     * 댓글 키로 특정 댓글 정보 가져오기
     */
    @Select("SELECT comment_key, comment_board_key, comment_user_key, comment_text, " +
            "TO_CHAR(created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "parent_comment_key, depth " +
            "FROM board_comment " +
            "WHERE comment_key = #{comment_key}")
    BoardCommentBean getCommentByKey(int comment_key);


    /**
     * 댓글 작성
     */
    @Insert("INSERT INTO board_comment (comment_key, comment_board_key, comment_user_key, comment_text, created_date) " +
            "VALUES (comment_seq.nextval, #{comment_board_key}, #{comment_user_key}, #{comment_text}, SYSDATE)")
    void writeComment(BoardCommentBean boardCommentBean);

    /**
     * 대댓글 작성
     */
    @Insert("INSERT INTO board_comment (comment_key, comment_board_key, comment_user_key, comment_text, created_date, parent_comment_key, depth) " +
            "VALUES (comment_seq.nextval, #{comment_board_key}, #{comment_user_key}, #{comment_text}, SYSDATE, #{parent_comment_key}, #{depth})")
    void writeReply(BoardCommentBean boardCommentBean);

    /**
     * 댓글 삭제
     */
    @Delete("DELETE FROM board_comment WHERE comment_key = #{comment_key} AND comment_user_key = #{comment_user_key}")
    void deleteComment(BoardCommentBean boardCommentBean);

    /**
     * 좋아요 추가
     */
    @Insert("INSERT INTO board_like (board_key, user_key) VALUES (#{board_key}, #{user_key})")
    void addLike(BoardLikeBean boardLikeBean);

    /**
     * 좋아요 삭제
     */
    @Delete("DELETE FROM board_like WHERE board_key = #{board_key} AND user_key = #{user_key}")
    void removeLike(BoardLikeBean boardLikeBean);

    /**
     * 게시글 좋아요 수 조회
     */
    @Select("SELECT COUNT(*) FROM board_like WHERE board_key = #{board_key}")
    int getLikeCount(int board_key);

    /**
     * 사용자의 게시글 좋아요 여부 조회
     */
    @Select("SELECT COUNT(*) FROM board_like WHERE board_key = #{param1} AND user_key = #{param2}")
    int getUserLikeStatus(int board_key, int user_key);

    /**
     * 게시글 검색 (제목 + 내용)
     */
    @Select("SELECT b.board_key, b.board_info_key, b.user_key, b.board_title, b.board_text, " +
            "TO_CHAR(b.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bi.board_info_name " +
            "FROM board b " +
            "JOIN users u ON b.user_key = u.user_key " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE (b.board_title LIKE '%' || #{keyword} || '%' OR b.board_text LIKE '%' || #{keyword} || '%') " +
            "AND bi.board_info_type = #{board_info_type} " +
            "ORDER BY b.board_key DESC")
    List<BoardBean> searchBoard(String keyword, String board_info_type);

    /**
     * 게시글 검색 (페이징)
     */
    @Select("SELECT * FROM (" +
            "SELECT ROWNUM AS rnum, A.* FROM (" +
            "SELECT b.board_key, b.board_info_key, b.user_key, b.board_title, b.board_text, " +
            "TO_CHAR(b.created_date, 'YYYY-MM-DD HH24:MI:SS') AS created_date, " +
            "u.user_name, u.user_nickname, bi.board_info_name " +
            "FROM board b " +
            "JOIN users u ON b.user_key = u.user_key " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE (b.board_title LIKE '%' || #{keyword} || '%' OR b.board_text LIKE '%' || #{keyword} || '%') " +
            "AND bi.board_info_type = #{board_info_type} " +
            "ORDER BY b.board_key DESC" +
            ") A WHERE ROWNUM <= #{endRow}" +
            ") WHERE rnum >= #{startRow}")
    List<BoardBean> searchBoardWithPaging(String keyword, String board_info_type, int startRow, int endRow);

    /**
     * 검색 결과 전체 개수
     */
    @Select("SELECT COUNT(*) FROM board b " +
            "JOIN board_info bi ON b.board_info_key = bi.board_info_key " +
            "WHERE (b.board_title LIKE '%' || #{keyword} || '%' OR b.board_text LIKE '%' || #{keyword} || '%') " +
            "AND bi.board_info_type = #{board_info_type}")
    int getTotalSearchCount(String keyword, String board_info_type);

    /**
     * 게시글 댓글 수 조회
     */
    @Select("SELECT COUNT(*) FROM board_comment WHERE comment_board_key = #{board_key}")
    int getCommentCount(int board_key);
}