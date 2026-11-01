package com.thinkmart.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thinkmart.exception.ApiException;

@Service
public class FileStorageService {

    private final Path dir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);
    }

    /** Saves a .png/.jpg/.jpeg and returns its public URL (/uploads/xxx), or null if no file was sent. */
    public String saveProfilePic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String ext;
        if (name.endsWith(".png")) {
            ext = ".png";
        } else if (name.endsWith(".jpg")) {
            ext = ".jpg";
        } else if (name.endsWith(".jpeg")) {
            ext = ".jpeg";
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile picture must be a .png, .jpg or .jpeg file");
        }
        String type = file.getContentType();
        if (type == null || !(type.equals("image/png") || type.equals("image/jpeg"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile picture must be a PNG or JPEG image");
        }

        String stored = UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the profile picture");
        }
        return "/uploads/" + stored;
    }
}
