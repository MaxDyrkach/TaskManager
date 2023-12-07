package io.mds.hty.taskmanager.service;

import io.mds.hty.taskmanager.common.Action;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import io.mds.hty.taskmanager.model.dto.EmlpoyeeGroupActionDto;
import io.mds.hty.taskmanager.model.dto.EmployeeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@AutoConfigureTestEntityManager
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserDataServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    @InjectMocks
    private UserDataService userDataService;
    @Mock
    private PasswordEncoder passwordEncoder;
    TaskGroup easy, hard;
    Employee dev1, dev2, dev3;

    @BeforeEach
    public void prepareData() {
        easy = TaskGroup.builder().name("Easy").users(new HashSet<>()).tasks(new HashSet<>()).build();
        hard = TaskGroup.builder().name("Hard").users(new HashSet<>()).tasks(new HashSet<>()).build();
        entityManager.persist(easy);
        entityManager.persist(hard);
        dev1 = Employee.builder().userName("Tom").personalNumber(1L)
                .roles(new HashSet<>(Set.of(Employee.Role.SENIOR_DEVELOPER)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(new HashSet<>())
                .build();
        dev2 = Employee.builder().userName("John").password("J1").personalNumber(2L)
                .roles(Stream.of(Employee.Role.SENIOR_DEVELOPER).collect(Collectors.toCollection(HashSet::new)))
                .tasksAssigned(new HashSet<>())
                .taskGroups(new HashSet<>())
                .build();
        dev3 = Employee.builder().userName("Alex").password("A1").personalNumber(3L)
                .roles(Stream.of(Employee.Role.TEAMLEAD).collect(Collectors.toCollection(HashSet::new)))
                .taskGroups(new HashSet<>())
                .tasksAssigned(new HashSet<>()).build();
        dev1.getTaskGroups().add(easy);
        dev2.getTaskGroups().add(easy);
        dev1.getTaskGroups().add(hard);
        dev2.getTaskGroups().add(hard);
        entityManager.persist(dev1);
        entityManager.persist(dev2);
        entityManager.persist(dev3);
        entityManager.persist(easy);
        entityManager.persist(hard);
        entityManager.flush();

    }

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = userDataService.getAllEmployees();

        assertEquals("Tom", dev1.getUsername());
        assertEquals(1L, dev1.getPersonalNumber());
        assertEquals(Stream.of(Employee.Role.SENIOR_DEVELOPER).collect(Collectors.toSet()), dev1.getRoles());
        assertEquals(new HashSet<>(), dev1.getTasksAssigned());
        assertEquals(Stream.of(easy,hard).collect(Collectors.toSet()), dev1.getTaskGroups());

        assertEquals("John", dev2.getUsername());
        assertEquals("J1", dev2.getPassword());
        assertEquals(2L, dev2.getPersonalNumber());
        assertEquals(Stream.of(Employee.Role.SENIOR_DEVELOPER).collect(Collectors.toSet()), dev2.getRoles());
        assertEquals(new HashSet<>(), dev2.getTasksAssigned());
        assertEquals(Stream.of(easy,hard).collect(Collectors.toSet()), dev2.getTaskGroups());

        assertEquals("Alex", dev3.getUsername());
        assertEquals("A1", dev3.getPassword());
        assertEquals(3L, dev3.getPersonalNumber());
        assertEquals(Stream.of(Employee.Role.TEAMLEAD).collect(Collectors.toSet()), dev3.getRoles());
        assertEquals(new HashSet<>(), dev3.getTasksAssigned());
        assertEquals(new HashSet<>(), dev3.getTaskGroups());
    }

    @Test
    public void editAnyEmployeeDataByUserNameTest() {
        EmployeeDto existing = EmployeeDto.builder().userName("Tom").build();
        assertThrows(IllegalArgumentException.class, () -> userDataService.editAnyEmployeeDataByUserName(existing, Action.NEW));
        EmployeeDto eDto2 = EmployeeDto.builder().id(1L).personalNumber(123L).name("John Doe").department("IT").position("Developer")
                .userName("Tom").password("password123").taskGroups(new HashSet<>()).tasksCreated(new HashSet<>()).tasksAssigned(new HashSet<>())
                .roles(new HashSet<>(Set.of(Employee.Role.TEAMLEAD))).nonExpired(true).nonLocked(true).credsNonExpired(true).isEnabled(true).build();

        Employee e1 = Employee.builder().id(dev1.getId()).personalNumber(123L).name("John Doe").department("IT").position("Developer")
                .userName("Tom").taskGroups(new HashSet<>()).tasksCreated(new HashSet<>()).tasksAssigned(new HashSet<>())
                .roles(new HashSet<>(Set.of(Employee.Role.TEAMLEAD))).nonExpired(true).nonLocked(true).credsNonExpired(true).isEnabled(true).build();
        assertEquals(e1, userDataService.editAnyEmployeeDataByUserName(eDto2, Action.EDIT));
    }

    @Test
    public void getEmployeeDataTest() {
        String userName = "Tom";
        assertEquals(dev1, userDataService.getEmployeeData(userName).get());
        assertThrows(IllegalArgumentException.class, () -> userDataService.getEmployeeData("userName")
                .orElseThrow(() -> new IllegalArgumentException("")));
    }

    @Test
    public void getUserDataByEmployeeGroupsTest() {
        String userName = "Tom";
        TaskGroup tg = TaskGroup.builder().name("group").build();
        dev1.getTaskGroups().add(tg);
        entityManager.persist(dev1);
        Employee testEm = Employee.builder().taskGroups(new HashSet<>(Set.of(tg))).build();
        assertEquals(dev1, userDataService.getUserDataByEmployeeGroups(testEm, userName).get());

    }

    @Test
    public void getGroupDevelopersDataTest() {
        entityManager.refresh(dev1);
        entityManager.refresh(dev2);
        assertEquals(Set.of(dev1, dev2), userDataService.getGroupDevelopersData(dev1, hard.getId()));
        assertEquals(Set.of(dev1, dev2), userDataService.getGroupDevelopersData(dev1, easy.getId()));
        assertThrows(AccessDeniedException.class, () -> userDataService.getGroupDevelopersData(dev1, 80L));
    }

    @Test
    public void editSelfDeveloperTest() {
        String uname = "Tom";
//        when(passwordEncoder.encode(anyString())).thenReturn("password");
        EmployeeDto edto = EmployeeDto.builder().userName("Tom_H").name("Tom Holland").password("passw").build();
        assertEquals("Updated", userDataService.editSelfDeveloper(uname,edto));
        assertThrows(AccessDeniedException.class, ()-> userDataService.editSelfDeveloper(uname,edto));
    }

    @Test
    public void editSelfTeamleadTest() {
        String uname = "Tom";
        entityManager.refresh(dev1);
        dev1.getTaskGroups().remove(easy);
        entityManager.persist(dev1);
        EmployeeDto edto = EmployeeDto.builder().userName("Tom_H").name("Tom Holland")
                .password("passw").taskGroups(new HashSet<>(Set.of(easy.getId()))).build();
        assertEquals("Updated", userDataService.editSelfTeamlead(uname,edto));
        assertTrue(dev1.getTaskGroups().contains(easy) && dev1.getTaskGroups().size()==1);
        assertThrows(AccessDeniedException.class, ()-> userDataService.editSelfDeveloper(uname,edto));
    }

    @Test
    public void getUserTasksVerboseTest() {
        String verboseAssigned = "Assigned";
        String verboseCreated =  "Created";
        Task t1 = Task.builder().header("t1").description("descr1").group(easy).build();
        Task t2 = Task.builder().header("t2").description("descr2").group(hard).build();
        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.refresh(dev3);
        entityManager.refresh(dev1);
        dev1.getTasksAssigned().addAll(Set.of(t1,t2));
        dev3.getTasksCreated().addAll(Set.of(t1,t2));
        assertEquals(Set.of(t1,t2), userDataService.getUserTasksVerbose("Tom", verboseAssigned));
        assertEquals(Set.of(t1,t2), userDataService.getUserTasksVerbose("Alex", verboseCreated));
        assertThrows(IllegalArgumentException.class, ()-> userDataService.getUserTasksVerbose("Alex", "WRONG"));
        assertThrows(AccessDeniedException.class, ()-> userDataService.getUserTasksVerbose("Susanna", verboseAssigned));
    }

    @Test
    public void addOrDeleteUserGroupTest() {
        EmlpoyeeGroupActionDto egaDto = EmlpoyeeGroupActionDto.builder()
                .userName(dev1.getUsername()).groupName(easy.getName()).action(Action.NEW).build();
        entityManager.refresh(dev3);
        entityManager.refresh(dev1);
        dev1.getTaskGroups().remove(easy);
        dev3.getTaskGroups().add(easy);
        entityManager.persist(dev1);
        entityManager.persist(dev3);
        assertEquals("{ \"userName\": \"Tom\", \r\n" +
                " \"addedGroup\": \"Easy\",\r\n" +
                " \"currentGroups\": \"[Easy, Hard]\"}", userDataService.addOrDeleteUserGroup(dev3,egaDto));
        egaDto.setAction(Action.DELETE);
        assertEquals("{ \"userName\": \"Tom\", \r\n" +
                " \"deletedGroup\": \"Easy\",\r\n" +
                " \"currentGroups\": \"[Hard]\"}", userDataService.addOrDeleteUserGroup(dev3,egaDto));
        assertEquals("{\"message\": \"User is not in the group\"}", userDataService.addOrDeleteUserGroup(dev3,egaDto));
        egaDto.setAction(Action.NEW);
        egaDto.setGroupName(hard.getName());
        dev3.getTaskGroups().add(hard);
        assertEquals("{\"message\": \"User is in the group already\"}", userDataService.addOrDeleteUserGroup(dev3,egaDto));
        assertThrows(AccessDeniedException.class, ()-> userDataService.addOrDeleteUserGroup(dev2,egaDto));


    }

}