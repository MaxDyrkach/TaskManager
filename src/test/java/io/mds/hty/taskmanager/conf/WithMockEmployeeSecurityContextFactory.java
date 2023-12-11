package io.mds.hty.taskmanager.conf;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Employee.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WithMockEmployeeSecurityContextFactory implements WithSecurityContextFactory<WithMockEmployee> {

    @Override
    public SecurityContext createSecurityContext(WithMockEmployee annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Employee employee = Employee.builder().userName(annotation.username())
                .password(annotation.password())
                .roles(Arrays.stream(annotation.roles()).map(Role::valueOf).collect(Collectors.toCollection(HashSet::new))).build();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(employee, "password", employee.getAuthorities()));
        return context;
    }
}