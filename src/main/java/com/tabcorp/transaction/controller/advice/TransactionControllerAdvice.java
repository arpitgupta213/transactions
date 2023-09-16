package com.tabcorp.transaction.controller.advice;

import com.tabcorp.transaction.common.InvalidTransactionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@ControllerAdvice
@Log4j2
public class TransactionControllerAdvice {

    private static final String DEFAULT_TITLE = "Request failed";

    @ExceptionHandler(HttpClientErrorException.class)
    ProblemDetail handleHttpClientException(final HttpServletRequest request, final HttpClientErrorException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(e.getStatusCode());
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("Error getting data from client");
        return problemDetail;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    ProblemDetail handleHttpClientException(final HttpServletRequest request, final InvalidTransactionException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("Transaction is invalid");
        return problemDetail;
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    ProblemDetail handleResultMappingException(final HttpServletRequest request, final InvalidDataAccessResourceUsageException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail("Data for given parameter is not found");
        problemDetail.setTitle("Data not found");
        return problemDetail;
    }

    @ExceptionHandler(JpaSystemException.class)
    ProblemDetail handleResultMappingException(final HttpServletRequest request, final JpaSystemException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail("Data for given parameter is not found");
        problemDetail.setTitle("Data not found");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(final HttpServletRequest request, final MethodArgumentNotValidException e) {
        StringBuilder err = new StringBuilder();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            if(err.length() > 0) {
                err.append(", ");
            }
            err.append(errorMessage);
        });
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(err.toString());
        problemDetail.setTitle("Request data is not correct");
        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupportedExceptions(final HttpServletRequest request, final HttpRequestMethodNotSupportedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("Method is not allowed");
        return problemDetail;
    }

    @ExceptionHandler(IOException.class)
    public ProblemDetail handleFileExceptions(final HttpServletRequest request, final IOException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setDetail(e.getMessage());
        problemDetail.setTitle("Bulk transaction failed");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(final HttpServletRequest request, final Exception e) {
        log.error("Error getting transaction data", e);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setDetail("Server error. Check logs for details");
        problemDetail.setTitle(DEFAULT_TITLE);
        return problemDetail;
    }

}
