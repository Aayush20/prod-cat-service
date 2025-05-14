package org.example.prodcatservice.dtos.product.responseDtos;

import io.swagger.v3.oas.annotations.media.Schema;

public class DeleteProductResponseDto {

    @Schema(description = "Success or failure", example = "SUCCESS")
    private ResponseStatus status;

    @Schema(description = "Detailed message", example = "Product deleted successfully")
    private String message;

    @Schema(description = "Optional error code if failure", example = "1002")
    private Long errorCode;


    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
