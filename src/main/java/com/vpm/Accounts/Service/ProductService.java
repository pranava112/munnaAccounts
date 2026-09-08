

// // package com.vpm.Accounts.Service;

// // import java.nio.file.FileSystems;
// // import java.util.List;
// // import java.util.concurrent.CompletableFuture;

// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.lang.NonNull;
// // import org.springframework.scheduling.annotation.Async;
// // import org.springframework.stereotype.Service;

// // import com.vpm.Accounts.Entity.Product;
// // import com.vpm.Accounts.Repository.ProductRepository;

// // @Service
// // public class ProductService {

// //     @Autowired
// //     private ProductRepository repo;

// //     public Product saveProduct(@NonNull Product p) {
// //         return repo.save(p);
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Product> saveProductAsync(@NonNull Product p) {
// //         return CompletableFuture.completedFuture(saveProduct(p));
// //     }

// //     public List<Product> getAllProducts() {
// //         return repo.findAll();
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<List<Product>> getAllProductsAsync() {
// //         return CompletableFuture.completedFuture(getAllProducts());
// //     }

// //     public Product getProductById(@NonNull Long id) {
// //         return repo.findById(id)
// //                 .orElseThrow(() -> new RuntimeException("Product not found"));
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Product> getProductByIdAsync(@NonNull Long id) {
// //         return CompletableFuture.completedFuture(getProductById(id));
// //     }

// //     public Product updateProduct(@NonNull Long id, @NonNull Product p) {
// //         Product existing = getProductById(id);

// //         existing.setName(p.getName());
// //         existing.setCode(p.getCode());
// //         existing.setPurchasePrice(p.getPurchasePrice());
// //         existing.setSellingPrice(p.getSellingPrice());
// //         existing.setOpeningStock(p.getOpeningStock());

// //         return repo.save(existing);
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Product> updateProductAsync(@NonNull Long id, @NonNull Product p) {
// //         return CompletableFuture.completedFuture(updateProduct(id, p));
// //     }

// //     public void deleteById(@NonNull Long id) {
// //         repo.deleteById(id);
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Void> deleteByIdAsync(@NonNull Long id) {
// //         deleteById(id);
// //         return CompletableFuture.completedFuture(null);
// //     }


// //     BitMatrix matrix = new MultiFormatWriter().encode(
// //     barcode,
// //     BarcodeFormat.CODE_128,
// //     300,
// //     100
// // );

// // Path path = FileSystems.getDefault().getPath(
// //     "uploads/barcodes/" + barcode + ".png"
// // );

// // MatrixToImageWriter.writeToPath(
// //     matrix,
// //     "PNG",
// //     path
// // );
// // }


// package com.vpm.Accounts.Service;

// import java.io.IOException;
// import java.nio.file.FileSystems;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.lang.NonNull;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.google.zxing.BarcodeFormat;
// import com.google.zxing.MultiFormatWriter;
// import com.google.zxing.common.BitMatrix;
// import com.google.zxing.client.j2se.MatrixToImageWriter;

// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Repository.ProductRepository;
// import java.util.Random;

// @Service
// public class ProductService {

//     @Autowired
//     private ProductRepository repo;

//     // public Product saveProduct(@NonNull Product p) {

//     //     Product saved = repo.save(p);

//     //     if(saved.getBarcode() != null &&
//     //        !saved.getBarcode().isEmpty()) {

//     //         generateBarcode(saved.getBarcode());
//     //     }

//     //     return saved;
//     // }

//     public Product saveProduct(@NonNull Product p) {

//     if (p.getBarcode() == null ||
//         p.getBarcode().isBlank()) {

//         p.setBarcode(generateBarcodeNumber());
//     }

//     Product saved = repo.save(p);

//     generateBarcode(saved.getBarcode());

//     return saved;
// }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> saveProductAsync(@NonNull Product p) {
//         return CompletableFuture.completedFuture(saveProduct(p));
//     }

//     public List<Product> getAllProducts() {
//         return repo.findAll();
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<List<Product>> getAllProductsAsync() {
//         return CompletableFuture.completedFuture(getAllProducts());
//     }

//     public Product getProductById(@NonNull Long id) {
//         return repo.findById(id)
//                 .orElseThrow(() ->
//                     new RuntimeException("Product not found"));
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> getProductByIdAsync(@NonNull Long id) {
//         return CompletableFuture.completedFuture(
//                 getProductById(id));
//     }

//     public Product getProductByBarcode(String barcode) {
//         return repo.findByBarcode(barcode)
//                 .orElseThrow(() ->
//                     new RuntimeException("Product not found"));
//     }

//     public Product updateProduct(
//             @NonNull Long id,
//             @NonNull Product p) {

//         Product existing = getProductById(id);

//         existing.setName(p.getName());
//         existing.setCode(p.getCode());
//         existing.setPurchasePrice(p.getPurchasePrice());
//         existing.setSellingPrice(p.getSellingPrice());
//         existing.setOpeningStock(p.getOpeningStock());
//         existing.setBarcode(p.getBarcode());

//         Product updated = repo.save(existing);

//         if(updated.getBarcode() != null &&
//            !updated.getBarcode().isEmpty()) {

//             generateBarcode(updated.getBarcode());
//         }

//         return updated;
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> updateProductAsync(
//             @NonNull Long id,
//             @NonNull Product p) {

//         return CompletableFuture.completedFuture(
//                 updateProduct(id, p));
//     }

//     public void deleteById(@NonNull Long id) {
//         repo.deleteById(id);
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Void> deleteByIdAsync(
//             @NonNull Long id) {

//         deleteById(id);
//         return CompletableFuture.completedFuture(null);
//     }

//     private void generateBarcode(String barcode) {

//         try {

//             Path folder = FileSystems
//                     .getDefault()
//                     .getPath("uploads/barcodes");

//             if (!Files.exists(folder)) {
//                 Files.createDirectories(folder);
//             }

//             BitMatrix matrix =
//                     new MultiFormatWriter().encode(
//                             barcode,
//                             BarcodeFormat.CODE_128,
//                             300,
//                             100
//                     );

//             Path path = folder.resolve(
//                     barcode + ".png");

//             MatrixToImageWriter.writeToPath(
//                     matrix,
//                     "PNG",
//                     path
//             );

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

    

// private String generateBarcodeNumber() {

//     Random random = new Random();

//     String barcode;

//     do {
//         barcode = String.format("%06d",
//                 random.nextInt(1000000));
//     } while (repo.findByBarcode(barcode).isPresent());

//     return barcode;
// }
// }


// package com.vpm.Accounts.Service;

// import java.nio.file.FileSystems;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;
// import java.util.Random;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.lang.NonNull;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.google.zxing.BarcodeFormat;
// import com.google.zxing.MultiFormatWriter;
// import com.google.zxing.client.j2se.MatrixToImageWriter;
// import com.google.zxing.common.BitMatrix;

// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Repository.ProductRepository;

// @Service
// public class ProductService {

//     @Autowired
//     private ProductRepository repo;

//     // public Product saveProduct(@NonNull Product p) {

//     //     if (p.getBarcode() == null || p.getBarcode().isBlank()) {
//     //         p.setBarcode(generateBarcodeNumber());
//     //     }

//     //     Product saved = repo.save(p);

//     //     generateBarcode(saved.getBarcode());

//     //     return saved;
//     // }

//     public Product saveProduct(Product p) {

//     Product saved = repo.save(p);

//     String barcode =
//             String.format("%06d",
//                     saved.getId());

//     saved.setBarcode(barcode);

//     saved = repo.save(saved);

//     generateBarcode(barcode);

//     return saved;
// }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> saveProductAsync(@NonNull Product p) {
//         return CompletableFuture.completedFuture(saveProduct(p));
//     }

//     public List<Product> getAllProducts() {
//         return repo.findAll();
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<List<Product>> getAllProductsAsync() {
//         return CompletableFuture.completedFuture(getAllProducts());
//     }

//     public Product getProductById(@NonNull Long id) {
//         return repo.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Product not found"));
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> getProductByIdAsync(@NonNull Long id) {
//         return CompletableFuture.completedFuture(getProductById(id));
//     }

//     public Product getProductByBarcode(String barcode) {
//         return repo.findByBarcode(barcode)
//                 .orElseThrow(() -> new RuntimeException("Product not found"));
//     }

//     public Product updateProduct(Long id, Product p) {

//         Product existing = getProductById(id);

//         existing.setName(p.getName());
//         existing.setCode(p.getCode());
//         existing.setPurchasePrice(p.getPurchasePrice());
//         existing.setSellingPrice(p.getSellingPrice());
//         existing.setOpeningStock(p.getOpeningStock());

//         // keep old barcode
//         if (existing.getBarcode() == null || existing.getBarcode().isBlank()) {
//             existing.setBarcode(generateBarcodeNumber());
//         }

//         Product updated = repo.save(existing);

//         generateBarcode(updated.getBarcode());

//         return updated;
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Product> updateProductAsync(
//             Long id,
//             Product p) {

//         return CompletableFuture.completedFuture(
//                 updateProduct(id, p));
//     }

//     public void deleteById(Long id) {
//         repo.deleteById(id);
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Void> deleteByIdAsync(Long id) {

//         deleteById(id);

//         return CompletableFuture.completedFuture(null);
//     }

//     // private String generateBarcodeNumber() {

//     //     Random random = new Random();

//     //     String barcode;

//     //     do {
//     //         barcode = String.format("%06d",
//     //                 random.nextInt(1000000));
//     //     }
//     //     while (repo.findByBarcode(barcode).isPresent());

//     //     return barcode;
//     // }

//     private void generateBarcode(String barcode) {

//         try {

//             Path folder = FileSystems
//                     .getDefault()
//                     .getPath("uploads/barcodes");

//             if (!Files.exists(folder)) {
//                 Files.createDirectories(folder);
//             }

//             BitMatrix matrix =
//                     new MultiFormatWriter().encode(
//                             barcode,
//                             BarcodeFormat.CODE_128,
//                             300,
//                             100
//                     );

//             Path path = folder.resolve(barcode + ".png");

//             MatrixToImageWriter.writeToPath(
//                     matrix,
//                     "PNG",
//                     path
//             );

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }



package com.vpm.Accounts.Service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    /**
     * Save Product and Auto Generate Barcode
     */
    // public Product saveProduct(@NonNull Product p) {

    //     // First save to generate ID
    //     Product saved = repo.save(p);

    //     // Barcode based on Product ID
    //     String barcode = String.format("%06d", saved.getId());

    //     saved.setBarcode(barcode);

    //     saved = repo.save(saved);

    //     generateBarcode(barcode);

    //     return saved;
    // }

    public Product saveProduct(Product p) {

    if (!p.getActive()) {
        p.setActive(true);
    }

    String barcode =
            String.format("%06d",
                    new Random().nextInt(999999));

    while (repo.findByBarcode(barcode).isPresent()) {

        barcode =
                String.format("%06d",
                        new Random().nextInt(999999));
    }

    p.setBarcode(barcode);

    Product saved = repo.save(p);

    generateBarcode(barcode);

    return saved;
}

    @Async("accountExecutor")
    public CompletableFuture<Product> saveProductAsync(@NonNull Product p) {
        return CompletableFuture.completedFuture(saveProduct(p));
    }

    /**
     * Get All Products
     */
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Product>> getAllProductsAsync() {
        return CompletableFuture.completedFuture(getAllProducts());
    }

    /**
     * Get Product By ID
     */
    public Product getProductById(@NonNull Long id) {

        return repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> getProductByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(
                getProductById(id));
    }

    /**
     * Get Product By Barcode
     */
    public Product getProductByBarcode(String barcode) {

        return repo.findByBarcode(barcode)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    /**
     * Update Product
     */
    public Product updateProduct(
            @NonNull Long id,
            @NonNull Product p) {

        Product existing = getProductById(id);

        existing.setName(p.getName());
        existing.setCode(p.getCode());
        existing.setPurchasePrice(p.getPurchasePrice());
        existing.setSellingPrice(p.getSellingPrice());
        existing.setOpeningStock(p.getOpeningStock());
        existing.setActive(p.getActive());

        // Keep Existing Barcode
        Product updated = repo.save(existing);

        // Regenerate barcode image if needed
        if (updated.getBarcode() != null &&
                !updated.getBarcode().isBlank()) {

            generateBarcode(updated.getBarcode());
        }

        return updated;
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> updateProductAsync(
            @NonNull Long id,
            @NonNull Product p) {

        return CompletableFuture.completedFuture(
                updateProduct(id, p));
    }

    /**
     * Delete Product
     */
    public void deleteById(@NonNull Long id) {
        repo.deleteById(id);
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteByIdAsync(
            @NonNull Long id) {

        deleteById(id);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Generate Barcode Image
     */
    private void generateBarcode(String barcode) {

        try {

            Path folder = FileSystems
                    .getDefault()
                    .getPath("uploads", "barcodes");

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            BitMatrix matrix =
                    new MultiFormatWriter().encode(
                            barcode,
                            BarcodeFormat.CODE_128,
                            600,
                            200
                    );

            Path path =
                    folder.resolve(barcode + ".png");

            MatrixToImageWriter.writeToPath(
                    matrix,
                    "PNG",
                    path
            );

            System.out.println(
                    "Barcode Generated : " +
                            path.toAbsolutePath());

        } catch (Exception e) {

            System.err.println(
                    "Barcode Generation Failed");

            e.printStackTrace();
        }
    }
}