package br.jsan.com.itext;

import java.io.IOException;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;

/**
 * Classe responsável por ser utilizada para processar imagens que estejam dentro de um HTML.
 * 
 * A imagem é o logo MPF padrão que foi pego da intranet.
 * 
 * @author alessandroteixeira
 *
 */
public class Base64ImageProvider extends AbstractImageProvider {

	/**
	 * Imagem de exemplo
	 */
	private String logoMpf =
		"data:image/png;base64,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";

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

	/**
	 * Exemplo de string a ser passado:
	 * logoMpf1
	 * 
	 * @param imageTagHtml
	 * @return Image
	 */
	public Image converterToImage(String imageTagHtml) {
		String b64Image = imageTagHtml.substring(imageTagHtml.indexOf("base64,") + 7);

		byte[] decoded = Base64.decode(b64Image);
		Image img = null;

		try {
			img = Image.getInstance(decoded);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return img;
	}

	public String getLogoMpf() {
		return this.logoMpf;
	}

	@Override
	public String getImageRootPath() {
		return null;
	}
}
