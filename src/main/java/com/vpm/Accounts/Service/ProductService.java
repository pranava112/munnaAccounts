

package com.vpm.Accounts.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    public Product saveProduct(Product p) {
        return repo.save(p);
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product updateProduct(Long id, Product p) {
        Product existing = getProductById(id);

        existing.setName(p.getName());
        existing.setCode(p.getCode());
        existing.setPurchasePrice(p.getPurchasePrice());
        existing.setSellingPrice(p.getSellingPrice());
        existing.setOpeningStock(p.getOpeningStock());

        return repo.save(existing);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}