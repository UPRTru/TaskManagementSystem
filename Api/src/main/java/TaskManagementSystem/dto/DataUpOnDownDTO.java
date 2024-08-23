package TaskManagementSystem.dto;

import TaskManagementSystem.model.SortByDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataUpOnDownDTO {
    private SortByDate SortByDate;
}
