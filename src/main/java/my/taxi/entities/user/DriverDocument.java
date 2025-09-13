package my.taxi.entities.user;

import jakarta.persistence.*;
import lombok.*;
import my.taxi.base.BaseEntity;
import my.taxi.entities.file.FileItem;
import my.taxi.entities.user.enums.DocumentStatus;
import my.taxi.entities.user.enums.DocumentType;

/**
 * Created by Avaz Absamatov
 * Date: 9/10/2025
 */
@Entity
@Table(
        name = "driver_documents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_driver_doc_type", columnNames = {"driver_id", "doc_type"}
        ),
        indexes = {
                @Index(name = "idx_driver_documents_driver", columnList = "driver_id"),
                @Index(name = "idx_driver_documents_status", columnList = "status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_driver_documents_driver"))
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 32)
    private DocumentType docType;

    @OneToOne(fetch = FetchType.LAZY)
    private FileItem image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private DocumentStatus status;

    @PrePersist
    public void prePersist() {
        if(status == null) this.status = DocumentStatus.PENDING;
    }



}
