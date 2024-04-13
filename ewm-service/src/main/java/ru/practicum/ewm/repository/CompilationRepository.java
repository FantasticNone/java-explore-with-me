package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("Select c from compilation c " +
            "Where c.pinned = :pinned")
    List<Compilation> findAllWherePinned(List<Boolean> pinnedFilter, Pageable pageable);
}
