package com.abhijit.gymbilling.repository;

import com.abhijit.gymbilling.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 🔍 Search
    List<Customer> findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
            String name, String phoneNumber
    );

    // 🔥 Simple upcoming (no status filter here)
    List<Customer> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
}
