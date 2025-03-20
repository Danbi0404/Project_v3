package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.GroupJoinRequestBean;
import project.mapper.GroupJoinRequestMapper;

import java.util.List;

@Repository
public class GroupJoinRequestRepository {

    @Autowired
    private GroupJoinRequestMapper groupJoinRequestMapper;

    public void createJoinRequest(GroupJoinRequestBean requestBean) {
        groupJoinRequestMapper.createJoinRequest(requestBean);
    }

    public List<GroupJoinRequestBean> getPendingRequestsByGroupKey(int groupKey) {
        return groupJoinRequestMapper.getPendingRequestsByGroupKey(groupKey);
    }

    public int checkExistingRequest(int groupKey, int userKey) { return groupJoinRequestMapper.checkExistingRequest(groupKey, userKey); }

    public void approveRequest(int requestKey) {
        groupJoinRequestMapper.approveRequest(requestKey);
    }

    public void addMember(int requestKey) {
        groupJoinRequestMapper.addMember(requestKey);
    };

    public void rejectRequest(int requestKey, String rejectReason) {
        groupJoinRequestMapper.rejectRequest(requestKey, rejectReason);
    }

}