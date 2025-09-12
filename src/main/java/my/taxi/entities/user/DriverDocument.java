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
@Table(name = "dirver_documents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_driver_documents_driver"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 32)
    private DocumentType docType;

    @OneToOne(fetch = FetchType.LAZY)
    private FileItem image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private DocumentStatus status;




}
