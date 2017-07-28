package br.jsan.com.itext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfCreator extends IConstantes {

	private PdfWriter writer;
	private ByteArrayOutputStream baos;
	private java.util.List<String> conteudoHmtl = new ArrayList<>();
	private HeaderFooter headerFooter;
	private PdfProcessor pdfProcessor;
	private boolean headerNoLoop;
	private boolean footerNoLoop;
	private boolean modeloTemplateMPF;
	private byte[] bytes;

	private float totalAlturaCabecalho;
	private float totalAlturaRodape;

	private Logger logger = java.util.logging.Logger.getLogger("PdfCreator");

	/**
	 * Gera um array de bytes baseado num A4, com base na definição do Document do iText.
	 * 
	 * @param html
	 * @return byte[]
	 */
	public byte[] gerarPdf(String htmlPadrao) throws Exception {

		String rodape = null;
		String cabecalho = null;
		String html = htmlPadrao;
		boolean bExisteCabecalhoRodape = false;

		if (html == null || "".equalsIgnoreCase(html)) {
			html = "<p>&nbsp;</p>";
		}

		this.headerNoLoop = html.contains(ID_HEADER_NO_LOOP_HTML);
		this.footerNoLoop = html.contains(ID_FOOTER_NO_LOOP_HTML);
		this.modeloTemplateMPF = html.contains(ID_MODELO_TEMPLATE_MPF_HTML);

		String style = PdfHtmlUtil.getInstancia().getStyleClassForDocument();

		//Funciona para a classe usada no td, é preciso definir atributos no config.js.
		//Foi definido uma classe myborder em um td específico de um tfoot
		String styleRodape = PdfHtmlUtil.getInstancia().getStyleClassForRodapePrincipal();

		try {
			registrarFontesParaPDF();

			if (this.modeloTemplateMPF) {
				processarHtmlTemplate(rodape, cabecalho, html, bExisteCabecalhoRodape, style, styleRodape);
			} else {
				processarHtmlNormal(html, style);
			}

		} catch (Exception e) {
			throw e;
		}

		return bytes;
	}

	private void processarHtmlNormal(String html, String style) throws Exception {
		String conteudo;
		conteudo = html;
		conteudo = style + conteudo;

		this.pdfProcessor = new PdfProcessor();
		bytes = this.pdfProcessor.parseToPDFStream(conteudo);

		//Para escrever a páginação no final da página
		escreverNumerosPaginas(null);

		bytes = this.baos.toByteArray();
	}

	private void processarHtmlTemplate(String rodapeOriginal, String cabecalhoOriginal, String html, boolean bExisteCabecalhoRodapeOriginal, String style, String styleRodape)
		throws Exception {
		String cabecalho = cabecalhoOriginal;
		String rodape = rodapeOriginal;
		boolean bExisteCabecalhoRodape = bExisteCabecalhoRodapeOriginal;

		String conteudo;
		List<String> listaPartesHtml;
		listaPartesHtml = PdfHtmlUtil.getInstancia().extrairCabecalhoERodape(html);

		if (Util.isEmpty(listaPartesHtml)) {
			throw new PdfCreatorException("Template sem blocos prédefinidos.");
		}

		if (listaPartesHtml.size() == 3) {
			cabecalho = listaPartesHtml.get(0);
			rodape = listaPartesHtml.get(1);
			conteudo = listaPartesHtml.get(2);
			bExisteCabecalhoRodape = true;
		} else {
			conteudo = listaPartesHtml.get(0);
		}

		//Adicionando o html ao conteúdo, após retirar de dentro de table,tr,td
		conteudo = HTML_HTML_START_TAG + BODY_HTML_START_TAG + PdfHtmlUtil.getInstancia().recuperarHtmlSobTagPai(conteudo) + BODY_HTML_END_TAG + HTML_HTML_END_TAG;

		conteudo = style + conteudo;

		PdfProcessor pdfProcessorDimension = PdfHtmlUtil.getInstancia().criarPdfProcessorPorWriter();

		this.totalAlturaCabecalho = PdfHtmlUtil.getInstancia().definirAlturaEmPointParaElementosDoCabecalho(cabecalho, pdfProcessorDimension);

		this.totalAlturaRodape = PdfHtmlUtil.getInstancia().definirAlturaEmPointParaElementosDoRodape(rodape, pdfProcessorDimension);

		//Document A4 - Disparando evento para gerar cabeçalho e rodapé
		if (Util.isNotEmpty(rodape)) {
			rodape = styleRodape + "" + rodape;
		}
		prepararDocumento(rodape, cabecalho, conteudo, bExisteCabecalhoRodape);

		bytes = this.baos.toByteArray();
	}

	/**
	 * Gera o objeto documento baseado no A4. Define as margens de cabeçalho, rodapé e margens esquerda e direita
	 * 
	 * pageSize the pageSize
	 * marginLeft the margin on the left
	 * marginRight the margin on the right
	 * marginTop the margin on the top ***
	 * marginBottom the margin on the bottom ***
	 * Document document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 500.90551181102F, 100.90551181102F);
	 * 
	 * A altura do cabeçalho vai diferença entre o primeiro e o terceiro parâmetro.
	 * 
	 * A altura do rodapé vai diferença entre o segundo e o quarto parâmetro.
	 * 
	 * Os valores padrões são:
	 * 85.35826771653F, 56.90551181102F, 200, 130.90551181102F
	 * 
	 * document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 200, 130.90551181102F);
	 * 
	 * valor TOP é definido pelo totalAlturaCabecalho e o BOTTOM pelo totalAlturaRodape (calculados dinamicamente).
	 * 
	 * @param rodape
	 * @param cabecalho
	 * @param conteudo
	 * @param bExisteCabecalhoRodape
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void prepararDocumento(String rodape, String cabecalho, String conteudo, boolean bExisteCabecalhoRodape) throws Exception {
		Document document;

		if (Float.valueOf(Math.abs(this.totalAlturaRodape)).equals(0f)) {
			document =
				new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, this.totalAlturaCabecalho + ACRESCIMO_MARGEM_CABECALHO_PARA_CONTEUDO,
					this.totalAlturaRodape + Utilities.millimetersToPoints(DISTANCIA_CONTEUDO_MARGEM_RODAPE_ZERO));
		} else {
			document =
				new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, this.totalAlturaCabecalho + ACRESCIMO_MARGEM_CABECALHO_PARA_CONTEUDO,
					this.totalAlturaRodape + Utilities.millimetersToPoints(DISTANCIA_CONTEUDO_MARGEM_RODAPE_PADRAO));
		}

		this.baos = new ByteArrayOutputStream();
		this.writer = PdfWriter.getInstance(document, this.baos);

		//Processador das tags HTML
		this.pdfProcessor = new PdfProcessor(this.writer);

		//Adicionando Header e Footer - disparando evento
		processaCabecalhoRodape(rodape, cabecalho, bExisteCabecalhoRodape);

		//processa o conteúdo principal
		processarConteudo(conteudo, document);

		//Para escrever a páginação no final da página
		escreverNumerosPaginas(document);
	}

	/**
	 * Processamento do Cabeçalho e Rodapé.
	 * 
	 * @param rodape
	 * @param cabecalho
	 * @param bExisteCabecalhoRodape
	 * @throws Exception
	 */
	private void processaCabecalhoRodape(String rodape, String cabecalho, boolean bExisteCabecalhoRodape) throws PdfCreatorException {
		try {
			if (bExisteCabecalhoRodape) {
				this.headerFooter = new HeaderFooter(cabecalho, rodape, this.pdfProcessor, this.totalAlturaCabecalho, this.totalAlturaRodape);
				this.headerFooter.setCabecalhoSomentePaginaUm(this.headerNoLoop);
				this.headerFooter.setRodapeSomentePaginaUm(this.footerNoLoop);

				//Processa o cabeçalho e rodapé.
				this.writer.setPageEvent(headerFooter);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "processaCabecalhoRodape = " + e);
			throw new PdfCreatorException(e.getMessage());
		}
	}

	/**
	 * Processa o conteúdo principal (body)
	 * 
	 * @param conteudo
	 * @param document
	 * @throws DocumentException
	 */
	private void processarConteudo(String conteudo, Document document) throws DocumentException {
		if (!this.modeloTemplateMPF) {
			document.setMargins(40, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_NOTEMPLATE, IConstantes.DOCUMENT_MARGIN_BOTTOM_REDUCED);
			document.setMarginMirroring(false);
		}

		document.open();

		conteudoHmtl.add(conteudo);

		if (this.modeloTemplateMPF) {
			if (this.headerNoLoop) {
				this.processarConteudoCorpoSemRepeticaoCabecalho(document, conteudoHmtl);
			} else {
				this.processarConteudoCorpoComRepeticaoCabecalho(document, conteudoHmtl);
			}
		} else {
			this.processarConteudoCorpoSemRepeticaoCabecalho(document, conteudoHmtl);
		}

		document.close();
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
		this.processarConteudoCorpoComOuSemRepeticaoCabecalho(document, pagesHmtl, this.totalAlturaCabecalho + ACRESCIMO_MARGEM_CABECALHO_PARA_CONTEUDO);
	}

	/**
	 * Adiciona o conteúdo do HTML.Este conteúdo vai para a área central do documento.
	 * 
	 * Neste caso a margem do cabeçalho é menor porque não vai existir nenhum conteúdo no cabeçalho.
	 * 
	 * A divisão por 4 é para a partir da segunda página, o cabeçalho aparecer mais perto do topo.
	 * 
	 * Primeiro formato usado, sempre passando a altura do cabelçalho, mas tem que levar em consideração a partir da segunda página:
	 * document.setMargins(IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, this.totalAlturaCabecalho, this.totalAlturaRodape + Utilities.millimetersToPoints(30));
	 * 
	 * @param document
	 * @param pagesHmtl
	 * @throws DocumentException
	 */
	private void processarConteudoCorpoSemRepeticaoCabecalho(Document document, java.util.List<String> pagesHmtl) throws DocumentException {
		this.processarConteudoCorpoComOuSemRepeticaoCabecalho(document, pagesHmtl, Utilities.millimetersToPoints(20));
	}

	private void processarConteudoCorpoComOuSemRepeticaoCabecalho(Document document, List<String> pagesHmtl, float medidaCabecalho) {
		if (pagesHmtl == null) {
			return;
		}

		for (String page : pagesHmtl) {
			try {
				for (Element e : this.pdfProcessor.parseToElementList(page, null)) {
					if (Float.valueOf(Math.abs(this.totalAlturaRodape)).equals(0f)) {
						document.setMargins(
							IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, medidaCabecalho,
							this.totalAlturaRodape + Utilities.millimetersToPoints(DISTANCIA_CONTEUDO_MARGEM_RODAPE_ZERO));
					} else {
						document.setMargins(
							IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, medidaCabecalho,
							this.totalAlturaRodape + Utilities.millimetersToPoints(DISTANCIA_CONTEUDO_MARGEM_RODAPE_PADRAO));
					}
					document.add(e);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "processarConteudoCorpoComOuSemRepeticaoCabecalho = " + e);
			}
		}

	}

	/**	
	 * Este código gera um novo array de bytes. O número de páginas são inseridos.
	 * @param document
	 * @throws Exception
	 */
	private void escreverNumerosPaginas(Document documentOriginal) throws Exception {
		Document document = documentOriginal;

		if (document == null) {
			document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 56.90551181102F, 56.90551181102F);
		}

		document.open();
		if (this.headerFooter != null) {
			this.baos = this.headerFooter.escreverNumerosDePagina(this.baos.toByteArray());
		} else {
			HeaderFooter tmp = new HeaderFooter();

			if (this.baos != null) {
				this.baos = tmp.escreverNumerosDePagina(this.baos.toByteArray());
			} else {
				this.baos = tmp.escreverNumerosDePagina(this.bytes);
			}
		}
		document.close();
	}

	private void registerFont(String fontPathName, String nomeAssociado) throws PdfCreatorException {
		URL urlFontPath;

		try {
			urlFontPath = new URL(fontPathName);

			if (Util.isNotEmpty(urlFontPath)) {
				FontFactory.register(urlFontPath.toString(), nomeAssociado);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "processaCabecalhoRodape = " + e);
			throw new PdfCreatorException(e.getMessage());
		}

	}

	/**
	 * TODO - passar o path de registro de fontes
	 * @throws PdfCreatorException
	 */
	private void registrarFontesParaPDF() throws PdfCreatorException {
		//		this.registerFont(Util.getResourcePath("times.ttf", PATH_FILES_TTF), "times new roman");
		//		this.registerFont(Util.getResourcePath("cour.ttf", PATH_FILES_TTF), "courier");
		//		this.registerFont(Util.getResourcePath("tahoma.ttf", PATH_FILES_TTF), "tahoma");
		//		this.registerFont(Util.getResourcePath("verdana.ttf", PATH_FILES_TTF), "verdana");
		//		this.registerFont(Util.getResourcePath("lsansuni.ttf", PATH_FILES_TTF), "lucida sans unicode");
		//		this.registerFont(Util.getResourcePath("lsansi.ttf", PATH_FILES_TTF), "lucida sans ansi");
		//		this.registerFont(Util.getResourcePath("Georgia.ttf", PATH_FILES_TTF), "Georgia");
	}

	/**
	 * 
	 * import javax.servlet.http.HttpServletRequest;
	 * import org.apache.struts2.ServletActionContext;
	 * import org.springframework.web.context.request.RequestContextHolder;
	 * import org.springframework.web.context.request.ServletRequestAttributes;
	 * 
	 */
	//	public static String getResourcePath(String filename, String extraPath) {
	//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	//		String url = request.getRequestURL().toString();
	//		String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
	//
	//		if (extraPath != null) {
	//			baseURL += extraPath;
	//		}
	//
	//		return baseURL + filename;
	//	}

}
