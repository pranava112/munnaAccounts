// // package com.vpm.Accounts.Repository;

// // import org.springframework.data.jpa.repository.JpaRepository;

// // import com.vpm.Accounts.Entity.Users;

// // public interface UserRepository extends JpaRepository<Users, Long>{
    
// // }



// // package com.vpm.Accounts.Repository;

// // import java.util.Optional;

// // import org.springframework.data.jpa.repository.JpaRepository;
// // import org.springframework.stereotype.Repository;

// // import com.vpm.Accounts.Entity.Users;

// // @Repository
// // public interface UserRepository extends JpaRepository<Users, Long> {

// //     Optional<Users> findByUsername(String username);

// // }



// package com.vpm.Accounts.Repository; 
// import java.util.Optional; 
// import org.springframework.data.jpa.repository.JpaRepository;
//  import org.springframework.stereotype.Repository; 
//  import com.vpm.Accounts.Entity.Users; 
//  @Repository 
//  public interface UserRepository extends JpaRepository<Users, Long> 
//     {
//          Optional<Users> findByUsername(String username);
//          Optional<Users> findByEmail(String email); 
//          boolean existsByUsername(String username); 
//          boolean existsByEmail(String email);
        
//     }


package com.vpm.Accounts.Repository; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import com.vpm.Accounts.Entity.Users; @Repository public interface UserRepository extends JpaRepository<Users, Long> { Optional<Users> findByUsername(String username); Optional<Users> findByEmail(String email); boolean existsByUsername(String username); boolean existsByEmail(String email); Optional<Users> findByUsernameAndTenantId( String username, Long tenantId ); boolean existsByUsernameAndTenantId( String username, Long tenantId ); boolean existsByEmailAndTenantId( String email, Long tenantId ); }