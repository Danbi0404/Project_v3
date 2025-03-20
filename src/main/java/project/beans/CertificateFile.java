package project.beans;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class CertificateFile {
    private int file_key;
    private int tutor_key;
    private String file_path;
    private String original_filename;
    private Date upload_date;
}