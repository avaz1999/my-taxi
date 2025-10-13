package my.taxi.exception;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Avaz Absamatov
 * Date: 9/25/2025
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError implements Serializable {
    private Integer code;
    private String message;
    private List<String> details;
}
