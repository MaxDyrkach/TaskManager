package io.mds.hty.taskmanager.model.dto;

import io.mds.hty.taskmanager.model.dao.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonStatDto {

    private PeriodDto period;
    private Long employeeCount;
    private Map<Employee.Role, Long> byRole;
    private Long taskCount;
    private Long finishedTasksCount;
    private Long notFinishedTasksCount;
    private Long taskGroupCount;
    private Double averageTasksAssignedOnEmployee;
    private Double averageTasksOnEmployeeCreated;
    private Double averageFinishedTasksOnEmployee;
    private Double averageTimeOnTasksWorkingDays;


}
