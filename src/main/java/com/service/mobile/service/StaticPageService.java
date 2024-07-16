package com.service.mobile.service;

import com.service.mobile.model.StaticPage;
import com.service.mobile.repository.StaticPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaticPageService {

    @Autowired
    private StaticPageRepository staticPageRepository;

    public StaticPage findByName(String name) {
        return staticPageRepository.findByName(name);
    }
}
