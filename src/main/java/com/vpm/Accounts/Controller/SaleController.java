package com.vpm.Accounts.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Service.SaleService;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin("*")
public class SaleController {
	
	@Autowired
	private SaleService service;
	
	@PostMapping
	public Sale create(@RequestBody Sale sale) {
		return service.saveSale(sale);
	}
	
	@GetMapping
	public List<Sale> getAll(){
		return service.getAll();
	}
	
	@GetMapping("/{id}")
	public Sale getSaleById(@PathVariable Long id) {
		return service.getSaleById(id);
	}
	
	@PutMapping("/{id}")
	public Sale updateSale(@PathVariable Long id) {
		return service.getSaleById(id);
	}
	
	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		service.delete(id);
		return "Deleted Successfully";
	}

}





