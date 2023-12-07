package io.mds.hty.taskmanager.service;

import io.mds.hty.taskmanager.common.Action;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.EmlpoyeeGroupActionDto;
import io.mds.hty.taskmanager.model.dto.EmployeeDto;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import io.mds.hty.taskmanager.repo.TaskGroupRepo;
import io.mds.hty.taskmanager.repo.TaskRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.mds.hty.taskmanager.common.Utils.nonEmpty;

@Service
public class UserDataService {

    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    TaskGroupRepo taskGroupRepo;
    @Autowired
    TaskRepo taskRepo;
    @PersistenceContext
    EntityManager entityManager;


    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Employee editAnyEmployeeDataByUserName(EmployeeDto dto, Action action)  {
        Employee e;
        switch (action) {
            case NEW -> {
                if (employeeRepo.existsByUserName(dto.getUserName()))
                    throw new IllegalArgumentException("User with UserName " + dto.getUserName() + " already existing");
                e = new Employee();
            }
            case EDIT -> {
                e = employeeRepo.findEmployeeByUserNameIs(dto.getUserName())
                        .orElseThrow(() -> new IllegalArgumentException("User with Username " + dto.getUserName() + " not found"));
            }
            default -> throw new IllegalArgumentException("Action must be NEW or EDIT");
        }
        if (nonEmpty(dto.getPersonalNumber())) e.setPersonalNumber(dto.getPersonalNumber());
        if (nonEmpty(dto.getName())) e.setName(dto.getName());
        if (nonEmpty(dto.getDepartment())) e.setDepartment(dto.getDepartment());
        if (nonEmpty(dto.getPosition())) e.setPosition(dto.getPosition());
        if (nonEmpty(dto.getUserName())) e.setUserName(dto.getUserName());
        if (nonEmpty((String) dto.getPassword())) e.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (nonEmpty(dto.getTaskGroups())) {
            Set<TaskGroup> tgs = new HashSet<>(taskGroupRepo.findAllById(dto.getTaskGroups()));
            e.setTaskGroups(tgs);
        }
        if (nonEmpty(dto.getTasksAssigned())) {
            Set<Task> t = new HashSet<>(taskRepo.findAllById(dto.getTasksAssigned()));
            e.setTasksAssigned(t);
        }
        if (nonEmpty(dto.getRoles())) e.setRoles(dto.getRoles());
        if (nonEmpty(dto.getNonExpired())) e.setNonExpired(dto.getNonExpired());
        if (nonEmpty(dto.getNonLocked())) e.setNonLocked(dto.getNonLocked());
        if (nonEmpty(dto.getCredsNonExpired())) e.setCredsNonExpired(dto.getCredsNonExpired());
        if (nonEmpty(dto.getIsEnabled())) e.setIsEnabled(dto.getIsEnabled());
        employeeRepo.save(e);
        return e;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Optional<Employee> getEmployeeData(String userName)  {
        return employeeRepo.findEmployeeByUserNameIs(userName);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Optional<Employee> getUserDataByEmployeeGroups(Employee e, String userName) throws IllegalArgumentException {
        Employee fromBase = employeeRepo.findEmployeeByUserNameIs(userName).orElseThrow(()-> new IllegalArgumentException("No such user"));
                if(fromBase.getTaskGroups().stream().anyMatch(tg-> e.getTaskGroups().contains(tg))) return Optional.of(fromBase);
                return Optional.empty();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Set<Employee> getGroupDevelopersData(Employee e, Long groupId)  {
        return taskGroupRepo.findTaskGroupByIdAndUsersContaining(groupId, e).orElseThrow(() -> new AccessDeniedException("No such group"))
                .getUsers().stream().filter(u -> u.getRoles().stream()
                        .anyMatch(r-> Set.of(Employee.Role.JUNIOR_DEVELOPER, Employee.Role.MIDDLE_DEVELOPER, Employee.Role.SENIOR_DEVELOPER).contains(r)))
                .collect(Collectors.toSet());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String editSelfDeveloper(String userName, EmployeeDto newParams)  {
        Employee e = employeeRepo.findEmployeeByUserNameIs(userName)
                .orElseThrow(() -> new AccessDeniedException("User " + userName + " not found"));
        String newName = newParams.getName();
        if (newName != null && !newName.isBlank()) e.setName(newName);
        String newUserName = newParams.getUserName();
        if (newUserName != null && !newUserName.isBlank()) e.setUserName(newUserName);
        if (newParams.getPassword() != null && !passwordEncoder.matches(newParams.getPassword(), e.getPassword())) {
            e.setPassword(passwordEncoder.encode(newParams.getPassword()));
        }
        return "Updated";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public String editSelfTeamlead(String userName, EmployeeDto newParams)  {
        Employee e = employeeRepo.findEmployeeByUserNameIs(userName)
                .orElseThrow(() -> new AccessDeniedException("User " + userName + " not found"));
        String newName = newParams.getName();
        if (newName != null && !newName.isBlank()) e.setName(newName);
        String newUserName = newParams.getUserName();
        if (newUserName != null && !newUserName.isBlank()) e.setUserName(newUserName);
        if (newParams.getPassword() != null) {
            e.setPassword(passwordEncoder.encode(newParams.getPassword()));
        }
        if (newParams.getTaskGroups() != null) {
            Set<TaskGroup> newGrps = new HashSet<>(taskGroupRepo.findAllById(newParams.getTaskGroups()));
            if (!newGrps.isEmpty()){
                e.setTaskGroups(newGrps);
            }
        }
        employeeRepo.saveAndFlush(e);
        return "Updated";
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Set<Task> getUserTasksVerbose(String userName, String verbose)  {
        return switch (verbose) {
            case "Assigned" -> employeeRepo.findEmployeeByUserNameIs(userName)
                    .orElseThrow(() -> new AccessDeniedException("User " + userName + " not found"))
                    .getTasksAssigned();
            case "Created" -> employeeRepo.findEmployeeByUserNameIs(userName)
                    .orElseThrow(() -> new AccessDeniedException("User " + userName + " not found"))
                    .getTasksCreated();
            default -> throw new IllegalArgumentException("Verbose must be Assigned or Created");
        };
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String addOrDeleteUserGroup(Employee principal, EmlpoyeeGroupActionDto dto)
            throws AccessDeniedException, IllegalArgumentException {
        TaskGroup tg = taskGroupRepo.findTaskGroupByName(dto.getGroupName()).orElseThrow(() -> new AccessDeniedException("No such group"));
        Employee e = employeeRepo.findEmployeeByUserNameIs(dto.getUserName()).orElseThrow(() -> new AccessDeniedException("User " + dto.getUserName() + " not found"));
        if (((e.getRoles().contains(Employee.Role.ADMIN) || e.getRoles().contains(Employee.Role.TEAMLEAD)) && !principal.getRoles().contains(Employee.Role.ADMIN)) ||
                !principal.getTaskGroups().contains(tg)) throw new AccessDeniedException("Unauthorized");
        return switch (dto.getAction()) {
            case NEW -> {
                if (!e.getTaskGroups().contains(tg)) {
                    e.getTaskGroups().add(tg);
                    Set<String> groupNames = e.getTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.toSet());
                    yield "{ \"userName\": \"" + e.getUsername() + "\", \r\n \"addedGroup\": \"" +
                            tg.getName() + "\",\r\n \"currentGroups\": \"" + groupNames + "\"}";
                }
                yield "{\"message\": \"User is in the group already\"}";
            }
            case DELETE -> {
                if (e.getTaskGroups().contains(tg)) {
                    e.getTaskGroups().remove(tg);
                    Set<String> groupNames = e.getTaskGroups().stream().map(TaskGroup::getName).collect(Collectors.toSet());
                    yield "{ \"userName\": \"" + e.getUsername() + "\", \r\n \"deletedGroup\": \"" +
                            tg.getName() + "\",\r\n \"currentGroups\": \"" + groupNames + "\"}";
                }
                yield "{\"message\": \"User is not in the group\"}";
            }
            default -> throw new IllegalArgumentException("Action name must be NEW or DELETE");
        };
    }


}
