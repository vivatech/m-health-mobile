package com.service.mobile.repository;

import com.service.mobile.dto.enums.UserType;
import com.service.mobile.model.City;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users,Integer> {

    @Query("Select u from Users u where u.userId = ?1")
    Optional<Users> findById(Integer id);

    @Query("Select u from Users u where u.contactNumber like ?1")
    Optional<Users> findByContactNumber(String contactNumber);

    @Query("Select c from City c where c.id in (Select u.city from Users u where u.type = ?1 group by u.city)")
    List<City> getCitiesByUsertype(UserType userType);

    @Query("Select u from Users u where u.status like ?1 and u.type = ?2 and u.isVerified like ?3")
    List<Users> findByStatusAndTypeAndVerified(String a, UserType userType, String yes);

    @Query("Select u from Users u where u.type = ?1")
    List<Users> findByType(UserType userType);

    List<Users> findByHospitalId(int hospitalId);

    @Query("Select u from Users u where u.status LIKE ?1 and u.type = ?2 order by u.sort asc")
    List<Users> findByStatusAndTypeOrderByAsc(String status,UserType type);

    @Query("SELECT u FROM Users u WHERE u.type = 'Doctor' AND u.status = 'A' AND u.hasDoctorVideo IN ('visit', 'both') AND u.hospitalId > 0")
    List<Users> findActiveDoctorsWithVideoAndHospital();
    
    @Query("SELECT o.doctorId.userId FROM Orders o GROUP BY o.doctorId.userId ORDER BY COUNT(o.doctorId.userId) DESC")
    List<Integer> findTopDoctors();

    @Query("SELECT u FROM Users u WHERE u.userId IN (?1)")
    List<Users> findDoctorsByIds(List<Integer> topDoctorIds);

    @Query("SELECT u FROM Users u WHERE u.type = 'Doctor' AND u.status = 'A' AND u.hospitalId IN ?1 AND u.hasDoctorVideo IN ('visit', 'both') AND u.hospitalId > 0")
    List<Users> findNearbyDoctors(List<Integer> hospitalIds);

//    @Query(value = "SELECT u.user_id " +
//            "FROM mh_users u " +
//            "LEFT JOIN mh_consultation_rating r ON r.doctor_id = u.user_id " +
//            "GROUP BY u.user_id " +
//            "ORDER BY SUM(CASE WHEN r.doctor_id = u.user_id THEN r.rating ELSE 0 END) DESC, u.user_id ASC",
//            nativeQuery = true)
//    List<Long> findSortedUserIds();
}
