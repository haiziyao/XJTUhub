package org.xjtuhub.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xjtuhub.common.api.BusinessException;

@Service
public class AdminService {
    public void throwCampusVerificationReserved() {
        throw new BusinessException(
                HttpStatus.NOT_IMPLEMENTED,
                "ADMIN_CAMPUS_VERIFICATION_RESERVED",
                "Admin campus verification is reserved for a later admin implementation."
        );
    }
}
