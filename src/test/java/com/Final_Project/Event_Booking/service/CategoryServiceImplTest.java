package com.Final_Project.Event_Booking.service;

import com.Final_Project.Event_Booking.exception.custom.ResourceAlreadyExistsException;
import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.CategoryResponseDTO;
import com.Final_Project.Event_Booking.model.entity.Category;
import com.Final_Project.Event_Booking.model.mapper.CategoryMapper;
import com.Final_Project.Event_Booking.repository.CategoryRepository;
import com.Final_Project.Event_Booking.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequestDTO requestDTO;
    private Category category;

    @BeforeEach
    void setUp() {
        requestDTO = CategoryRequestDTO.builder()
                .name("test category")
                .build();

        category = Category.builder()
                .id(1L)
                .name("test category")
                .build();
    }

    @Test
    void shouldCreateCategory() {
        CategoryResponseDTO responseDTO = CategoryResponseDTO.builder()
                .id(1L)
                .name("test category")
                .build();

        when(categoryRepository.existsByName("test category")).thenReturn(false);
        when(categoryMapper.toEntity(requestDTO)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDTO(category)).thenReturn(responseDTO);

        CategoryResponseDTO response = categoryService.createCategory(requestDTO);

        assertThat(response).isEqualTo(responseDTO);
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldNotCreateCategoryWithExistingName() {
        when(categoryRepository.existsByName("test category")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(requestDTO))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void shouldGetCategory() {
        CategoryResponseDTO responseDTO = CategoryResponseDTO.builder()
                .id(1L)
                .name("test category")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponseDTO(category)).thenReturn(responseDTO);

        CategoryResponseDTO response = categoryService.getCategory(1L);

        assertThat(response).isEqualTo(responseDTO);
    }

    @Test
    void shouldReturnAllCategories() {
        Category secondCategory = Category.builder()
                .id(2L)
                .name("second category")
                .build();

        CategoryResponseDTO firstResponse = CategoryResponseDTO.builder()
                .id(1L)
                .name("test category")
                .build();

        CategoryResponseDTO secondResponse = CategoryResponseDTO.builder()
                .id(2L)
                .name("second category")
                .build();

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, secondCategory));

        when(categoryMapper.toResponseDTO(category))
                .thenReturn(firstResponse);

        when(categoryMapper.toResponseDTO(secondCategory))
                .thenReturn(secondResponse);

        List<CategoryResponseDTO> response = categoryService.getAllCategories();

        assertThat(response).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void shouldUpdateCategory() {
        CategoryRequestDTO updateRequest = CategoryRequestDTO.builder()
                .name("updated category")
                .build();

        CategoryResponseDTO responseDTO = CategoryResponseDTO.builder()
                .id(1L)
                .name("updated category")
                .build();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameAndIdNot("updated category", 1L))
                .thenReturn(false);

        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponseDTO(category)).thenReturn(responseDTO);

        CategoryResponseDTO response =
                categoryService.updateCategory(1L, updateRequest);

        assertThat(response).isEqualTo(responseDTO);
        verify(categoryMapper).updateEntity(updateRequest, category);
    }

    @Test
    void shouldNotUpdateCategoryWithExistingName() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.existsByNameAndIdNot("test category", 1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                categoryService.updateCategory(1L, requestDTO))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }
}