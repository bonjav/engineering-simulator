package com.datacenterflow.cad.domain.port.out;

import com.datacenterflow.auth.domain.model.UserId;
import com.datacenterflow.project.domain.model.ProjectId;

/**
 * Allows the CAD domain to verify project membership without coupling to the project domain internals.
 */
public interface ProjectAccessPort {

    boolean isMember(ProjectId projectId, UserId userId);
}
