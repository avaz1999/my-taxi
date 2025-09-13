package my.taxi.entities.car;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;


/**
 * Created by Avaz Absamatov
 * Date: 9/12/2025
 */
@Entity
@Table(name = "cars")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Car extends BaseEntity {
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "year_of_issue", nullable = false)
    private Integer yearOfIssue;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "color", nullable = false)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    private Model model;

}
