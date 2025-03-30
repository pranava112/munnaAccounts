package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vpm.Accounts.Entity.Journal_Book;

@Repository
public interface Accounts_Repository extends JpaRepository<Journal_Book, Long> {
}
