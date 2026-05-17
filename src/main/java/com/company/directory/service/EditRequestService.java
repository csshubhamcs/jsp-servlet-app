package com.company.directory.service;

import com.company.directory.dao.EditRequestDao;
import com.company.directory.model.EditRequest;
import com.company.directory.model.RequestStatus;
import com.company.directory.service.exception.AppException;

import java.util.List;

/**
 * Business logic for the profile edit-request workflow.
 *
 * <p>When a user's profile is locked they can submit an {@link EditRequest}
 * explaining why they need to change it.  An Admin or Super Admin then either
 * approves (which unlocks the profile via {@link UserService#unlockProfile})
 * or rejects the request.
 *
 * <p>The service prevents duplicate pending requests and ensures only a
 * {@code PENDING} request can be resolved.
 */
public class EditRequestService {

    private final EditRequestDao requestDao;
    private final UserService userService;

    public EditRequestService(EditRequestDao requestDao, UserService userService) {
        this.requestDao = requestDao;
        this.userService = userService;
    }

    /** Returns all pending requests, oldest first (for the admin queue view). */
    public List<EditRequest> pendingRequests() {
        return requestDao.findByStatusOldestFirst(RequestStatus.PENDING);
    }

    /** Returns {@code true} if the user already has an unresolved pending request. */
    public boolean hasPendingRequest(long requesterId) {
        return requestDao.existsPendingForRequester(requesterId);
    }

    /**
     * Submits a new edit request.
     *
     * @throws com.company.directory.service.exception.AppException if the user already has a pending request
     */
    public EditRequest raiseRequest(long requesterId, String reason) {
        if (requestDao.existsPendingForRequester(requesterId)) {
            throw new AppException("You already have a pending edit request.");
        }
        EditRequest request = new EditRequest();
        request.setRequesterId(requesterId);
        request.setReason(reason);
        request.setStatus(RequestStatus.PENDING);
        return requestDao.insert(request);
    }

    /**
     * Approves the request: marks it APPROVED and unlocks the requester's profile.
     *
     * @param resolverId the id of the admin who is approving
     * @throws com.company.directory.service.exception.AppException if the request does not exist or is already resolved
     */
    public void approve(long requestId, long resolverId) {
        EditRequest request = loadPending(requestId);
        requestDao.resolve(requestId, RequestStatus.APPROVED, resolverId);
        userService.unlockProfile(request.getRequesterId());
    }

    /**
     * Rejects the request: marks it REJECTED.  The profile remains locked.
     *
     * @param resolverId the id of the admin who is rejecting
     * @throws com.company.directory.service.exception.AppException if the request does not exist or is already resolved
     */
    public void reject(long requestId, long resolverId) {
        loadPending(requestId);
        requestDao.resolve(requestId, RequestStatus.REJECTED, resolverId);
    }

    private EditRequest loadPending(long requestId) {
        EditRequest request = requestDao.findById(requestId);
        if (request == null) {
            throw new AppException("Edit request not found.");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new AppException("This request has already been resolved.");
        }
        return request;
    }
}
