package com.service.mobile.service;

import com.service.mobile.model.Charges;
import com.service.mobile.repository.ChargesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargesService {

    @Autowired
    private ChargesRepository chargesRepository;

    public Integer getMaxConsultationFees() {
        return chargesRepository.getMaxConsultationFees();
    }

    public List<Charges> findByUserId(Integer userId) {
        return chargesRepository.findByUserId(userId);
    }
}
