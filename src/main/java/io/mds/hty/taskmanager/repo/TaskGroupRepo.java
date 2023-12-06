package io.mds.hty.taskmanager.repo;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskGroupRepo extends JpaRepository<TaskGroup, Long> {

    Optional<TaskGroup> findTaskGroupByName(String name);
    Optional<TaskGroup> findTaskGroupByIdAndAndUsersContainingOrderByUsers(Employee u, Long gId);
    Integer deleteTaskGroupById(Long id);
    Boolean existsTaskGroupByNameNot(String name);

    Set<TaskGroup> findAllByNameIn(Set<String> names);

}
