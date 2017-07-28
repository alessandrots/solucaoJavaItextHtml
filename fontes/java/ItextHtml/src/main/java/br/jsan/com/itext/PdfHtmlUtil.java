package br.jsan.com.itext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;

public class PdfHtmlUtil extends IConstantes {

	private static PdfHtmlUtil pdfHtmlUtil;

	private Logger logger = java.util.logging.Logger.getLogger("PdfHtmlUtil");

	public static PdfHtmlUtil getInstancia() {
		if (pdfHtmlUtil == null) {
			pdfHtmlUtil = new PdfHtmlUtil();
		}

		return pdfHtmlUtil;
	}

	/**
	 * A lista vai guardar na seguinte sequência:
	 * 	0: posição inicial <thead>
	 * 	1: posição inicial </thead>
	 * 	2: posição inicial <tfoot>
	 * 	3: posição inicial </tfoot>
	 * 	4: posição inicial <tbody>
	 *  5: posição inicial </tbody>
	 * 
	 * Busca a string <thead> até </thead> (ini, fim + 8) => o +8 corresponde ao tamanho total desta string.
	 * 
	 * Busca a string <tfoot> até </tfoot> (ini, fim + 8) => o +8 corresponde ao tamanho total desta string.
	 * 
	 * Conteúdo atualizado = a string <thead> até </tfoot> (ini, fim + 8)
	 *  
	 * @param conteudo
	 */
	public List<String> extrairCabecalhoERodape(String conteudoOld) throws PdfCreatorException {
		String conteudo = conteudoOld;
		List<String> listaHeadFooterConteudo = new ArrayList<>();
		List<Integer> listaCabecalhoRodape = gerarListaTagsTable(conteudo);
		String footer = "";
		String header = "";

		try {
			if (Util.isNotEmpty(listaCabecalhoRodape)) {
				if (listaCabecalhoRodape.get(0) != -1 && listaCabecalhoRodape.get(1) != -1) {
					header = conteudo.substring(listaCabecalhoRodape.get(0), listaCabecalhoRodape.get(1) + 8);
					header = limparTags(header, THEAD_HTML);
					header = this.adicionarAlignHouverTextAlign(header);
					header = limparTags(header, DIV_HTML);
					header = TABLE_WIDTH_BORDER_ZERO_HTML + " " + header + " " + TABLE_HTML_END_TAG;
					listaHeadFooterConteudo.add(header);
				} else {
					//tratamento para qdo o thead for retirado do template
					listaHeadFooterConteudo.add(header);
				}

				if (listaCabecalhoRodape.get(2) != -1 && listaCabecalhoRodape.get(3) != -1) {
					footer = conteudo.substring(listaCabecalhoRodape.get(2), listaCabecalhoRodape.get(3) + 8);
					footer = limparTags(footer, TFOOT_HTML);
					footer = limparTags(footer, DIV_HTML);
					footer = this.adicionarAlignHouverTextAlign(footer);
					footer = TABLE_WIDTH_BORDER_ONE_HTML + footer + " " + TABLE_HTML_END_TAG;
					listaHeadFooterConteudo.add(footer);
				} else {
					//tratamento para qdo o tfoot for retirado do template
					listaHeadFooterConteudo.add(footer);
				}

				if (listaCabecalhoRodape.get(4) != -1 && listaCabecalhoRodape.get(5) != -1) {
					conteudo = conteudo.substring(listaCabecalhoRodape.get(4), listaCabecalhoRodape.get(5) + 8);
					conteudo = limparTags(conteudo, TBODY_HTML);
					listaHeadFooterConteudo.add(conteudo);
				} else {
					listaHeadFooterConteudo.add("");
				}
			}
		} catch (Exception e) {
			throw new PdfCreatorException(e);
		}

		return listaHeadFooterConteudo;
	}

	/**
	 * Gera a lista de tags a serem processadas.
	 * 
	 * @param conteudo
	 * @return List<Integer>
	 */
	private List<Integer> gerarListaTagsTable(String conteudo) {
		List<Integer> listaCabecalhoRodape = new ArrayList<>();

		this.adicionarPosicaoNaLista(conteudo, THEAD_HTML_START_TAG, listaCabecalhoRodape);

		this.adicionarPosicaoNaLista(conteudo, THEAD_HTML_END_TAG, listaCabecalhoRodape);

		this.adicionarPosicaoNaLista(conteudo, TFOOT_HTML_START_TAG, listaCabecalhoRodape);

		this.adicionarPosicaoNaLista(conteudo, TFOOT_HTML_END_TAG, listaCabecalhoRodape);

		this.adicionarPosicaoNaLista(conteudo, TBODY_HTML_START_TAG, listaCabecalhoRodape);

		this.adicionarUltimaPosicaoNaLista(conteudo, TBODY_HTML_END_TAG, listaCabecalhoRodape);

		return listaCabecalhoRodape;
	}

