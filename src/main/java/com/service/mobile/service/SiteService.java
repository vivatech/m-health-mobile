package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.Status;
import com.service.mobile.dto.enums.DeviceType;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.response.MobileReleaseDto;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.MobileRelease;
import com.service.mobile.repository.MobileReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SiteService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MobileReleaseRepository mobileReleaseRepository;

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
            DeviceType deviceType = getDeviceType(request.getDeviceType());
            String appVersion = request.getAppVersion();

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
}
