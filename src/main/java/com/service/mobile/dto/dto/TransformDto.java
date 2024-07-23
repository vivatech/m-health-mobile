package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.NotificationType;
import com.service.mobile.dto.response.HospitalListResponse;
import com.service.mobile.model.Notification;
import com.service.mobile.model.Users;
import com.service.mobile.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
public class TransformDto {
    @Autowired
    private NotificationRepository notificationRepository;

    public static HospitalListResponse PUserToHospitalListResponse(Users user){
        return HospitalListResponse.builder()
                .hospital_id(user.getUserId())
                .hospital_address(user.getHospitalAddress())
                .picture(user.getProfilePicture())
                .clinic_name(user.getClinicName())
                .build();
    }
    public static <T> Page<T> paginate(List<T> response, int pageNo, int pageSize) {
        int startIndex = pageNo * pageSize;
        int endIndex = Math.min(startIndex + pageSize, response.size());
        if(startIndex > endIndex){
            startIndex = endIndex;
        }
        List<T> pageContent = response.subList(startIndex, endIndex);
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        return new PageImpl<>(pageContent, pageRequest, response.size());
    }
    public void saveNotification(int toId, String message, NotificationType type){
        Notification notification = new Notification();
        notification.setToId(toId);
        notification.setMessage(message);
        notification.setIsRead(String.valueOf(0));
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

}
