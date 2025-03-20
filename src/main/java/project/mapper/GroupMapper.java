package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.GroupBean;
import project.beans.GroupMemberBean;

import java.util.List;

@Mapper
public interface GroupMapper {

    // (teach_group) 그룹 첫 생성시
    @Insert("INSERT INTO teach_group (group_key, tutor_key, group_board_key, room_name, teach_language, created_date) " +
            "VALUES (teach_group_seq.nextval, #{tutor_key}, #{group_board_key, jdbcType=INTEGER}, #{room_name}, #{teach_language}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "group_key", keyColumn = "group_key")
    void createGroup(GroupBean groupBean);

    // (teach_group) 그룹 정보 리스트
    @Select("SELECT tg.*, u.user_name as tutor_name, " +
            "(SELECT COUNT(*) FROM teach_group_member WHERE group_key = tg.group_key) as member_count, " +
            "(SELECT COUNT(*) FROM group_join_request WHERE group_key = tg.group_key AND request_status = 'pending') as pending_requests " +
            "FROM teach_group tg " +
            "JOIN tutor t ON tg.tutor_key = t.tutor_key " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE tg.tutor_key = #{tutorKey} " +
            "ORDER BY tg.created_date DESC")
    List<GroupBean> getGroupsByTutorKey(@Param("tutorKey") int tutorKey);

    // (teach_group) 그룹 보드 키로 조회
    @Select("SELECT tg.*, u.user_name as tutor_name, " +
            "(SELECT COUNT(*) FROM group_join_request WHERE group_key = tg.group_key AND request_status = 'pending') as pending_requests, " +
            "TO_CHAR(tg.created_date, 'YYYY-MM-DD') as created_date " +
            "FROM teach_group tg " +
            "JOIN tutor t ON tg.tutor_key = t.tutor_key " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE tg.group_board_key = #{group_board_key, jdbcType=INTEGER} " +
            "ORDER BY tg.created_date DESC")
    GroupBean getGroupsByGroupBoardKey(@Param("group_board_key") Integer group_board_key);

    // (teach_group) 그룹 보드 키, 튜터 키로 조회
    @Select("SELECT tg.*, u.user_name as tutor_name, " +
            "(SELECT COUNT(*) FROM teach_group_member WHERE group_key = tg.group_key) as member_count, " +
            "(SELECT COUNT(*) FROM group_join_request WHERE group_key = tg.group_key AND request_status = 'pending') as pending_requests, " +
            "TO_CHAR(tg.created_date, 'YYYY-MM-DD') as created_date " +
            "FROM teach_group tg " +
            "JOIN tutor t ON tg.tutor_key = t.tutor_key " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE (#{group_board_key, jdbcType=INTEGER} IS NULL OR tg.group_board_key = #{group_board_key, jdbcType=INTEGER}) " +
            "AND tg.tutor_key = #{tutorKey} " +
            "ORDER BY tg.created_date DESC")
    List<GroupBean> getGroupsByGroupBoardKeyAndTutorKey(@Param("group_board_key") Integer group_board_key, @Param("tutorKey") int tutorKey);

    // (teach_group) 그룹 키로 정보 가져옴
    @Select("SELECT tg.*, u.user_name as tutor_name, " +
            "(SELECT COUNT(*) FROM teach_group_member WHERE group_key = tg.group_key) as member_count, " +
            "(SELECT COUNT(*) FROM group_join_request WHERE group_key = tg.group_key AND request_status = 'pending') as pending_requests, " +
            "TO_CHAR(tg.created_date, 'YYYY-MM-DD') as created_date " +
            "FROM teach_group tg " +
            "JOIN tutor t ON tg.tutor_key = t.tutor_key " +
            "JOIN users u ON t.user_key = u.user_key " +
            "WHERE tg.group_key = #{groupKey}")
    GroupBean getGroupByKey(@Param("groupKey") int groupKey);

    // (teach_group) 그룹 업데이트
    @Update("UPDATE teach_group SET room_name = #{room_name}, teach_language = #{teach_language} " +
            "WHERE group_key = #{group_key}")
    void updateGroup(GroupBean groupBean);

    // (teach_group) 게시글 정보 담기
    @Update("UPDATE teach_group SET group_board_key = #{group_board_key, jdbcType=INTEGER} " +
            "WHERE group_key = #{group_key}")
    void updateGroupBoardKey(GroupBean groupBean);

    // (teach_group) 그룹 삭제
    @Delete("DELETE FROM teach_group WHERE group_key = #{groupKey}")
    void deleteGroup(int groupKey);

    // (teach_group) 튜터 키로 그룹 수 조회
    @Select("SELECT COUNT(*) FROM teach_group WHERE tutor_key = #{tutorKey}")
    int countGroupsByTutorKey(int tutorKey);

    // (teach_group_member) 그룹 멤버 수 조회
    @Select("SELECT COUNT(*) FROM teach_group_member WHERE group_key = #{groupKey}")
    int countMembersByGroupKey(@Param("groupKey") int groupKey);

    // (teach_group_member) 특정 그룹에 속한 멤버들 조회
    @Select("SELECT u.user_key, u.user_nickname " +
            "FROM teach_group_member tgm " +
            "JOIN users u ON tgm.user_key = u.user_key " +
            "WHERE tgm.group_key = #{groupKey}")
    List<GroupMemberBean> getMembersByGroupKey(@Param("groupKey") int groupKey);
}