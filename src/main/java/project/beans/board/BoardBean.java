package project.beans.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardBean {
    // 게시글 기본 정보
    private int board_key;
    private int board_info_key;
    private int user_key;
    private String board_title;
    private String board_text;
    private String created_date;

    // 게시글 작성자 정보 (JOIN 결과)
    private String user_name;
    private String user_nickname;

    // 게시글 카테고리 정보 (JOIN 결과)
    private String board_info_name;
    private String board_info_type;

    // 좋아요, 댓글 수 (집계)
    private int like_count;
    private int comment_count;

    // 사용자 좋아요 여부
    private boolean user_like;
}
