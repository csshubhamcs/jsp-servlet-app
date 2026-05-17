package com.company.directory.service;

import com.company.directory.dao.EditRequestDao;
import com.company.directory.model.EditRequest;
import com.company.directory.model.RequestStatus;
import com.company.directory.service.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditRequestServiceTest {

    @Mock EditRequestDao requestDao;
    @Mock UserService userService;
    EditRequestService service;

    @BeforeEach
    void setUp() {
        service = new EditRequestService(requestDao, userService);
        lenient().when(requestDao.insert(any(EditRequest.class))).thenAnswer(i -> i.getArgument(0));
    }

    private EditRequest pending(long id, long requesterId) {
        EditRequest r = new EditRequest();
        r.setId(id);
        r.setRequesterId(requesterId);
        r.setStatus(RequestStatus.PENDING);
        return r;
    }

    @Test
    void raiseRequestCreatesPendingRequest() {
        when(requestDao.existsPendingForRequester(5L)).thenReturn(false);
        EditRequest created = service.raiseRequest(5L, "Moved teams");
        assertEquals(RequestStatus.PENDING, created.getStatus());
        assertEquals("Moved teams", created.getReason());
        assertEquals(5L, created.getRequesterId());
    }

    @Test
    void raiseRequestRejectedWhenOneAlreadyPending() {
        when(requestDao.existsPendingForRequester(5L)).thenReturn(true);
        assertThrows(AppException.class, () -> service.raiseRequest(5L, "again"));
        verify(requestDao, never()).insert(any());
    }

    @Test
    void approveMarksApprovedAndUnlocksRequester() {
        when(requestDao.findById(9L)).thenReturn(pending(9L, 5L));
        service.approve(9L, 2L);
        verify(requestDao).resolve(9L, RequestStatus.APPROVED, 2L);
        verify(userService).unlockProfile(5L);
    }

    @Test
    void rejectMarksRejectedAndDoesNotUnlock() {
        when(requestDao.findById(9L)).thenReturn(pending(9L, 5L));
        service.reject(9L, 2L);
        verify(requestDao).resolve(9L, RequestStatus.REJECTED, 2L);
        verify(userService, never()).unlockProfile(anyLong());
    }

    @Test
    void approveRejectsAlreadyResolvedRequest() {
        EditRequest r = pending(9L, 5L);
        r.setStatus(RequestStatus.APPROVED);
        when(requestDao.findById(9L)).thenReturn(r);
        assertThrows(AppException.class, () -> service.approve(9L, 2L));
    }

    @Test
    void approveMissingRequestThrows() {
        when(requestDao.findById(9L)).thenReturn(null);
        assertThrows(AppException.class, () -> service.approve(9L, 2L));
    }
}
