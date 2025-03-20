package project.beans;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * TutorBean.java - 튜터 정보 객체
 * 튜터 관련 정보 및 신청 상태를 관리합니다.
 */
@Getter
@Setter
public class TutorBean {
    // 기본 정보
    private int tutor_key;
    private int user_key; // 변경: user_user_key -> user_key
    private String born_language;
    private String teach_language;

    // 파일 정보
    private String tutor_image;
    private List<CertificateFile> certificateFiles;

    // 상태 정보
    private String status;
    private Date apply_date;
    private Date approve_date;
    private Date create_date;

    // 추가 정보 (조인용)
    private String user_name;
    private String user_nickname;

    //튜터 신청 거부 사유
    private String reject_reason;

    // 재신청 여부를 명시적으로 표현
    private boolean isReapply = false;

    // 생성자
    public TutorBean() {
        this.status = "pending"; // 기본 상태는 대기중
        this.certificateFiles = new ArrayList<>();
    }

    //tutor의 가르칠 언어 추가
    public List<String> getTeachLanguageList() {
        if (teach_language == null || teach_language.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(teach_language.split(","));
    }

    public void addTeachLanguage(String language) {
        List<String> languages = getTeachLanguageList();
        if (!languages.contains(language)) {
            languages.add(language);
            this.teach_language = String.join(",", languages);
        }
    }

    public boolean hasTeachLanguage(String language) {
        return getTeachLanguageList().contains(language);
    }
}