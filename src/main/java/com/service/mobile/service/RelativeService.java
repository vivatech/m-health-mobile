package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.UserRelativeDto;
import com.service.mobile.dto.request.CreateRelativeProfileRequest;
import com.service.mobile.dto.request.GetSingleRelativeProfileRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.UserRelative;
import com.service.mobile.repository.UserRelativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RelativeService {

    @Autowired
    private UserRelativeRepository userRelativeRepository;

    @Autowired
    private MessageSource messageSource;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${storage.location}")
    private String uploadDir;

    public ResponseEntity<?> relativeList(Locale locale, Integer userId) {
        List<UserRelative> relatives = userRelativeRepository.findByCreatedBy(userId);
        List<UserRelativeDto> list = new ArrayList<>();
        for(UserRelative data:relatives){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            list.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                list
        ));
    }

    public ResponseEntity<?> createRelativeProfile(Locale locale, CreateRelativeProfileRequest request) throws IOException {
        UserRelative userRelative = new UserRelative();
        userRelative.setUserId(request.getUser_id());
        userRelative.setName(request.getName());
        userRelative.setDob(request.getDob());
        userRelative.setRelationWithPatient(request.getRelation_with_patient());
        userRelative.setCreatedBy(request.getUser_id());
        userRelative.setStatus("A");
        userRelative.setCreatedAt(LocalDateTime.now());
        userRelative.setUpdatedAt(LocalDateTime.now());

        if (request.getProfile_picture() != null && !request.getProfile_picture().isEmpty()) {
            validateFile(request.getProfile_picture());
            String fileName = saveProfilePicture(request.getProfile_picture(), userRelative.getId());
            userRelative.setProfilePicture(fileName);
        }

        userRelative = userRelativeRepository.save(userRelative);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                userRelative.getId()
        ));
    }

    private void validateFile(MultipartFile file) {
        String[] allowedExtensions = {"gif", "png", "jpg"};
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            throw new IllegalArgumentException("profile_pic_ext_not_allowed");
        }

        if (file.getSize() > 5000000) {
            throw new IllegalArgumentException("max_profile_pic_size");
        }
    }

    private String saveProfilePicture(MultipartFile file, Byte userId) throws IOException {
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + "/uploaded_file/relatives/" + userId + "/";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        file.transferTo(new File(filePath + fileName));
        return fileName;
    }

    public ResponseEntity<?> updateRelativeProfile(Locale locale, CreateRelativeProfileRequest request) throws IOException {
        UserRelative userRelative = userRelativeRepository.findById(request.getId()).orElse(null);
        if(userRelative!=null){
            userRelative.setUserId(request.getUser_id());
            userRelative.setName(request.getName());
            userRelative.setDob(request.getDob());
            userRelative.setRelationWithPatient(request.getRelation_with_patient());
            userRelative.setCreatedBy(request.getUser_id());
            userRelative.setStatus("A");
            userRelative.setCreatedAt(LocalDateTime.now());
            userRelative.setUpdatedAt(LocalDateTime.now());

            if (request.getProfile_picture() != null && !request.getProfile_picture().isEmpty()) {
                validateFile(request.getProfile_picture());
                String fileName = saveProfilePicture(request.getProfile_picture(), userRelative.getId());
                userRelative.setProfilePicture(fileName);
            }

            userRelative = userRelativeRepository.save(userRelative);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    userRelative.getId()
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG,null,locale),
                    userRelative.getId()
            ));
        }
    }

    public ResponseEntity<?> getSingleRelativeProfile(Locale locale, GetSingleRelativeProfileRequest request) {
        UserRelative data = userRelativeRepository.findById(request.getId()).orElse(null);
        if(data!=null){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    dto
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale)
            ));
        }
    }

    public ResponseEntity<?> relativeType(Locale locale, GetSingleRelativeProfileRequest request) {
        List<UserRelative> relatives = userRelativeRepository.findAll();
        List<UserRelativeDto> list = new ArrayList<>();
        for(UserRelative data:relatives){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            list.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                list
        ));
    }
}
