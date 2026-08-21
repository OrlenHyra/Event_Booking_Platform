package com.Final_Project.Event_Booking.controller;

import com.Final_Project.Event_Booking.model.dto.request.CategoryRequestDTO;
import com.Final_Project.Event_Booking.model.dto.response.CategoryResponseDTO;
import com.Final_Project.Event_Booking.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO request
            ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.getAllCategories());
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<CategoryResponseDTO>getCategory(
            @PathVariable Long categoryId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.getCategory(categoryId));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequestDTO request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId
    ){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
