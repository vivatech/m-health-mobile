package com.service.mobile.service;

import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.response.ActiveHealthTipsPackageResponse;
import com.service.mobile.model.HealthTipCategoryMaster;
import com.service.mobile.model.HealthTipOrders;
import com.service.mobile.model.HealthTipPackageCategories;
import com.service.mobile.repository.HealthTipOrdersRepository;
import com.service.mobile.repository.HealthTipPackageCategoriesRepository;
import com.service.mobile.model.HealthTipPackageUser;
import com.service.mobile.repository.HealthTipPackageUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class HealthTipPackageUserService {
    @Autowired
    HealthTipPackageUserRepository healthTipPackageUserRepository;

    @Autowired
    private HealthTipPackageCategoriesRepository healthTipPackageCategoriesRepository;

    @Autowired
    private HealthTipOrdersRepository healthTipOrdersRepository;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.categories.path}")
    private String categoryPath;

    @Value("${app.default.image}")
    private String defaultImage;


    public List<ActiveHealthTipsPackageResponse> getActiveHealthTipsPackage(Integer userId,String lang) {
        List<ActiveHealthTipsPackageResponse> responses = new ArrayList<>();
        List<HealthTipPackageUser> tipPackageUsers = healthTipPackageUserRepository.findByUserIdAndExpirey(userId, YesNo.No);
        List<Integer> healthTipsId = new ArrayList<>();
        for(HealthTipPackageUser t:tipPackageUsers){
            healthTipsId.add(t.getHealthTipPackage().getPackageId());
        }
        List<HealthTipPackageCategories> healthTipPackageCategories = healthTipPackageCategoriesRepository.findByPackageIds(healthTipsId);
        for(HealthTipPackageCategories pcat:healthTipPackageCategories){
            String categories= "";
            if(lang.equalsIgnoreCase("en")){
                categories = pcat.getHealthTipCategoryMaster().getName();
            }else{
                categories = pcat.getHealthTipCategoryMaster().getNameSl();
            }

            String image = getCategoryImageUrl(pcat.getHealthTipCategoryMaster());
            HealthTipOrders order = healthTipOrdersRepository.findByPatientIdAndHathTipPackageId(userId,pcat.getHealthTipPackage().getPackageId()).orElse(null);
            if(order!=null){
                String currency = (order.getCurrency()!=null && !order.getCurrency().isEmpty())?
                        order.getCurrency():currencySymbolFdj;
                Float ammount = (order.getCurrencyAmount()!=null)? order.getCurrencyAmount() : order.getAmount();
                HealthTipPackageUser t =tipPackageUsers.stream().filter(c->c.getHealthTipPackage().getPackageId()==pcat.getHealthTipPackage().getPackageId()).findFirst().orElse(null);

                ActiveHealthTipsPackageResponse temp = new ActiveHealthTipsPackageResponse();
                temp.setPackage_id(pcat.getHealthTipPackage().getPackageId());
                temp.setPackage_name(categories);
                temp.setImage(image);
                temp.setPackage_price(currency+" "+ammount);
                temp.setPackage_type(pcat.getHealthTipPackage().getType());
                temp.setExpired_at(formatExpiryDate(t.getExpiredAt()));
                responses.add(temp);
            }
        }
        return responses;
    }

    public static String formatExpiryDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.ENGLISH);
        return dateTime.format(formatter);
    }

    public String getCategoryImageUrl(HealthTipCategoryMaster category) {
        String photo = category.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            Path imagePath = Paths.get(categoryPath, String.valueOf(category.getCategoryId()), photo);
            if (Files.exists(imagePath)) {
                return baseUrl + "uploaded_file/category/" + category.getCategoryId() + "/" + photo;
            }
        }
        return baseUrl + defaultImage;
    }

    public List<Integer> getIdByUserIdAndExpiery(Integer userId, YesNo yesNo) {
        return healthTipPackageUserRepository.getIdByUserIdAndExpiery(userId,yesNo);
    }

    public HealthTipPackageUser getByIdAndExpiery(Integer userId, YesNo yesNo) {
        return healthTipPackageUserRepository.getByIdAndExpiry(userId,yesNo).orElse(null);
    }

    public HealthTipPackageUser save(HealthTipPackageUser packageUser) {
        return healthTipPackageUserRepository.save(packageUser);
    }

    public List<Integer> findPackageIdsByUserIdAndExpire(int userId, YesNo yesNo) {
        return healthTipPackageUserRepository.findPackageIdsByUserIdAndExpire(userId,yesNo);
    }

    public Optional<HealthTipPackageUser> findByUserIdAndPackageId(Integer userId, Integer categoryId) {
        return healthTipPackageUserRepository.findByUserIdAndPackageId(userId,categoryId);
    }
}
