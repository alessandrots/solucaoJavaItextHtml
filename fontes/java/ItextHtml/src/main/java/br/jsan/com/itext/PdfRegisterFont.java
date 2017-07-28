package br.jsan.com.itext;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.FontFactory;

public class PdfRegisterFont extends IConstantes {

	private static final String DIR = "/tmp";
	private static final String FILE2 = "FirstPdf2.pdf";

	java.util.List<String> pagesHmtl = new ArrayList<>();
	private Logger logger = java.util.logging.Logger.getLogger("Test");

	public PdfRegisterFont() {
		super();
	}

	public static void main(String[] args) {
		PdfRegisterFont first = new PdfRegisterFont();
		File f = new File(DIR);
		String htmlInicial1 = "";

		if (!f.exists()) {
			System.exit(0);
		}

		try {

			first.gerarPdf(htmlInicial1);
		} catch (Exception e) {
			System.out.println("Exception main = " + e.getMessage());
			first.logger.log(Level.SEVERE, "Exceção = " + e);
		}
	}

	public byte[] gerarPdf(String htmlPadrao) throws PdfCreatorException {
		try {
			this.registerFont("http://unico-homologacao-02.mpf.mp.br/unico/recursos/fonts-itext/times.ttf", "times");
			System.out.println("\n SUCESSO... => http://unico-homologacao-02.mpf.mp.br/unico/recursos/fonts-itext/times.ttf \n");
		} catch (Exception e) {
			System.out.println("Exception gerarPdf = " + e.getMessage());
			throw new PdfCreatorException("Problema geral", e);
		}

		return null;
	}

	private void registerFont(String fontName, String nomeAssociado) throws PdfCreatorException {
		URL fontPath;
		try {
			fontPath = new URL(fontName);
			FontFactory.register(fontPath.toString(), nomeAssociado);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "processaCabecalhoRodape = " + e);
			System.out.println("Exception registerFont = " + e.getMessage());
			throw new PdfCreatorException(e.getMessage());
		}

	}

}
