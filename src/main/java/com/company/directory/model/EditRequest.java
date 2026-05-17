package com.company.directory.model;

import java.time.LocalDateTime;

/**
 * A request submitted by a user whose profile is locked, asking an admin to
 * unlock it so they can make changes.
 *
 * <p>Lifecycle: {@code PENDING} → {@code APPROVED} or {@code REJECTED}.
 * Once resolved the status never changes again (enforced in
 * {@link com.company.directory.service.EditRequestService}).
 *
 * <p>{@code requesterName} is a denormalised display value joined from
 * {@code app_user.full_name} at query time — it is not stored in the table.
 */
public class EditRequest {

    private Long id;
    private Long requesterId;
    private String requesterName;   // joined from app_user for display
    private String reason;
    private RequestStatus status = RequestStatus.PENDING;
    private LocalDateTime createdAt;
    private Long resolvedById;
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getResolvedById() { return resolvedById; }
    public void setResolvedById(Long resolvedById) { this.resolvedById = resolvedById; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
