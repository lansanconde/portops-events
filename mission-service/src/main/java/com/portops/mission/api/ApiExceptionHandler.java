package com.portops.mission.api;

import com.portops.mission.domain.IllegalStatusTransitionException;
import com.portops.mission.domain.MissionNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Erreurs au format RFC 9457 (ProblemDetail), un code HTTP par cause métier. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MissionNotFoundException.class)
    ProblemDetail notFound(MissionNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalStatusTransitionException.class)
    ProblemDetail illegalTransition(IllegalStatusTransitionException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concurrentUpdate(OptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La mission a été modifiée en parallèle, rechargez-la puis réessayez");
    }
}
