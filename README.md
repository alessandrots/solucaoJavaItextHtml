# solucaoJavaItextHtml
 A idéia é receber um HTML e a partir do mesmo renderizar um arquivo PDF.

 A solução gera um pdf com base em qualquer html, mas os htmls que estão definidos na pasta templates geram htmls com cabeçalho e rodapé nas respectivas áreas com imagem e tudo mais.

 Gera o objeto documento baseado no A4. Define as margens de cabeçalho, rodapé e margens esquerda e direita

 pageSize the pageSize
 marginLeft the margin on the left
 marginRight the margin on the right
 marginTop the margin on the top 
 marginBottom the margin on the bottom 
 Document document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 500.90551181102F, 100.90551181102F);

 A altura do cabeçalho vai diferença entre o primeiro e o terceiro parâmetro.
 
 A altura do rodapé vai diferença entre o segundo e o quarto parâmetro.

 Os valores de início são:
 85.35826771653F, 56.90551181102F, 200, 130.90551181102F

 document = new Document(PageSize.A4, 85.35826771653F, 56.90551181102F, 200, 130.90551181102F);

 A classe PdfExample é uma classe com método main que serve para testar a solução como um todo.

 As demais classes são para ser integradas dentro de um projeto.
