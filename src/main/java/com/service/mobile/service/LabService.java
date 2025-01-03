package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.LabCategoryDto;
import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.dto.SubCategoryDto;
import com.service.mobile.dto.enums.CategoryStatus;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.LabCategoryMaster;
import com.service.mobile.model.LabSubCategoryMaster;
import com.service.mobile.repository.LabCategoryMasterRepository;
import com.service.mobile.repository.LabSubCategoryMasterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.service.mobile.config.Constants.NO_CONTENT_FOUNT_CODE;
import static com.service.mobile.config.Constants.SOMETHING_WENT_WRONG;

@Service
@Slf4j
public class LabService {

    @Autowired
    private PublicService publicService;

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private LabCategoryMasterRepository labCategoryMasterRepository;
    @Autowired
    private LabSubCategoryMasterRepository labSubCategoryMasterRepository;

    public ResponseEntity<?> getLabCategoryList(Locale locale) {
        log.info("Entering into get lab category list api");
        Map<String, Object> res = new HashMap<>();
        try{
        List<LabCategoryDto> dtoList = getLabCategoryDtos(locale);
        if(dtoList.size()>0){
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_CATEGORY_RETRIEVED,null,locale),
                    dtoList
            ));
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_LAB_CATEGORY_NOT_FOUND,null,locale)
            ));
        }
    }catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in lab category list api : {}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(SOMETHING_WENT_WRONG, null, locale),
                    res
            ));
        }
    }

    private List<LabCategoryDto> getLabCategoryDtos(Locale locale) {
        List<LabCategoryMaster> labCategoryMasters = publicService.getAssignedLabsCategory();
        List<LabCategoryDto> dtoList = new ArrayList<>();
        for(LabCategoryMaster labCategory : labCategoryMasters){
            LabCategoryDto temp = new LabCategoryDto();
            temp.setCat_id(labCategory.getCatId().toString());
            if(locale.getLanguage().equalsIgnoreCase("en")){
                temp.setCat_name(labCategory.getCatName());
            }else {
                temp.setCat_name(labCategory.getCatNameSl());
            }
            dtoList.add(temp);
        }
        return dtoList;
    }

    public ResponseEntity<?> getLabSubcategoryList(Locale locale, Integer categoryId) {
        LabCategoryMaster labCategoryMasters = labCategoryMasterRepository.findById(categoryId).orElse(null);
        if (labCategoryMasters != null) {
            List<LabSubCategoryMaster> subCategoryList = labSubCategoryMasterRepository.findByCategoryId(categoryId, CategoryStatus.Active);
            if (!subCategoryList.isEmpty()) {
                List<SubCategoryDto> list = new ArrayList<>();
                for (LabSubCategoryMaster subCat : subCategoryList) {
                    SubCategoryDto dto = new SubCategoryDto();
                    dto.setCat_id(categoryId.toString());
                    dto.setSub_cat_id(subCat.getSubCatId());
                    dto.setCat_name(labCategoryMasters.getCatName());
                    dto.setSub_cat_name(subCat.getSubCatName());

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.LAB_SUB_CATEGORY_RETRIEVED, null, locale),
                        list
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_LAB_SUB_CATEGORY_NOT_FOUND, null, locale)
                ));
            }
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
    }
}
