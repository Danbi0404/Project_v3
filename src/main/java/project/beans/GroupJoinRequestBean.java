package project.beans;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class GroupJoinRequestBean {
    private Integer request_key;
    private Integer group_key;
    private Integer user_key;
    private String request_status;
    private String request_message;
    private Date request_date;
    private Date response_date;
    private String reject_reason;

    // 추가 정보 (조인 등을 위한)
    private String user_name;
    private String user_nickname;
    private String user_image;
    private String room_name;

    public GroupJoinRequestBean() {
        this.request_status = "pending";
    }
}