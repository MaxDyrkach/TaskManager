package io.mds.hty.taskmanager.repo;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.CommonStatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {


    Optional<Employee> findByUserName(String name);

    @Query("SELECT e FROM Employee e WHERE e.userName= :name AND e.taskGroups IN :g")
    Optional<Employee> findByUserNameAndGroupsIn(String name, Set<TaskGroup> g);

    @Query("SELECT e FROM Employee e WHERE e.userName= :name AND e.roles IN :r")
    Optional<Employee> findByUserNameAndRolesIn(String name, Set<Employee.Role> r);

    Optional<Employee> findByPersonalNumber(Long num);

    Set<Employee> findAllByUserNameIn(Set<String> uns);

    Boolean existsByUserName(String name);

    @Query("SELECT avg(count(t))FROM Employee e INNER JOIN Task t ON t.employeeAssigned=e" +
            " WHERE FUNCTION('date_trunc', 'DAY', t.dateCreated) between :start AND :end " +
            "group by e")
    Double getAverageTasksAssignedOnEmployee(Instant start, Instant end);

    @Query("SELECT avg(count(t))FROM Employee e INNER JOIN Task t ON t.employeeCreated=e" +
            " WHERE FUNCTION('date_trunc', 'DAY', t.dateCreated) between :start AND :end " +
            "group by e")
    Double getAverageTasksCreateOnEmployee(Instant start, Instant end);

    @Query("SELECT avg(count(t)) FROM Employee e INNER JOIN Task t ON t.employeeAssigned=e" +
            " WHERE FUNCTION ('date_trunc', 'DAY', t.dateCreated) between :start AND :end " +
            "AND t.isCompleted = true group by e")
    Double getAverageFinishedTasksAssignedOnEmployee(Instant start, Instant end);



    @Query(value = "SELECT count(t) FROM Employee e INNER JOIN Task t ON t.employeeAssigned=e" +
            " WHERE FUNCTION ('date_trunc', 'DAY', t.dateCreated) between :start AND :end " +
            "AND t.isCompleted = true group by e")
    Long countTasksInWork();


}
