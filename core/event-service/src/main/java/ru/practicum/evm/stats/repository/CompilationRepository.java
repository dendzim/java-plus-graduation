package ru.practicum.evm.stats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evm.stats.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

	Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);
}
