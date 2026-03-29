//package com.vpm.Accounts.Controller;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.vpm.Accounts.Entity.Product;
//import com.vpm.Accounts.Service.ProductService;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.*;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/products")
//@CrossOrigin(origins="*")
//public class ProductController {
//
//	@Autowired
//	private ProductService productService;
//	
//	
//	@PostMapping
//	public Product createProduct(@RequestBody Product product) {
//		return productService.saveProduct(product);
//	}
//	
//	@GetMapping
//	public List<Product> getAllProducts(){
//		return productService.getAllProducts();
//	}
//	
//	@GetMapping("/{id}")
//	public Product getProductById(@PathVariable Long id) {
//		return productService.getProductById(id);
//	}
//	
//	@PutMapping("/{id}")
//	public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
//		return productService.updateProduct(id, product);
//	}
//	
//	@DeleteMapping("/{id}")
//	public String deleteById(@PathVariable Long id) {
//		productService.deleteById(id);
//		return "Product deleted successfully";
//	}
//
//}
//
//
//


package com.vpm.Accounts.Controller;

import org.springframework.web.bind.annotation.*;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Service.ProductService;

import java.util.List;

import org.springframework.beans.factory.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductService service;

    @PostMapping
    public Product create(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    
    @GetMapping
    public List<Product> getAll() {
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return service.getProductById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        return service.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "Deleted successfully";
    }
}