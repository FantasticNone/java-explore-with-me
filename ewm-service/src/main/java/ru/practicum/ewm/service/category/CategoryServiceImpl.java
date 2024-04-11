package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.exceptions.ConflictDataException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CategoriesRepository;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.service.category.CategoryService;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoriesRepository categoriesRepository;
    private final EventsRepository eventsRepository;

    @Override
    @Transactional
    public CategoryDto newCategory(NewCategoryDto newCategoryDto) {
        if (categoriesRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictDataException("Name must be unique.");
        }

        Category category = categoriesRepository.save(CategoryMapper.toCat(newCategoryDto));
        return CategoryMapper.toCatDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long categoryId, NewCategoryDto newCategoryDto) {
        Category category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found"));

        category.setName(newCategoryDto.getName());

        Category newCategory = categoriesRepository.save(category);
        return CategoryMapper.toCatDto(newCategory);
    }
    @Override
    @Transactional
    public void deleteCategory(long categoryId) {
        categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=%d was not found"));
        List<Event> eventsWithCategory = eventsRepository.findEventByCategory(categoryId);
        if (eventsWithCategory.size() > 0) {
            throw new ConflictDataException("The category has events");
        }
        categoriesRepository.deleteById(categoryId);
    }
    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        if (from < 0 || size < 0) {
            throw new IllegalArgumentException("From and Size must be positive or zero");
        }

        List<Category> categories = categoriesRepository.findAll(PageRequest.of(from / size, size)).getContent();

        return CategoryMapper.toCatDtoList(categories);
    }

    @Override
    public CategoryDto getCategory(long catId) {
        Category category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));

        return CategoryMapper.toCatDto(category);
    }
}


