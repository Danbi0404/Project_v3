package project.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMemberBean {
    private int group_key;
    private int user_key;
    private String user_nickname; // users 테이블 조인용

    // 기본 생성자
    public GroupMemberBean() {}

    // 모든 필드를 포함한 생성자
    public GroupMemberBean(int group_key, int user_key, String user_nickname) {
        this.group_key = group_key;
        this.user_key = user_key;
        this.user_nickname = user_nickname;
    }
}