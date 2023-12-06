package io.mds.hty.taskmanager.model.dto;

import io.mds.hty.taskmanager.common.Action;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmlpoyeeGroupActionDto {

    @NotNull
    private String userName;
    @NotNull
    private String groupName;
    @NotNull
    private Action action;
}
