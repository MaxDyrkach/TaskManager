package io.mds.hty.taskmanager;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Employee.Role;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import io.mds.hty.taskmanager.repo.TaskGroupRepo;
import io.mds.hty.taskmanager.repo.TaskRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Component
@RequiredArgsConstructor(onConstructor_ ={@Lazy})
public class DataLoader implements CommandLineRunner {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private EmployeeRepo userRepo;
    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private TaskGroupRepo taskGroupRepo;
    @Lazy
    @Autowired
    private DataLoader self;



    @Override
    public void run(String... args) throws Exception {
        self.loadData();
    }

    private String enc(String passw){
        return passwordEncoder.encode(passw);
    }


    @Transactional
    public void loadData() {
        TaskGroup easy = TaskGroup.builder().name("Easy").build();
        TaskGroup normal = TaskGroup.builder().name("Normal").build();
        TaskGroup hard = TaskGroup.builder().name("Hard").build();
        taskGroupRepo.saveAll(Stream.of(easy,normal,hard).collect(Collectors.toList()));

        Employee adminUser = Employee.builder().userName("Max").password(enc("passwordAdm")).personalNumber(0L)
                .roles(Stream.of(Role.ADMIN, Role.TEAMLEAD).collect(Collectors.toCollection(HashSet::new)))
                .taskGroups(Stream.of(hard).collect(Collectors.toCollection(HashSet::new)))
                .build();

        Employee dev1 = Employee.builder().userName("Tom").password(enc("passwTom")).personalNumber(1L)
                .roles(Stream.of(Role.SENIOR_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(Stream.of(hard).collect(Collectors.toCollection(HashSet::new)))
                .build();


        Employee dev2 = Employee.builder().userName("John").password(enc("passwJohn")).personalNumber(2L)
                .roles(Stream.of(Role.SENIOR_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(Stream.of(hard, normal).collect(Collectors.toCollection(HashSet::new)))
                .build();

        Employee dev3 = Employee.builder().userName("Alex").password(enc("passwAlex")).personalNumber(3L)
                .roles(Stream.of(Role.MIDDLE_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .taskGroups(new HashSet<>())
                .tasksAssigned(new HashSet<>()).build();

        Employee dev4 = Employee.builder().userName("Amir").password(enc("passwAmir")).personalNumber(4L)
                .roles(Stream.of(Role.JUNIOR_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(Stream.of(easy).collect(Collectors.toCollection(HashSet::new))).build();


        Employee dev5 = Employee.builder().userName("Natalia").password(enc("passwNatalia")).personalNumber(5L)
                .roles(Stream.of(Role.MIDDLE_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(Stream.of(normal).collect(Collectors.toCollection(HashSet::new))).build();


        Employee dev6 = Employee.builder().userName("Anastasia").password(enc("passwAnastasia")).personalNumber(6L)
                .roles(Stream.of(Role.JUNIOR_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(Stream.of(easy, normal).collect(Collectors.toCollection(HashSet::new))).build();


        Employee teamlead1 = Employee.builder().userName("Ben").password(enc("p")).personalNumber(7L)
                .roles(Stream.of(Role.TEAMLEAD).collect(Collectors.toCollection(HashSet::new)))
                .tasksCreated(new HashSet<>())
                .taskGroups(Stream.of(easy).collect(Collectors.toCollection(HashSet::new))).build();


        Employee teamlead2 = Employee.builder().userName("Peter").password(enc("p")).personalNumber(8L)
                .roles(Stream.of(Role.TEAMLEAD).collect(Collectors.toCollection(HashSet::new)))
                .tasksCreated(new HashSet<>())
                .taskGroups(Stream.of(normal).collect(Collectors.toCollection(HashSet::new))).build();

        Employee teamlead3 = Employee.builder().userName("Lilith").password(enc("p")).personalNumber(9L)
                .roles(Stream.of(Role.TEAMLEAD).collect(Collectors.toCollection(HashSet::new)))
                .tasksCreated(new HashSet<>())
                .taskGroups(Stream.of(hard).collect(Collectors.toCollection(HashSet::new))).build();


        Task task1 = Task.builder()
                .employeeCreated(teamlead2)
                .dateCreated(Instant.now())
                .group(normal)
                .header("Task1").description("Description").priority(10).complexity(5)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant())
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(4)))
                .employeeAssigned(dev3)
                .progress(0)
                .build();
        teamlead2.getTasksCreated().add(task1);
        Task task2 = Task.builder()
                .employeeCreated(teamlead2)
                .dateCreated(Instant.now().plus(Duration.ofSeconds(ThreadLocalRandom.current().nextLong(3600 * 4))))
                .group(normal)
                .header("Task2").description("Description2").priority(8).complexity(7)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.TUESDAY).atZone(ZoneId.systemDefault()).toInstant())
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.TUESDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(3)))
                .employeeAssigned(dev5)
                .progress(0)
                .build();
        Task task3 = Task.builder()
                .employeeCreated(teamlead1)
                .dateCreated(Instant.now().plus(Duration.ofSeconds(ThreadLocalRandom.current().nextLong(3600 * 4))))
                .group(easy)
                .header("Task3").description("Description3").priority(6).complexity(3)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(1)))
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(2)))
                .employeeAssigned(dev4)
                .progress(0)
                .build();
        Task task4 = Task.builder()
                .employeeCreated(teamlead3)
                .dateCreated(Instant.now().plus(Duration.ofSeconds(ThreadLocalRandom.current().nextLong(3600 * 4))))
                .group(hard)
                .header("Task4").description("Description4").priority(9).complexity(9)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.FRIDAY).atZone(ZoneId.systemDefault()).toInstant())
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.FRIDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(3)))
                .employeeAssigned(dev1)
                .progress(0)
                .build();
        Task task5 = Task.builder()
                .employeeCreated(teamlead3)
                .dateCreated(Instant.now())
                .group(hard)
                .header("Task5").description("Description5").priority(6).complexity(9)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.WEDNESDAY).atZone(ZoneId.systemDefault()).toInstant())
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.WEDNESDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(8)))
                .employeeAssigned(dev2)
                .progress(0)
                .build();
        Task task6 = Task.builder()
                .employeeCreated(teamlead1)
                .dateCreated(Instant.now())
                .group(easy)
                .header("Task6").description("Description6").priority(4).complexity(2)
                .status(Task.TaskStatus.CREATED)
                .plannedStart(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant())
                .plannedFinish(LocalDateTime.now().with(DayOfWeek.MONDAY).atZone(ZoneId.systemDefault()).toInstant().plus(Period.ofWeeks(2)))
                .employeeAssigned(dev6)
                .progress(0)
                .build();

        taskRepo.saveAll(List.of(task1, task2, task3, task4, task5, task6));
        userRepo.saveAll(Stream.of(adminUser, dev1, dev2, dev3, dev4, dev5, dev6, teamlead1, teamlead2, teamlead3)
                .collect(Collectors.toList()));



    }
}