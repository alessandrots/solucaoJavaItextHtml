# solucaoJavaItextHtml
 A idéia é receber um HTML e a partir do mesmo renderizar um arquivo PDF.

 A solução gera um pdf com base em qualquer html, mas os htmls que estão definidos na pasta templates geram htmls com cabeçalho e rodapé nas respectivas áreas com imagem e tudo mais.

 Gera o objeto documento baseado no A4. Define as margens de cabeçalho, rodapé e margens esquerda e direita

 A classe PdfExample é uma classe com método main que serve para testar a solução como um todo.

 As demais classes são para ser integradas dentro de um projeto.

 o arquivo js contém um objeto e cada atributo html é um modelo de html q pode ser enviado para ser processado pela classe.

 A idéia é ter uma classe rest que receba esse html e passe para a classe PdfCreator.
