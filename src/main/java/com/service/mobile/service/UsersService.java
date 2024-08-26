package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.request.UpdatePictureRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Users;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RelativeService relativeService;

    @Value("${app.base.url}")
    private String baseUrl;


    public ResponseEntity<?> updateProfilePicture(UpdatePictureRequest request, Locale locale) {
        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
        if(users!=null){
            String profilePicPath = null;
            if (request.getProfile_picture() != null && !request.getProfile_picture().isEmpty()) {
                String filename = request.getProfile_picture().getOriginalFilename();
                String ext = StringUtils.getFilenameExtension(filename);
                if (!isAllowedExtension(request.getProfile_picture())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new Response(
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    Constants.NO_CONTENT_FOUNT,
                                    messageSource.getMessage(Constants.PROFILE_PICTURE_EXTENSION_NOT_ALLOWED,null,locale)
                            ));
                }

                if (request.getProfile_picture().getSize() > 5000000) { // 5MB
                    Map<String,String> mpadata = new HashMap<>();
                    mpadata.put("type","error");
                    mpadata.put("message",messageSource.getMessage(Constants.MAXIMIM_PROFILE_PIC_SIZE_EXCIDED,null,locale));
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mpadata);
                }

                profilePicPath = saveProfilePicture(users.getUserId(), request.getProfile_picture());
                if (profilePicPath == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save profile picture");
                }
            }

            users.setProfilePicture(profilePicPath != null ? profilePicPath : users.getProfilePicture());
            usersRepository.save(users);

            String photoUrl = profilePicPath != null?
                    baseUrl+"/uploaded_file/UserProfile/" + request.getUser_id() + "/" + profilePicPath : "";
            Map<String,String> resMap= new HashMap<>();
            resMap.put("profile_picture",photoUrl);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.PROFILE_PIC_UPLOAD_SUCCESS,null,locale),
                    resMap
            ));

        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    Constants.UNAUTHORIZED_CODE,
                    Constants.UNAUTHORIZED_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
            ));
        }
    }

    private String saveProfilePicture(Integer userId, MultipartFile profilePicture) {
        try {
            String uploadDir = baseUrl+ "uploaded_file/UserProfile/" + userId + "/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = StringUtils.cleanPath(profilePicture.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);
            Files.copy(profilePicture.getInputStream(), filePath);
            return filename;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean isAllowedExtension(MultipartFile profilePicture) {
        String[] fileFrags = profilePicture.getOriginalFilename().split("\\.");
        String extension = fileFrags[fileFrags.length-1];
        return extension.equals("gif") || extension.equals("png") || extension.equals("jpg");
    }
}
