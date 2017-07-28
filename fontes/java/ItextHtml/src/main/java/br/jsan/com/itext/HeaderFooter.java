package br.jsan.com.itext;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;

/**
 * Classe responsável por processar o cabeçalho e rodapé.
 * 
 * A mesma recebe os htmls de cada um transforma em um objeto ElementList do itext e coloca os mesmos em retângulos em posições
 * que definem a localização do cabeçalho e rodapé.
 * 
 * Se as variáveis booleanas cabecalhoSomentePaginaUm e rodapeSomentePaginaUm forem true, somente a primeira página será preenchido.
 * 
 * Essas variáveis são definidas na classe PdfCreator com base na leitura dos atributos headernoloop e footernoloop.
 * 
 * https://stackoverflow.com/questions/25545868/how-to-pass-rectangle-size-available-in-millimetre-to-create-docment-using-itext
 * 
 * @author alessandroteixeira
 *
 */
public class HeaderFooter extends PdfPageEventHelper {

	protected ElementList elementosCabecalho;
	protected ElementList elementosRodape;
	private PdfProcessor pdfProcessor;
	private int linhas;
	private boolean cabecalhoSomentePaginaUm;
	private boolean rodapeSomentePaginaUm;

	private float totalAlturaCabecalho;
	private float totalAlturaRodape;

	private Logger logger = java.util.logging.Logger.getLogger("HeaderFooter");

	public HeaderFooter() {
		//construtor padrão para chamada ao gerador de número de páginas
	}

	public HeaderFooter(String cabecalhoHtml, String rodapeHtml, PdfProcessor pdfProcessor, float totalAlturaCabecalho, float totalAlturaRodape) throws PdfCreatorException {
		this.pdfProcessor = pdfProcessor;

		//faz o parser do html tratando as imagens
		try {

			this.elementosCabecalho = this.pdfProcessor.parseToElementList(cabecalhoHtml, null);
			this.elementosRodape = this.pdfProcessor.parseToElementList(rodapeHtml, null);
			this.totalAlturaCabecalho = totalAlturaCabecalho;
			this.totalAlturaRodape = totalAlturaRodape;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "constructor HeaderFooter = " + e);
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) throws ExceptionConverter {
		linhas++;

		processar(writer);
	}

	public void processar(PdfWriter writer) {
		try {
			//Para imprimir o cabeçalho somente na primeira página
			if (cabecalhoSomentePaginaUm) {
				if (linhas == 1) {
					manipularElementosCabecalho(writer);
				}
			} else {
				manipularElementosCabecalho(writer);
			}

			//Para imprimir o rodapé somente na primeira página
			if (rodapeSomentePaginaUm) {
				if (linhas == 1) {
					manipularElementosRodape(writer);
				}
			} else {
				manipularElementosRodape(writer);
			}

		} catch (Exception de) {
			throw new ExceptionConverter(de);
		}
	}

	/**
	 * Exemplos de definição do posicionamento do cabeçalho :
	 * 
	 * http://developers.itextpdf.com/question/how-should-i-interpret-coordinates-rectangle-pdf
	 * 
	 * r1 = new Rectangle(5, 640, PageSize.A4.getWidth() - 5, PageSize.A4.getHeight() - 55);
	 * 
	 * @param writer
	 * @throws DocumentException
	 */
	protected void manipularElementosCabecalho(PdfWriter writer) throws DocumentException {
		ColumnText ct = new ColumnText(writer.getDirectContent());
		Rectangle r1;
		float ury = PageSize.A4.getHeight() - 55;
		float lly = ury - this.totalAlturaCabecalho;

		r1 = new Rectangle(5, lly - 10, PageSize.A4.getWidth() - 5, ury);

		//		Para garantir os 2 cm da margem superior
		r1.setBorderColor(BaseColor.RED);
		r1.setBorderWidth(2);
		ct.setSimpleColumn(r1);

		//FUNCIONA  tudo dentro do parágrafo
		Paragraph p = new Paragraph();
		for (Element e : elementosCabecalho) {
			p.add(e);
		}

		ct.addElement(p);
		r1.setBorderColor(BaseColor.RED);
		r1.setBorderWidth(2);
		ct.setSimpleColumn(r1);
		ct.go();
	}

