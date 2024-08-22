package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.Status;
import com.service.mobile.dto.enums.DeviceType;
import com.service.mobile.dto.request.GetSloatsRequest;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.response.AppBannerResponse;
import com.service.mobile.dto.response.MobileReleaseDto;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.VideoAttachmentResponse;
import com.service.mobile.model.AppBanner;
import com.service.mobile.model.MobileRelease;
import com.service.mobile.model.VideoAttachment;
import com.service.mobile.repository.AppBannerRepository;
import com.service.mobile.repository.MobileReleaseRepository;
import com.service.mobile.repository.VideoAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SiteService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MobileReleaseRepository mobileReleaseRepository;

    @Autowired
    private AppBannerRepository appBannerRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.project.base}")
    private String projectBase;
    @Autowired
    private VideoAttachmentRepository videoAttachmentRepository;

    public static DeviceType getDeviceType(String deviceTypeStr) {
        try {
            return DeviceType.valueOf(deviceTypeStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            return DeviceType.Android;
        }
    }

    public ResponseEntity<?> getMobileRelease(MobileReleaseRequest request, Locale locale) {
        Response response = new Response();
        if (request != null) {
            DeviceType deviceType = getDeviceType(request.getDevice_type());
            String appVersion = request.getApp_version();

            MobileRelease releaseData = mobileReleaseRepository.findByAppVersionAndDeviceType(appVersion, deviceType);

            if (releaseData != null) {
                response.setData(new MobileReleaseDto(releaseData));
                response.setMessage(messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new Response(Constants.NO_RECORD_FOUND_CODE,
                                Constants.NO_RECORD_FOUND_CODE,
                                messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                        ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> appBanner(Locale locale) {
        List<AppBanner> banners = appBannerRepository.findAllByIdDesc();
        List<AppBannerResponse> response = new ArrayList<>();
        String url = baseUrl + "/uploaded_file/appbanner/"+projectBase+"/";
        String video = baseUrl + "/uploaded_file/videos/app_banner_video.mp4";
        for(AppBanner banner:banners){
            AppBannerResponse dto = new AppBannerResponse();

            dto.setType(banner.getType());
            dto.setType(projectBase);
            if(banner.getType().equalsIgnoreCase("video")){
                dto.setPath((banner.getVname()!=null && !banner.getVname().isEmpty())?baseUrl+"/uploaded_file/videos/"+banner.getVname():video);
                dto.setThumb((banner.getVname()!=null && !banner.getVname().isEmpty())?baseUrl+"/uploaded_file/image-gallery/"+banner.getIname():video);
            }else{
                dto.setPath(baseUrl+"/uploaded_file/image-gallery/"+banner.getIname());
                dto.setThumb(null);
            }
            dto.setType(banner.getType());
            dto.setType(banner.getType());

            response.add(dto);
        }
        if(!response.isEmpty()){
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.DATA_RETRIEVED,null,locale),
                    response
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getVideoAttachment(Locale locale, GetSloatsRequest request) {
        List<VideoAttachment> attachments = videoAttachmentRepository.findByCaseIdIdDesc(request.getCase_id());
        List<VideoAttachmentResponse> responses = new ArrayList<>();
        if(!attachments.isEmpty()){
            for(VideoAttachment attachment:attachments){
                VideoAttachmentResponse dto = new VideoAttachmentResponse();
                dto.setId(attachment.getId());
                dto.setFrom_id(attachment.getFromId());
                dto.setTo_id(attachment.getToId());
                dto.setUrl(attachment.getUrl());
                dto.setCreated_at(attachment.getCreatedAt());
                dto.setUrl_type(attachment.getFileType());

                responses.add(dto);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.VIDEO_HISTORY_SUCCESS,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }
}
