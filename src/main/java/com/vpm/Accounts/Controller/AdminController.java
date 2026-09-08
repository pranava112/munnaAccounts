// package com.vpm.Accounts.Controller;

// import java.util.Map;
// import jakarta.validation.Valid;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException;
// import com.vpm.Accounts.DTO.MemberRequest;
// import com.vpm.Accounts.Entity.Users;
// import com.vpm.Accounts.Repository.UserRepository;
// import com.vpm.Accounts.security.TenantContext;
// import com.vpm.Accounts.security.TenantUserDetails;

// @RestController
// @RequestMapping("/api/admin")
// public class AdminController {
//     private final UserRepository users;
//     private final PasswordEncoder encoder;
//     public AdminController(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

//     @PostMapping("/members")
//     @ResponseStatus(HttpStatus.CREATED)
//     public Map<String, Object> addMember(@Valid @RequestBody MemberRequest request, Authentication authentication) {
//         if (users.existsByUsername(request.getUsername())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
//         if (users.existsByEmail(request.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
//         TenantUserDetails admin = (TenantUserDetails) authentication.getPrincipal();
//         Users member = new Users(); member.setUsername(request.getUsername().trim()); member.setEmail(request.getEmail().trim().toLowerCase());
//         member.setPassword(encoder.encode(request.getPassword())); member.setRole("USER"); member.setTenantId(TenantContext.require()); member.setEnabled(true);
//         users.save(member);
//         return Map.of("username", member.getUsername(), "tenantId", member.getTenantId(), "role", member.getRole(), "createdBy", admin.getUsername());
//     }
// }



package com.vpm.Accounts.Controller; 
import java.util.Map; import jakarta.validation.Valid; 
import org.springframework.http.HttpStatus; 
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.bind.annotation.*; 
import org.springframework.web.server.ResponseStatusException; 
import com.vpm.Accounts.DTO.MemberRequest; 
import com.vpm.Accounts.Entity.Users; 
import com.vpm.Accounts.Repository.UserRepository; import com.vpm.Accounts.security.TenantContext; 
import com.vpm.Accounts.security.TenantUserDetails; 
@RestController
@RequestMapping("/api/admin") 
public class AdminController 
{
     private final UserRepository users; 
     private final PasswordEncoder encoder; 
     public AdminController( UserRepository users, PasswordEncoder encoder) 
     {
         this.users = users; 
         this.encoder = encoder; 
      } 
      @PostMapping("/members")
      @ResponseStatus(HttpStatus.CREATED) 
      public Map<String, Object> addMember( @Valid @RequestBody MemberRequest request, Authentication authentication) {
         Long tenantId = TenantContext.require(); 
         String username = request.getUsername().trim(); 
         String email = request.getEmail().trim().toLowerCase();
          // Username must be unique inside the tenant 
          if (users.existsByUsernameAndTenantId(username, tenantId))
           {
             throw new ResponseStatusException( HttpStatus.CONFLICT, "Username already exists in this company" );
            }
             // Email must be unique inside the tenant 
             if (users.existsByEmailAndTenantId(email, tenantId))
                 {
                     throw new ResponseStatusException( HttpStatus.CONFLICT, "Email already exists in this company" );
                 }
             TenantUserDetails admin = (TenantUserDetails) authentication.getPrincipal();
             Users member = new Users(); 
             member.setUsername(username); 
             member.setEmail(email); 
             member.setPassword( encoder.encode(request.getPassword()) ); 
             member.setRole("USER"); 
             member.setTenantId(tenantId);
             member.setEnabled(true); 
             users.save(member); return Map.of( "username", member.getUsername(), "email", member.getEmail(), "tenantId", member.getTenantId(), "role", member.getRole(), "createdBy", admin.getUsername() ); 
            }
         }
