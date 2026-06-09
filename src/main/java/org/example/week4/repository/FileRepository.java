package org.example.week4.repository;

import org.example.week4.domain.File;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FileRepository {
    void init(List<File> files);

    File save(File file);

    void deleteById(Long id);

    Optional<File> findById(Long id);
}
