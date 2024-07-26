package com.service.mobile.model;

import com.service.mobile.dto.enums.SupportTicketStatus;
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
@Table(name = "mh_support_ticket")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "support_ticket_id")
    private Integer supportTicketId;

    @Column(name = "support_ticket_title", nullable = false)
    private String supportTicketTitle;

    @Column(name = "support_ticket_description", nullable = false)
    private String supportTicketDescription;

    @ManyToOne
    @JoinColumn(name = "attachment_id", referencedColumnName = "attachment_id", nullable = false)
    private Attachment attachmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_ticket_status", nullable = false)
    private SupportTicketStatus supportTicketStatus = SupportTicketStatus.Open;

    @Column(name = "support_ticket_created_by", nullable = false)
    private Integer supportTicketCreatedBy;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "support_ticket_created_at", nullable = false)
    private LocalDateTime supportTicketCreatedAt;

    @Column(name = "support_ticket_updated_at")
    private LocalDateTime supportTicketUpdatedAt;

}

