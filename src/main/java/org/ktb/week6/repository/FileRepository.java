package org.ktb.week6.repository;

import org.ktb.week6.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    File save(File file);

    void deleteById(Long id);

    Optional<File> findById(Long id);
}
