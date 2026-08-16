package com.Final_Project.Event_Booking.service.impl;

import com.Final_Project.Event_Booking.exception.custom.ResourceAlreadyExistsException;
import com.Final_Project.Event_Booking.exception.custom.ResourcesNotFoundException;
import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.CategoryResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.mapper.CategoryMapper;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category with name:"+request.getName()+" already exists!");
        }
        Category category=categoryMapper.toEntity(request);
        Category savedCategory=categoryRepository.save(category);
        return categoryMapper.toResponseDTO(savedCategory);
    }

    @Override
    public CategoryResponseDTO getCategory(Long id) {
        Category category=categoryRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Category with id:"+id+" not found!"));
        return categoryMapper.toResponseDTO(category);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category=categoryRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Category with id:"+id+" not found!"));
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResourceAlreadyExistsException("Category with name "+request.getName()+" already exists!");
        }

        categoryMapper.updateEntity(request,category);
        Category updatedCategory=categoryRepository.save(category);
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category=categoryRepository.findById(id)
                .orElseThrow(()->new ResourcesNotFoundException("Category with id:"+id+" not found!"));
        categoryRepository.delete(category);
    }
}
