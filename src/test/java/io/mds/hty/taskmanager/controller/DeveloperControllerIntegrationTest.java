package io.mds.hty.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mds.hty.taskmanager.conf.WithMockEmployee;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dto.Action;
import io.mds.hty.taskmanager.model.dto.CommentDto;
import io.mds.hty.taskmanager.model.dto.EmployeeDto;
import io.mds.hty.taskmanager.model.dto.TaskDto;
import io.mds.hty.taskmanager.service.StatisticsService;
import io.mds.hty.taskmanager.service.TaskService;
import io.mds.hty.taskmanager.service.UserDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.mds.hty.taskmanager.model.dao.Employee.Role.SENIOR_DEVELOPER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({SpringExtension.class/*, MockitoExtension.class*/})
@AutoConfigureMockMvc
public class DeveloperControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDataService mockUserDataService;
    @MockBean
    private TaskService mockTaskService;


    @MockBean
    private StatisticsService statisticsService;

    @Value("https://localhost:16116/${api.devPrefix}")
    private String path;

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testGetSelfData() throws Exception {
        Employee employee = Employee.builder().userName("Tom").roles(new HashSet<>(Set.of(SENIOR_DEVELOPER)))
                .password("password").build(); // create a mock employee
        when(mockUserDataService.getEmployeeData(anyString())).thenReturn(Optional.of(employee));
        mockMvc.perform(get(path + "/data"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(employee)))
                .andDo(print());
    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testEditSelfData() throws Exception {
        EmployeeDto employeeDto = EmployeeDto.builder().userName("Tom").roles(Set.of(SENIOR_DEVELOPER))
                .password("password").build();
        String username = "Tom";
        when(mockUserDataService.editSelfDeveloper(username, employeeDto))
                .thenReturn("UPDATED");
        mockMvc.perform(post(path + "/data/{uname}", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("UPDATED"))
                .andDo(print());
    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testGetEmployeeTasks() throws Exception {
        Set<Task> resp = Set.of(Task.builder().id(568974L).build());
        when(mockUserDataService.getUserTasksVerbose("Tom", "Assigned"))
                .thenReturn(resp).thenThrow(new AccessDeniedException("User " + "Tom" + " not found"));
        mockMvc.perform(get(path + "/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
        mockMvc.perform(get(path + "/tasks"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testGetEmployeeFinishedTasks() throws Exception {
        Employee mockPrincipal = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Task> resp = Set.of(Task.builder().id(89632L).build());
        when(mockTaskService.getEmployeeFinishedTasks(eq(mockPrincipal), any()))
                .thenReturn(resp).thenReturn(resp);
        mockMvc.perform(post(path + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode()
                                .putObject("finished")
                                .booleanNode(Boolean.TRUE).asText()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)))
                .andDo(print());
        mockMvc.perform(post(path + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Boolean.FALSE)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)))
                .andDo(print());;
        mockMvc.perform(post(path + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.nullNode().asText()))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testGetEmployeeTasksById() throws Exception {
        Task resp = Task.builder().id(5687L).build();
        when(mockTaskService.getEmployeeTaskById(any(), any()))
                .thenReturn(Optional.of(resp)).thenReturn(Optional.empty());
        mockMvc.perform(get(path + "/task/byid/5687"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)))
                .andDo(print());
        mockMvc.perform(get(path + "/task/byid/5687"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testUpdateEmployeeTask() throws Exception {
        Employee mp = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TaskDto tDto = TaskDto.builder().id(231L).status("STARTED").progress(50).build();
        Task resp = Task.builder().id(5687L).build();
        when(mockTaskService.updateEmployeeTask(eq(mp),eq(231L), any(), eq(50)))
                .thenReturn(Optional.of(resp)).thenReturn(Optional.empty());
        mockMvc.perform(post(path + "/task/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tDto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("UPDATED"))
                .andDo(print());

        mockMvc.perform(post(path + "/task/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tDto)))
                .andExpect(status().isForbidden())
                .andDo(print());
        tDto.setStatus("CREATED");
        mockMvc.perform(post(path + "/task/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tDto)))
                .andExpect(status().isForbidden())
                .andDo(print());

    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testLeaveCommentOnTask() throws Exception {
        Employee mp = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CommentDto cDto = CommentDto.builder().task(588L).header("H").text("T").build();
        when(mockTaskService.commentCud(eq(mp), any(), eq(Action.NEW)))
                .thenReturn("OK").thenThrow(AccessDeniedException.class).thenReturn("OK");
        mockMvc.perform(post(path + "/comment/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"))
                .andDo(print());

        mockMvc.perform(post(path + "/comment/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isForbidden())
                .andDo(print());
        cDto.setTask(null);
        mockMvc.perform(post(path + "/comment/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    @WithMockEmployee(roles = {"SENIOR_DEVELOPER"}, username = "Tom", password = "password")
    public void testEditCommentOnTask() throws Exception {
        CommentDto cDto = CommentDto.builder().task(588L).header("H").text("T").build();
        when(mockTaskService.commentCud(any(), any(), eq(Action.EDIT)))
                .thenReturn("OK").thenThrow(AccessDeniedException.class).thenReturn("OK");
        mockMvc.perform(post(path + "/comment/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"))
                .andDo(print());

        mockMvc.perform(post(path + "/comment/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isForbidden())
                .andDo(print());
        cDto.setHeader(null);
        cDto.setText(null);
        mockMvc.perform(post(path + "/comment/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cDto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }





}
