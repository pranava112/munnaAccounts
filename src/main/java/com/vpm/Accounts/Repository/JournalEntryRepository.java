// package com.vpm.Accounts.Repository;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.vpm.Accounts.Entity.JournalEntry;

// // public interface JournalEntryRepository extends JpaRepository<JournalEntry,Long> {

// // }

// public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
// }



package com.vpm.Accounts.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.JournalEntry;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByVoucherNumber(
            String voucherNumber
    );

    @Modifying
    @Transactional

    void deleteByVoucherNumber(
            String voucherNumber
    );
}