package project.mapper;

import org.apache.ibatis.annotations.*;
import project.beans.CertificateFile;

import java.util.List;

@Mapper
public interface CertificateFileMapper {

    @Insert("INSERT INTO certificate_files (file_key, tutor_key, file_path, original_filename, upload_date) " +
            "VALUES (certificate_file_seq.nextval, #{tutor_key}, #{file_path}, #{original_filename}, SYSDATE)")
    void insertFile(CertificateFile file);

    @Select("SELECT cf.* FROM certificate_files cf " +
            "JOIN tutor t ON cf.tutor_key = t.tutor_key " +
            "WHERE t.user_key = #{userKey}")
    List<CertificateFile> getFilesByUserKey(int userKey);

    @Select("SELECT * FROM certificate_files WHERE tutor_key = #{tutorKey}")
    List<CertificateFile> getFilesByTutorKey(int tutorKey);

    @Select("SELECT * FROM certificate_files WHERE file_key = #{fileKey}")
    CertificateFile getFileByKey(int fileKey);

    @Delete("DELETE FROM certificate_files WHERE file_key = #{fileKey}")
    void deleteFile(int fileKey);

    @Delete("DELETE FROM certificate_files WHERE tutor_key = #{tutorKey}")
    void deleteFilesByTutorKey(int tutorKey);
}