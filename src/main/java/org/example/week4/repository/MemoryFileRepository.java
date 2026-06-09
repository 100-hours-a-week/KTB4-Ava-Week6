package org.example.week4.repository;

import org.example.week4.domain.File;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryFileRepository implements FileRepository {

    private final ConcurrentHashMap<Long, File> files = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public void init(List<File> initialFiles) {
        files.clear();

        long maxId = 0;
        for (File file : initialFiles) {
            files.put(file.getId(), file);
            maxId = Math.max(maxId, file.getId());
        }

        counter.set(maxId + 1);
    }

    @Override
    public File save(File file) {
        Long id = file.getId();

        if (id == null) {
            id = counter.getAndIncrement();
        } else {
            Long savedId = id;
            counter.updateAndGet(current -> Math.max(current, savedId + 1));
        }

        File savedFile = new File(
                id,
                file.getPath(),
                file.getCategory(),
                file.getUploaderId()
        );
        files.put(id, savedFile);
        return savedFile;
    }

    @Override
    public void deleteById(Long id) {
        files.remove(id);
    }

    public Optional<File> findById(Long id) {
        return Optional.ofNullable(files.get(id));
    }

}
