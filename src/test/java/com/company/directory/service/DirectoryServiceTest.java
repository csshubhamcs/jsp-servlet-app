package com.company.directory.service;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectoryServiceTest {

    @Mock AppUserDao dao;

    @Test
    void blankQuerySearchesWithEmptyTermFromPageZero() {
        DirectoryService service = new DirectoryService(dao);
        when(dao.countActiveMatching("")).thenReturn(3L);
        when(dao.searchActive(eq(""), eq(0), anyInt())).thenReturn(List.of(new AppUser()));

        Page<AppUser> page = service.search("   ", 0);

        assertEquals(1, page.getItems().size());
        assertEquals(3L, page.getTotalItems());
        verify(dao).searchActive("", 0, DirectoryService.PAGE_SIZE);
    }

    @Test
    void nullQueryIsTreatedAsBlank() {
        DirectoryService service = new DirectoryService(dao);
        when(dao.countActiveMatching("")).thenReturn(0L);
        when(dao.searchActive(eq(""), anyInt(), anyInt())).thenReturn(List.of());

        Page<AppUser> page = service.search(null, 0);

        assertTrue(page.getItems().isEmpty());
        verify(dao).searchActive("", 0, DirectoryService.PAGE_SIZE);
    }

    @Test
    void nonBlankQueryIsTrimmedAndLowercased() {
        DirectoryService service = new DirectoryService(dao);
        when(dao.countActiveMatching("doe")).thenReturn(1L);
        when(dao.searchActive(eq("doe"), anyInt(), anyInt())).thenReturn(List.of(new AppUser()));

        service.search("  Doe  ", 0);

        verify(dao).searchActive("doe", 0, DirectoryService.PAGE_SIZE);
    }

    @Test
    void pageOffsetIsComputedFromPageNumber() {
        DirectoryService service = new DirectoryService(dao);
        when(dao.countActiveMatching("")).thenReturn(100L);
        when(dao.searchActive(eq(""), anyInt(), anyInt())).thenReturn(List.of());

        service.search("", 2);

        verify(dao).searchActive("", 2 * DirectoryService.PAGE_SIZE, DirectoryService.PAGE_SIZE);
    }

    @Test
    void negativePageIsClampedToZero() {
        DirectoryService service = new DirectoryService(dao);
        when(dao.countActiveMatching("")).thenReturn(0L);
        when(dao.searchActive(eq(""), anyInt(), anyInt())).thenReturn(List.of());

        Page<AppUser> page = service.search("", -5);

        assertEquals(0, page.getPageNumber());
        verify(dao).searchActive("", 0, DirectoryService.PAGE_SIZE);
    }
}
