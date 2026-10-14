package com.vpm.Accounts.Entity.GST;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.vpm.Accounts.Enum.GSTRegistrationType;
import com.vpm.Accounts.security.TenantEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "gst_registrations", uniqueConstraints = @UniqueConstraint(
        name = "uk_gst_registration_tenant_gstin", columnNames = {"tenant_id", "gstin"}))
@EntityListeners(TenantEntityListener.class)
public class GSTRegistration {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(nullable = false, length = 15)
    private String gstin;

    @Column(nullable = false)
    private String legalName;

    private String tradeName;

    private String state;

    private String stateCode;

    private String address;

    private LocalDate registrationDate;

    private boolean active;

    @Enumerated(EnumType.STRING)
    private GSTRegistrationType registrationType;

    private boolean eInvoiceEnabled;

    private boolean eWayBillEnabled;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getState(){
        return state;
    }

    public void setState(String state){
        this.state=state;
    }

    public String getStateCode(){
        return stateCode;
    }

    public void setStateCode(String stateCode){
        this.stateCode=stateCode;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address=address;
    }

    public LocalDate getRegistrationDate(){
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate){
        this.registrationDate=registrationDate;
    }

    public boolean isActive(){
        return active;
    }

    public void setActive(boolean active){
        this.active=active;
    }

    public GSTRegistrationType getRegistrationType(){
        return registrationType;
    }

    public void setRegistrationType(GSTRegistrationType registrationType){
        this.registrationType = registrationType;
    }

    public boolean isEInvoiceEnabled(){
        return eInvoiceEnabled;
    }

    public void setEInvoiceEnabled(boolean eInvoiceEnabled){
        this.eInvoiceEnabled=eInvoiceEnabled;
    }

    public boolean isEWayBillEnabled(){
        return eWayBillEnabled;
    }

    public void setEWayBillEnabled(boolean eWayBillEnabled){
        this.eWayBillEnabled= eWayBillEnabled;
    }

    @JsonIgnore
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
