package my.taxi.entities.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "file_item")
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
