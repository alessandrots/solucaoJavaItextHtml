package br.jsan.com.itext;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
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

	public HeaderFooter(String cabecalhoHtml, String rodapeHtml, PdfProcessor pdfProcessor) throws Exception {
		this.pdfProcessor = pdfProcessor;

		//faz o parser do html tratando as imagens
		elementosCabecalho = this.pdfProcessor.parseToElementList(cabecalhoHtml, null);
		elementosRodape = this.pdfProcessor.parseToElementList(rodapeHtml, null);
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) throws ExceptionConverter {
		processar(writer);

		linhas++;
	}

	public void processar(PdfWriter writer) {
		try {
			//Para imprimir o cabeçalho somente na primeira página
			if (cabecalhoSomentePaginaUm) {
				if (linhas == 0) {
					manipularElementosCabecalho(writer);
				}
			} else {
				manipularElementosCabecalho(writer);
			}

			//Para imprimir o rodapé somente na primeira página
			if (rodapeSomentePaginaUm) {
				if (linhas == 0) {
					manipularElementosRodape(writer);
				}
			} else {
				manipularElementosRodape(writer);
			}

			//
			//			manipularElementosCabecalho(writer);
			//			manipularElementosRodape(writer);
			//			manipularElementosParaPosicaoAbsoluta();//Não utilizado
		} catch (Exception de) {
			throw new ExceptionConverter(de);
		}
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

	/**
	 * Exemplos de definição do posicionamento do cabeçalho :
	 * 
	 * http://developers.itextpdf.com/question/how-should-i-interpret-coordinates-rectangle-pdf
	 * 
	 * @param writer
	 * @throws DocumentException
	 */
	protected void manipularElementosCabecalho(PdfWriter writer) throws DocumentException {
		ColumnText ct = new ColumnText(writer.getDirectContent());

		//		Para garantir os 2 cm da margem superior
		Rectangle r1 = new Rectangle(5, 630, PageSize.A4.getWidth() - 5, PageSize.A4.getHeight() - 55);
		r1.setBorderColor(BaseColor.RED);
		r1.setBorderWidth(2);
		ct.setSimpleColumn(r1);

		//FUNCIONA  tudo dentro do parágrafo
		Paragraph p = new Paragraph();
		for (Element e : elementosCabecalho) {
			if (e instanceof Chunk) {
				Image i = ((Chunk) e).getImage();
				i.setBorder(Rectangle.BOX);
				i.enableBorderSide(Rectangle.BOX);
				i.setBorderColor(BaseColor.RED);
				i.setBorderWidth(2);
			}
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
	 * http://developers.itextpdf.com/question/how-should-i-interpret-coordinates-rectangle-pdf
	 * 
	 * A imagem tem que aparecer dentro desse retângulo, se ela não aparecer é pq a imagem é maior,
	 * nesse caso a imagem pode ser redimensionada no próprio ckEditor.
	 * 
	 * @param writer
	 * @throws DocumentException
	 */
	protected void manipularElementosRodape(PdfWriter writer) throws DocumentException {
		Paragraph p2 = new Paragraph();
		ColumnText ct2 = new ColumnText(writer.getDirectContent());

		//Para garantir 2 cm do rodapé
		Rectangle r2 = new Rectangle(14, 10, PageSize.A4.getWidth() - 14, 108);

		ct2.setSimpleColumn(r2);
		for (Element e : elementosRodape) {
			if (e instanceof Chunk) {
				Image i = ((Chunk) e).getImage();
				i.setBorder(Rectangle.BOX);
				i.enableBorderSide(Rectangle.BOX);
				i.setBorderColor(BaseColor.RED);
				i.setBorderWidth(2);
			}
			p2.add(e);
		}

		ct2.addElement(p2);
		ct2.go();
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
