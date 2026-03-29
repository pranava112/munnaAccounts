package com.vpm.Accounts.Entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class Sale {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String customerName;
	private String date;
	private Long phoneNo;
	
	@OneToMany(mappedBy="sale",
			cascade=CascadeType.ALL,
			orphanRemoval=true,
			fetch=FetchType.EAGER
			)
	private List<SaleItem> items;
	
	
	public Long getId() {return id;}
	public void setId(Long id) {this.id=id;}
	
	public String getCustomerName() {return customerName;}
	public void setCustomerName(String customerName) {this.customerName=customerName;}
	
	public String getDate() {return date;}
	public void setDate(String date) {this.date=date;}
	
	public Long getPhoneNo() {return phoneNo;}
	public void setPhoneNo(Long phoneNo) {this.phoneNo=phoneNo;}
	
	public List<SaleItem> getItems(){return items;}
	public void setItems(List<SaleItem> items) {this.items=items;}

}
