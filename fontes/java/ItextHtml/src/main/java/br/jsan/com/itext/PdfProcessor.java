package br.jsan.com.itext;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

/**
 * ESSA CLASSE SERVE PARA GERAR O PDF NO MESMO FORMATO DEFINIDO NO HTML.
 * 
 * Classe responsável por fazer o parse do HTML.
 * 
 * Além disso, ela processa imagens tanto no formato base64 quanto no padrão url no src.
 * 
 * Além disso tem métodos para tratar o posicionamento absoluto de textos e imagens no cabeçalho e rodapé.
 * 
 * @author alessandroteixeira
 *
 */
public class PdfProcessor {

	public Base64ImageProvider base64ImageProvider;
	public PdfWriter writer;

	public PdfProcessor(PdfWriter writer) {
		super();
		this.base64ImageProvider = new Base64ImageProvider();
		this.writer = writer;
	}

	/**
	 * Faz o parser do HTML/CSS considerando o processador de imagens em base64.
	 * 
	 * @param html
	 * @param css
	 * @return ElementList
	 */
	public ElementList parseToElementList(String html, String css) throws Exception {
		ElementList elements = null;

		try {
			// CSS
			CSSResolver cssResolver = new StyleAttrCSSResolver();
			if (css != null) {
				CssFile cssFile = XMLWorkerHelper.getCSS(new ByteArrayInputStream(css.getBytes()));
				cssResolver.addCss(cssFile);
			}

			// HTML
			CssAppliers cssAppliers = new CssAppliersImpl(FontFactory.getFontImp());
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			htmlContext.setImageProvider(this.base64ImageProvider);

			// Pipelines
			elements = new ElementList();
			ElementHandlerPipeline end = new ElementHandlerPipeline(elements, null);
			HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext, end);
			CssResolverPipeline cssPipeline = new CssResolverPipeline(cssResolver, htmlPipeline);

			// XML Worker
			XMLWorker worker = new XMLWorker(cssPipeline, true);
			XMLParser p = new XMLParser(worker);
			p.parse(new ByteArrayInputStream(html.getBytes()));
		} catch (Exception e) {
			throw e;
		}

		return elements;
	}

	/**
	 * Escreve um texto numa posição Absoluta no cabeçalho da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeTextAbsolutePositionTop() throws DocumentException, IOException {
		PdfContentByte over = writer.getDirectContent();
		over.saveState();
		BaseFont bf = BaseFont.createFont();
		over.beginText();
		over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
		over.setFontAndSize(bf, 10);
		over.setTextMatrix(160, 830);
		over.showText("SOLD OUT");
		over.setTextMatrix(212, 830);
		over.showText("SOLD OUT");
		over.setTextMatrix(264, 830);
		over.showText("SOLD OUT");
		over.endText();
		over.restoreState();
	}

	/**
	 * Escreve uma imagem numa posição Absoluta no cabeçalho da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeImageAbsolutePositionTop() throws DocumentException, IOException {
		PdfContentByte over = writer.getDirectContent();
		over.saveState();
		over.beginText();
		over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
		Image img_ = this.base64ImageProvider.converterToImage(this.base64ImageProvider.getLogoMpf());
		img_.setAbsolutePosition(316, 760);
		img_.scaleAbsolute(160, 80);
		over.addImage(img_);

		over.endText();
		over.restoreState();
	}

	/**
	 * Escreve um texto numa posição Absoluta no rodapé da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeTextAbsolutePositionBottom() throws DocumentException, IOException {
		PdfContentByte over = writer.getDirectContent();
		over.saveState();
		BaseFont bf = BaseFont.createFont();
		over.beginText();
		over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
		over.setFontAndSize(bf, 10);
		over.setTextMatrix(160, 80);
		over.showText("SOLD OUT 2");
		over.setTextMatrix(240, 80);
		over.showText("SOLD OUT 2");
		over.setTextMatrix(320, 80);
		over.showText("SOLD OUT 2");
		over.endText();
		over.restoreState();
	}

	/**
	 * Escreve uma imagem numa posição Absoluta no rodapé da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeImageAbsolutePositionBottom() throws DocumentException, IOException {
		PdfContentByte over = writer.getDirectContent();
		over.saveState();
		over.beginText();
		over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
		Image img_ = this.base64ImageProvider.converterToImage(this.base64ImageProvider.getLogoMpf());
		img_.setAbsolutePosition(316, 10);

		//Redimensiona a imagem... MAS PERDE A QUALIDADE, TRABALHANDO COM RECTANGLE NÃO É O CASO
		img_.scaleAbsolute(100, 30);

		over.addImage(img_);

		over.endText();
		over.restoreState();
	}

	/**
	 * Exemplo de como a imagem é gerada com um Base64 real.
	 * 
	 * @return Image
	 */
	public Image getImageMPFExample() {
		return this.base64ImageProvider.converterToImage(this.base64ImageProvider.getLogoMpf());
	}

	/**
	 * Retornar um elemento string de uma imagem em Base64.
	 * 
	 * @return String
	 */
	public String getLogoMpfExample() {
		return this.base64ImageProvider.getLogoMpf();
	}

}
