package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.request.ListSupportTicketsRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.SupportTicketsDto;
import com.service.mobile.model.SupportTicket;
import com.service.mobile.model.Users;
import com.service.mobile.repository.SupportTicketRepository;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.default.image}")
    private String defaultImage;

    public TicketService(SupportTicketRepository supportTicketRepository,
                         UsersRepository usersRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.usersRepository = usersRepository;
    }

    public ResponseEntity<?> listSupportTickets(ListSupportTicketsRequest request, Locale locale) {
        List<SupportTicketsDto> response = new ArrayList<>();
        List<SupportTicket> supportTicketList = new ArrayList<>();
        Long total = 0L;
        Pageable pageable = PageRequest.of(request.getPage(), 10);
        request.setName((request.getName()==null)?"":request.getName());
        if(request.getId()!=null && request.getId()!=0){
            SupportTicket supportTicket = supportTicketRepository.findById(request.getId()).orElse(null);
            if(supportTicket!=null){
                supportTicketList.add(supportTicket);
            }
        }else{
            if(request.getStatus()!=null){
                Page<SupportTicket> ticketPage = supportTicketRepository.findByStatusAndNameAndUserId(request.getStatus(),request.getName(),request.getUser_id(),pageable);
                supportTicketList = ticketPage.getContent();
                total = ticketPage.getTotalElements();
            }else {
                Page<SupportTicket> ticketPage = supportTicketRepository.findByNameAndUserId(request.getName(),request.getUser_id(),pageable);
                supportTicketList = ticketPage.getContent();
                total = ticketPage.getTotalElements();
            }
        }
        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
        if(!supportTicketList.isEmpty()){
            for(SupportTicket ticket:supportTicketList){

                String photo = "";
                String attachmentType = "";
                try{
                    photo = baseUrl + "uploaded_file/Support_Ticket/"+ticket.getSupportTicketId()+"/"+ticket.getAttachmentId().getAttachmentName();
                }catch (Exception e){}
                try{
                    attachmentType = ticket.getAttachmentId().getAttachmentType();
                }catch (Exception e){}

                SupportTicketsDto dto = new SupportTicketsDto();
                dto.setId(ticket.getSupportTicketId());
                dto.setName(ticket.getSupportTicketTitle());
                dto.setDescription(ticket.getSupportTicketDescription());
                dto.setPhoto(photo);
                dto.setAttachment_type(attachmentType);
                dto.setStatus(ticket.getSupportTicketStatus());
                dto.setCreated_by(users.getFirstName()+" "+users.getLastName());
                dto.setCreated_date(ticket.getSupportTicketCreatedAt());
                dto.setTotal_count(total);

                response.add(dto);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUPPORT_TICKET_FOUND_SUCCESSFULLY,null,locale),
                    response
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale),
                    new ArrayList<>()
            ));
        }
    }
}
