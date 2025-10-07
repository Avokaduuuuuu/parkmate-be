package com.parkmate.session;

import com.parkmate.session.dto.req.SessionCreateRequest;
import com.parkmate.session.dto.req.SessionUpdateRequest;
import com.parkmate.session.dto.resp.SessionResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SessionService {
    SessionResponse createSession(Long lotId,SessionCreateRequest request);

    Page<SessionResponse> getSessions(
            int page,
            int size,
            String sortBy,
            String sortOrder
    );

    SessionResponse getSession(String cardUUID);
    SessionResponse updateSession(String cardUUID, SessionUpdateRequest request);
}

