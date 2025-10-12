package my.taxi.entities.file;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my.taxi.base.BaseEntity;

/**
 * Created by: Avaz Absamatov
 * 11.09.2025
 */
@Entity
@Table(name = "FILE_ITEM", indexes = {
        @Index(name = "idx_file_hash", columnList = "hashId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_hash", columnNames = {"HASH_ID"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileItem extends BaseEntity {
    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "HASH_ID")
    private String hashId;
}
