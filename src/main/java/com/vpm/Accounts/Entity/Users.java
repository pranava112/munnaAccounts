// package com.vpm.Accounts.Entity;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;

// @Entity
// public class Users {
    
//     @Id
    
//     @GeneratedValue(strategy=GenerationType.IDENTITY)
//     private Long id;
//     private String username;
//     private String password;
//     private String role;

//     public Long getId() {return id;}
//     public void setId(Long id) {this.id=id;}
    
//     public String getUsername() {return username;}
//     public void setUsername(String username) {this.username=username;}
    
//     public String getPassword() {return password;}
//     public void setPassword(String password) {this.password=password;}
    
//     public String getRole() {return role;}
//     public void setRole(String role) {this.role=role;}

// }


// package com.vpm.Accounts.Entity;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;

// @Entity
// public class Users {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String username;

//     private String password;

//     private String role;

//     private Long tenantId;

//     private boolean enabled = true;

//     // =========================
//     // ID
//     // =========================

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     // =========================
//     // USERNAME
//     // =========================

//     public String getUsername() {
//         return username;
//     }

//     public void setUsername(String username) {
//         this.username = username;
//     }

//     // =========================
//     // PASSWORD
//     // =========================

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     // =========================
//     // ROLE
//     // =========================

//     public String getRole() {
//         return role;
//     }

//     public void setRole(String role) {
//         this.role = role;
//     }

//     // =========================
//     // TENANT ID
//     // =========================

//     public Long getTenantId() {
//         return tenantId;
//     }

//     public void setTenantId(Long tenantId) {
//         this.tenantId = tenantId;
//     }

//     // =========================
//     // ENABLED
//     // =========================

//     public boolean isEnabled() {
//         return enabled;
//     }

//     public void setEnabled(boolean enabled) {
//         this.enabled = enabled;
//     }
// }


// //////////////////////////////////////////////////////


package com.vpm.Accounts.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private boolean enabled = true;

    // =========================
    // ID
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // =========================
    // USERNAME
    // =========================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // =========================
    // PASSWORD
    // =========================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // =========================
    // EMAIL
    // =========================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // =========================
    // ROLE
    // =========================

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // =========================
    // TENANT ID
    // =========================

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    // =========================
    // ENABLED
    // =========================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
