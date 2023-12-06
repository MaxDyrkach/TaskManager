package io.mds.hty.taskmanager.model.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //On create
    @JsonIncludeProperties(value = {"name"})
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private TaskGroup group;

    @JsonIncludeProperties({"personalNumber", "userName", "roles"})
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_created_id")
    private Employee employeeCreated;

    @JsonIncludeProperties({"personalNumber", "userName", "roles"})
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_assigned_id")
    private Employee employeeAssigned;

    @Column(name = "header")
    private String header;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    @Max(10)
    @Min(0)
    private Integer priority;

    @Column(name = "complexity")
    @Max(10)
    @Min(1)
    private Integer complexity;

    @Column(name = "date_created")
    private Instant dateCreated;

    @Column(name = "planned_start")
    private Instant plannedStart;

    @Column(name = "planned_finish")
    private Instant plannedFinish;

    //Progress
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;

    @JsonIgnoreProperties(value = {"id", "appUserCreated", "appUserAssigned"}, ignoreUnknown = true)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "task", cascade = CascadeType.PERSIST)
    private Set<Comment> comments = new HashSet<>();

    @Column(name = "progress")
    @Max(100)
    @Min(0)
    private Integer progress;

    @Column(name = "started")
    private Instant started;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "completed")
    private Instant completed;

    public enum TaskStatus {CREATED, ASSIGNED, STARTED, PAUSED, MARKED_FINISHED, FINISH_APPROVED}

}
