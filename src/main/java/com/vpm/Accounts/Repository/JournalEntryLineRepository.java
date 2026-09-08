package com.vpm.Accounts.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vpm.Accounts.Entity.JournalEntryLine;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> 
{
	@Query("SELECT l FROM JournalEntryLine l WHERE l.account.id = :accountId")
	List<JournalEntryLine> findByAccountId(@Param("accountId") Long accountId);

	boolean existsByAccount_Id(Long accountId);

}


