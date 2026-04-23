package org.xjtuhub.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.xjtuhub.common.api.BusinessException;

@Service
public class CampusScanService {
    public void throwReserved() {
        throw new BusinessException(
                HttpStatus.NOT_IMPLEMENTED,
                "AUTH_CAMPUS_SCAN_RESERVED",
                "Campus scan login is reserved for future integration."
        );
    }
}
