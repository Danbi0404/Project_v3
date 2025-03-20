package project.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.beans.CertificateFile;
import project.beans.UserBean;
import project.repository.CertificateFileRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * FileService.java - 파일 업로드 서비스
 * 이미지 및 PDF 파일 업로드 처리를 담당합니다.
 */
@Service
public class FileService {

    @Value("${file.upload.path}")
    private String fileUploadPath;

    @Resource(name = "loginUserBean")
    private UserBean loginUserBean;

    @Autowired
    private CertificateFileRepository certificateFileRepository;

    // 허용된 이미지 확장자
    private final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    // 허용된 문서 확장자
    private final String[] ALLOWED_DOCUMENT_EXTENSIONS = {"pdf"};

    // 이미지 파일 업로드 처리
    public String saveImage(MultipartFile file) throws IOException {
        // 이미지 파일 확장자 검증
        if (file.isEmpty() || !isImageFile(file)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 파일입니다.");
        }

        String userId = loginUserBean != null && loginUserBean.isLogin() ?
                loginUserBean.getUser_id() : "unknown";
        return saveFile(file, "user_images", userId);
    }

    // PDF 파일 업로드 처리
    public String saveCertificate(MultipartFile file) throws IOException {
        // PDF 파일 검증
        if (file.isEmpty() || !isPdfFile(file)) {
            throw new IllegalArgumentException("유효하지 않은 PDF 파일입니다.");
        }

        String userId = loginUserBean != null && loginUserBean.isLogin() ?
                loginUserBean.getUser_id() : "unknown";
        return saveFile(file, "certificates", userId);
    }

    /**
     * 불필요한 인증서 파일 삭제
     * @param tutorKey 튜터 키
     * @param keepFileIds 유지할 파일 ID 목록
     */
    public void deleteUnusedCertificateFiles(int tutorKey, List<Integer> keepFileIds) {
        try {
            // 튜터의 모든 인증서 파일 조회
            List<CertificateFile> files = certificateFileRepository.getFilesByTutorKey(tutorKey);

            for (CertificateFile file : files) {
                // keepFileIds에 없는 파일은 삭제
                if (keepFileIds == null || !keepFileIds.contains(file.getFile_key())) {
                    // 물리적 파일 삭제
                    deleteFile(file.getFile_path());

                    // DB에서 파일 정보 삭제
                    certificateFileRepository.deleteFile(file.getFile_key());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 파일 저장 공통 로직 수정
    private String saveFile(MultipartFile file, String category, String userId) throws IOException {
        // 사용자 ID 기반 폴더 경로 생성 (이메일에서 특수문자 제거)
        String userPath = userId.replaceAll("[^a-zA-Z0-9]", "_");

        // 전체 저장 경로 생성 (사용자별 폴더)
        String uploadPath = fileUploadPath + "/" + category + "/" + userPath;

        // 디렉토리가 존재하지 않으면 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 원본 파일명 추출
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        String filenameWithoutExt = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            filenameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        }

        // 파일 유형에 따라 파일명 결정
        String newFileName;

        // 프로필 이미지는 항상 같은 이름으로
        if (category.equals("user_images")) {
            newFileName = "profile" + fileExtension;
        } else {
            // 인증서 등 다른 파일은 고유한 이름으로
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            newFileName = filenameWithoutExt + "_" + dateStr + fileExtension;
        }

        // 파일 저장 경로
        String filePath = uploadPath + "/" + newFileName;

        // 파일이 이미 존재하면 삭제 (덮어쓰기)
        File existingFile = new File(filePath);
        if (existingFile.exists()) {
            existingFile.delete();
        }

        // 파일 저장
        file.transferTo(new File(filePath));

        // 데이터베이스 저장용 상대 경로 반환
        return category + "/" + userPath + "/" + newFileName;
    }

    // 이미지 파일 여부 확인
    private boolean isImageFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return false;
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList(ALLOWED_IMAGE_EXTENSIONS).contains(extension);
    }

    // PDF 파일 여부 확인
    private boolean isPdfFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return false;
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList(ALLOWED_DOCUMENT_EXTENSIONS).contains(extension);
    }

    // 다중 인증서 파일 저장 메서드
    public List<CertificateFile> saveCertificates(List<MultipartFile> files, int tutorKey, String userId) throws IOException {
        List<CertificateFile> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty() || !isPdfFile(file)) {
                throw new IllegalArgumentException("유효하지 않은 PDF 파일입니다: " + file.getOriginalFilename());
            }

            String filePath = saveFile(file, "certificates", userId);

            CertificateFile certFile = new CertificateFile();
            certFile.setTutor_key(tutorKey);
            certFile.setFile_path(filePath);
            certFile.setOriginal_filename(file.getOriginalFilename());

            savedFiles.add(certFile);
        }

        return savedFiles;
    }

    /**
     * 파일 내용 읽기
     */
    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(fileUploadPath + "/" + filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("요청한 파일을 찾을 수 없습니다: " + filePath);
        }
        return Files.readAllBytes(path);
    }


    // 파일 삭제
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        // 전체 경로 생성
        String fullPath = fileUploadPath + "/" + filePath;

        try {
            Path path = Paths.get(fullPath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}