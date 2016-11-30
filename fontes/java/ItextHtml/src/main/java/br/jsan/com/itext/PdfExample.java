package br.jsan.com.itext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
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
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class PdfExample implements IConstantes {

	private static String DIR = "/home/alessandroteixeira";
	private static String COMP_DIR = "Projetos/outros/testes";
	private static String FILE2 = "FirstPdf2.pdf";

	private PdfWriter writer;
	private ByteArrayOutputStream baos;
	java.util.List<String> pagesHmtl = new ArrayList<String>();
	private HeaderFooter headerFooter;
	private boolean headerNoLoop;
	private boolean footerNoLoop;

	public PdfExample() {
		super();
	}

	public static void main(String[] args) {
		PdfExample first = new PdfExample();
		File f = new File(DIR);

		if (!f.exists()) {
			System.out.println("\n **** Não existe o diretório para criar o PDF.\n");
			System.exit(0);
		}

		first.gerarPdf();
	}

	private byte[] gerarPdf() {
		byte[] bytes = null;

		this.headerNoLoop = HEADER5.contains("headernoloop");
		this.footerNoLoop = FOOTER2.contains("footernoloop");
		//		this.modeloTemplateMPF = html.contains("modeloTemplateMPF");

		try {
			//			@param pageSize the pageSize
			//			@param marginLeft the margin on the left
			//			@param marginRight the margin on the right
			//			@param marginTop the margin on the top ***
			//			@param marginBottom the margin on the bottom ***
			Document document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 160, 130.90551181102F);

			System.out.println("PageSize.A4.getHeight = " + PageSize.A4.getHeight());

			this.baos = new ByteArrayOutputStream();
			this.writer = PdfWriter.getInstance(document, this.baos);

			//Adicionando Header e Footer
			this.headerFooter = this.new HeaderFooter();
			this.writer.setPageEvent(headerFooter);

			document.open();

			this.pagesHmtl.add("A1 <BR>" + this.htmlTeste + " <BR>");
			//			this.pagesHmtl.add("A25 <BR>" + this.htmlTeste + " <BR>");
			this.pagesHmtl.add(this.htmlInicial);

			//Adicionando o html ao conteúdo
			//			this.addContentHtml(document, pagesHmtl);
			if (this.headerNoLoop) {
				this.processarConteudoCorpo(document, pagesHmtl);
			} else {
				this.processarConteudoCorpoComRepeticaoCabecalho(document, pagesHmtl);
			}

			//			this.headerFooter.writeText(document);

			document.close();

			this.criarPdfEmDisco(this);

			bytes = this.baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bytes;
	}

	/**
	 * 
	 * @param first
	 */
	private void criarPdfEmDisco(PdfExample first) {
		/**
		 * Criando o PDF a partir do array de Bytes
		 */
		byte[] bytes = first.baos.toByteArray();
		FileOutputStream fileOutputStream = null;
		System.out.println("outputstream = " + ((bytes != null) ? bytes.length : -1));

		try {
			//convert file into array of bytes
			if (COMP_DIR != null && !COMP_DIR.equals("")) {
				fileOutputStream = new FileOutputStream(DIR + File.separator + COMP_DIR + File.separator + FILE2);
			} else {
				fileOutputStream = new FileOutputStream(DIR + File.separator + FILE2);
			}

			fileOutputStream.write(bytes);
			fileOutputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adiciona o conteúdo do HTML.Este conteúdo vai para a área central do documento.
	 * 
	 * Neste caso a margem do cabeçalho é maior porque ele vem preenchido com imagem e outras informações.
	 * 
	 * @param document
	 * @param pagesHmtl
	 */
	private void processarConteudoCorpoComRepeticaoCabecalho(Document document, List<String> pagesHmtl) {
		if (pagesHmtl != null) {
			for (String page : pagesHmtl) {
				try {
					for (Element e : this.headerFooter.parseToElementList(page, null)) {
						//document.setMargins(85.35826771653F, 56.90551181102F, 200F, 130.90551181102F);
						document.setMargins(
							IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_MODELO_MPF, IConstantes.DOCUMENT_MARGIN_BOTTOM);
						document.add(e);
					}
					//					document.newPage();//criando uma nova página
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Adiciona o conteúdo do HTML.Este conteúdo vai para a área central do documento.
	 * 
	 * Neste caso a margem do cabeçalho é menor porque não vai existir nenhum conteúdo no cabeçalho.
	 * 
	 * @param document
	 * @param pagesHmtl
	 * @throws DocumentException
	 */
	private void processarConteudoCorpo(Document document, java.util.List<String> pagesHmtl) throws DocumentException {
		if (pagesHmtl != null) {
			for (String page : pagesHmtl) {
				try {
					for (Element e : this.headerFooter.parseToElementList(page, null)) {
						//						document.setMargins(85.35826771653F, 56.90551181102F, 58F, 130.90551181102F);
						document.setMargins(
							IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_REDUCED, IConstantes.DOCUMENT_MARGIN_BOTTOM);
						document.add(e);
					}
					//					document.newPage();//criando uma nova página
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Classe para criar o cabeçalho e o rodapé e repetir o mesmo em todas as páginas e assim determinado.
	 * 
	 * @author alessandroteixeira
	 *
	 */
	class HeaderFooter extends PdfPageEventHelper {
		protected ElementList image1;
		protected ElementList header;
		protected ElementList footer;

		public HeaderFooter() throws IOException {

			image1 = parseToElementList(logoTag, null);

			HEADER5 = this.adicionarAlignHouverTextAlign(HEADER5);
			HEADER5 = this.limparTags(HEADER5, DIV_HTML);
			HEADER5 = TABLE_WIDTH_BORDER_ZERO_HTML + " " + HEADER5 + " " + TABLE_HTML_END_TAG;
			header = parseToElementList(HEADER5, null);

			FOOTER2 = limparTags(FOOTER2, TFOOT_HTML);
			FOOTER2 = limparTags(FOOTER2, DIV_HTML);
			FOOTER2 = this.adicionarAlignHouverTextAlign(FOOTER2);
			FOOTER2 = TABLE_WIDTH_BORDER_BLACK_HTML + FOOTER2 + " " + TABLE_HTML_END_TAG;
			footer = parseToElementList(this.limparTags(FOOTER2, "div"), null);
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			try {

				float[] columnWidths = {50, 50};
				PdfPTable table = new PdfPTable(columnWidths);
				table.setWidthPercentage(100);

				ColumnText ct = new ColumnText(writer.getDirectContent());
				Rectangle r1 = new Rectangle(5, 630, PageSize.A4.getWidth() - 5, PageSize.A4.getHeight() - 55);

				ct.setSimpleColumn(r1);

				//FUNCIONA  tudo dentro do parágrafo
				Paragraph p = new Paragraph();
				//				for (Element e : footer) {
				for (Element e : header) {
					if (e instanceof Chunk) {
						Image i = ((Chunk) e).getImage();
						i.setBorder(Rectangle.BOX);
						i.enableBorderSide(Rectangle.BOX);
						i.setBorderColor(BaseColor.RED);
						i.setBorderWidth(2);
						//						i.setAlignment(Element.ALIGN_CENTER);
					}
					p.add(e);
				}

				ct.addElement(p);
				ct.go();

				Paragraph p2 = new Paragraph();
				ColumnText ct2 = new ColumnText(writer.getDirectContent());
				Rectangle r2 = new Rectangle(14, 10, PageSize.A4.getWidth() - 14, 108);

				ct2.setSimpleColumn(r2);
				for (Element e : footer) {
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

				//Setando imagem e texto em posição específica
				//				try {
				//					writeTextAbsolutePositionTop();
				//					writeImageAbsolutePositionTop();
				//					writeImageAbsolutePositionBottom();
				//					writeTextAbsolutePositionBottom();
				//				} catch (DocumentException e) {
				//					e.printStackTrace();
				//				} catch (IOException e) {
				//					e.printStackTrace();
				//				}

			} catch (Exception de) {
				de.printStackTrace();
				throw new ExceptionConverter(de);
			}
		}

		/**
		 * Método para limpar as tags HTML (strPattern) da string passada (str).
		 * 
		 * @param str
		 * @param strPattern
		 * @return String
		 */
		protected String limparTags(String str, String strPattern) {
			if (str != null && str.indexOf("<" + strPattern + ">") != -1) {
				StringBuffer stb = new StringBuffer();
				str = str.substring(str.indexOf("<" + strPattern + ">"));
				Pattern pattern = Pattern.compile("<*?.(" + strPattern + ")>");
				String[] result = pattern.split(str.trim());

				List<String> list = new ArrayList<String>(Arrays.asList(result));
				for (String s : list) {
					if (s != null && !s.trim().equals("")) {
						stb.append(s.trim());
						stb.append(" ");
					}
				}
				str = stb.toString();
			}

			return str;
		}

		/**
		 * Para resolver o problema de alinhamento de componentes. o text-align não funciona no iText.
		 * 
		 * Tem que ser o align. Mas o align não é setado pelo CKEditor.
		 * 
		 * http://ckeditor.com/forums/CKEditor/Paragraph-center-alignment-problem
		 * 
		 * Retorna o HTML do cabeçalho ou rodapé
		 * 
		 * @param html
		 * @return String
		 */
		protected String adicionarAlignHouverTextAlign(String html) {
			html = TABLE_HTML_START_TAG + html + TABLE_HTML_END_TAG;
			boolean bVariosTrs = false;
			StringBuffer stb = new StringBuffer();

			if (html != null) {
				String saneHtmlTxt = this.cleanXmlAndRemoveUnwantedTags(html);
				org.jsoup.nodes.Document doc = Jsoup.parse(saneHtmlTxt, "", Parser.xmlParser());

				Elements elementos = doc.getElementsByTag(TD_HTML);

				Elements elementosTr = doc.getElementsByTag(TR_HTML);

				if (elementosTr.size() > 1) {
					bVariosTrs = true;
				}

				if (elementos != null) {
					html = "";
					for (org.jsoup.nodes.Element element : elementos) {
						element.attr(ALIGN_TAG_HTML, recuperarAlinhamentoTd(element));
						String s = element.toString().replaceAll("text-align", ALIGN_TAG_HTML);
						if (bVariosTrs) {
							//para o caso de ter vários trs no thead ou no tfoot
							html += TR_HTML_START_TAG + s + TR_HTML_END_TAG;
						} else {
							// este caso são vários tds num tr
							stb.append(s);
						}
					}
				}
			}

			if (!bVariosTrs) {
				html += TR_HTML_START_TAG + stb.toString() + TR_HTML_END_TAG;
			}

			return html;
		}

		/**
		 * Recupera o alinhamento que está dentro do style para poder setar o atributo align no td.
		 * 
		 * @param element
		 * @return String
		 */
		protected String recuperarAlinhamentoTd(org.jsoup.nodes.Element element) {
			org.jsoup.nodes.Attributes atributos = element.attributes();
			String alinhamento = "";

			if (atributos != null && atributos.size() > 0) {
				for (org.jsoup.nodes.Attribute attr : atributos) {
					if (attr.getKey().equalsIgnoreCase(STYLE_TAG_HTML)) {
						if (attr.getValue().indexOf(ALIGN_CENTER_HTML) != -1) {
							alinhamento = ALIGN_CENTER_HTML;
						} else if (attr.getValue().indexOf(ALIGN_RIGHT_HTML) != -1) {
							alinhamento = ALIGN_RIGHT_HTML;
						} else if (attr.getValue().indexOf(ALIGN_LEFT_HTML) != -1) {
							alinhamento = ALIGN_LEFT_HTML;
						}
					}
				}
			}

			return alinhamento;
		}

		/**
		 * http://stackoverflow.com/questions/34218225/jsoup-clean-leaves-unclosed-and-opens-tags
		 * 
		 * https://github.com/jhy/jsoup/issues/511
		 * 
		 * A tag img não fecha na hora que é feito o parse do HTML. Isso porque no HTML a tag img, br, não é obrigatório ter uma tag de fechamento.
		 * 
		 * A idéia é gerar um xhtml bem formado.
		 * 
		 * @param textToEscape
		 * @return String
		 */
		protected String cleanXmlAndRemoveUnwantedTags(String textToEscape) {
			Whitelist whitelist = Whitelist.relaxed();
			whitelist.addTags(STYLE_TAG_HTML);
			whitelist.addAttributes(":all", STYLE_TAG_HTML);
			whitelist.addAttributes(":all", "class");
			whitelist.addProtocols("img", "src", "data"); //for base64 encoded images

			OutputSettings outputSettings = new OutputSettings().syntax(OutputSettings.Syntax.xml).charset(StandardCharsets.UTF_8).prettyPrint(false);

			String safe = Jsoup.clean(textToEscape, "", whitelist, outputSettings);

			return safe;
		}

		private void writeTextAbsolutePositionTop() throws DocumentException, IOException {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			//			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);//NEGRITÃO
			//			over.setLineWidth(1.5f);
			//			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			//			over.setRGBColorFill(0xFF, 0xFF, 0xFF);
			over.setFontAndSize(bf, 10);
			over.setTextMatrix(160, 830);
			over.showText("SOLD OUT");
			over.setTextMatrix(212, 830);
			over.showText("SOLD OUT");
			over.setTextMatrix(264, 830);
			over.showText("SOLD OUT");
			//			Image img_ = converterToImage(logoTag);
			//			img_.setAbsolutePosition(250, 760);
			//			img_.scaleAbsolute(160, 80);
			//			over.addImage(img_);
			over.endText();
			over.restoreState();
		}

		private void writeImageAbsolutePositionTop() throws DocumentException, IOException {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			//			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			//			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);//NEGRITÃO
			//			over.setLineWidth(1.5f);
			//			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			//			over.setRGBColorFill(0xFF, 0xFF, 0xFF);
			//			over.setFontAndSize(bf, 10);
			//			over.setTextMatrix(160, 830);
			//			over.showText("SOLD OUT");
			//			over.setTextMatrix(212, 830);
			//			over.showText("SOLD OUT");
			Image img_ = converterToImage(logoTag);
			img_.setAbsolutePosition(316, 760);
			img_.scaleAbsolute(160, 80);
			over.addImage(img_);

			over.endText();
			over.restoreState();
		}

		private void writeTextAbsolutePositionBottom() throws DocumentException, IOException {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			//			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);//NEGRITÃO
			//			over.setLineWidth(1.5f);
			//			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			//			over.setRGBColorFill(0xFF, 0xFF, 0xFF);
			over.setFontAndSize(bf, 10);
			over.setTextMatrix(160, 80);
			over.showText("SOLD OUT 2");
			over.setTextMatrix(240, 80);
			over.showText("SOLD OUT 2");
			over.setTextMatrix(320, 80);
			over.showText("SOLD OUT 2");
			//			Image img_ = converterToImage(logoTag);
			//			img_.setAbsolutePosition(250, 760);
			//			img_.scaleAbsolute(160, 80);
			//			over.addImage(img_);
			over.endText();
			over.restoreState();
		}

		private void writeImageAbsolutePositionBottom() throws DocumentException, IOException {
			PdfContentByte over = writer.getDirectContent();
			over.saveState();
			//			BaseFont bf = BaseFont.createFont();
			over.beginText();
			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
			//			over.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);//NEGRITÃO
			//			over.setLineWidth(1.5f);
			//			over.setRGBColorStroke(0xFF, 0x00, 0x00);
			//			over.setRGBColorFill(0xFF, 0xFF, 0xFF);
			//			over.setFontAndSize(bf, 10);
			//			over.setTextMatrix(160, 830);
			//			over.showText("SOLD OUT");
			//			over.setTextMatrix(212, 830);
			//			over.showText("SOLD OUT");
			Image img_ = converterToImage(logoTag);
			img_.setAbsolutePosition(316, 10);
			//			img_.scaleAbsolute(160, 80);
			img_.scaleAbsolute(100, 30);//Redimensiona a imagem... ficou um pouco deformada
			over.addImage(img_);

			over.endText();
			over.restoreState();
		}

		/**
		 * Faz o parser do HTML/CSS considerando o processador de imagens.
		 * 
		 * @param html
		 * @param css
		 * @return
		 */
		public ElementList parseToElementList(String html, String css) {
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
				//				htmlContext.autoBookmark(false);
				htmlContext.setImageProvider(new Base64ImageProvider());

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
				e.printStackTrace();
			}

			return elements;
		}

		public Image converterToImage(String imageTagHtml) {
			String b64Image = imageTagHtml.substring(imageTagHtml.indexOf("base64,") + 7);
			//			String b64Image = logoTag.substring(logoTag.indexOf("base64,") + 7);

			byte[] decoded = Base64.decode(b64Image);
			Image img = null;

			try {
				img = Image.getInstance(decoded);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return img;
		}
	}

	/**
	 * Classe para processar tag img do html.
	 * Ou base64 ou o caminho propriamente dito.
	 * 
	 * @author alessandroteixeira
	 *
	 */
	class Base64ImageProvider extends AbstractImageProvider {

		@Override
		public Image retrieve(String src) {
			int pos = src.indexOf("base64,");
			try {
				if (src.startsWith("data") && pos > 0) {
					byte[] img = Base64.decode(src.substring(pos + 7));
					//					System.out.println("img data = " + Image.getInstance(img));
					return Image.getInstance(img);
				} else {
					//					System.out.println("img src = " + Image.getInstance(src));
					return Image.getInstance(src);
				}
			} catch (BadElementException ex) {
				return null;
			} catch (IOException ex) {
				return null;
			}
		}

		@Override
		public String getImageRootPath() {
			return null;
		}
	}

	private String htmlTeste = "<table style=\"width:100%\"> " + "<tr> " + "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> "
		+ "    <td>Jill</td> " + "    <td>Smith</td>  " + "    <td>50</td> " + "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> "
		+ "  </tr> " + "<tr> " + "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> " + "    <td>Jill</td> "
		+ "    <td>Smith</td>  " + "    <td>50</td> " + "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> " + "  </tr> " + "<tr> "
		+ "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> " + "    <td>Jill</td> " + "    <td>Smith</td>  " + "    <td>50</td> "
		+ "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> " + "  </tr> " + "<tr> " + "    <th>Firstname</th> " + "    <th>Lastname</th>  "
		+ "    <th>Age</th> " + "  </tr> " + "  <tr> " + "    <td>Jill</td> " + "    <td>Smith</td>  " + "    <td>50</td> " + "  </tr> " + "  <tr> " + "    <td>Eve</td> "
		+ "    <td>Jackson</td> " + "    <td>94</td> " + "  </tr> " + "<tr> " + "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> "
		+ "    <td>Jill</td> " + "    <td>Smith</td>  " + "    <td>50</td> " + "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> "
		+ "  </tr> " + "<tr> " + "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> " + "    <td>Jill</td> "
		+ "    <td>Smith</td>  " + "    <td>50</td> " + "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> " + "  </tr> " + "<tr> "
		+ "    <th>Firstname</th> " + "    <th>Lastname</th>  " + "    <th>Age</th> " + "  </tr> " + "  <tr> " + "    <td>Jill</td> " + "    <td>Smith</td>  " + "    <td>50</td> "
		+ "  </tr> " + "  <tr> " + "    <td>Eve</td> " + "    <td>Jackson</td> " + "    <td>94</td> " + "  </tr> " + "</table> ";

	String image = "data:image/gif;base64,R0lGODlhEAAQAOMIAAAAABoaGjMzM0xMTGZmZoCAgJmZmbKysv///////////////////////////////"
		+ "yH/C05FVFNDQVBFMi4wAwEAAAAh+QQBCgAIACwAAAAAEAAQAAAESBDJiQCgmFqbZwjVhhwH9n3hSJbeSa1sm5GUIHSTYSC2jeu63q0D3PlwCB1lMMgUChgmk/J8LqUIAgFRhV6z2q0VF94i"
		+ "J9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+UKgmFqbpxDV9gAA9n3hSJbeSa1sm5HUMHTTcTy2jeu63q0D3PlwDx2FQMgYDBgmk/J8LqWPQuFRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACw"
		+ "AAAAAEAAQAAAESPDJ+YSgmFqb5xjV9gQB9n3hSJbeSa1sm5EUQXQTADy2jeu63q0D3PlwDx2lUMgcDhgmk/J8LqUPg+FRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+"
		+ "cagmFqbJyHV9ggC9n3hSJbeSa1sm5FUUXRTEDy2jeu63q0D3PlwDx3FYMgAABgmk/J8LqWPw+FRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+QihmFqbZynV9gwD9n3h"
		+ "SJbeSa1sm5GUYXSTIDy2jeu63q0D3PlwDx3lcMgEAhgmk/J8LqUPAOBRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+UqhmFqbpzHV9hAE9n3hSJbeSa1sm5HUcXTTMDy2j"
		+ "eu63q0D3PlwDx0FAMgIBBgmk/J8LqWPQOBRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+YyhmFqb5znV9hQF9n3hSJbeSa1sm5EUAHQTQTy2jeu63q0D3PlwDx0lEMgMBhgmk"
		+ "/J8LqUPgeBRhV6z2q0VF94iJ9pOBAAh+QQBCgAPACwAAAAAEAAQAAAESPDJ+c6hmFqbJwDV9hgG9n3hSJbeSa1sm5FUEHRTUTy2jeu63q0D3PlwDx1FIMgQCBgmk/J8LqWPweBRhV6z2q0VF94iJ9pOBAA7";

	String logoMpf1 =
		"data:image/png;base64,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";

	String imgTag = "<img src=\"" + this.image + "\"/>";

	String logoTag = "<img src=\"" + this.logoMpf1 + "\"/>";

	String htmlInicial = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
		+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + "<head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
		+ "<title>Untitled Document</title>" + "<style type=\"text/css\">" + "html, body {" + "   height:100&#37;;" +
		//    		"   height:100%;"+//Teste
		"   margin:0;" + "   padding:0;" + "}" +
		//    		"table {"+
		//    		"    height:100%;"+
		//    		"    width:100%;"+
		//    		"    border:1;"+//Teste
		//    		"    border-collapse:collapse"+
		//    		"}"+
		"table td {" + "    vertical-align:top" + "}" + ".footer {" + "    position:fixed;" + "    height:100px;" +
		//    		"    background:red;"+
		"    bottom:0;" + "    width:100%;" + "}" + "thead {" + "    position:fixed;" + "    height:100px;" + "    background:yellow;" + "    width:100%;" + "}" + "tbody td {"
		+ "    padding-bottom:100px" + "}" + "</style>" + "<!--[if gte IE 8]>" + "<style type=\"text/css\">" + ".footer{position:static}" + "</style>" + "<![endif]-->" + "</head>"
		+ "<body>" + "</body>" + "<table cellspacing=\"0\" cellpadding=\"0\" style=\"width:100%; border:'1px solid black'\">" + "    <thead>" + "        <tr>"
		+ "            <td>Header</td>" + "        </tr>" + "    </thead>" + "    <tfoot>" + "        <tr>" + "            <td><div class=\"footer\">Footer</div></td>"
		+ "        </tr>" + "    </tfoot>" + "    <tbody>" + "        <tr>" + "            <td><p>" + logoTag + "</p>" + "                <p>content 2</p>"
		+ "                <p>content 3</p>" + "                <p>content 4</p>" + "                <p>content 5</p>" + "                <p>content 6</p>"
		+ "                <p>content 7</p>" + "                <p>content 8</p>" + "                <p>content 9</p>" + "                <p>content 10</p>"
		+ "                <p>content 1</p>" + "                <p>content 2</p>" + "                <p>content 3</p>" + "                <p>content 4</p>"
		+ "                <p>content 5</p>" + "                <p>content 6</p>" + "                <p>content 7</p>" + "                <p>content 8</p>"
		+ "                <p>content 9</p>" + "                <p>content 10</p>" + "                <p>content 1</p>" + "                <p>content 2</p>"
		+ "                <p>content 3</p>" + "                <p>content 4</p>" + "                <p>content 5</p>" + "                <p>content 6</p>"
		+ "                <p>content 7</p>" + "                <p>content 8</p>" + "                <p>content 9</p>" + "                <p>content 10</p>"
		+ "                <p>content 1</p>" + "                <p>content 2</p>" + "                <p>content 3</p>" + "                <p>content 4</p>"
		+ "                <p>content 5</p>" + "                <p>content 6</p>" + "                <p>content 7</p>" + "                <p>content 8</p>"
		+ "                <p>content 7</p>" + "                <p>content 8</p>" + "                <p>content 9</p>" + "                <p>content 10</p>"
		+ "                <p>content 1</p>" + "                <p>content 2</p>" + "                <p>content 3</p>" + "                <p>content 4</p>"
		+ "                <p>content 5</p>" + "                <p>content 6</p>" + "                <p>content 7</p>" + "                <p>content 8</p>"
		+ "                <p>content 9</p>" + "                <p>content 10</p>" + "                <p>content 1</p>" + "                <p>content 2</p>"
		+ "                <p>content 3</p>" + "                <p>content 4</p>" + "                <p>content 5</p>" + "                <p>content 6</p>"
		+ "                <p>content 7</p>" + "                <p>content 8</p>" + "                <p>content 9</p>" + "                <p>content 10</p>"
		+ "                <p>content - last</p></td>" + "        </tr>" + "    </tbody>" + "</table>" + "<body>" + "</body>" + "</html>";

	//	<img src=\"data:image/png;base64,

	//	public String HEADER = "<table width=\"100%\" border=\"0\"><tr><td><strong>Header</strong></td><td align=\"right\">222 ALESSANDRO HEADER 2222 </td></tr></table>";

	//	public String HEADER = "<table> <tr>" + "<td>aaa" + "<table border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"height:30px; width:500px\">" + "<tbody>" + "<tr>"
	//		+ "<td>a</td>" + "<td>d</td>" + "</tr>" + "<tr>" + "<td>b</td>" + "<td>e</td>" + "</tr>" + "<tr>" + "<td>c</td>" + "<td>f</td>" + "</tr>" + "</tbody>" + "</table>"
	//		+ "<p>&nbsp;</p>" + "</td>" + "</tr> </table>";

	//OK ALINHAMENTO DA TABLE
	//	public String HEADER = "<table align=\"right\" width=\"50%\" border=\"1\"> " + "<tr> <td>a</td>" + "<td>d</td>" + "</tr>" + "<tr>" + "<td>b</td>" + "<td>e</td>" + "</tr>"
	//		+ "<tr>" + "<td>c</td>" + "<td>f</td>" + "</tr>" + "</table>";
	//	
	//OK ALINHAMENTO DE TEXTO no TD
	//	public String HEADER = "<table align=\"right\" width=\"50%\" border=\"1\"> " + "<tr> <td align=\"right\">adfawerwewrwqrewqrw</td>" + "</tr>" + "</table>";

	public String HEADER1 = "<table align=\"right\" width=\"50%\" border=\"1\"> " + "<tr> <td align=\"right\">adfawerwewrwqrewqrw</td>" + "</tr>" + "</table>";
	public String HEADER2 = "<table align=\"right\" width=\"50%\" border=\"1\"> " + "<tr> <td align=\"left\">adfawerwewrwqrewqrw</td>" + "</tr>" + "</table>";
	public String HEADER = "<p align=\"left\">1223333</p>" + HEADER1 + HEADER2;

	//	public String HEADER3 =
	//		"<table width=\"100%\"> <tr><td style=\"text-align: right;\" align=\"right\">Conteúdo à Direita</td><td style=\"text-align: center;\" align=\"center\">Conteúdo ao CENTRO</td><td>Conteúdo à Esquerda</td></tr> </table>";
	//	
	//	public String HEADER3 =
	//		"<table width=\"100%\"> <tr><td colspan=\"3\"><img alt=\"\" src=\"data:<;base64,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\"/>Conteúdo à Direita</td></tr></table>";

	//	public String HEADER = "fafsdafsfdsfdsafsdafsdafsadfasdfasdfsa";

	//	public String HEADER = "<table width=\"50%\" border=\"0\"><tr><td align=\"right\">222</td></tr></table>";
	//	public String HEADER = "<table width=\"100%\" border=\"0\"><tr><td>Header</td><td align=\"right\">222 ALESSANDRO HEADER 2222 </td> <td align=\"right\"> <img src=\""	+ this.image + "\" /> </td></tr></table>";

	//	public String FOOTER = "<table width=\"100%\" border=\"0\"><tr><td>Footer</td><td align=\"right\">222 ALESSANDRO FOOTER 2222 </td></tr></table>";

	/*	public String FOOTER = "<table> <tr>" + "<td>"
		//		+ "<div><img alt=\"\" src=\"data:&lt;;base64,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\" />Footer</div>"
			+ "<img alt=\"\" src=\"data:&lt;;base64,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\" />Footer"
			+ "</td>" + "</tr> </table>";
	*/
	public String FOOTER = "<p align=\"center\">1223333</p>" + "<p align=\"left\">TESTE TESTE TESTE</p>"
		+ "<p align=\"right\"> <img alt=\"\" src=\"data:&lt;;base64,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\" />Footer"
		+ "</p>";

	public String HEADER3 = "<table width=\"100%\"> " + " <tr> " + " <td style=\"text-align: right;\" align=\"right\"> "
		+ " <img alt=\"\" src=\"data:<;base64,iVBORw0KGgoAAAANSUhEUgAAAJoAAABNCAYAAABexH2AAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAACTdJREFUeNrsXV1S4zgQFhTv4wPs7njfd4twgjgnIDkB4QTACUJOkHACwgkmnCDOCcbU7jven3c8J5iVQgsarWzrL4mV6a8qFUgsqdX9qbsl2QpjBAKBQCAQCAQCgUAgbAtHLoV++/16zN9SmzJ//jG/9RE0ljYbUMLr7X8uXxnaoFxmIe9410Rq07Ur0b7yt55lsTMuTOGhwBf+luy4zRV/y7ZsI0G2nL/W/LXk8laeRBPyrvZAtEYuHTvW23MoM/RQ3tCBZK5y7hrSA93z1wvv6z14pYPCseOIccG5h5x9DyPGBkG6Z67n2x+aaB7G63HlJY5lhzsmaBcwEaHbQ2fRE+3Uoz1rwkAYcSV37CFok28dAtlciOaT9/R3QU5MtAMwUg/yNyLalklzHsBQsWMISy0/BtEgjPl4iMRmMgHeKCOibTCL2TvberQQOY+Nh8oCtHd6IERLPNOIveLEITkNkeDuKmzuY0Iw1RA9ATl8Zbnir8UWZC7Zx12LvRMthHcQyxyp4fbLcMfE9kbTVgyEPpFrTRxTEBvd2eDBd7uui6HTyPhcoT3PfFCta+8Q20v8Ned//urhQaLMOY/31EmTkBgyH0m7pHTYz7whoum9QsgQlAUiY7TG4WRbOhb9fOgeLaRXSGCjvI7UaWBydHUryiV8podOtNDLBP0dJvBdNc5B7GOGJlro8DPcUdjcEK1ri50ei99VjEQ72RLRlgbJfNowVc8CtaHKn3dI92PHck9bkKXvcVuSuFN4EYRoDqPvzpAE4pq50pbpTY53sRKN9/Gava6luWAbHi3zSFeETsMQzSHHqUCANuH7KtEsEvcCkmlT2U73SKwU5BT6uPDMGQsWIU4sGG8zdS+4ctcG5YaWudvbKBJrUbwNG6KlOyLV9y1WLxZ88xiJdrxFb7A0NMxQM/LbsHYMD7FjGavgx1vwBoX0asxsnejc0pthhVsRritbUR64O3Si2RioUhJFG09jsqxRejxCl0ZMsoXPo4OdJ5rD1hNWxqOJ8ZGnMV3WcE2MY/VoPnuj0UwGbL3AN0uPtgmZFs8yrj2m+v1ISTbwfbA4htBpOxHI0eyzMkxg+4bepsKb0Q4zsDRSkhUscph4NN9wszZI8jNbEivGMF1M3mxFReIdBLkud0Sy3HEmzwwnfOGJpvEywgPNArXzWGMQmzyya1tROsNNTbZ1AmK97TtsT1omAimz23qqNMQrDRdWE8OR19pmZESrYLDIQ16iD5MuHs02pykaZorXvqGkZgP+idnteW77xsGBRX8q9oOgjWi2SxtlQ8jzJdqjT46wqyWOWLeI9j3rtJ1x/tWgfN/RuwxEtIzM3j2ihQqdjPnt0zXtBljnNId4/ljsRLMNM1XLMofP9LsuVLl4yh6ZviNEc3nqqSU/8fFoj65EJKJ136MFDS/geXLHsm0ktfVqp2T67hDNeevJwzO5ekLb++jJo3WIaCHzM9cQZ5rb2c48aTIQMdFavYrFzZC2Hs22ztBP3hNciOb4zGEZkDgSheHJOaVD3yl8dsCjuYQWU2PbLHMYhVrHY5w+k/n3TzSXsFIYkmJpMUt8CN0+ebRuEc16+m+5cGriqSrLOxlslzgoR4swdNrOJk2WOWwXeK13Hmgraneou3vD9kEIW2+yNMjpbPOuhQPh2+S+YfGd+COiwMCyTElDgUAgEAgEAoFAIBAI8eKIVGCHn3/5KWWv64zVP3//W2i+z+DPgn9fdUju4HLxOsXSz2aHhdeZN1170lDJDFUyQMLKIzHFEUqi8mf2utZ0plO8Up94EmrOr7uxNOwzKOis5hrxuN2Xpms0ZUTf1AebS9Ev3A9+3S30ecQ/F+t/Y/hf9F23XrWC9wHSoSzratA3WyA88DoXFtW8ycXCPdfaQ/UeORGNvT4rmUpDgqLEsZhyZIiV+IK9L2j2WPN+o1RUamD4G2TsGWtfhEyVNkxQ9xONQy6LGDQlkPwKBsfS0RC1fbasR5V1zSLCiYHxBMQhLEuls6dgDPFrbuJOiCV2z0jJpbiOvR8iN0XEStD1GapfvBfgmsV9bg9wbaW6a2izAPmeNCHOJFzIHQD5Y2BXvLzcJhuBLFlDGK0LHaKvpyCbVibcH6SrOhTsfdembOqnUm/RJDuS5S0tgPp77H3XIFVsaxV+jxq8y1clpIyUzzZhil/7HbnklYZo8rsJEEgo/xNrf6D4DOrDW0DiYOVHTTsFIt8Rl+mefTxefXMqjxISMyWcVEr/BHKRNijXTqEv6uEyC37tZY0+pqDDe4WEJXhsXI/w5nPFHivQXcne72jJwfj3Sj9vgNgrZAMs66Cm3CVcP4F2EnjNQZeJUlcOfVhJvTcZ02RTvYT/Z3gktYSplH383UrVG0gSLKCDc/bxCPEF5FwJ1DOCz6+VduU5GphAY6V+qbQvLfkLJplJDpOwj/urY8gV6zBDg2OOPktARlnPTOc9kV4n8LpAZBmBnhKoc4z0tFA8Wq+m3L3G/jnoPAGCTZnjg+DHLbkFPt8sQ5MA7Pm0oYgz/LbBYDkinFDMN/bx3rMn1NmJQpJzpZ2BUraPPO4lEHmjvAYDMsVrmuQ/m/qh/apl8PWRRxjBZChHn51BPWXNwMSDforIIfEFTdISROocdDCq0Z9aDh9UKGVSbep08mRdjvYZjT58boYk3gwJV6eQJtxBvX00i6sbKSP4LkXeNavLPVA9CeQpiYFcA5xj8XLXysjWJfOyfqaEFdYgE1P6wdAgKFn7nSIlGFvKOIS6LpW6ZZqSaOxUIV2o5c6lbjW5YuozsWkLnSUYQAq3VATIHNr8BKFq0iB0gUh0BQqYgXtvc92PqA8vyBvmLYm2zuMKAnxFA2up6OgZXkzzPVM8tGx7xet8RsZlELZfUIgyWbZYogF4gULpFfLwPWjrWdGPrtwF+3gsrNqnCdQ1C+nRpLLXKM73UAcWaHaSo5GSK6OmQKPnE/z9jb3/jlOKwlWulB+AAoZA6AraLzXtvLUNM9ERKCRFyrrUeJlc54nEpAFm01coHMrkV+aFclYmZ2A3MAvX6UNOpmbQF6k7/NnbdZoBUageHNqSOhqj8mJ9TawAnEIkSqH/eIY/0pR7REm+OiNPoZ9yciAnYM4PhRMIBAKBQCAQCAQCgUAgEAiE/+M/AQYAZVhrKJYF3HYAAAAASUVORK5CYII=\"/></td> "
		+ " <td style=\"text-align: center;\" align=\"center\"> "
		+ " <img alt=\"\" src=\"data:<;base64,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\"/></td> "
		+ " <td> "
		+ " <img alt=\"\" src=\"data:<;base64,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\" style=\"float:left\" /> "
		+ " </td></tr> </table> ";

	public String HEADER4 = "<table width=\"100%\" border=\"0\">" + "<tr>" + "<td colspan=\"3\" style=\"text-align: center;\" align=\"center\">"
		+ "<img alt=\"\" src=\"data:<;base64,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\" />"
		+ "</td> " + "</tr> " + "<tr> " + "<td colspan=\"3\" style=\"text-align:center\" align=\"center\"> " + "<p>MINISTÉRIO PÚBLICO FEDERAL</p> "
		+ "<p>PROCURADORIA GERAL DA REPÚBLICA</p> " + "<p> SECRETARIA JURÍDICA E DE DOCUMENTAÇÃO</p> " + "<p> DIVISÃO DE EXPEDIENTE</p></td> </tr> " + "</table>";

	public String HEADER5 = "    <thead>" + "        <tr>" + "			<td colspan=\"3\" style=\"text-align:center\">"
		+ "				<img alt=\"\" src=\"data:&lt;;base64,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\" style=\"height:74px; width:67px\" />"
		+ "			</td>" + "        </tr>" + "        <tr>" + "            <td style=\"text-align:center\" colspan=\"3\"> "
		+ "             <p> <strong><span style=\"font-size:15px\"> MINISTÉRIO PÚBLICO FEDERAL </span> </strong></p>"
		+ "             <p> <strong><span style=\"font-size:14px\"> {{nomeUnidadeOrganicaMacro}} </span> </strong></p>"
		+ "             <p> <strong><span style=\"font-size:13px\"> {{nomeUnidadeOrganicaPai}} </span> </strong></p>"
		+ "             <p> <strong><span style=\"font-size:12px\"> {{nomeUnidadeOrganica}} </span> </strong></p>" + "            </td>" + "        </tr>" + "    </thead>";

	public String FOOTER2 = "    <tfoot class=\"footer\">" + "        <tr>" + "        	 <td style=\"text-align:center;width:15%\">"
		+ "        		<img alt=\"\" src=\"data:&lt;;base64,iVBORw0KGgoAAAANSUhEUgAAAJoAAABNCAYAAABexH2AAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAACTdJREFUeNrsXV1S4zgQFhTv4wPs7njfd4twgjgnIDkB4QTACUJOkHACwgkmnCDOCcbU7jven3c8J5iVQgsarWzrL4mV6a8qFUgsqdX9qbsl2QpjBAKBQCAQCAQCgUAgbAtHLoV++/16zN9SmzJ//jG/9RE0ljYbUMLr7X8uXxnaoFxmIe9410Rq07Ur0b7yt55lsTMuTOGhwBf+luy4zRV/y7ZsI0G2nL/W/LXk8laeRBPyrvZAtEYuHTvW23MoM/RQ3tCBZK5y7hrSA93z1wvv6z14pYPCseOIccG5h5x9DyPGBkG6Z67n2x+aaB7G63HlJY5lhzsmaBcwEaHbQ2fRE+3Uoz1rwkAYcSV37CFok28dAtlciOaT9/R3QU5MtAMwUg/yNyLalklzHsBQsWMISy0/BtEgjPl4iMRmMgHeKCOibTCL2TvberQQOY+Nh8oCtHd6IERLPNOIveLEITkNkeDuKmzuY0Iw1RA9ATl8Zbnir8UWZC7Zx12LvRMthHcQyxyp4fbLcMfE9kbTVgyEPpFrTRxTEBvd2eDBd7uui6HTyPhcoT3PfFCta+8Q20v8Ned//urhQaLMOY/31EmTkBgyH0m7pHTYz7whoum9QsgQlAUiY7TG4WRbOhb9fOgeLaRXSGCjvI7UaWBydHUryiV8podOtNDLBP0dJvBdNc5B7GOGJlro8DPcUdjcEK1ri50ei99VjEQ72RLRlgbJfNowVc8CtaHKn3dI92PHck9bkKXvcVuSuFN4EYRoDqPvzpAE4pq50pbpTY53sRKN9/Gava6luWAbHi3zSFeETsMQzSHHqUCANuH7KtEsEvcCkmlT2U73SKwU5BT6uPDMGQsWIU4sGG8zdS+4ctcG5YaWudvbKBJrUbwNG6KlOyLV9y1WLxZ88xiJdrxFb7A0NMxQM/LbsHYMD7FjGavgx1vwBoX0asxsnejc0pthhVsRritbUR64O3Si2RioUhJFG09jsqxRejxCl0ZMsoXPo4OdJ5rD1hNWxqOJ8ZGnMV3WcE2MY/VoPnuj0UwGbL3AN0uPtgmZFs8yrj2m+v1ISTbwfbA4htBpOxHI0eyzMkxg+4bepsKb0Q4zsDRSkhUscph4NN9wszZI8jNbEivGMF1M3mxFReIdBLkud0Sy3HEmzwwnfOGJpvEywgPNArXzWGMQmzyya1tROsNNTbZ1AmK97TtsT1omAimz23qqNMQrDRdWE8OR19pmZESrYLDIQ16iD5MuHs02pykaZorXvqGkZgP+idnteW77xsGBRX8q9oOgjWi2SxtlQ8jzJdqjT46wqyWOWLeI9j3rtJ1x/tWgfN/RuwxEtIzM3j2ihQqdjPnt0zXtBljnNId4/ljsRLMNM1XLMofP9LsuVLl4yh6ZviNEc3nqqSU/8fFoj65EJKJ136MFDS/geXLHsm0ktfVqp2T67hDNeevJwzO5ekLb++jJo3WIaCHzM9cQZ5rb2c48aTIQMdFavYrFzZC2Hs22ztBP3hNciOb4zGEZkDgSheHJOaVD3yl8dsCjuYQWU2PbLHMYhVrHY5w+k/n3TzSXsFIYkmJpMUt8CN0+ebRuEc16+m+5cGriqSrLOxlslzgoR4swdNrOJk2WOWwXeK13Hmgraneou3vD9kEIW2+yNMjpbPOuhQPh2+S+YfGd+COiwMCyTElDgUAgEAgEAoFAIBAI8eKIVGCHn3/5KWWv64zVP3//W2i+z+DPgn9fdUju4HLxOsXSz2aHhdeZN1170lDJDFUyQMLKIzHFEUqi8mf2utZ0plO8Up94EmrOr7uxNOwzKOis5hrxuN2Xpms0ZUTf1AebS9Ev3A9+3S30ecQ/F+t/Y/hf9F23XrWC9wHSoSzratA3WyA88DoXFtW8ycXCPdfaQ/UeORGNvT4rmUpDgqLEsZhyZIiV+IK9L2j2WPN+o1RUamD4G2TsGWtfhEyVNkxQ9xONQy6LGDQlkPwKBsfS0RC1fbasR5V1zSLCiYHxBMQhLEuls6dgDPFrbuJOiCV2z0jJpbiOvR8iN0XEStD1GapfvBfgmsV9bg9wbaW6a2izAPmeNCHOJFzIHQD5Y2BXvLzcJhuBLFlDGK0LHaKvpyCbVibcH6SrOhTsfdembOqnUm/RJDuS5S0tgPp77H3XIFVsaxV+jxq8y1clpIyUzzZhil/7HbnklYZo8rsJEEgo/xNrf6D4DOrDW0DiYOVHTTsFIt8Rl+mefTxefXMqjxISMyWcVEr/BHKRNijXTqEv6uEyC37tZY0+pqDDe4WEJXhsXI/w5nPFHivQXcne72jJwfj3Sj9vgNgrZAMs66Cm3CVcP4F2EnjNQZeJUlcOfVhJvTcZ02RTvYT/Z3gktYSplH383UrVG0gSLKCDc/bxCPEF5FwJ1DOCz6+VduU5GphAY6V+qbQvLfkLJplJDpOwj/urY8gV6zBDg2OOPktARlnPTOc9kV4n8LpAZBmBnhKoc4z0tFA8Wq+m3L3G/jnoPAGCTZnjg+DHLbkFPt8sQ5MA7Pm0oYgz/LbBYDkinFDMN/bx3rMn1NmJQpJzpZ2BUraPPO4lEHmjvAYDMsVrmuQ/m/qh/apl8PWRRxjBZChHn51BPWXNwMSDforIIfEFTdISROocdDCq0Z9aDh9UKGVSbep08mRdjvYZjT58boYk3gwJV6eQJtxBvX00i6sbKSP4LkXeNavLPVA9CeQpiYFcA5xj8XLXysjWJfOyfqaEFdYgE1P6wdAgKFn7nSIlGFvKOIS6LpW6ZZqSaOxUIV2o5c6lbjW5YuozsWkLnSUYQAq3VATIHNr8BKFq0iB0gUh0BQqYgXtvc92PqA8vyBvmLYm2zuMKAnxFA2up6OgZXkzzPVM8tGx7xet8RsZlELZfUIgyWbZYogF4gULpFfLwPWjrWdGPrtwF+3gsrNqnCdQ1C+nRpLLXKM73UAcWaHaSo5GSK6OmQKPnE/z9jb3/jlOKwlWulB+AAoZA6AraLzXtvLUNM9ERKCRFyrrUeJlc54nEpAFm01coHMrkV+aFclYmZ2A3MAvX6UNOpmbQF6k7/NnbdZoBUageHNqSOhqj8mJ9TawAnEIkSqH/eIY/0pR7REm+OiNPoZ9yciAnYM4PhRMIBAKBQCAQCAQCgUAgEAiE/+M/AQYAZVhrKJYF3HYAAAAASUVORK5CYII=\" style=\"height:49px; width:98px\" />"
		+ "        	 </td>" + "            <td style=\"text-align:left;width:25%\"> <span style=\"font-size:11px\"> {{nomeUnidadeOrganicaMacro}} </td>"
		+ "            <td style=\"text-align:left;width:60%\">" + "            	<p> <span style=\"font-size:10px\">{{enderecoUnidadeOrganicaMacro}} </span> </p> "
		+ "            	<p> <span style=\"font-size:10px\">Tel. {{telefoneUnidadeOrganicaMacro}} – Fax:  – Email:{{emailUnidadeOrganicaMacro}} </span> </p> " + "            </td>"
		+ "        </tr>" + "    </tfoot>";

}
