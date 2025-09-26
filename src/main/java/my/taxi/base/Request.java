package my.taxi.base;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Avaz Absamatov
 * Date: 9/25/2025
 */
@Getter
@Builder
public class Request<R extends Serializable> implements Serializable {
    @JsonView
    private String id;
    @Valid
    private R params;

    @Builder
    public Request(String id, R params) {
        if (id == null) this.id = UUID.randomUUID().toString();
        else this.id = id;
        this.params = params;
    }

    public void setId(String id) {
        if (id == null) this.id = UUID.randomUUID().toString();
        else this.id = id;
    }
}
