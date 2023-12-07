package io.mds.hty.taskmanager.service;

import io.mds.hty.taskmanager.model.dao.Comment;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Employee.Role;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.common.Action;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.CommentDto;
import io.mds.hty.taskmanager.model.dto.TaskDto;
import io.mds.hty.taskmanager.repo.CommentRepo;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import io.mds.hty.taskmanager.repo.TaskGroupRepo;
import io.mds.hty.taskmanager.repo.TaskRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.mds.hty.taskmanager.common.Utils.nonEmpty;

@Service
public class TaskService {

    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private TaskGroupRepo taskGroupRepo;

    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private CommentRepo commentRepo;


    @PersistenceContext
    private EntityManager entityManager;


    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Optional<Task> getEmployeeTaskById(Long employeeId, Long taskId) {
        return taskRepo.findTaskByEmployeeAssignedIdAndId(employeeId, taskId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Set<Task> getEmployeeFinishedTasks(Employee e, Boolean finished) throws UsernameNotFoundException {
        return taskRepo.findAllByEmployeeAssignedAndIsCompleted(e, finished);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Task> updateEmployeeTask(Employee e, Long id, Task.TaskStatus status, Integer progress) throws UsernameNotFoundException {
        Optional<Task> ot = taskRepo.findTaskByEmployeeAssignedAndId(e, id);
        ot.ifPresent((t) -> {
            if (!t.getStatus().equals(status)) {
                if (t.getStatus().equals(Task.TaskStatus.CREATED)) t.setStarted(Instant.now());
                if (status.equals(Task.TaskStatus.MARKED_FINISHED)) t.setIsCompleted(true);
                t.setStatus(status);
            }
            t.setProgress(progress);
            taskRepo.save(t);
        });
        return ot;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public String commentCud(Employee principal, CommentDto cDto, Action action) {
        entityManager.merge(principal);
        return switch (action) {
            case NEW -> {
                Task t = getEmployeeTaskById(principal.getId(), cDto.getTask()).orElseThrow(() -> new AccessDeniedException("No such task"));
                Comment comment = Comment.builder().header(cDto.getHeader()).text(cDto.getText()).dateTime(Instant.now())
                        .onProgress(t.getProgress()).task(t).user(principal).build();
                commentRepo.save(comment);
                yield "{\"comment\": \"left\", \r\n\"text\": \"" + comment + "\"}";
            }
            case EDIT -> {
                Comment comment = commentRepo.findCommentByIdAndUserId(cDto.getId(), principal.getId()).orElseThrow(() -> new AccessDeniedException("No such comment"));
                boolean updated = false;
                if (cDto.getHeader() != null && !cDto.getHeader().isBlank()) {
                    comment.setHeader(cDto.getHeader());
                    updated = true;
                }
                if (cDto.getText() != null && !cDto.getText().isBlank()) {
                    comment.setText(cDto.getText());
                    updated = true;
                }
                if (updated) {
                    comment.setLastUpdate(Instant.now());
                }
                yield "{\"comment\": \"edited\", \r\n\"text\": \"" + comment + "\"}";
            }
            case DELETE -> {
                int dels = commentRepo.deleteCommentByIdAndUserId(cDto.getId(), principal.getId());
                yield "{\"comment\": \"deleted\", \r\n\"count\": \"" + dels + "\"}";
            }
        };

    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String taskCud(Employee principal, TaskDto tDto, Action action) {
        entityManager.merge(principal);
        return switch (action) {
            case NEW -> {
                Task t = Task.builder()
                        .group(taskGroupRepo.findTaskGroupByName(tDto.getGroup())
                                .orElseThrow(() -> new IllegalArgumentException("No such group")))
                        .employeeCreated(principal)
                        .employeeAssigned(Stream.of(employeeRepo.findEmployeeByUserNameIs(tDto.getEmployeeAssigned())
                                        .orElseThrow(() -> new IllegalArgumentException("No such employee")))
                                .filter(e -> e.getRoles().stream()
                                        .anyMatch(r -> Set.of(Role.JUNIOR_DEVELOPER, Role.MIDDLE_DEVELOPER, Role.SENIOR_DEVELOPER).contains(r)))
                                .findFirst().orElseThrow(() -> new IllegalArgumentException("No such employee")
                                ))
                        .header(tDto.getHeader()).description(tDto.getDescription()).complexity(tDto.getComplexity())
                        .priority(tDto.getPriority()).plannedStart(tDto.getPlannedStart()).plannedFinish(tDto.getPlannedFinish())
                        .comments(new HashSet<>()).dateCreated(Instant.now()).isCompleted(false)
                        .progress(0).status(Task.TaskStatus.CREATED).build();
                taskRepo.save(t);
                yield "{\"task\": \"created\", \r\n\"task\": \"" + t + "\"}";
            }
            case EDIT -> {
                if (!principal.getRoles().contains(Role.ADMIN) && principal.getTaskGroups().stream().noneMatch(tg -> tg.getName().equals(tDto.getGroup()))) {
                    throw new AccessDeniedException("Unauthorized");
                }
                boolean updated = false;
                Task t = taskRepo.findById(tDto.getId()).orElseThrow(() -> new IllegalArgumentException("No such task Id" + tDto.getId()));
                if (nonEmpty(tDto.getGroup()) && !tDto.getGroup().equals(t.getGroup().getName())) {
                    TaskGroup ng = taskGroupRepo.findTaskGroupByName(tDto.getGroup()).orElseThrow(() -> new IllegalArgumentException("No such group " + tDto.getGroup()));
                    t.setGroup(ng);
                    updated = true;
                }
                if (nonEmpty(tDto.getEmployeeAssigned()) && !t.getEmployeeAssigned().getUsername().equals(tDto.getEmployeeAssigned())) {
                    Employee ne = employeeRepo.findEmployeeByUserNameIs(tDto.getEmployeeAssigned()).orElseThrow(() -> new IllegalArgumentException("No such user " + tDto.getEmployeeAssigned()));
                    t.setEmployeeAssigned(ne);
                    updated = true;
                }
                if (nonEmpty(tDto.getHeader())) {
                    t.setHeader(tDto.getHeader());
                    updated = true;
                }
                if (nonEmpty(tDto.getDescription())) {
                    t.setDescription(tDto.getDescription());
                    updated = true;
                }
                if (nonEmpty(tDto.getPriority())) {
                    t.setPriority(tDto.getPriority());
                    updated = true;
                }
                if (nonEmpty(tDto.getComplexity())) {
                    t.setComplexity(tDto.getComplexity());
                    updated = true;
                }
                if (nonEmpty(tDto.getPlannedStart())) {
                    t.setPlannedStart(tDto.getPlannedStart());
                    updated = true;
                }
                if (nonEmpty(tDto.getPlannedFinish())) {
                    t.setPlannedFinish(tDto.getPlannedFinish());
                    updated = true;
                }
                if (nonEmpty(tDto.getStatus())) {
                    t.setStatus(Task.TaskStatus.valueOf(tDto.getStatus()));
                    updated = true;
                }
                if (nonEmpty(tDto.getProgress())) {
                    t.setProgress(tDto.getProgress());
                    updated = true;
                }
                if (nonEmpty(tDto.getStarted())) {
                    t.setStarted(tDto.getStarted());
                    updated = true;
                }
                if (nonEmpty(tDto.getIsCompleted())) {
                    t.setIsCompleted(tDto.getIsCompleted());
                    updated = true;
                }
                if (nonEmpty(tDto.getCompleted())) {
                    t.setCompleted(tDto.getCompleted());
                    updated = true;
                }
                taskRepo.save(t);
                String edited = updated ? "Edited" : "Not edited";
                yield "{\"task\": \"" + edited + "\", \r\n\"text\": \"" + t + "\"}";
            }
            case DELETE -> {
                if (!principal.getRoles().contains(Role.ADMIN) && principal.getTaskGroups().stream().noneMatch(tg -> tg.getName().equals(tDto.getGroup()))) {
                    throw new AccessDeniedException("Unauthorized");
                }
                int dels = taskRepo.deleteTaskById(tDto.getId());
                yield "{\"task\": \"deleted\", \r\n\"count\": \"" + dels + "\"}";
            }
        };
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Set<Task> getTasksByGroupNames(Employee principal) {
        entityManager.refresh(principal);
        Set<TaskGroup> tgs = principal.getTaskGroups();
        return taskRepo.findTasksByGroupNamesIn(tgs);
    }


}
