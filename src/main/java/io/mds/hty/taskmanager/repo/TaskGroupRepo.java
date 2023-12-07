package io.mds.hty.taskmanager.repo;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TaskGroupRepo extends JpaRepository<TaskGroup, Long> {

    Optional<TaskGroup> findTaskGroupByName(String name);
    Optional<TaskGroup> findTaskGroupByIdAndUsersContaining(Long gId, Employee u);
    Integer deleteTaskGroupById(Long id);
    Boolean existsTaskGroupByNameNot(String name);

    Set<TaskGroup> findAllByNameIn(Set<String> names);

}
