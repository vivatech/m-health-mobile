package com.service.mobile.model;

import com.service.mobile.dto.enums.RefundStatus;
import com.service.mobile.dto.enums.RejectBy;
import com.service.mobile.dto.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mh_consultation")
public class LabRefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "lab_order_id", nullable = false)
    private Integer labOrderId;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "payment_method", length = 10)
    private String paymentMethod;

    @Column(name = "refund_transaction_id", length = 50)
    private String refundTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "enum('Pending','Approve','Reject') default 'Pending'")
    private RefundStatus status = RefundStatus.Pending;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_by", nullable = false, columnDefinition = "enum('Patient','System') default 'Patient'")
    private RejectBy rejectBy = RejectBy.Patient;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;
}
