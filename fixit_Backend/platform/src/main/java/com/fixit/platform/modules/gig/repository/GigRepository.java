package com.fixit.platform.modules.gig.repository;

import com.fixit.platform.modules.gig.entity.Gig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GigRepository extends JpaRepository<Gig, UUID> {

    List<Gig> findByActiveTrue();
}
