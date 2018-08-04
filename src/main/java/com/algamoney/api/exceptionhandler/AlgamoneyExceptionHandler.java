package com.algamoney.api.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String messageUsuario = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
		String messageDesenvolvedor = ex.getCause() != null ? ex.getCause().toString() : ex.toString();

		List<Erro> erros = Arrays.asList(new Erro(messageUsuario, messageDesenvolvedor));

		return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<Erro> erros = criarListaDeErros(ex.getBindingResult());

		return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}
	
	@ExceptionHandler({ EmptyResultDataAccessException.class })
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex ,WebRequest request) {
		
		String messageUsuario = messageSource.getMessage("recurso.nao-encontrado", null, LocaleContextHolder.getLocale());
		String messageDesenvolvedor = ex.toString();
		
		List<Erro> erros = Arrays.asList(new Erro(messageUsuario, messageDesenvolvedor));
		
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
		
		String messageUsuario = messageSource.getMessage("recurso.operacao-nao-permitida", null, LocaleContextHolder.getLocale());
		String messageDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
		
		List<Erro> erros = Arrays.asList(new Erro(messageUsuario, messageDesenvolvedor));
		
		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	// Criar uma lista de erro para validação de campos
	private List<Erro> criarListaDeErros(BindingResult bindingResult) {
		List<Erro> erros = new ArrayList<>();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String messageUsuario = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
			String messageDesenvolvedor = fieldError.toString();
			erros.add(new Erro(messageUsuario, messageDesenvolvedor));
		}

		return erros;

	}

	// Criar uma classe static de Erro
	public static class Erro {

		private String messageUsuario;
		private String messageDesenvolvedor;

		public Erro(String messageUsuario, String messageDesenvolvedor) {
			super();
			this.messageUsuario = messageUsuario;
			this.messageDesenvolvedor = messageDesenvolvedor;
		}

		public String getMessageUsuario() {
			return messageUsuario;
		}

		public String getMessageDesenvolvedor() {
			return messageDesenvolvedor;
		}

	}

}
