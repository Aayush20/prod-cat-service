package org.example.prodcatservice.dtos.common;

public class BaseResponse<T> {
    private String status;
    private String message;
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
