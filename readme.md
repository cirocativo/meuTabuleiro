# App Meu Tabuleiro
> Aplicativo Android que conecta o tabuleiro físico de xadrez automatizado com uma inteligência artificial, ou com alguém na internet, para jogar online

O projeto foi criado através do Android Studio. Pode ser jogado localmente contra a engine, ou contra qualquer pessoa do mundo através do [Lichess](https://lichess.org/).
O Aplicativo foi feito unicamente para desenvolvimento, portanto não é tão prático para usuários comuns.

Ele se conecta ao tabuleiro [(esp-8266)](https://www.baudaeletronica.com.br/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/n/o/nodemcu_1_1.jpg) através de uma conexão wi-fi. Possui um tabuleiro virtual, e mostra em tempo real as jogadas tanto do adversário, quanto do tabuleiro em questão.

É através do aplicativo que todas as jogadas são verificadas se são legais ou não. 



https://user-images.githubusercontent.com/6518500/189916433-1ae29620-7058-441a-b4f5-7d3f2315212f.mp4

O tabuleiro recebe o movimento realizado pelo usuário e envia a informação para o app. O app faz a verificação do movimento, então autoriza a engine a realizar a jogada. Ao ter a resposta, o app envia o movimento da engine para o tabuleiro, que realiza a jogada fisicamente.
