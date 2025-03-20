package project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import project.beans.CertificateFile;
import project.mapper.CertificateFileMapper;

import java.util.List;

@Repository
public class CertificateFileRepository {

    @Autowired
    private CertificateFileMapper certificateFileMapper;

    /**
     * 인증서 파일 등록
     */
    public void insertFile(CertificateFile file) {
        certificateFileMapper.insertFile(file);
    }

    /**
     * 유저 키로 인증서 파일 목록 조회
     */
    public List<CertificateFile> getFilesByUserKey(int userKey) {
        return certificateFileMapper.getFilesByUserKey(userKey);
    }

    /**
     * 튜터 키로 인증서 파일 목록 조회
     */
    public List<CertificateFile> getFilesByTutorKey(int tutorKey) {
        return certificateFileMapper.getFilesByTutorKey(tutorKey);
    }

    /**
     * 파일 키로 인증서 파일 조회
     */
    public CertificateFile getFileByKey(int fileKey) {
        return certificateFileMapper.getFileByKey(fileKey);
    }

    /**
     * 튜터 키로 인증서 파일 삭제
     */
    public void deleteFilesByTutorKey(int tutorKey) {
        certificateFileMapper.deleteFilesByTutorKey(tutorKey);
    }

    /**
     * 특정 파일 삭제
     */
    public void deleteFile(int fileKey) {
        certificateFileMapper.deleteFile(fileKey);
    }
}