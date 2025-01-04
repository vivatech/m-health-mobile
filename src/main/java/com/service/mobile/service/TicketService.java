package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.customException.MobileServiceExceptionHandler;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
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
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${app.ZoneId}")
    private String zone;

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
        if(request.getUser_id() == null || request.getPage() == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    Constants.BLANK_DATA_GIVEN,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
        StringBuilder sb = new StringBuilder("SELECT u FROM SupportTicket u WHERE u.supportTicketCreatedBy = "+request.getUser_id());
        if(request.getStatus() != null && !request.getStatus().isEmpty()){
            sb.append(" AND u.supportTicketStatus = :").append(SupportTicketStatus.valueOf(request.getStatus()));
        }
        if(request.getName() != null && !request.getName().isEmpty()){
            sb.append(" AND u.supportTicketTitle =").append(request.getName());
        }if(request.getId() != null){
            sb.append(" AND u.supportTicketId =").append(request.getId());
        }
        sb.append(" Order By u.supportTicketId DESC");
        Query query = entityManager.createQuery(sb.toString(), SupportTicket.class);
        List<SupportTicket> supportTicketList = query.getResultList();
        Long total = (long) supportTicketList.size();

        int pageNo = request.getPage();
        int pageSize = 10;

        query.setFirstResult(pageNo * pageSize);
        query.setMaxResults(pageSize);

        supportTicketList = query.getResultList();

        List<SupportTicketsDto> response = new ArrayList<>();
        if(!supportTicketList.isEmpty()){
            for(SupportTicket ticket:supportTicketList){

                String photo = "";
                String attachmentType = "";
                try{
                    if(ticket.getAttachmentId()!=null && ticket.getAttachmentId()!=0){
                        Attachment attachmentId = attachmentRepository.findById(ticket.getAttachmentId()).orElse(null);
                        if(attachmentId!=null){
                            attachmentType = attachmentId.getAttachmentType();
                            photo = baseUrl + "uploaded_file/Support_Ticket/"+ticket.getSupportTicketId()+"/"+attachmentId.getAttachmentName();
                        }
                    }
                }catch (Exception e){
                    log.error("Error while fetching attachment in ticket list : {}",e);
                }

                String createdDate = "";
                if (ticket.getSupportTicketCreatedAt()!=null) {
                    try{
                        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = Date.from(ticket.getSupportTicketCreatedAt().atZone(ZoneId.of(zone)).toInstant());
                        createdDate = formatter.format(date);
                    }catch (Exception e){
                        log.error("Error while fetching ticket : {}", e);
                    }
                }

                Users patient = usersRepository.findById(request.getUser_id()).orElse(null);
                SupportTicketsDto dto = new SupportTicketsDto();
                dto.setId(ticket.getSupportTicketId());
                dto.setName(ticket.getSupportTicketTitle());
                dto.setDescription(ticket.getSupportTicketDescription());
                dto.setPhoto(photo);
                dto.setAttachment_type(attachmentType);
                dto.setStatus(ticket.getSupportTicketStatus());
                dto.setCreated_by(patient == null ? "" : patient.getFirstName()+" "+patient.getLastName());
                dto.setCreated_date(createdDate);
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
        log.info("Entering into create support ticket api : {}", request);
        Map<String, Object> res = new HashMap<>();
        try {

            String ext = "";
            if (request.getFilename() != null && !request.getFilename().isEmpty()) {
                String filename = request.getFilename().getOriginalFilename();
                assert filename != null;
                ext = filename.substring(filename.lastIndexOf(".") + 1);
                List<String> ALLOWED_EXTENSIONS = List.of("gif", "png", "jpg", "jpeg", "doc", "pdf", "docx");
                if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.ATTACH_FILE_ALLOWED_ONLY, null, locale)
                    ));
                }

                if (request.getFilename().getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.MAXIMIM_PROFILE_PIC_SIZE_EXCIDED, null, locale)
                    ));
                }
            }

            SupportTicket supportTicket = new SupportTicket();
            supportTicket.setSupportTicketTitle(request.getSupport_ticket_title());
            supportTicket.setSupportTicketDescription(request.getSupport_ticket_title());
            supportTicket.setSupportTicketStatus(SupportTicketStatus.Open);
            supportTicket.setSupportTicketCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
            supportTicket.setSupportTicketCreatedBy(Integer.parseInt(request.getUser_id()));
            supportTicket = supportTicketRepository.save(supportTicket);
            Integer attachmentId = null;
            if (request.getFilename() != null && !request.getFilename().isEmpty()) {
                Attachment attachment = new Attachment();
                attachment.setAttachmentLabel(request.getFilename().getOriginalFilename());
                attachment.setAttachmentName(UUID.randomUUID().toString() + "." + ext);
                attachment.setAttachmentType(request.getAttachment_type() == null || request.getAttachment_type().isEmpty()
                        ? request.getFilename().getContentType() : request.getAttachment_type());
                attachment.setAttachmentStatus(1);
                attachment = attachmentRepository.save(attachment);
                attachmentId = attachment.getAttachmentId();
                supportTicket.setAttachmentId(attachment.getAttachmentId());

                String uploadsDir = path_to_uploads_dir + "/Support_Ticket/" + supportTicket.getSupportTicketId();
                Files.createDirectories(Paths.get(uploadsDir));
                Files.copy(request.getFilename().getInputStream(), Paths.get(uploadsDir, attachment.getAttachmentName()));

                attachment.setAttachmentPath(uploadsDir + File.separator + attachment.getAttachmentName());
                attachmentRepository.save(attachment);
            }

            supportTicket = supportTicketRepository.save(supportTicket);

            SupportTicketMessage supportTicketMsg = new SupportTicketMessage();
            supportTicketMsg.setSupportTicket(supportTicket);
            supportTicketMsg.setSupportTicketMsgsDetail(request.getSupport_ticket_description());
            supportTicketMsg.setAttachmentId(attachmentId);
            supportTicketMsg.setSupportTicketMsgsCreatedBy(Integer.parseInt(request.getUser_id()));
            supportTicketMsg.setSupportTicketMsgsCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
            supportTicketMessageRepository.save(supportTicketMsg);

            Map<String, Object> response = new HashMap<>();
            response.put("status", Constants.SUCCESS_CODE);
            response.put("message", messageSource.getMessage(Constants.SUPPORT_TICKET_CREATED_SUCCESSFULLY, null, locale));
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in create ticket api : {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
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

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

                dto.setId(data.getSupportTicketMsgsId());
                dto.setMessage((data.getSupportTicketMsgsDetail()!=null)?data.getSupportTicketMsgsDetail().replaceAll("\\s+", " ").trim():null);
                dto.setTicket_name((ticket!=null)?ticket.getSupportTicketTitle():null);
                dto.setStatus((ticket!=null)?ticket.getSupportTicketStatus():null);
                dto.setAttachment(photoPath);
                dto.setAttachment_type(attachmentType);
                dto.setCreated_by((user!=null)?user.getFirstName() + " " + user.getLastName():null);
                dto.setCreated_by_id(data.getSupportTicketMsgsCreatedBy());
                dto.setCreated_date(formatter.format(data.getSupportTicketMsgsCreatedAt()));
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
        log.info("Entering into reply ticket request api : {}", request);
        Map<String, Object> res = new HashMap<>();
        try{
            SupportTicket supportTicket = supportTicketRepository.findById(Integer.parseInt(request.getSupport_ticket_id())).orElseThrow(()-> new MobileServiceExceptionHandler(messageSource.getMessage(Constants.SUPPORT_TICKET_NOT_FOUND, null, locale)));

            //check file condition
            Attachment attachment = null;
            if (request.getFilename() != null && !request.getFilename().isEmpty()) {
                String filename = request.getFilename().getOriginalFilename();
                assert filename != null;
                String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                if (!Arrays.asList("gif", "png", "jpg", "jpeg", "doc", "pdf", "docx").contains(ext)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.ATTACH_FILE_ALLOWED_ONLY, null, locale)
                    ));
                }
                if (request.getFilename().getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.CANNOT_UPLOAD_TEN_MB, null, locale)
                    ));
                }

                attachment = new Attachment();
                attachment.setAttachmentLabel(filename);
                attachment.setAttachmentName(UUID.randomUUID() + "." + ext);
                attachment.setAttachmentType(!StringUtils.isEmpty(request.getAttachment_type())
                        ? request.getAttachment_type() : request.getFilename().getContentType());
                attachmentRepository.save(attachment);

                // Save the file to disk
                Path path = Paths.get(path_to_uploads_dir + request.getSupport_ticket_id() + "/" + attachment.getAttachmentName());
                Files.createDirectories(path.getParent());
                Files.write(path, request.getFilename().getBytes());
            }

            if (StringUtils.isEmpty(request.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Reply message cannot be blank");
            }

            SupportTicketMessage reply = new SupportTicketMessage();
            reply.setSupportTicket(supportTicket);
            reply.setSupportTicketMsgsDetail(request.getMessage());
            reply.setAttachmentId(attachment != null ? attachment.getAttachmentId() : null);
            reply.setSupportTicketMsgsCreatedBy(Integer.parseInt(request.getUser_id()));
            reply.setSupportTicketMsgsCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
            reply = supportTicketMessageRepository.save(reply);

            SupportTicketMessage savedReply = reply;
            Optional<Users> userOpt = usersRepository.findById(savedReply.getSupportTicketMsgsCreatedBy());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.USER_NOT_FOUND, null, locale)
                ));
            }

            Users user = userOpt.get();
            String photoPath = attachment != null ? baseUrl + "uploaded_file/Support_Ticket/" + request.getSupport_ticket_id() + "/" + attachment.getAttachmentName() : "";

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
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error found in reply support ticket api : {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG,null,locale)
            ));
        }
    }

    public ResponseEntity<Response> changeSupportTicketStatus(ChangeSupportTicketStatusRequest request, Locale locale) {
        log.info("Entering into change support ticket status api : {}", request);
        if(StringUtils.isEmpty(request.getSupport_ticket_id()) || StringUtils.isEmpty(request.getStatus())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
        SupportTicket ticket = supportTicketRepository.findById(Integer.valueOf(request.getSupport_ticket_id())).orElseThrow(()-> new MobileServiceExceptionHandler(messageSource.getMessage(Constants.SUPPORT_TICKET_NOT_FOUND, null, locale)));
        ticket.setSupportTicketStatus(SupportTicketStatus.valueOf(request.getStatus()));
        ticket.setSupportTicketUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
        supportTicketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUPPORT_TICKET_UPDATED_SUCCESS,null,locale)
        ));
    }
}
