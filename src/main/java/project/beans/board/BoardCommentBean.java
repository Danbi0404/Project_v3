package project.beans.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCommentBean {
    private int comment_key;
    private int comment_board_key;
    private int comment_user_key;
    private String comment_text;
    private String created_date;

    // 댓글 작성자 정보 (JOIN 결과)
    private String user_name;
    private String user_nickname;

    // 대댓글 관련 필드 추가
    private Integer parent_comment_key; // Integer로 선언하여 null 허용
    private int depth;
    private boolean is_author; // 댓글 작성자 여부

    // 대댓글 목록 (재귀 구조가 아닌 평면 구조로 관리)
    private boolean has_replies;
}
