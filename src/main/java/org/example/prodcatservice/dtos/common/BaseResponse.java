package org.example.prodcatservice.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic API response wrapper")
public class BaseResponse<T> {
    @Schema(description = "Operation status", example = "SUCCESS")
    private String status;

    @Schema(description = "Descriptive message", example = "Product created successfully")
    private String message;

    @Schema(description = "Payload of the response")
    private T data;


    public BaseResponse() {
    }

    public BaseResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>("SUCCESS", message, data);
    }

    public static <T> BaseResponse<T> failure(String message) {
        return new BaseResponse<>("FAILURE", message, null);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
