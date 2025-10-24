package com.nhnacademy.nhnmartcs.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler{

    /**
     * Handle uncaught exceptions and prepare the error view.
     *
     * Adds the caught exception to the model under the attribute "exception" and selects the logical view name "error".
     *
     * @param ex    the exception that was thrown
     * @param model the model used to expose attributes to the view
     * @return      the logical view name "error"
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("exception", ex);
        return "error";
    }
}