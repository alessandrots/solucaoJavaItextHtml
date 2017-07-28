package br.jsan.com.itext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Util {

	private static final Log log = LogFactory.getLog(Util.class);

	/**
	 * @param obj
	 *            Objeto a ser validado
	 * @return Verificar se um dado objeto NÃO esta vazio
	 * @author Renato Alves Félix - CDS [renatof@pgr.mpf.gov.br]
	 */
	public static final boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	/**
	 * @param objs
	 * @return Se todos os objetos informado NÃO ESTÃO VAZIOS!
	 */
	public static final boolean isNotEmpty(Object... objs) {
		return !isEmpty(objs);
	}

	/**
	 * @param objs
	 * @return Se tem algum objeto vazio na lista informada
	 */
	public static final boolean isEmpty(Object... objs) {
		if (objs == null) {
			return true;
		} else {
			boolean estaVazio = true;
			for (Object obj : objs) {
				estaVazio = estaVazio && isEmpty(obj);
			}
			return estaVazio;
		}
	}

	public static final String formatarNomeVariavelScript(String nomeVariavel) {
		String nomeLimpo = nomeVariavel.replace(".", "_");
		nomeLimpo = nomeLimpo.replace("[", "_");
		nomeLimpo = nomeLimpo.replace("]", "_");
		return nomeLimpo;
	}

	public static final String removeAcentuacao(String acentuada) {
		if (isEmpty(acentuada)) {
			return acentuada;
		}
		char[] acentuados = new char[] {'ç', 'á', 'à', 'ã', 'â', 'ä', 'é', 'è', 'ê', 'ë', 'í', 'ì', 'î', 'ï', 'ó', 'ò', 'õ', 'ô', 'ö', 'ú', 'ù', 'û', 'ü'};
		char[] naoAcentuados = new char[] {'c', 'a', 'a', 'a', 'a', 'a', 'e', 'e', 'e', 'e', 'i', 'i', 'i', 'i', 'o', 'o', 'o', 'o', 'o', 'u', 'u', 'u', 'u'};
		for (int i = 0; i < acentuados.length; i++) {
			acentuada = acentuada.replace(acentuados[i], naoAcentuados[i]);
			acentuada = acentuada.replace(Character.toUpperCase(acentuados[i]), Character.toUpperCase(naoAcentuados[i]));
		}
		return acentuada;
	}

	/**
	 * @param obj
	 *            Objeto a ser validado
	 * @return Valida se um objeto esta nulo ou vazio.
	 * @author Renato Alves Félix - CDS
	 */
	public static final boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		} else if (obj instanceof Integer) {
			return ((Integer) obj).intValue() <= 0;
		} else if (obj instanceof Long) {
			return ((Long) obj).longValue() <= 0;
		} else if (obj instanceof Integer) {
			return ((Integer) obj).intValue() <= 0;
		} else if (obj instanceof Collection) {
			return ((Collection) obj).isEmpty();
		} else if (obj instanceof String) {
			return ((String) obj).trim().length() == 0;
		} else if (obj instanceof String[]) {
			return ((String[]) obj).length == 0;
		} else if (obj instanceof Integer[]) {
			return ((Integer[]) obj).length == 0;
		} else if (obj instanceof Long[]) {
			return ((Long[]) obj).length == 0;
		} else if (obj instanceof StringBuilder) {
			return ((StringBuilder) obj).length() == 0;
		} else {
			return false;
		}
	}

	/**
	 * @param value
	 * @return Vazio quando o campo for null
	 */
	public static final String emptyOnNull(String value) {
		return (value == null ? "" : value);
	}

	//	public static final String getRealPath(String relativePath) {
	//		return ServletActionContext.getServletContext().getRealPath(relativePath);
	//	}

	public static final String getScriptImagemCaixaPesquisaAssincrona(String prefixoCaixaPesquisa, String nomeComponente, String urlImagem, String IdleOrLoadingOrDone) {
		// idle - campo vazio/em branco
		// loading - requisição assincrona em execução
		// done - campo preenchido após seleção da lista de resultados encontrados
		return "mostrarImagemCaixaPesquisaAssincrona('" + prefixoCaixaPesquisa + "','" + nomeComponente + "','" + urlImagem + "','" + IdleOrLoadingOrDone + "'); ";
	}

	public static final void removerObjetosNulos(Collection<Object> listObjects) {
		if (listObjects != null) {
			for (Iterator<Object> iterator = listObjects.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				if (object == null) {
					iterator.remove();
				}
			}
		}
	}

	public static final String numeroRomano(int valor) {
		int[] aInteiros = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
		String[] aRomanos = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
		String romano = "";
		for (int i = 0; i < aInteiros.length; i++) {
			while (valor >= aInteiros[i]) {
				valor -= aInteiros[i];
				romano += aRomanos[i];
			}
		}
		return romano;
	}

	public static final String numeroOrdinalMaculino(int numero) {
		if (numero <= 0 | numero > 999) return "";
		String[] aOrdinalUnidades = {"", "Primeiro", "Segundo", "Terceiro", "Quarto", "Quinto", "Sexto", "Sétimo", "Oitavo", "Nono"};
		String[] aOrdinalDezenas = {"", "Décimo ", "Vigésimo ", "Trigésimo ", "Quadragésimo ", "Quinquagésimo ", "Sexagésimo ", "Septuagésimo ", "Octogésimo ", "Nonagésimo "};
		String[] aOrdinalCentenas =
			{"", "Centésimo ", "Ducentésimo ", "Trecentésimo ", "Quadrigentésimo ", "Quingentésimo ", "Sexcentésimo ", "Septicentésimo ", "Octigentésimo ", "Nongentésimo "};

		return montarNumeroOrdinal(numero, aOrdinalUnidades, aOrdinalDezenas, aOrdinalCentenas);
	}

	public static final String numeroOrdinalFeminino(int numero) {
		if (numero <= 0 | numero > 999) return "";
		String[] aOrdinalUnidades = {"", "Primeira", "Segunda", "Terceira", "Quarta", "Quinta", "Sexta", "Sétima", "Oitava", "Nona"};
		String[] aOrdinalDezenas = {"", "Décima ", "Vigésima ", "Trigésima ", "Quadragésima ", "Quinquagésima ", "Sexagésima ", "Septuagésima ", "Octogésima ", "Nonagésima "};
		String[] aOrdinalCentenas =
			{"", "Centésima ", "Ducentésima ", "Trecentésima ", "Quadrigentésima ", "Quingentésima ", "Sexcentésima ", "Septicentésima ", "Octigentésima ", "Nongentésima "};

		return montarNumeroOrdinal(numero, aOrdinalUnidades, aOrdinalDezenas, aOrdinalCentenas);
	}

	private static String montarNumeroOrdinal(int numero, String[] aOrdinalUnidades, String[] aOrdinalDezenas, String[] aOrdinalCentenas) {
		int[] aIntDezenas = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90};
		int[] aIntCentenas = {0, 100, 200, 300, 400, 500, 600, 700, 800, 900};

		StringBuilder ordinal = new StringBuilder();

		int unidades;
		int dezenas;
		dezenas = numero - aIntCentenas[numero / 100];
		unidades = dezenas - (aIntDezenas[dezenas / 10]);
		ordinal.append(aOrdinalCentenas[numero / 100]);
		ordinal.append(aOrdinalDezenas[dezenas / 10]);
		ordinal.append(aOrdinalUnidades[unidades]);

		return ordinal.toString();
	}

	/**
	 * Metodo utilitario para restricao de valores numa tabela
	 * @param lista
	 * @param quantidade Quantidade Especifica
	 * @return
	 * @author Renato Felix - CDS
	 */
	public static final boolean temApenas(Collection<?> lista, int quantidade) {
		return lista != null && lista.size() == quantidade;
	}

	/**
	 * Verifica se na lista tem a quantidade informada ou menos.
	 * @param lista Valores a sere comparados seu tamanho
	 * @param quantidade Apenas valores positivos
	 * @return
	 */
	public static final boolean temAte(Collection<?> lista, int quantidade) {
		return (lista == null && quantidade == 0) || (lista != null && lista.size() <= quantidade);
	}

	/**
	 * Converter uma lista de objetos para uma lista de string Se a lista estiver vazia, returna null.
	 * 
	 * @param lista
	 * @return
	 */
	public static final String[] convertObjectToString(Collection lista) {
		if (Util.isEmpty(lista)) {
			return null;
		}
		String[] result = new String[lista.size()];
		int i = 0;
		for (Iterator iter = lista.iterator(); iter.hasNext();) {
			Object element = iter.next();
			result[i++] = element.toString();
		}
		return result;
	}

	public static final synchronized void copiar(Object destino, Object origem) throws Exception {
		if (destino == null || origem == null) {
			return;
		}
		try {
			BeanUtils.copyProperties(destino, origem);
		} catch (IllegalAccessException e) {
			throw new Exception(e);
		} catch (InvocationTargetException e) {
			throw new Exception(e);
		}
	}

	/**
	 * @return Se o E-mail informado NÃO É válido
	 * @param E-mail
	 * @author Renato Alves Felix - CDS [renatof@pgr.mpf.gov.br]
	 */
	public static final boolean isNaoValidoEmail(String email) {
		return !isValidoEmail(email);
	}

	/**
	 * @return Se o email informado é valido
	 * @param email
	 * @author Renato Alves Felix - CDS [renatof@pgr.mpf.gov.br]
	 */
	public static final boolean isValidoEmail(String email) {
		if (Util.isEmpty(email)) {
			return false;
		}
		Pattern padrao = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher pesquisa = padrao.matcher(email);
		return pesquisa.matches();
	}

	/**
	 * Permite adionar Parametros , podendo ser REPETIDOS
	 * @param url
	 * @param parametro
	 * @param valor
	 * @return
	 */
	public static final String addParametroUrl(String url, String parametro, Object valor) throws Exception {
		StringBuffer buf = new StringBuffer();
		if (url != null && parametro != null && valor != null) {
			buf.append(url);
			if (url.indexOf("?") < 0) {
				buf.append("?");
			} else if (!url.endsWith("&")) {
				buf.append("&");
			}
			try {
				buf.append(parametro).append("=").append(URLEncoder.encode(String.valueOf(valor), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new Exception("Erro ao tentar montar a url. url[" + url + "]parametro[" + parametro + "]valor[" + valor + "]", e);
			}
		}
		return buf.toString();
	}

	/**
	 * Retornar o Valor padrao se for NULL o objecto informado
	 * @param <O>
	 * @param object
	 * @param DEFAULT_VALUE
	 * @return
	 */
	public static final <O extends Object> O getDefaultValueOnNull(O object, O DEFAULT_VALUE) {
		return (object == null ? DEFAULT_VALUE : object);
	}

	/**
	 * Verificar se os campos estao nulos e efetua a comparação.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static final int compareTO(Comparable obj1, Comparable obj2) {
		if (obj1 != null && obj2 != null) {
			return obj1.compareTo(obj2);
		}
		return -1;
	}

	public static final Locale getDefaultLocale() {
		return new Locale("pt", "BR");
	}

	//	public static final boolean isTemParametroRequisicao(String nomeParametro) {
	//		return Util.isNotEmpty(ServletActionContext.getRequest().getParameter(nomeParametro));
	//	}
	//
	//	public static final boolean isTemNaRequisicaoAtual(String valor) {
	//		StringBuffer sb = new StringBuffer();
	//		for (Object parametro : ServletActionContext.getRequest().getParameterMap().keySet()) {
	//			sb.append(parametro);
	//			sb.append("|");
	//			String[] valores = (String[]) ServletActionContext.getRequest().getParameterMap().get(parametro);
	//			if (valores != null) {
	//				for (String s : valores) {
	//					sb.append("|");
	//					sb.append(s);
	//				}
	//			}
	//		}
	//		return sb.indexOf(valor) > 0;
	//	}

	public static final String retirarFormatacaoCnpj(String cnpj) {
		if (isNotEmpty(cnpj)) {
			String out2 = cnpj.replace(".", "");
			String out3 = out2.replace("/", "");
			String out = out3.replace("-", "");
			return out;
		}
		return null;
	}

	public static final String getValorComMascara(String mascara, String valor, boolean retornarValorVazio) {
		/*  
		 * Verifica se se foi configurado para nao retornar a   
		 * mascara se a string for nulo ou vazia se nao  
		 * retorna somente a mascara.  
		 */
		if (retornarValorVazio == true && (valor == null || valor.trim().equals(""))) {
			return "";
		}
		/*  
		 * Substituir as mascaras passadas como  9, X, * por # para efetuar a formatcao 
		 */
		//		pMask = pMask.replaceAll(UtilComponente.getLegendaCampoObrigatorio(), "#");
		mascara = mascara.replaceAll("9", "#");
		//		pMask = pMask.toUpperCase().replaceAll("X", "#");
		/*  
		 * Formata valor com a mascara passada   
		 */
		for (int i = 0; i < valor.length(); i++) {
			String c = valor.substring(i, i + 1);
			if (c.equals(".") || c.equals("-")) {
				continue;
			}
			mascara = mascara.replaceFirst("#", c);
		}
		/*  
		 * Subistitui por string vazia os digitos restantes da mascara  
		 * quando o valor passado é menor que a mascara    
		 */
		return mascara.replaceAll("#", "");
	}

	/**
	 * Valida os argumentos obrigatórios a fim de evitar {@link NullPointerException} (uma espécie de NullSafeGet).
	 * 
	 * @param argumentos Lista de argumentos que não podem ser nulos
	 * @author André Thiago / Pimenta
	 */
	public static final void validarArgumentosObrigatorios(Object... argumentos) {
		for (Object argumento : argumentos) {
			if (argumento == null) {
				throw new IllegalArgumentException("Deve-se informar os argumentos obrigatórios.");
			}
		}
	}

	public static final Properties carregaParamentrosArquivoProperties(String arquivo) {
		return carregaParamentrosArquivoProperties(arquivo, Util.class);
	}

	public static final Properties carregaParamentrosArquivoProperties(String arquivo, Class classe) {

		Properties properties = new Properties();

		URL url = classe.getResource("/" + arquivo);
		InputStream in = null;
		try {
			if (url != null) {
				in = url.openStream();
				properties.load(in);
			}
		} catch (IOException e) {
			log.error("Arquivo " + arquivo + " não foi encontrado,verifique se ele se encotra no classpath da aplicação", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return properties;
	}

	/**
	 * Converte um caracter para boolean, quando caracter informado for null retorna false
	 * Conversão: 'S' -> True, 'N' -> False
	 * 
	 * @param caracter a ser convertido
	 * @return Boolean que foi convertido
	 * @author Marcel Martinelli / marcelmartinelli@pgr.mpf.gov.br
	 */
	public static final Boolean convertCharToBoolean(Character caracter) {
		return ((caracter != null) && (caracter == 'S')) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Converte um boolean para caracter, quando boolean informado for null retorna 'N'
	 * Conversão: True -> 'S', False -> 'N'
	 * 
	 * @param booleano a ser convertido
	 * @return Character para o qual foi convertido
	 * @author Marcel Martinelli / marcelmartinelli@pgr.mpf.gov.br
	 */
	public static final Character convertBooleanToChar(Boolean booleano) {
		return ((booleano != null) && booleano) ? 'S' : 'N';
	}

	public static String converteISO88591emUTF8(String texto) {
		if (texto != null) {
			try {
				byte textoBytes[] = texto.getBytes("ISO-8859-1");
				texto = new String(textoBytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return texto;
	}

}