	/**
	 * Guardando a primeira posição aonde aparece a tag.
	 * 
	 * @param conteudo
	 * @param tag
	 * @param listaCabecalhoRodape
	 */
	private void adicionarPosicaoNaLista(String conteudo, String tag, List<Integer> listaCabecalhoRodape) {
		if (conteudo.indexOf(tag) != -1) {
			listaCabecalhoRodape.add(conteudo.indexOf(tag));
		} else {
			listaCabecalhoRodape.add(-1);
		}
	}

	private void adicionarUltimaPosicaoNaLista(String conteudo, String tag, List<Integer> listaCabecalhoRodape) {
		if (conteudo.lastIndexOf(tag) != -1) {
			listaCabecalhoRodape.add(conteudo.lastIndexOf(tag));
		} else {
			listaCabecalhoRodape.add(-1);
		}
	}

	/**
	 * Método para limpar as tags HTML (strPattern) da string passada (str).
	 * 
	 * @param str
	 * @param strPattern
	 * @return String
	 */
	protected String limparTags(String strOld, String strPattern) {
		String str = strOld;

		if (str != null && str.indexOf("<" + strPattern + ">") != -1) {
			StringBuilder stb = new StringBuilder();
			str = str.substring(str.indexOf("<" + strPattern + ">"));
			Pattern pattern = Pattern.compile("<*?.(" + strPattern + ")>");
			String[] result = pattern.split(str.trim());

			List<String> list = new ArrayList<>(Arrays.asList(result));
			for (String s : list) {
				if (s != null && !"".equals(s.trim())) {
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
	 * para o caso de ter vários trs no thead ou no tfoot html += TR_HTML_START_TAG + s + TR_HTML_END_TAG;
	 * 
	 * @param html
	 * @return String
	 */
	protected String adicionarAlignHouverTextAlign(String htmlOld) {
		String html = htmlOld;
		if (html.indexOf(TABLE_HTML) == -1) {
			html = TABLE_HTML_START_TAG + html + TABLE_HTML_END_TAG;
		}

		boolean bVariosTrs = false;
		StringBuilder stb = new StringBuilder();

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
					html = verificarNiveisTrHtml(html, bVariosTrs, stb, s);
				}
			}
		}

		if (!bVariosTrs) {
			html += TR_HTML_START_TAG + stb.toString() + TR_HTML_END_TAG;
		}

		return html;
	}

	private String verificarNiveisTrHtml(String htmlOld, boolean bVariosTrs, StringBuilder stb, String s) {
		String html = htmlOld;

		if (bVariosTrs) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(html);
			tmp.append(TR_HTML_START_TAG);
			tmp.append(s);
			tmp.append(TR_HTML_END_TAG);
			html = tmp.toString();
		} else {
			// este caso são vários tds num tr
			stb.append(s);
		}
		return html;
	}

