package com.parkmate.session;

import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionSyncRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
import com.parkmate.session.dto.resp.SessionResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface SessionService {
    SessionResponse createSession(Long lotId,SessionCreateRequest request);

    Page<SessionResponse> getSessions(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            SessionFilterParams filterParams
    );

    SessionResponse getSession(String cardUUID);
    SessionResponse updateSession(String cardUUID, SessionUpdateRequest request);
    Long count();
    void deleteSession(String cardUUID);
    Integer syncSessions(Long lotId, List<SessionSyncRequest> requests);
}