	/**
	 * Exemplos de definição do posicionamento do rodapé :
	 * 
	 * http://developers.itextpdf.com/question/how-should-i-interpret-coordinates-rectangle-pdf (importante)
	 * 
	 * A imagem tem que aparecer dentro desse retângulo, se ela não aparecer é pq a imagem é maior,
	 * nesse caso a imagem pode ser redimensionada no próprio ckEditor.
	 * 
	 * r2 = new Rectangle(20, 10, PageSize.A4.getWidth() - 20, this.totalAlturaRodape + Utilities.millimetersToPoints(20));
	 * 
	 * r2 = new Rectangle(20, 10, PageSize.A4.getWidth() - 20, this.totalAlturaRodape);
	 * 
	 * @param writer
	 * @throws DocumentException
	 */
	protected void manipularElementosRodape(PdfWriter writer) throws DocumentException {
		Paragraph p2 = new Paragraph();
		ColumnText ct2 = new ColumnText(writer.getDirectContent());
		Rectangle r2;

		//Para tratar o início da altura do rodapé quando utilizar templates 5, 6, 7 e 8 do templates.js
		if (Float.valueOf(Math.abs(this.totalAlturaRodape)) < Float.valueOf(39f)) {
			r2 = new Rectangle(Utilities.millimetersToPoints(20), 10, PageSize.A4.getWidth() - 20, this.totalAlturaRodape + Utilities.millimetersToPoints(17));
		} else {
			r2 = new Rectangle(Utilities.millimetersToPoints(20), 10, PageSize.A4.getWidth() - 20, this.totalAlturaRodape + Utilities.millimetersToPoints(20));
		}

		ct2.setSimpleColumn(r2);
		for (Element e : elementosRodape) {
			p2.add(e);
		}

		ct2.addElement(p2);

		ct2.go();
	}

	/**
	 * Adiciona a paginação ao final da página. Gera um novo array de bytes após o documento ser processado
	 * 
	 * @param pdf
	 * @return ByteArrayOutputStream
	 * @throws Exception
	 */
	public ByteArrayOutputStream escreverNumerosDePagina(byte[] pdf) throws Exception {
		PdfReader reader = new PdfReader(pdf);
		int pages = reader.getNumberOfPages();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PdfStamper stamper = new PdfStamper(reader, baos);

		for (int i = 1; i <= pages; i++) {
			PdfContentByte overContent;
			overContent = stamper.getOverContent(i);
			BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, true);
			overContent.saveState();
			overContent.beginText();
			overContent.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			overContent.setFontAndSize(bf, 10.0f);
			overContent.setColorFill(BaseColor.BLACK);
			overContent.setTextMatrix(520, 17);
			overContent.showText("Pág. " + (i) + " de " + pages);
			overContent.endText();
			overContent.restoreState();
		}
		stamper.close();
		reader.close();

		return baos;
	}

	/**
	 * Esses métodos podem servir no futuro para colocar elementos em uma posição absoluta, tanto texto quanto imagem.
	 * 
	 * <strong>Nesse primeiro momento essas funcionalidades não serão usadas mas foram testadas para um uso futuro.</strong>
	 * 
	 * Setando imagem e texto em posição específica
	 */
	protected void manipularElementosParaPosicaoAbsoluta() {
		try {
			this.pdfProcessor.writeTextAbsolutePositionTop();
			this.pdfProcessor.writeImageAbsolutePositionTop();
			this.pdfProcessor.writeImageAbsolutePositionBottom();
			this.pdfProcessor.writeTextAbsolutePositionBottom();
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	public int getLinhas() {
		return linhas;
	}

	public void setCabecalhoSomentePaginaUm(boolean cabecalhoSomentePaginaUm) {
		this.cabecalhoSomentePaginaUm = cabecalhoSomentePaginaUm;
	}

	public void setRodapeSomentePaginaUm(boolean rodapeSomentePaginaUm) {
		this.rodapeSomentePaginaUm = rodapeSomentePaginaUm;
	}

}
