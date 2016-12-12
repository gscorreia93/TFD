<b>README</b><br>
Para correr o RAFT dividimos o processo em 3 passos, configuração de servidores, correr servidores e correr clientes.<br>
O nosso projeto foi desenvolvido no eclipse, e é possível ser executado no mesmo.<br>
O projeto está também disponível no Git (https://github.com/gscorreia93/TFD).<br>
<br>
Disponibilizamos 4 jars para a execução do projeto. Um para o servidor e três para os clientes, e a sua utilização é detalhada nos próximos pontos.<br>

<br>
<b>Configuração de Servidores</b><br>
Para editar os servidores a serem usados é necessário alterar o ficheiro servers.txt que se tem que colocar na mesma diretoria onde irá correr o servidor ou o cliente. Lá dentro são colocados os servidores e respetivos portos com o seguinte formato:<br>
- 192.168.1.6:8090

Posteriormente é a este ficheiro que os servidores e clientes vêm buscar a localização dos outros servidores.<br>

<br>
<b>Instanciar Servidores</b><br>
Para criar instâncias dos servidores apenas é necessário executar o jar Server.jar através do comando java -jar Server.jar.<br>
Por cada instância criada é iniciado um servidor que tem que estar listado no ficheiro servers.txt.<br>

<br>
<b>Instanciar clientes</b><br>
Para instanciar os clientes temos disponíveis 3 formas diferentes com diferentes tipos de interação com o servidor.<br>

- A mais simples é executar o jar Client.jar que se liga ao RAFT e permite ao utilizador inserir comandos manualmente;
java<br>
--jar Client.jar
- Podemos ao invés disso executar o jar AutoClient.jar que a cada 5 segundos envia um comando aleatório para o servidor;
java<br>
--jar AutoClient.jar
- Ao executar o jar BatchClient.jar podemos configurar vários clientes para serem criados e enviarem pedidos em simultâneo, configurando os seguintes atributos:<br>
-- java -jar AutoClient.jar 20 10 500 1<br>
-- numClients = 20, msgNum = 10, timeBetweenRequests = 500, checkpoint = 1;<br>
-- Número de clientes a executar;<br>
-- Número de mensagens por cliente;<br>
-- Tempo entre pedidos ao RAFT em ms;<br>
-- Flag para sincronizar as threads entre cada pedido.<br>
