package ru.practicum.evm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evm.stats.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	boolean existsByName(String name);
}
