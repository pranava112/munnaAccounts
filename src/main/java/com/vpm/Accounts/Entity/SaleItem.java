package com.vpm.Accounts.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class SaleItem {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private int quantity;
	private double price;
	
	@ManyToOne
	@JoinColumn(name="product_id")
	private Product product;
	
	@ManyToOne
	@JoinColumn(name="sale_id")
	@JsonBackReference
	private Sale sale;
	
	public double getTotal() {
		return quantity*price;
	}
	
	public Long getId() {return id;}
	public void setId(Long id) {this.id=id;}
	
	public int getQuantity() {return quantity;}
	public void setQuantity(int quantity) {this.quantity=quantity;}
	
	public double getPrice() {return price;}
	public void setPrice(double price) {this.price=price;}
	
	public Product getProduct(){return product;}
	public void setProduct(Product product) {this.product=product;}
	
	public Sale getSale() {return sale;}
	public void setSale(Sale sale) {this.sale=sale;}
	
}
