package io.mds.hty.taskmanager.repo;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.PeriodDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;


@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {

    Set<Task> findAllByEmployeeAssignedAndIsCompleted(Employee e, Boolean c);
    Optional<Task> findTaskByEmployeeAssignedAndId(Employee e, Long id);

    Optional<Task> findTaskByEmployeeAssignedIdAndId (Long empId, Long taskId);
    Integer deleteTaskById(Long id);

    @Query("SELECT t FROM Task t WHERE t.group IN :grps")
    Set<Task> findTasksByGroupNamesIn(Set<TaskGroup> grps);

    Long countAllByStatusIs(Task.TaskStatus status);
    Long countAllByStatusIsNot(Task.TaskStatus status);


    @Query("SELECT  new io.mds.hty.taskmanager.model.dto.PeriodDto (t.dateCreated, t.completed) FROM Task t " +
            " WHERE FUNCTION('date_trunc', 'DAY', t.dateCreated) between :start AND :end AND FUNCTION('date_trunc', 'DAY', t.completed) between :start AND :end " +
            "AND t.isCompleted = true")
    PeriodDto[] getPeriodsOfFinishedTasksAssignedOnEmployee(Instant start, Instant end);

    @Query(value = "SELECT count(t) FROM Task t " +
            " WHERE " +
            " (FUNCTION('date_trunc', 'DAY', t.dateCreated) between :start AND :end OR FUNCTION('date_trunc', 'DAY', t.completed) between :start AND :end) " +
            "AND t.employeeAssigned = :user AND t.isCompleted = true")
    Set<Task> getTasksStartedOrFinishedInPeriod(Employee user, Instant start, Instant end);

}
