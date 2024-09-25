package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.SupportTicketMessageDTO;
import com.service.mobile.dto.enums.SupportTicketStatus;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.SupportTicketReplyResponseDTO;
import com.service.mobile.dto.response.SupportTicketsDto;
import com.service.mobile.dto.response.ViewReplyMessageRequest;
import com.service.mobile.model.Attachment;
import com.service.mobile.model.SupportTicket;
import com.service.mobile.model.SupportTicketMessage;
import com.service.mobile.model.Users;
import com.service.mobile.repository.AttachmentRepository;
import com.service.mobile.repository.SupportTicketMessageRepository;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

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

    @Value("${app.file.max.size}")
    private Long MAX_FILE_SIZE;

    @Value("${storage.location}")
    private String path_to_uploads_dir;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private SupportTicketMessageRepository supportTicketMessageRepository;

    public TicketService(SupportTicketRepository supportTicketRepository,
                         UsersRepository usersRepository,
                         AttachmentRepository attachmentRepository,
                         SupportTicketMessageRepository supportTicketMessageRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.usersRepository = usersRepository;
        this.attachmentRepository = attachmentRepository;
        this.supportTicketMessageRepository = supportTicketMessageRepository;
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
            SupportTicketStatus status = null;
            try {
                status = Enum.valueOf(SupportTicketStatus.class, request.getStatus());
            } catch (Exception e) { }
            if(status!=null){
                Page<SupportTicket> ticketPage = supportTicketRepository.findByStatusAndNameAndUserId(status,request.getName(),request.getUser_id(),pageable);
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
                    if(ticket.getAttachmentId()!=null && ticket.getAttachmentId()!=0){
                        Attachment attachmentId = attachmentRepository.findById(ticket.getAttachmentId()).orElse(null);
                        if(attachmentId!=null){
                            photo = baseUrl + "uploaded_file/Support_Ticket/"+ticket.getSupportTicketId()+"/"+attachmentId.getAttachmentName();
                        }
                    }
                }catch (Exception e){}
                try{
                    if(ticket.getAttachmentId()!=null && ticket.getAttachmentId()!=0){
                        Attachment attachmentId = attachmentRepository.findById(ticket.getAttachmentId()).orElse(null);
                        if(attachmentId!=null){
                            attachmentType = attachmentId.getAttachmentType();
                        }
                    }
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

    public ResponseEntity<?> createSupportTicket(CreateSupportTicketsRequest request, Locale locale) throws IOException {

        String ext = "";
        if (request.getFilename() != null && !request.getFilename().isEmpty()) {
            String filename = request.getFilename().getOriginalFilename();
            ext = filename.substring(filename.lastIndexOf(".") + 1);
            List<String> ALLOWED_EXTENSIONS = List.of("gif", "png", "jpg", "jpeg", "doc", "pdf", "docx");
            if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.ATTACH_FILE_ALLOWED_ONLY,null,locale),
                        new ArrayList<>()
                ));
            }

            if (request.getFilename().getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.SUPPORT_TICKET_CREATED_SUCCESSFULLY,null,locale),
                        new ArrayList<>()
                ));
            }
        }

        SupportTicket supportTicket = new SupportTicket();
        supportTicket.setSupportTicketTitle(request.getSupport_ticket_title());
        supportTicket.setSupportTicketDescription(request.getSupport_ticket_title());
        supportTicket.setSupportTicketStatus(SupportTicketStatus.Open);
        supportTicket.setSupportTicketCreatedAt(LocalDateTime.now());
        supportTicket.setSupportTicketCreatedBy(request.getUser_id());
        Integer attachmentId = null;
        if (request.getFilename() != null && !request.getFilename().isEmpty()) {
            Attachment attachment = new Attachment();
            attachment.setAttachmentLabel(request.getFilename().getOriginalFilename());
            attachment.setAttachmentName(UUID.randomUUID().toString() + "." + ext);
            attachment.setAttachmentType(request.getAttachment_type());
            attachment.setAttachmentStatus(1);
            attachment = attachmentRepository.save(attachment);
            attachmentId = attachment.getAttachmentId();
            supportTicket.setAttachmentId(attachment.getAttachmentId());

            String uploadsDir = path_to_uploads_dir + supportTicket.getSupportTicketId();
            Files.createDirectories(Paths.get(uploadsDir));
            Files.copy(request.getFilename().getInputStream(), Paths.get(uploadsDir, attachment.getAttachmentName()));

            attachment.setAttachmentPath(uploadsDir + File.separator + attachment.getAttachmentName());
            attachmentRepository.save(attachment);
        }

        supportTicketRepository.save(supportTicket);

        SupportTicketMessage supportTicketMsg = new SupportTicketMessage();
        supportTicketMsg.setSupportTicket(supportTicket);
        supportTicketMsg.setSupportTicketMsgsDetail(request.getSupport_ticket_description());
        supportTicketMsg.setAttachmentId(attachmentId);
        supportTicketMsg.setSupportTicketMsgsCreatedBy(request.getUser_id());
        supportTicketMsg.setSupportTicketMsgsCreatedAt(LocalDateTime.now());
        supportTicketMessageRepository.save(supportTicketMsg);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUPPORT_TICKET_CREATED_SUCCESSFULLY,null,locale),
                new ArrayList<>()
        ));
    }

    public ResponseEntity<?> viewReplyMessage(ViewReplyMessageRequest request, Locale locale) {
        Pageable pageable = PageRequest.of(request.getPage(),10);
        Page<SupportTicketMessage> messages = supportTicketMessageRepository.findByTicketId(request.getSupport_ticket_id(),pageable);
        List<SupportTicketMessage> supportTicketMessages = messages.getContent();
        List<SupportTicketMessageDTO> response = new ArrayList<>();
        if(!supportTicketMessages.isEmpty()){
            for(SupportTicketMessage data:supportTicketMessages){
                SupportTicketMessageDTO dto = new SupportTicketMessageDTO();

                SupportTicket ticket = data.getSupportTicket();
                Users user = null;
                if(data.getSupportTicketMsgsCreatedBy()!=null && data.getSupportTicketMsgsCreatedBy()!=0){
                    user = usersRepository.findById(data.getSupportTicketMsgsCreatedBy()).orElse(null);
                }
                Attachment attachment = null;
                if(data.getAttachmentId()!=null && data.getAttachmentId()!=0){
                    attachment = attachmentRepository.findById(data.getAttachmentId()).orElse(null);
                }

                String photoPath = attachment != null ? baseUrl + "uploaded_file/Support_Ticket/" + data.getSupportTicket().getSupportTicketId() + "/" + attachment.getAttachmentName() : "";
                String attachmentType = attachment != null ? attachment.getAttachmentType() : "";

                dto.setId(data.getSupportTicketMsgsId());
                dto.setMessage((data.getSupportTicketMsgsDetail()!=null)?data.getSupportTicketMsgsDetail().replaceAll("\\s+", " ").trim():null);
                dto.setTicket_name((ticket!=null)?ticket.getSupportTicketTitle():null);
                dto.setStatus((ticket!=null)?ticket.getSupportTicketStatus():null);
                dto.setAttachment(photoPath);
                dto.setAttachment_type(attachmentType);
                dto.setCreated_by((user!=null)?user.getFirstName() + " " + user.getLastName():null);
                dto.setCreated_by_id(data.getSupportTicketMsgsCreatedBy());
                dto.setCreated_date(data.getSupportTicketMsgsCreatedAt());
                dto.setTotal_count(messages.getTotalElements());
                response.add(dto);
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.REPLIED_MSG_GET_SUCCESS,null,locale),
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

    public ResponseEntity<?> replySupportTicket(ReplySupportTicketRequest request, Locale locale) throws IOException {
        SupportTicket supportTicket = supportTicketRepository.findById(request.getSupport_ticket_id()).orElse(null);
        if (supportTicket!=null) {
            Attachment attachment = null;

            if (request.getFilename() != null && !request.getFilename().isEmpty()) {
                String filename = request.getFilename().getOriginalFilename();
                String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                if (!Arrays.asList("gif", "png", "jpg", "jpeg", "doc", "pdf", "docx").contains(ext)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Attached files allowed only");
                }
                if (request.getFilename().getSize() > 10485760) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot upload file larger than 10 MB");
                }

                attachment = new Attachment();
                attachment.setAttachmentLabel(filename);
                attachment.setAttachmentName(UUID.randomUUID().toString() + "." + ext);
                attachment.setAttachmentType(request.getAttachment_type() != null ? request.getAttachment_type() : request.getFilename().getContentType());
                attachmentRepository.save(attachment);

                // Save the file to disk
                Path path = Paths.get("path_to_uploads_dir" + request.getSupport_ticket_id() + "/" + attachment.getAttachmentName());
                Files.createDirectories(path.getParent());
                Files.write(path, request.getFilename().getBytes());
            }

            if (request.getMessage() == null || request.getMessage().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Reply message cannot be blank");
            }

            SupportTicketMessage reply = new SupportTicketMessage();
            reply.setSupportTicket(supportTicket);
            reply.setSupportTicketMsgsDetail(request.getMessage());
            reply.setAttachmentId(attachment != null ? attachment.getAttachmentId() : null);
            reply.setSupportTicketMsgsCreatedBy(request.getUser_id());
            reply.setSupportTicketMsgsCreatedAt(LocalDateTime.now());
            reply = supportTicketMessageRepository.save(reply);

            SupportTicketMessage savedReply = reply;
            Optional<Users> userOpt = usersRepository.findById(savedReply.getSupportTicketMsgsCreatedBy());
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User not found");
            }

            Users user = userOpt.get();
            String photoPath = attachment != null ? "BASE_URL" + "uploaded_file/Support_Ticket/" + request.getSupport_ticket_id() + "/" + attachment.getAttachmentName() : "";

            SupportTicketReplyResponseDTO responseDTO = new SupportTicketReplyResponseDTO();
            responseDTO.setId(savedReply.getSupportTicketMsgsId());
            responseDTO.setMessage(savedReply.getSupportTicketMsgsDetail());
            responseDTO.setTicketName(supportTicket.getSupportTicketTitle());
            responseDTO.setStatus(supportTicket.getSupportTicketStatus());
            responseDTO.setAttachment(photoPath);
            responseDTO.setCreatedBy(user.getFirstName() + " " + user.getLastName());
            responseDTO.setCreatedById(savedReply.getSupportTicketMsgsCreatedBy());
            responseDTO.setCreatedDate(savedReply.getSupportTicketMsgsCreatedAt());

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUPPORT_TICKET_REPLYED_SUCCESS,null,locale),
                    responseDTO
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SUPPORT_TICKET_NOT_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> changeSupportTicketStatus(ChangeSupportTicketStatusRequest request, Locale locale) {
        SupportTicket ticket = supportTicketRepository.findById(request.getSupport_ticket_id()).orElse(null);
        if(ticket!=null){
            ticket.setSupportTicketStatus(request.getStatus());
            ticket.setSupportTicketUpdatedAt(LocalDateTime.now());
            supportTicketRepository.save(ticket);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUPPORT_TICKET_UPDATED_SUCCESS,null,locale)
            ));
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SUPPORT_TICKET_NOT_FOUND,null,locale)
            ));
        }
    }
}
