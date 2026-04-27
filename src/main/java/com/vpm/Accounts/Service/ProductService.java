

package com.vpm.Accounts.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    public Product saveProduct(@NonNull Product p) {
        return repo.save(p);
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> saveProductAsync(@NonNull Product p) {
        return CompletableFuture.completedFuture(saveProduct(p));
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Product>> getAllProductsAsync() {
        return CompletableFuture.completedFuture(getAllProducts());
    }

    public Product getProductById(@NonNull Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> getProductByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(getProductById(id));
    }

    public Product updateProduct(@NonNull Long id, @NonNull Product p) {
        Product existing = getProductById(id);

        existing.setName(p.getName());
        existing.setCode(p.getCode());
        existing.setPurchasePrice(p.getPurchasePrice());
        existing.setSellingPrice(p.getSellingPrice());
        existing.setOpeningStock(p.getOpeningStock());

        return repo.save(existing);
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> updateProductAsync(@NonNull Long id, @NonNull Product p) {
        return CompletableFuture.completedFuture(updateProduct(id, p));
    }

    public void deleteById(@NonNull Long id) {
        repo.deleteById(id);
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteByIdAsync(@NonNull Long id) {
        deleteById(id);
        return CompletableFuture.completedFuture(null);
    }
}