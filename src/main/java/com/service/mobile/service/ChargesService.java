package com.service.mobile.service;

import com.service.mobile.repository.ChargesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargesService {

    @Autowired
    private ChargesRepository chargesRepository;

    public Long getMaxConsultationFees() {
        return chargesRepository.getMaxConsultationFees();
    }
}
