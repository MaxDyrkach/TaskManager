package io.mds.hty.taskmanager.model.dao;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.PrivateKey;
import java.util.*;

import static jakarta.persistence.CascadeType.*;

@Entity
@Table(name = "app_users")
@Data
@ToString(of = {"personalNumber","name","userName"})
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"id", "password"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employee implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "personal_number",unique = true, nullable = false)
    private Long personalNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "user_name")
    private String userName;
    @Column(name = "password")
    private String password;



    @JsonIncludeProperties(value = {"name"})
    @ManyToMany(fetch = FetchType.EAGER, cascade = {PERSIST, REFRESH, MERGE} )
    private Set<TaskGroup> taskGroups;

    @JsonIgnore
    @OneToMany( mappedBy = "employeeCreated", cascade = PERSIST)
    private Set<Task> tasksCreated;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "employeeAssigned", cascade = PERSIST)
    private Set<Task> tasksAssigned;


    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "non_expired")
    private Boolean nonExpired = true;

    @Column(name = "non_locked")
    private Boolean nonLocked = true;

    @Column(name = "creds_non_expired")
    private Boolean credsNonExpired = true;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return roles.stream().map(s-> new SimpleGrantedAuthority("ROLE_"+s.name().toUpperCase())).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @JsonGetter("userName")
    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum Role {ADMIN, JUNIOR_DEVELOPER, MIDDLE_DEVELOPER, SENIOR_DEVELOPER, TEAMLEAD}
}
