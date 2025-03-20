package project.service;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.beans.*;
import project.repository.GroupJoinRequestRepository;
import project.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private GroupJoinRequestRepository groupJoinRequestRepository;

    /**
     * 그룹 생성
     */
    public boolean createGroup(GroupBean groupBean) {
        try {
            // 현재 로그인한 튜터 정보 설정
            int key = loginUserBean.getUser_key();
            TutorBean tutor = tutorService.getTutorByUserKey(key);
            groupBean.setTutor_key(tutor.getTutor_key());

            groupRepository.createGroup(groupBean);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 튜터의 모든 그룹 조회
     */
    public List<GroupBean> getGroupsByTutorKey(int tutorKey) {
        return groupRepository.getGroupsByTutorKey(tutorKey);
    }

    /* group_board_key로 조회 */

    public GroupBean getGroupByGroupBoardKey(Integer groupBoardKey) { return groupRepository.getGroupByGroupBoardKey(groupBoardKey); }

    /* group_board_key, tutor_key로 조회 */

    public List<GroupBean> getGroupsByGroupBoardKey(Integer groupBoardKey, int tutorkey)
    { return groupRepository.getGroupsByGroupBoardKey(groupBoardKey, tutorkey); }

    /**
     * 특정 그룹 정보 조회
     */
    public GroupBean getGroupByKey(int groupKey) {
        return groupRepository.getGroupByKey(groupKey);
    }

    /**
     * 그룹 정보 업데이트
     */
    public boolean updateGroup(GroupBean groupBean) {
        try {
            groupRepository.updateGroup(groupBean);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* group_board_key 업데이트 */

    public void updateGroupBoardKey(GroupBean groupBean) {
        try {
            groupRepository.updateGroupBoardKey(groupBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 그룹 삭제
     */
    public boolean deleteGroup(int groupKey) {
        try {
            groupRepository.deleteGroup(groupKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 튜터의 그룹 수 조회
     */
    public int countGroupsByTutorKey(int tutorKey) {
        return groupRepository.countGroupsByTutorKey(tutorKey);
    }

    /* 그룹 가입 신청 테이블 생성 */

    public void createJoinRequest(GroupJoinRequestBean groupJoinRequestBean)
    { groupJoinRequestRepository.createJoinRequest(groupJoinRequestBean); }

    /* 그룹 가입 신청자 조회(중복방지) */

    public int checkExistingRequest(int groupKey, int userKey)
    { return groupJoinRequestRepository.checkExistingRequest(groupKey, userKey); }

    /**
     * 그룹 가입 신청 목록 조회
     */
    public List<GroupJoinRequestBean> getPendingRequestsByGroupKey(int groupKey) {
        return groupJoinRequestRepository.getPendingRequestsByGroupKey(groupKey);
    }

    /**
     * 가입 신청 승인 및 멤버 추가
     */
    @Transactional
    public boolean approveJoinRequest(int requestKey) {
        try {
            // 가입 신청 승인
            groupJoinRequestRepository.approveRequest(requestKey);

            // 멤버 추가
            groupJoinRequestRepository.addMember(requestKey);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 가입 신청 거부
     */
    public boolean rejectJoinRequest(int requestKey, String rejectReason) {
        try {
            groupJoinRequestRepository.rejectRequest(requestKey, rejectReason);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* 그룹 멤버 리스트 조회 */
    public List<GroupMemberBean> getMembersByGroupKey(int groupKey) {
        List<GroupMemberBean> members = groupRepository.getMembersByGroupKey(groupKey);

        // 결과가 없을 경우 빈 리스트 반환
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        return members;
    }

}