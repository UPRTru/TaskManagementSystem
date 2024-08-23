package TaskManagementSystem.dto;

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
public class ResultCommentDto {
    private String text;
    private UserDto author;
    private Date date;
}
