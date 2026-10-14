// package com.vpm.Accounts.Service;

// import java.util.List;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.vpm.Accounts.Entity.JournalEntryLine;
// import com.vpm.Accounts.Repository.JournalEntryLineRepository;

// @Service 
// public class BookService {

    
//     private final JournalEntryLineRepository lineRepository;

//     public BookService(JournalEntryLineRepository lineRepository){
//         this.lineRepository = lineRepository;
//     }

//     @Transactional(readOnly = true)
//     public List<JournalEntryLine> getCashBook(Long accountId){
//         return lineRepository.findByAccountId(accountId);
//     }


// }




package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.DTO.BookReportDTO;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.JournalEntryLineRepository;

@Service 
public class BookService{

    private final JournalEntryLineRepository lineRepository;

    public BookService(JournalEntryLineRepository lineRepository){
        this.lineRepository = lineRepository;
    }

   


     @Transactional(readOnly = true)
     public List<BookReportDTO> getCashBook(
            Long accountId,
            LocalDate from,
            LocalDate to) {

        validateDates(from, to);

         return getBookThroughDate(accountId, to);
     }

   @Transactional(readOnly = true)
     public List<BookReportDTO> getBankBook(
             Long accountId,
             LocalDate from,
             LocalDate to) {

         validateDates(from, to);

         return getBookThroughDate(accountId, to);
     }

     private List<BookReportDTO> getBookThroughDate(
             Long accountId,
             LocalDate to) {

         List<BookReportDTO> rows = new ArrayList<>();
         BigDecimal balance = BigDecimal.ZERO;

         for (JournalEntryLine line :
                 lineRepository.findBookByAccountIdThroughDate(accountId, to)) {
             BigDecimal debit = line.getDebit() == null
                     ? BigDecimal.ZERO
                     : line.getDebit();
             BigDecimal credit = line.getCredit() == null
                     ? BigDecimal.ZERO
                     : line.getCredit();
             balance = balance.add(debit).subtract(credit);

             BookReportDTO row = new BookReportDTO();
             row.setDate(line.getJournalEntry().getEntryDate());
             row.setJournalEntryId(line.getJournalEntry().getId());
             row.setVoucherNumber(line.getJournalEntry().getVoucherNumber());
             row.setParticulars(line.getJournalEntry().getDescription());
             row.setDebit(debit);
             row.setCredit(credit);
             row.setBalance(balance);
             rows.add(row);
         }

         return rows;
     }

    private void validateDates(LocalDate from, LocalDate to){

        if(from == null){
            throw new IllegalArgumentException("From date is required");
        }

         if(to == null){
            throw new IllegalArgumentException("to date is required");
        }

        if(from.isAfter(to)){
            throw new IllegalArgumentException("From date cannot be after To date");
        }
    }


}