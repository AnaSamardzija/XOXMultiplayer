package rs.ac.ni.pmf.ana.XOXMultiplayer.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.exception.ResourceNotFoundException;
import rs.ac.ni.pmf.ana.XOXMultiplayer.model.dto.ErrorDto;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler
{
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorDto handleResourceNotFoundException(final ResourceNotFoundException ex)
	{
		return ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message(ex.getMessage())
		               .build();
	}

	@ExceptionHandler(DuplicateResourceException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorDto handleDuplicateResourceException(final DuplicateResourceException ex)
	{
		return ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message(ex.getMessage())
		               .build();
	}

	@ExceptionHandler(InvalidOperationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorDto handleInvalidOperationException(final InvalidOperationException ex)
	{
		return ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message(ex.getMessage())
		               .build();
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorDto> handleResponseStatusException(final ResponseStatusException ex)
	{
		final ErrorDto error =  ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message(ex.getReason())
		               .build();

		return ResponseEntity.status(ex.getStatusCode()).body(error);
	}

	// Poruke su bez imena polja ispred, jer ih front prikazuje direktno korisniku
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDto handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex)
	{
		final String message = ex.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.collect(Collectors.joining("; "));

		return ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message(message)
		               .build();
	}

	// Bez ovoga bi pogrešna lozinka vratila Spring-ovu poruku „Bad credentials"
	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorDto handleBadCredentialsException(final BadCredentialsException ex)
	{
		return ErrorDto.builder()
		               .timestamp(OffsetDateTime.now())
		               .message("Pogrešno korisničko ime ili lozinka")
		               .build();
	}
}
