package io.mds.hty.taskmanager.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.mds.hty.taskmanager.model.dao.Comment;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.TaskGroup;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    @NotNull
    private Long id;

    private String group;

    private Long employeeCreated;

    private String employeeAssigned;

    private String header;

    private String description;

    private Integer priority;

    private Integer complexity;

    private Instant dateCreated;

    private Instant plannedStart;

    private Instant plannedFinish;

    private String status;

    private Set<Comment> comments;

    private Integer progress;

    private Instant started;

    private Boolean isCompleted;

    private Instant completed;

}

