package io.mds.hty.taskmanager.service;


import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.TaskGroupDto;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import io.mds.hty.taskmanager.repo.TaskGroupRepo;
import io.mds.hty.taskmanager.repo.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskGroupService {

    @Autowired
    private TaskGroupRepo taskGroupRepo;
    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private EmployeeRepo employeeRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TaskGroup createOrUpdateGroup(TaskGroupDto dto, Employee principal) {
        TaskGroup tg = taskGroupRepo.findTaskGroupByName(dto.getName()).orElseGet(TaskGroup::new);
        if(tg.getUsers()!=null && !tg.getUsers().contains(principal)) throw new AccessDeniedException("Unauthorized");
        tg.setTasks(new HashSet<>());
        tg.setUsers(new HashSet<>());
        if (dto.getTasks() != null && !dto.getTasks().equals(tg.getTasks().stream().map(Task::getId).collect(Collectors.toSet()))) {
            List<Task> t = taskRepo.findAllById(dto.getTasks());
            if (!t.isEmpty()) {
                tg.getTasks().clear();
                tg.getTasks().addAll(t);
            }
        }
        if (dto.getUserNames() != null && !dto.getUserNames().equals(tg.getUsers().stream().map(Employee::getUsername).collect(Collectors.toSet()))) {
            Set<Employee> em = employeeRepo.findAllByUserNameIn(dto.getUserNames());
            if (!em.isEmpty()) {
                tg.getUsers().clear();
                tg.getUsers().addAll(em);
            }
        }
        taskGroupRepo.save(tg);
        return tg;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer deleteGroupByIdOrName(TaskGroupDto dto) {
        TaskGroup tg = null;
        if(dto.getId()!=null){
            tg = taskGroupRepo.findById(dto.getId()).orElseThrow(() -> new AccessDeniedException("No such group: id = "+ dto.getId()));
        } else {
            tg = taskGroupRepo.findTaskGroupByName(dto.getName()).orElseThrow(() -> new AccessDeniedException("No such group: group name = "+ dto.getName()));
        }
        return taskGroupRepo.deleteTaskGroupById(tg.getId());
    }
}
