package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Service.JournalEntryLineService;

@RestController
@RequestMapping("/api/lines")
@CrossOrigin(origins = "*")
public class JournalEntryLineController {

    @Autowired
    private JournalEntryLineService service;
    
    @PostMapping
    public ResponseEntity<?> save(@RequestBody JournalEntryLine line) {
        try {
            return ResponseEntity.ok(service.saveJournalEntryLine(line));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public Iterable<JournalEntryLine> getAll() {
        return service.getAllJournalEntryLine();
    }

    @GetMapping("/{id}")
    public Optional<JournalEntryLine> getById(@PathVariable Long id) {
        return service.getJournalEntryLineById(id);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody JournalEntryLine line) {
        try {
            return ResponseEntity.ok(service.updateJournalEntryLine(id, line));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PutMapping("/{id}")
//    public JournalEntryLine update(@PathVariable Long id, @RequestBody JournalEntryLine line) {
//        return service.updateJournalEntryLine(id, line);
//    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteJournalEntryLine(id);
    }
    
    @GetMapping("/ledger/{accountId}")
    public List<JournalEntryLine> getLedger(@PathVariable Long accountId) {
        return service.getLedger(accountId);
    }
}
