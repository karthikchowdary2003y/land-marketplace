package com.landmarket.repository;

import com.landmarket.model.Inquiry;
import com.landmarket.model.Land;
import com.landmarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByLand(Land land);

    List<Inquiry> findByBuyer(User buyer);

    List<Inquiry> findByLandOwner(User owner);

    boolean existsByLandAndBuyer(Land land, User buyer);
}
