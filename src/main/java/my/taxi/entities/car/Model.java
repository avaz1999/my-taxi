package my.taxi.entities.car;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;
import my.taxi.entities.file.FileItem;

/**
 * Created by Avaz Absamatov
 * Date: 9/12/2025
 */
@Entity
@Table(name = "car_models")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Model extends BaseEntity {
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @OneToOne
    private FileItem icon;
}