	protected String recuperarHtmlSobTagPai(String htmlOld) {
		String html = htmlOld;
		if (Util.isEmpty(html)) {
			html = "&nbsp;&nbsp;";
		} else if (html.indexOf(TABLE_HTML_START_TAG) == -1) {
			html = "<table style=\"border:'0'; width:100%\"> " + html + TABLE_HTML_END_TAG;
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

		if (atributos == null) {
			return alinhamento;
		}

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

		return Jsoup.clean(textToEscape, "", whitelist, outputSettings);
	}

	/**
	 * 
	 * The measurement used in PDF is called the user unit. By default 1 user unit equals 1 point.
	 * There are 72 points in one inch.
	 * This explains why you document is smaller than expected if you pass a value that is expressed in millimeters rather than user units.
	 * 
	 */
	public float definirAlturaEmPointParaElementosDoCabecalho(String html, PdfProcessor pdfProcessor) throws DocumentException {
		ElementList elementosCabecalho;
		float totalAltura = 0;

		try {
			elementosCabecalho = pdfProcessor.parseToElementList(html, null);

			for (Element e : elementosCabecalho) {
				if (e instanceof PdfPTable && ((PdfPTable) e).getRows() != null) {
					List<PdfPRow> listaLinhasPDF = ((PdfPTable) e).getRows();
					totalAltura = percorrerCelulasPorLinhasParaCalcularTotalAlturaMilimetros(listaLinhasPDF);
				}
			}
		} catch (Exception e1) {
			logger.log(Level.SEVERE, " ==> definirDimensoesElementosDoCabecalho = " + e1);
		}

		totalAltura += 20;

		return Utilities.millimetersToPoints(totalAltura);
	}

	private float percorrerCelulasPorLinhasParaCalcularTotalAlturaMilimetros(List<PdfPRow> lista1LinhasPDF) {
		float totalAlturaMilimetros = 0;

		for (PdfPRow cmp : lista1LinhasPDF) {
			for (PdfPCell pdfPCell : cmp.getCells()) {
				if (pdfPCell != null) {
					totalAlturaMilimetros += Utilities.pointsToMillimeters(pdfPCell.getHeight());
				}
			}
		}

		return totalAlturaMilimetros;
	}

	public float definirAlturaEmPointParaElementosDoRodape(String html, PdfProcessor pdfProcessor) throws DocumentException {
		ElementList elementosCabecalho;
		float totalAltura = 0;

		try {
			elementosCabecalho = pdfProcessor.parseToElementList(html, null);

			for (Element e : elementosCabecalho) {
				if (e instanceof PdfPTable && ((PdfPTable) e).getRows() != null) {
					List<PdfPRow> listaLinhasPDF = ((PdfPTable) e).getRows();
					totalAltura = calcularMaiorTotalAlturaMilimetros(listaLinhasPDF);
				}
			}
		} catch (Exception e1) {
			logger.log(Level.SEVERE, " ==> definirAlturaEmPointParaElementosDoRodape = " + e1);
		}

		return Utilities.millimetersToPoints(totalAltura);
	}

	private float calcularMaiorTotalAlturaMilimetros(List<PdfPRow> lista1LinhasPDF) {
		float totalAlturaMilimetros = 0;

		for (PdfPRow cmp : lista1LinhasPDF) {
			for (PdfPCell pdfPCell : cmp.getCells()) {
				if (pdfPCell != null) {
					if (Utilities.pointsToMillimeters(pdfPCell.getHeight()) > totalAlturaMilimetros) {
						totalAlturaMilimetros = Utilities.pointsToMillimeters(pdfPCell.getHeight());
					}
				}
			}
		}

		return totalAlturaMilimetros;
	}

	public PdfProcessor criarPdfProcessorPorWriter() throws DocumentException {
		PdfProcessor pdfProcessor;
		Document document = new Document(PageSize.A4, IConstantes.DOCUMENT_MARGIN_LEFT, IConstantes.DOCUMENT_MARGIN_RIGHT, IConstantes.DOCUMENT_MARGIN_TOP_MODELO_MPF,
			IConstantes.DOCUMENT_MARGIN_BOTTOM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = PdfWriter.getInstance(document, baos);

		//Processador das tags HTML
		pdfProcessor = new PdfProcessor(writer);
		return pdfProcessor;
	}

	/**
	 * Para definir a margem de impressão.
	 * 
	 * @return String
	 */
	public String getStyleClassForDocument() {
		String style;

		style = " <style type=\"text/css\"> ";
		style += " @page{ width: 21cm; height: 29.7cm; margin-bottom: 3cm; margin-up: 2cm;} ";
		style += " p {font-family: times, serif; font-size: 12px; line-height: 1;} ";
		style += " </style>";

		return style;
	}

	/**
	 * O funcionamento desse método só é possível graças a configuração no config.js do ckEditor.
	 * 
	 * Conforme a explicação abaixo:
	 *  https://stackoverflow.com/questions/17835871/ckeditor-removes-class-attribute-from-table
	 *  http://docs.ckeditor.com/#!/guide/dev_allowed_content_rules-section-string-format
	 *  http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-allowedContent
	 *  http://sdk.ckeditor.com/samples/acf.html
	 * 
	 * @return String
	 */
	public String getStyleClassForRodapePrincipal() {
		String styleRodape;

		styleRodape = " <style type=\"text/css\">";
		styleRodape += "  .myborder {";
		styleRodape += "   border-right:1px solid black;";
		styleRodape += "   border-left:1px solid black;";
		styleRodape += "   text-align:left;";
		styleRodape += "   width:25%";
		styleRodape += "  }";
		styleRodape += " </style>";

		return styleRodape;
	}

}
