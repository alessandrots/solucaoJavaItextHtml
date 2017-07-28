package br.jsan.com.itext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
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
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
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

	private Base64ImageProvider base64ImageProvider;
	private PdfWriter writer;

	public PdfProcessor() {
		super();
	}

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
	public ElementList parseToElementList(String html, String css) throws PdfCreatorException {
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
			throw new PdfCreatorException("Problema geral", e);
		}

		return elements;
	}

	public byte[] parseToPDFStream(String conteudo) throws PdfCreatorException {
		Document document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 56.90551181102F, 56.90551181102F);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			PdfWriter writer2 = PdfWriter.getInstance(document, baos);

			document.open();

			HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			htmlContext.setImageProvider(new Base64ImageProvider());

			PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer2);
			HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);

			CSSResolver cssResolver = new StyleAttrCSSResolver();
			CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

			XMLWorker worker = new XMLWorker(css, true);
			XMLParser xmlParser = new XMLParser(true, worker, Charset.forName("UTF-8"));
			xmlParser.parse(new ByteArrayInputStream(conteudo.getBytes()));

			document.close();
		} catch (Exception e) {
			throw new PdfCreatorException("Problema geral", e);
		}

		return baos.toByteArray();
	}

	/**
	 * Escreve um texto numa posição Absoluta no cabeçalho da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeTextAbsolutePositionTop() throws PdfCreatorException {
		try {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			over.setFontAndSize(bf, 10);
			over.setTextMatrix(160, 830);
			over.showText("SOLD OUT 1");
			over.setTextMatrix(212, 830);
			over.showText("SOLD OUT 2");
			over.setTextMatrix(264, 830);
			over.showText("SOLD OUT 3");
			over.endText();
			over.restoreState();
		} catch (Exception e) {
			throw new PdfCreatorException("writeTextAbsolutePositionTop error: ", e);
		}
	}

	/**
	 * Escreve uma imagem numa posição Absoluta no cabeçalho da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeImageAbsolutePositionTop() throws PdfCreatorException {
		try {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			Image image = this.base64ImageProvider.converterToImage(this.base64ImageProvider.getLogoMpf());
			image.setAbsolutePosition(316, 760);
			image.scaleAbsolute(160, 80);
			over.addImage(image);

			over.endText();
			over.restoreState();
		} catch (Exception e) {
			throw new PdfCreatorException("writeImageAbsolutePositionTop error: ", e);
		}
	}

	/**
	 * Escreve um texto numa posição Absoluta no rodapé da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeTextAbsolutePositionBottom() throws PdfCreatorException {
		try {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			over.setFontAndSize(bf, 10);
			over.setTextMatrix(160, 80);
			over.showText("SOLD OUT 3");
			over.setTextMatrix(240, 80);
			over.showText("SOLD OUT 2");
			over.setTextMatrix(320, 80);
			over.showText("SOLD OUT 4");
			over.endText();
			over.restoreState();
		} catch (Exception e) {
			throw new PdfCreatorException("writeImageAbsolutePositionBottom error: ", e);
		}
	}

	/**
	 * Escreve uma imagem numa posição Absoluta no rodapé da página.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void writeImageAbsolutePositionBottom() throws PdfCreatorException {
		try {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			Image image = this.base64ImageProvider.converterToImage(this.base64ImageProvider.getLogoMpf());
			image.setAbsolutePosition(316, 10);

			//Redimensiona a imagem... MAS PERDE A QUALIDADE, TRABALHANDO COM RECTANGLE NÃO É O CASO
			image.scaleAbsolute(100, 30);

			over.addImage(image);

			over.endText();
			over.restoreState();
		} catch (Exception e) {
			throw new PdfCreatorException("writeImageAbsolutePositionBottom error: ", e);
		}

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
