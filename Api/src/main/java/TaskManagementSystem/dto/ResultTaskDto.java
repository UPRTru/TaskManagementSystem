package TaskManagementSystem.dto;

import TaskManagementSystem.model.Priority;
import TaskManagementSystem.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;

@Jacksonized
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultTaskDto {
    private String name;
    private String description;
    private Status status;
    private Priority priority;
    private UserDto author;
    private UserDto executor;
    private Date date;
}
