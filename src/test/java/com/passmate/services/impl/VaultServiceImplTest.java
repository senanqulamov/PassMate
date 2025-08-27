package com.passmate.services.impl;

import com.passmate.models.Category;
import com.passmate.models.Vault;
import com.passmate.services.VaultService;
import com.passmate.services.exceptions.CategoryNotFoundException;
import com.passmate.services.exceptions.DuplicateCategoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VaultServiceImplTest {

    private VaultService service;

    @BeforeEach
    void setUp() {
        service = new VaultServiceImpl(new Vault());
    }

    @Test
    void createCategory_TrimsAndStores() {
        Category c = service.createCategory("  Work  ");
        assertNotNull(c.getId());
        assertEquals("Work", c.getName());
        assertEquals(1, service.getCategories().size());
    }

    @Test
    void createCategory_Blank_Throws() {
        assertThrows(IllegalArgumentException.class, () -> service.createCategory("   "));
    }

    @Test
    void createCategory_DuplicateIgnoreCase_Throws() {
        service.createCategory("personal");
        assertThrows(DuplicateCategoryException.class, () -> service.createCategory("Personal"));
    }

    @Test
    void deleteCategory_Removes() {
        Category c = service.createCategory("Mails");
        service.deleteCategory(c.getId());
        assertTrue(service.getCategories().isEmpty());
    }

    @Test
    void deleteCategory_NotFound_Throws() {
        assertThrows(CategoryNotFoundException.class, () -> service.deleteCategory(UUID.randomUUID()));
    }

    @Test
    void getCategories_SortedIgnoreCase() {
        service.createCategory("zeta");
        service.createCategory("Alpha");
        service.createCategory("beta");
        List<Category> list = service.getCategories();
        assertEquals(List.of("Alpha", "beta", "zeta"), list.stream().map(Category::getName).toList());
    }

    @Test
    void renameCategory_UpdatesName() {
        Category a = service.createCategory("Home");
        Category updated = service.renameCategory(a.getId(), "  Family  ");
        assertEquals("Family", updated.getName());
        assertEquals("Family", service.findById(a.getId()).orElseThrow().getName());
    }

    @Test
    void renameCategory_DuplicateIgnoreCase_Throws() {
        Category a = service.createCategory("Alpha");
        Category b = service.createCategory("Beta");
        assertThrows(DuplicateCategoryException.class, () -> service.renameCategory(b.getId(), "alpha"));
    }
}
