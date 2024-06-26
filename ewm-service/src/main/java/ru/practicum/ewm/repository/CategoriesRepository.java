package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.category.Category;

public interface CategoriesRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Category findCategoryByName(String name);
}
