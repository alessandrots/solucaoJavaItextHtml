package br.jsan.com.itext;

public class PdfCreatorException extends Exception {

	private static final long serialVersionUID = -8910151338687300190L;

	public PdfCreatorException() {
		super();
	}

	public PdfCreatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PdfCreatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public PdfCreatorException(String message) {
		super(message);
	}

	public PdfCreatorException(Throwable cause) {
		super(cause);
	}

}
