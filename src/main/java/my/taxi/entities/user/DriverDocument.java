package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.base.BaseEntity;
import my.taxi.entities.user.enums.DocumentStatus;
import my.taxi.entities.user.enums.DocumentType;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(
        name = "DRIVER_DOCUMENTS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_driver_doc_type", columnNames = {"DRIVER_ID", "DOC_TYPE"}
        ),
        indexes = {
                @Index(name = "idx_driver_documents_driver", columnList = "driverId"),
                @Index(name = "idx_driver_documents_status", columnList = "status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverDocument extends BaseEntity {

    @Column(name = "DRIVER_ID")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "DOC_TYPE", nullable = false, length = 32)
    private DocumentType docType;

    @Column(name = "IMAGE_ID")
    private Long image;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 16)
    private DocumentStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) this.status = DocumentStatus.PENDING;
    }


}
