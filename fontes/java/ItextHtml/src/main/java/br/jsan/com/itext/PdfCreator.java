package br.jsan.com.itext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfCreator implements IConstantes {

	private PdfWriter writer;
	private ByteArrayOutputStream baos;
	private java.util.List<String> conteudoHmtl = new ArrayList<String>();
	private HeaderFooter headerFooter;
	private PdfProcessor pdfProcessor;
	private boolean headerNoLoop;
	private boolean footerNoLoop;
	private boolean modeloTemplateMPF;
	private boolean modeloTemplateMPF2;

	/**
	 * Gera um array de bytes baseado num A4, com base na definição do Document do iText.
	 * 
	 * @param html
	 * @return byte[]
	 */
	public byte[] gerarPdf(String html) throws Exception {
		byte[] bytes = null;
		String rodape = null, cabecalho = null, conteudo = null;
		List<String> listaPartesHtml = null;
		boolean bExisteCabecalhoRodape = false;
		this.headerNoLoop = html.contains(ID_HEADER_NO_LOOP_HTML);
		this.footerNoLoop = html.contains(ID_FOOTER_NO_LOOP_HTML);
		this.modeloTemplateMPF = html.contains(ID_MODELO_TEMPLATE_MPF_HTML);
		this.modeloTemplateMPF2 = html.contains(ID_MODELO_TEMPLATE_MPF_HTML2);

		try {
			if (this.modeloTemplateMPF) {
				listaPartesHtml = PdfHtmlUtil.getInstancia().extrairCabecalhoERodape(html);

				if (listaPartesHtml != null && listaPartesHtml.size() > 0) {
					if (listaPartesHtml != null && listaPartesHtml.size() == 3) {
						cabecalho = listaPartesHtml.get(0);
						rodape = listaPartesHtml.get(1);
						conteudo = listaPartesHtml.get(2);
						bExisteCabecalhoRodape = true;
					} else {
						conteudo = listaPartesHtml.get(0);
					}
				}
			} else {
				conteudo = html;
				conteudo = TR_HTML_START_TAG + " " + TD_HTML_START_TAG + conteudo + TD_HTML_END_TAG + " " + TR_HTML_END_TAG;
			}

			//Document A4 - Disparando evento para gerar cabeçalho e rodapé
			prepararDocumento(rodape, cabecalho, conteudo, bExisteCabecalhoRodape);

			bytes = this.baos.toByteArray();
		} catch (Exception e) {
			throw e;
		}

		return bytes;
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
	 * Os valores de início são:
	 * 85.35826771653F, 56.90551181102F, 200, 130.90551181102F
	 * 
	 * document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 200, 130.90551181102F);
	 * 
	 * @param rodape
	 * @param cabecalho
	 * @param conteudo
	 * @param bExisteCabecalhoRodape
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void prepararDocumento(String rodape, String cabecalho, String conteudo, boolean bExisteCabecalhoRodape) throws Exception {
		Document document = null;

		//		if (this.modeloTemplateMPF2) {
		//			document = new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_MODELO_MPF2,
		//				IConstantes.DOCUMENT_MARGIN_BOTTOM);
		//		} else 
		if (this.modeloTemplateMPF) {
			document = new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_MODELO_MPF,
				IConstantes.DOCUMENT_MARGIN_BOTTOM);
		} else {
			document = new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_NOTEMPLATE,
				IConstantes.DOCUMENT_MARGIN_BOTTOM);
		}

		this.baos = new ByteArrayOutputStream();
		this.writer = PdfWriter.getInstance(document, this.baos);

		//Processador das tags HTML
		this.pdfProcessor = new PdfProcessor(this.writer);

		//Adicionando Header e Footer - disparando evento
		if (bExisteCabecalhoRodape) {
			this.headerFooter = new HeaderFooter(cabecalho, rodape, this.pdfProcessor);
			this.headerFooter.setCabecalhoSomentePaginaUm(this.headerNoLoop);
			this.headerFooter.setRodapeSomentePaginaUm(this.footerNoLoop);

			//Processa o cabeçalho e rodapé.
			this.writer.setPageEvent(headerFooter);
			//			System.out.println(" **** Total de Linhas = " + this.headerFooter.getLinhas());
		}

		document.open();

		//Adicionando o html ao conteúdo
		conteudo = TABLE_WIDTH_BORDER_ZERO_HTML + conteudo + " " + TABLE_HTML_END_TAG;
		conteudoHmtl.add(conteudo);
		if (this.modeloTemplateMPF && this.headerNoLoop) {
			this.processarConteudoCorpo(document, conteudoHmtl);
		} else if (this.modeloTemplateMPF) {
			this.processarConteudoCorpoComRepeticaoCabecalho(document, conteudoHmtl);
		} else {
			this.processarConteudoCorpo(document, conteudoHmtl);
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
		if (pagesHmtl != null) {
			for (String page : pagesHmtl) {
				try {
					for (Element e : this.pdfProcessor.parseToElementList(page, null)) {
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
					for (Element e : this.pdfProcessor.parseToElementList(page, null)) {
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

}
