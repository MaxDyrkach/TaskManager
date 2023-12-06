package io.mds.hty.taskmanager.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private PeriodDto period;
    private Long tasksInWorkForNow;
    private Long tasksStartedInPeriod;
    private Long tasksFinishedInPeriod;
    private Long tasksStartedAndFinishedInPeriod;
    private Double averageCountDaysOnTask;
    private Double averageTasksPriority;
    private Double averageTasksComplexity;
    private Double efficiency;
    private Double value;


}
