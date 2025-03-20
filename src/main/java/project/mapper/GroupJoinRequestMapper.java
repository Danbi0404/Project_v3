package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.GroupJoinRequestBean;
import java.util.List;

@Mapper
public interface GroupJoinRequestMapper {

    /* request status는 오라클 db에서 default값을 pending으로 잡아둬서 생략함 */
    @Insert("INSERT INTO group_join_request (request_key, group_key, user_key, request_message, request_date) " +
            "VALUES (group_join_request_seq.nextval, #{group_key}, #{user_key}, #{request_message}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "request_key", keyColumn = "request_key")
    void createJoinRequest(GroupJoinRequestBean requestBean);

    /* 그룹 중복 신청 방지를 위한 확인 메서드 */
    @Select("SELECT COUNT(*) FROM group_join_request WHERE group_key = #{group_key} AND user_key = #{user_key} AND request_status = 'pending'")
    int checkExistingRequest(@Param("group_key") int groupKey, @Param("user_key") int userKey);

    @Select("SELECT gjr.*, u.user_name, u.user_nickname, u.user_image, tg.room_name " +
            "FROM group_join_request gjr " +
            "JOIN users u ON gjr.user_key = u.user_key " +
            "JOIN teach_group tg ON gjr.group_key = tg.group_key " +
            "WHERE gjr.group_key = #{groupKey} AND gjr.request_status = 'pending' " +
            "ORDER BY gjr.request_date DESC")
    List<GroupJoinRequestBean> getPendingRequestsByGroupKey(int groupKey);

    // 신청 승인 메서드(트랜잭션으로 동시에 멤버 추가 메서드 동작)
    @Update("UPDATE group_join_request SET request_status = 'approved', response_date = SYSDATE " +
            "WHERE request_key = #{requestKey}")
    void approveRequest(int requestKey);

    // 멤버 추가 메서드
    @Insert("INSERT INTO teach_group_member (group_key, user_key) " +
            "SELECT group_key, user_key FROM group_join_request WHERE request_key = #{requestKey}")
    void addMember(int requestKey);

    @Update("UPDATE group_join_request SET request_status = 'rejected', reject_reason = #{rejectReason}, response_date = SYSDATE " +
            "WHERE request_key = #{requestKey}")
    void rejectRequest(@Param("requestKey") int requestKey, @Param("rejectReason") String rejectReason);

}