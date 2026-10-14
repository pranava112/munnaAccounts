// //package com.vpm.Accounts.Controller;
// //
// //import org.springframework.web.bind.annotation.RequestMapping;
// //
// //import com.vpm.Accounts.Entity.Product;
// //import com.vpm.Accounts.Service.ProductService;
// //
// //import java.util.List;
// //
// //import org.springframework.beans.factory.annotation.*;
// //import org.springframework.web.bind.annotation.*;
// //
// //@RestController
// //@RequestMapping("/api/products")
// //@CrossOrigin(origins="*")
// //public class ProductController {
// //
// //	@Autowired
// //	private ProductService productService;
// //	
// //	
// //	@PostMapping
// //	public Product createProduct(@RequestBody Product product) {
// //		return productService.saveProduct(product);
// //	}
// //	
// //	@GetMapping
// //	public List<Product> getAllProducts(){
// //		return productService.getAllProducts();
// //	}
// //	
// //	@GetMapping("/{id}")
// //	public Product getProductById(@PathVariable Long id) {
// //		return productService.getProductById(id);
// //	}
// //	
// //	@PutMapping("/{id}")
// //	public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
// //		return productService.updateProduct(id, product);
// //	}
// //	
// //	@DeleteMapping("/{id}")
// //	public String deleteById(@PathVariable Long id) {
// //		productService.deleteById(id);
// //		return "Product deleted successfully";
// //	}
// //
// //}
// //
// //
// //


// package com.vpm.Accounts.Controller;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.lang.NonNull;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Repository.ProductRepository;
// import com.vpm.Accounts.Service.ProductService;

// @RestController
// @RequestMapping("/api/products")
// @CrossOrigin("*")
// public class ProductController {

//     @Autowired
//     private ProductService service;
//     private ProductRepository productRepository;

//     @PostMapping
//     public CompletableFuture<ResponseEntity<Product>> create(@NonNull @RequestBody Product product) {
//         return service.saveProductAsync(product)
//                 .thenApply(saved -> ResponseEntity.ok(saved));
//     }

//     @GetMapping
//     public CompletableFuture<List<Product>> getAll() {
//         return service.getAllProductsAsync();
//     }

//     @GetMapping("/{id}")
//     public CompletableFuture<ResponseEntity<Product>> getById(@NonNull @PathVariable Long id) {
//         return service.getProductByIdAsync(id)
//                 .thenApply(product -> ResponseEntity.ok(product));
//     }

//     @PutMapping("/{id}")
//     public CompletableFuture<ResponseEntity<Product>> update(@NonNull @PathVariable Long id,@NonNull @RequestBody Product product) {
//         return service.updateProductAsync(id, product)
//                 .thenApply(updated -> ResponseEntity.ok(updated));
//     }

//     @DeleteMapping("/{id}")
//     public CompletableFuture<ResponseEntity<String>> delete(@NonNull @PathVariable Long id) {
//         return service.deleteByIdAsync(id)
//                 .thenApply(ignored -> ResponseEntity.ok("Deleted successfully"));
//     }

//     @GetMapping("/barcode/{barcode}")
// public Product getProductByBarcode(
//         @PathVariable String barcode) {

//     return productRepository
//             .findByBarcode(barcode)
//             .orElseThrow();
// }
// }


package com.vpm.Accounts.Controller.Accounts;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Service.ProductService;
import com.vpm.Accounts.security.TenantContext;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductService service;

    @PostMapping
    public CompletableFuture<ResponseEntity<Product>> create(
            @RequestBody Product product) {

        return service.saveProductAsync(TenantContext.require(), product)
                .thenApply(saved -> ResponseEntity.status(201).body(saved));
    }

    @GetMapping
    public CompletableFuture<List<Product>> getAll() {
        return service.getAllProductsAsync(TenantContext.require());
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Product>> getById(
            @PathVariable Long id) {

        return service.getProductByIdAsync(TenantContext.require(), id)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Product>> update(
            @PathVariable Long id,
            @RequestBody Product product) {

        return service.updateProductAsync(TenantContext.require(), id, product)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<String>> delete(
            @PathVariable Long id) {

        return service.deleteByIdAsync(TenantContext.require(), id)
                .thenApply(v ->
                        ResponseEntity.ok(
                                "Deleted Successfully"));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Product> getByBarcode(
            @PathVariable String barcode) {

        return ResponseEntity.ok(
                service.getProductByBarcode(barcode)
        );
    }

    @GetMapping(value = "/barcode/{barcode}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcodeImage(
            @PathVariable String barcode) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(service.getBarcodeImage(barcode));
    }
}