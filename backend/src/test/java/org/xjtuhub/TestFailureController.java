package org.xjtuhub;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xjtuhub.common.api.BusinessException;
import org.xjtuhub.common.validation.SnowflakeId;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Validated
@RequestMapping("/api/v1/test")
public class TestFailureController {

    @GetMapping("/failures/validation")
    public String validationFailure(@RequestParam(defaultValue = "101") @Min(1) @Max(100) int pageSize) {
        return String.valueOf(pageSize);
    }

    @GetMapping("/failures/business")
    public String businessFailure() {
        throw new BusinessException(NOT_FOUND, "CONTENT_NOT_FOUND", "Content not found.");
    }

    @GetMapping("/ids/{contentId}")
    public String idCheck(@PathVariable @SnowflakeId(message = "contentId must be a numeric string.") String contentId) {
        return contentId;
    }
}
