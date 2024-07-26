package com.service.mobile.controllers;

import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.service.LabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/lab")
public class LabController {

    @Autowired
    private LabService labRequest;

    @GetMapping("/get-lab-category-list")
    public ResponseEntity<?> getLabCategoryList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return labRequest.getLabCategoryList(locale);
    }

    @GetMapping("/get-lab-subcategory-list")
    public ResponseEntity<?> getLabCategoryList(
            @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
            @RequestParam(name = "category_id")Integer category_id) {
        return labRequest.getLabSubcategoryList(locale,category_id);
    }
}
