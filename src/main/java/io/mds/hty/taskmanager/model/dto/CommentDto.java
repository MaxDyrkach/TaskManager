package io.mds.hty.taskmanager.model.dto;

import io.mds.hty.taskmanager.model.dao.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {


    private Long id;
    private Employee user;
    @NotNull
    private Long task;
    private Instant dateTime;
    @NotBlank
    private String header;
    private Integer onProgress;
    @NotBlank
    private String text;

}
