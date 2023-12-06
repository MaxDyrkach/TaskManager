package io.mds.hty.taskmanager.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"user", "task"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"id"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private Employee user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "date_time")
    private Instant dateTime;

    @Column(name = "header")
    private String header;

    @Column(name = "on_progress")
    private Integer onProgress;

    @Column(name = "text")
    private String text;

    @Column(name = "last_update")
    private Instant lastUpdate;

}
