package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Consultation;
import com.service.mobile.repository.ConsultationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class PatientLabService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private MessageSource messageSource;

    public ResponseEntity<?> labRequest(LabRequestDto request, Locale locale) {
        if(request.getName()==null){request.setName("");}
        List<Consultation> consultations = new ArrayList<>();
        Pageable pageable= PageRequest.of(request.getPage(), 5);
        if(request.getDate()!=null){
            consultations = consultationRepository
                    .findByPatientReportSuggestedAndRequestTypeAndNameAndDate(request.getUser_id(),"1", RequestType.Book,request.getName(),request.getDate(),pageable);
        }else {
            consultations = consultationRepository
                    .findByPatientReportSuggestedAndRequestTypeAndName(request.getUser_id(),"1", RequestType.Book,request.getName(),pageable);
        }
        if(consultations.size()>0){
            //TODO make remaing logic
//            for(Consultation consultation:consultations){
//                if(consultation.getL)
//            }
            return null;
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
            ));
        }


    }
}
