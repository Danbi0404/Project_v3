package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.GroupBean;
import project.beans.GroupMemberBean;
import project.mapper.GroupMapper;

import java.util.List;

@Repository
public class GroupRepository {

    @Autowired
    private GroupMapper groupMapper;

    public void createGroup(GroupBean groupBean) {
        groupMapper.createGroup(groupBean);
    }

    public List<GroupBean> getGroupsByTutorKey(int tutorKey) {
        return groupMapper.getGroupsByTutorKey(tutorKey);
    }

    public GroupBean getGroupByGroupBoardKey(Integer groupBoardKey) { return groupMapper.getGroupsByGroupBoardKey(groupBoardKey); }

    public List<GroupBean> getGroupsByGroupBoardKey(Integer groupBoardKey, int tutorkey)
    { return  groupMapper.getGroupsByGroupBoardKeyAndTutorKey(groupBoardKey, tutorkey); }

    public GroupBean getGroupByKey(int groupKey) {
        return groupMapper.getGroupByKey(groupKey);
    }

    public void updateGroup(GroupBean groupBean) {  groupMapper.updateGroup(groupBean);}

    public void updateGroupBoardKey(GroupBean groupBean) { groupMapper.updateGroupBoardKey(groupBean);}

    public void deleteGroup(int groupKey) {
        groupMapper.deleteGroup(groupKey);
    }

    public int countGroupsByTutorKey(int tutorKey) {
        return groupMapper.countGroupsByTutorKey(tutorKey);
    }

    public List<GroupMemberBean> getMembersByGroupKey(int groupKey) {return groupMapper.getMembersByGroupKey(groupKey);}
}