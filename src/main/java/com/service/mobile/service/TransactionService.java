package com.service.mobile.service;

import com.service.mobile.dto.request.MyTransactionsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class TransactionService {
    public ResponseEntity<?> myTransactions(Locale locale, MyTransactionsRequest request) {
        return null;
    }
}
