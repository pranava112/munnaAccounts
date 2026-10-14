package com.vpm.Accounts.Controller.GST;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vpm.Accounts.DTO.GST.GSTCalculationRequest;
import com.vpm.Accounts.DTO.GST.GSTCalculationResponse;
import com.vpm.Accounts.Entity.GST.GSTRegistration;
import com.vpm.Accounts.Service.GST.TaxApiService;

@RestController
@RequestMapping("/api/tax/gst")
@CrossOrigin(origins = "*")
public class GSTController {

    private final TaxApiService taxApiService;

    public GSTController(TaxApiService taxApiService) {
        this.taxApiService = taxApiService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<GSTCalculationResponse>
    calculate(
            @RequestBody GSTCalculationRequest request) {

        return ResponseEntity.ok(
                taxApiService.calculateGst(request));
    }

    @GetMapping("/registrations")
    public ResponseEntity<java.util.List<GSTRegistration>> registrations() {
        return ResponseEntity.ok(taxApiService.registrations());
    }

    @PostMapping("/registrations")
    public ResponseEntity<GSTRegistration> createRegistration(
            @RequestBody GSTRegistration registration) {
        return ResponseEntity.status(201).body(taxApiService.createRegistration(registration));
    }

    @PutMapping("/registrations/{id}")
    public ResponseEntity<GSTRegistration> updateRegistration(
            @PathVariable Long id,
            @RequestBody GSTRegistration registration) {
        return ResponseEntity.ok(taxApiService.updateRegistration(id, registration));
    }
}
