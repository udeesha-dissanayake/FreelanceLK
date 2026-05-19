package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when file upload fails
 */
public class FileUploadException extends FreelanceLKException {
    public FileUploadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED");
    }
}