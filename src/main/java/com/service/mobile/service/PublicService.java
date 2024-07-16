package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.response.ActivitiesResponse;
import com.service.mobile.dto.response.ConsultTypeResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Country;
import com.service.mobile.model.GlobalConfiguration;
import com.service.mobile.model.PackageUser;
import com.service.mobile.model.StaticPage;
import com.service.mobile.repository.CountryRepository;
import com.service.mobile.repository.GlobalConfigurationRepository;
import com.service.mobile.repository.PackageUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private ChargesService chargesService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private StaticPageService staticPageService;
    @Autowired
    private PackageUserRepository packageUserRepository;

    public List<Country> findAllCountry(){
        return countryRepository.findAll();
    }

    public ResponseEntity<?> getGlobalParams(Locale locale) {
        Map<String, String> globalDetail = globalConfigurationRepository.findByKeyIn(
                List.of("TURN_PASSWORD", "STURN_SERVER", "TURN_SERVER", "TURN_USERNAME")).stream()
                .collect(Collectors.toMap(GlobalConfiguration::getKey, GlobalConfiguration::getValue));

        Long maxFees = chargesService.getMaxConsultationFees();
        Long minFees = 0l;

        Map<String, String>[] slotTime = new HashMap[3];
        slotTime[0] = new HashMap<>();
        slotTime[0].put("key", "Morning");
        slotTime[0].put("value", "0-11");

        slotTime[1] = new HashMap<>();
        slotTime[1].put("key", "Afternoon");
        slotTime[1].put("value", "12-16");

        slotTime[2] = new HashMap<>();
        slotTime[2].put("key", "Evening");
        slotTime[2].put("value", "17-23");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("min_fees", minFees);
        responseData.put("max_fees", maxFees);
        responseData.put("turn_username", globalDetail.getOrDefault("TURN_USERNAME", ""));
        responseData.put("turn_password", globalDetail.getOrDefault("TURN_PASSWORD", ""));
        responseData.put("turn_server", globalDetail.getOrDefault("TURN_SERVER", ""));
        responseData.put("sturn_server", globalDetail.getOrDefault("STURN_SERVER", ""));
        responseData.put("currency_fdj", "currency_fdj_value"); // replace with appropriate value
        responseData.put("currency_symbol_fdj", "currency_fdj_symbol"); // replace with appropriate value
        responseData.put("upload_doc_msg", "upload_doc_msg_value"); // replace with appropriate value
        responseData.put("upload_id_msg", "upload_id_msg_value"); // replace with appropriate value
        responseData.put("slot_time", slotTime);

        Response response = new Response();
        response.setData(responseData);
        response.setMessage(messageSource.getMessage(Constants.GLOBAL_VARIABLES_FETCH,null,locale));
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getStaticPageContent(Locale locale, String type) {
        StaticPage page = staticPageService.findByName(type);
        String msg;
        Response response = new Response();
        if ("privacy_policy".equals(type)) {
            msg = messageSource.getMessage(Constants.PRIVEECY_POLICY_SUCCESS,null,locale);
        } else if ("terms_and_conditions".equals(type)) {
            msg = messageSource.getMessage(Constants.TERM_AND_CONDITION_SUCCESS,null,locale);
        } else {
            return ResponseEntity.status(403).body(
                    new Response(Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
                    )
            );
        }

        if (page == null) {
            return ResponseEntity.status(403).body(
                    new Response(Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
                    )
            );
        } else {
            response.setData(page.getDescription());
            response.setMessage(msg);
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<?> activities(Locale locale, Integer userId) {
        Response response = new Response();
        if (userId!=null && userId!=0) {
            Pageable pageable = PageRequest.of(0,1);
            List<PackageUser> packageDetailOpt = packageUserRepository.getActivePackageDetail(userId, YesNo.No,pageable);
            if (packageDetailOpt.size()>0) {
                PackageUser packageDetail = packageDetailOpt.get(0);
                ActivitiesResponse data = new ActivitiesResponse();

                data.setPackage_expired(packageDetail.getExpiredAt().format(DateTimeFormatter.ofPattern("d, MMMM yyyy HH:mm")));
                data.setPackage_status(Constants.ACTIVE);
                data.setPackage_name(packageDetail.getPackageInfo().getPackageName());
                data.setPackage_price("$"+packageDetail.getPackageInfo().getPackagePrice());

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ACTIVITIES_FETCH_SUCCESSFULLY,null,locale),
                        data
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ACTIVITIES_NOT_FOUND,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getConsultType(Locale locale, Integer userId) {
        List<ConsultTypeResponse> response = new ArrayList<>();
        ConsultTypeResponse data = new ConsultTypeResponse(
                messageSource.getMessage(Constants.VIDEO_CONSULTATION,null,locale),
                "",
                1,"video"
        );
        response.add(data);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                response
        ));
    }

}
