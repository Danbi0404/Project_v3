package project.beans;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class GroupBean {
    private int group_key;
    private int tutor_key;
    private Integer group_board_key;
    private String room_name;
    private String teach_language;
    private Date created_date;

    // 추가 정보 (조인 등을 위한)
    private String tutor_name;
    private int member_count;
    private int pending_requests;

    public GroupBean() {
        // 기본 생성자
    }
}