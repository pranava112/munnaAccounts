package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.HsnSacCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HsnSacCodeRepository extends JpaRepository<HsnSacCode, Long> {
    List<HsnSacCode> findAllByActiveTrueOrderByCodeAsc();

    Optional<HsnSacCode> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);
}
