package io.mds.hty.taskmanager.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id","personalNumber"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {


    private Long id;

    private Long personalNumber;

    private String name;

    private String department;

    private String position;

    @NotNull
    private String userName;

    private CharSequence password;

    private Set<Long> taskGroups;

    private Set<Long> tasksCreated;

    private Set<Long> tasksAssigned;

    private Set<io.mds.hty.taskmanager.model.dao.Employee.Role> roles;

    private Boolean nonExpired = true;

    private Boolean nonLocked = true;

    private Boolean credsNonExpired = true;

    private Boolean isEnabled = true;
    }
