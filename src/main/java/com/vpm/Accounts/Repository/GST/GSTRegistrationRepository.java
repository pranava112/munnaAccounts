package com.vpm.Accounts.Repository.GST;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.GST.GSTRegistration;

public interface GSTRegistrationRepository  extends JpaRepository<GSTRegistration, Long> {

    Optional<GSTRegistration> findByGstin(String gstin);
    Optional<GSTRegistration> findByGstinAndActiveTrue(String gstin);

    List<GSTRegistration> findAllByActiveTrue();

    boolean existsByGstin(String gstin);
}