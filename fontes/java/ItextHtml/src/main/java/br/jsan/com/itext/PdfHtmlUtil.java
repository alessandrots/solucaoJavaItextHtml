package br.jsan.com.itext;

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

public class PdfHtmlUtil implements IConstantes {

	private static PdfHtmlUtil pdfHtmlUtil;

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
	public List<String> extrairCabecalhoERodape(String conteudo) throws Exception {
		List<String> listaHeadFooterConteudo = new ArrayList<String>();
		List<Integer> listaCabecalhoRodape = gerarListaTagsTable(conteudo);

		if (listaCabecalhoRodape != null && listaCabecalhoRodape.size() > 0) {
			if (listaCabecalhoRodape != null && listaCabecalhoRodape.size() > 0) {
				String header = conteudo.substring(listaCabecalhoRodape.get(0), listaCabecalhoRodape.get(1) + 8);
				header = limparTags(header, THEAD_HTML);
				header = this.adicionarAlignHouverTextAlign(header);
				header = limparTags(header, DIV_HTML);
				header = TABLE_WIDTH_BORDER_ZERO_HTML + " " + header + " " + TABLE_HTML_END_TAG;
				listaHeadFooterConteudo.add(header);

				String footer = conteudo.substring(listaCabecalhoRodape.get(2), listaCabecalhoRodape.get(3) + 8);
				footer = limparTags(footer, TFOOT_HTML);
				footer = limparTags(footer, DIV_HTML);
				footer = this.adicionarAlignHouverTextAlign(footer);
				footer = TABLE_WIDTH_BORDER_BLACK_HTML + footer + " " + TABLE_HTML_END_TAG;

				conteudo = conteudo.substring(listaCabecalhoRodape.get(4), listaCabecalhoRodape.get(5) + 8);
				conteudo = limparTags(conteudo, TBODY_HTML);
				listaHeadFooterConteudo.add(footer);
			}

			listaHeadFooterConteudo.add(conteudo);
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
		List<Integer> listaCabecalhoRodape = new ArrayList<Integer>();

		if (conteudo.indexOf(THEAD_HTML_START_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.indexOf(THEAD_HTML_START_TAG));
		}
		if (conteudo.indexOf(THEAD_HTML_END_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.indexOf(THEAD_HTML_END_TAG));
		}
		if (conteudo.indexOf(TFOOT_HTML_START_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.indexOf(TFOOT_HTML_START_TAG));
		}
		if (conteudo.indexOf(TFOOT_HTML_END_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.indexOf(TFOOT_HTML_END_TAG));
		}
		if (conteudo.indexOf(TBODY_HTML_START_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.lastIndexOf(TBODY_HTML_START_TAG));
		}
		if (conteudo.indexOf(TBODY_HTML_END_TAG) != -1) {
			listaCabecalhoRodape.add(conteudo.lastIndexOf(TBODY_HTML_END_TAG));
		}

		return listaCabecalhoRodape;
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

}
