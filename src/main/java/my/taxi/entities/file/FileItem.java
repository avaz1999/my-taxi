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
@Table(name = "file_item", indexes = {
        @Index(name = "idx_file_hash", columnList = "hash_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_file_hash", columnNames = {"hash_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileItem extends BaseEntity {
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "hash_id")
    private String hashId;
}
