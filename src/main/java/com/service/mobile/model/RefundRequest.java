package com.service.mobile.model;

import com.service.mobile.dto.enums.AddedType;
import com.service.mobile.dto.enums.ConsultationStatus;
import com.service.mobile.dto.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mh_refund_request")
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Orders orderId;

    @ManyToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    private WalletTransaction transactionId;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "payment_method", length = 10)
    private String paymentMethod;

    @Column(name = "refund_transaction_id", length = 50)
    private String refundTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsultationStatus status = ConsultationStatus.Pending;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_by", nullable = false)
    private AddedType rejectBy = AddedType.Doctor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
