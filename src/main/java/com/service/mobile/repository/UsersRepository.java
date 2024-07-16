package com.service.mobile.repository;

import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users,Integer> {

    @Query("Select u from Users u where u.contactNumber like ?1")
    Optional<Users> findByContactNumber(String contactNumber);
}
