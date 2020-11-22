package com.example.mysmartchess.Jogo;

import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class TabuleiroDeXadrez {

    //

    private final static int TORRE_BRANCO1 = 0;
    private final static int CAVALO_BRANCO1 = 1;
    private final static int BISPO_BRANCO1 = 2;
    private final static int DAMA_BRANCO = 3;
    public final static int REI_BRANCO = 4;
    private final static int BISPO_BRANCO2 = 5;
    private final static int CAVALO_BRANCO2 = 6;
    private final static int TORRE_BRANCO2 = 7;
    private final static int PEAO_BRANCO1 = 8;
    private final static int PEAO_BRANCO2 = 9;
    private final static int PEAO_BRANCO3 = 10;
    private final static int PEAO_BRANCO4 = 11;
    private final static int PEAO_BRANCO5 = 12;
    private final static int PEAO_BRANCO6 = 13;
    private final static int PEAO_BRANCO7 = 14;
    private final static int PEAO_BRANCO8 = 15;

    private final static int TORRE_PRETO1 = 16;
    private final static int CAVALO_PRETO1 = 17;
    private final static int BISPO_PRETO1 = 18;
    private final static int DAMA_PRETO = 19;
    public final static int REI_PRETO = 20;
    private final static int BISPO_PRETO2 = 21;
    private final static int CAVALO_PRETO2 = 22;
    private final static int TORRE_PRETO2 = 23;
    private final static int PEAO_PRETO1 = 24;
    private final static int PEAO_PRETO2 = 25;
    private final static int PEAO_PRETO3 = 26;
    private final static int PEAO_PRETO4 = 27;
    private final static int PEAO_PRETO5 = 28;
    private final static int PEAO_PRETO6 = 29;
    private final static int PEAO_PRETO7 = 30;
    private final static int PEAO_PRETO8 = 31;

    //Atributos do jogo
    private boolean brancasJogam = true;
    private int[][] jogo;
    private List<PecaXadrez> pecas = new ArrayList<>();
    private List<ImageView> imageViewList;
    private boolean enPassant = false;
    private boolean promocao = false;
    private int[] peaoAPromoverXY = new int[2];
    private boolean peaoAPromoverBranco = true;

    public boolean isEnPassant() {
        return enPassant;
    }

    private int pecaTomadaInt = -1;

    public TabuleiroDeXadrez(List<ImageView> imageViewList) {
        this.imageViewList = imageViewList;
        arrumarPecas();
        criaListaDePecas();
    }

    public ImageView getImageView(int x, int y) {
        if (jogo[x][y] != -1)
            return pecas.get(jogo[x][y]).getImageView();
        else return null;
    }

    public boolean isBrancasJogam() {
        return brancasJogam;
    }

    public boolean isCasaDesocupada(int x, int y) {
        if (jogo[x][y] == -1)
            return true;
        else return false;
    }

    private boolean isNotInCheck(int inicioX, int inicioY, int destinoX, int destinoY) {
        int pecaInt = jogo[inicioX][inicioY];
        pecaTomadaInt = jogo[destinoX][destinoY];
        PecaXadrez pecaXadrez = pecas.get(pecaInt), pecaXadrezInimiga;
        atualizaPosicaoNoTabuleiro(inicioX, inicioY, destinoX, destinoY);
        //Analisa se a peça está cravada
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                pecaInt = jogo[i][j];
                //verifica se tem alguma peça ali
                if (pecaInt > -1) {
                    pecaXadrezInimiga = pecas.get(pecaInt);

                    //Verifica se é peça inimiga, e se está dando cheque
                    if (pecaXadrez.isPecaBranca() != pecaXadrezInimiga.isPecaBranca()) {
                        //Log.d("cyrus", "analisando se " + pecaInt + ": " + pecaXadrezInimiga.toString() + " está dando cheque");
                        if ((pecaXadrezInimiga.isDeliveringCheck(i, j, jogo))) {
                            //Log.d("cyrus", "peça cravada por: " + jogo[i][j]);
                            atualizaPosicaoNoTabuleiro(destinoX, destinoY, inicioX, inicioY);
                            jogo[destinoX][destinoY] = pecaTomadaInt;
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean moverPeca(int inicioX, int inicioY, int destinoX, int destinoY) {

        //Não pode mover peça nenhuma enquanto não promover
        if (promocao)
            return false;
        //Log.d("cyrus", "==== PEÇAS SERÃO AGORA IMPRESSAS ======");
        //imprimePecas();
        PecaXadrez pecaXadrez;
        int pecaInt = jogo[inicioX][inicioY];
        //Log.d("cyrus", "Peça que será movida: " + pecaInt);
        //verifica se não é alguém querendo zoar o jogo
        if (pecaInt > -1)
            pecaXadrez = pecas.get(pecaInt);
        else return false;

        //Log.d("cyrus", "Peça que será movida (nome): " + pecas.get(pecaInt).toString());

        //Verifica se não se está movendo peça inimiga
        if ((brancasJogam && !pecaXadrez.isPecaBranca()) || (!brancasJogam && pecaXadrez.isPecaBranca())) {
            return false;
        }
        //
        //
        //Tratamento do roque
        //
        //
        if ((Math.abs(destinoY - inicioY) >= 2) && inicioX == destinoX && inicioY == 4)
            if (pecas.get(jogo[inicioX][inicioY]).hasNotMoved() && pecas.get(jogo[inicioX][inicioY]).toString().equals("Rei"))
                if (pecaXadrez.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas)) {
                    //Constatou-se que é roque, e pode fazer. Verifica-se agora se há cheques pelo caminho
                    int torreY = 0, destinoTorreY = 0;


                    // faz o tratamento devido para caso de roque grande ou pequeno
                    int var = 0;
                    if ((destinoY == 2) || destinoY == 0) {//roque grande. tem que verificar também se o comando veio do lichess...
                        var = -1;
                        torreY = 0;
                        destinoTorreY = 3;
                    }
                    if (destinoY >= 6) {//roque pequeno
                        var = 1;
                        torreY = 7;
                        destinoTorreY = 5;
                    }

                    //verifica-se se a torre se mexeu né, importante
                    if (jogo[inicioX][torreY] != -1 && pecas.get(jogo[inicioX][torreY]).hasNotMoved()) {

                        if (isNotInCheck(inicioX, inicioY, destinoX, inicioY + var)) {
                            if (isNotInCheck(inicioX, inicioY + var, destinoX, inicioY + 2 * var)) {
                                PecaXadrez pecaTorre = pecas.get(jogo[inicioX][torreY]);
                                atualizaPosicaoNoTabuleiro(destinoX, torreY, destinoX, destinoTorreY);
                                //Log.d("cyrus", "alterou o tabuleiro");
                                pecaXadrez.setHasNotMoved(false);
                                pecaTorre.setHasNotMoved(false);
                                //muda a vez de quem joga
                                brancasJogam = !brancasJogam;
                                desativaEnPassantExceto(destinoX, destinoY);
                                enPassant = false;
                                return true;
                            }
                            //rei andou um pouco e descobriu que ta em cheque. tem que voltar humildemente para sua residência
                            else {
                                atualizaPosicaoNoTabuleiro(destinoX, inicioY + var, inicioX, inicioY);
                                pecaXadrez.setHasNotMoved(true);
                                return false;
                            }
                        } else {
                            //tão dando cheque pelo caminho do roque, já retorna logo o falsete
                            return false;
                        }
                    } else {//torre se mexeu, deu ruim
                        return false;
                    }
                }
        //fim da análise do roque

        //Verifica se o movimento é legal, sem análise de peça cravada
        if (pecaXadrez.isMovePossible(inicioX, inicioY, destinoX, destinoY, jogo, pecas)) {//ta valendo pra brancas e pretas
            //Log.d("cyrus", "Movimento considerado legal");

            //Verificação de peça cravada
            if (isNotInCheck(inicioX, inicioY, destinoX, destinoY)) {
                //movimento legal
                enPassant = false;
                //Verifica se foi en passant
                if (jogo[inicioX][destinoY] != -1) {
                    PecaXadrez peca = pecas.get(jogo[inicioX][destinoY]);
                    if (peca.toString().equals("Peao")) {
                        Peao peao = (Peao) peca;
                        if (pecaXadrez.toString().equals("Peao") && peao.isEnPassantActive()) {
                            jogo[inicioX][destinoY] = -1;
                            enPassant = true;
                        }
                    }
                }

                //verifica se é caso de promoção
                if (pecaXadrez.toString().equals("Peao")) {
                    if (destinoX == 7 && pecaXadrez.isPecaBranca()) {
                        //peao branco será promovido
                        peaoAPromoverXY[0] = destinoX;
                        peaoAPromoverXY[1] = destinoY;
                        peaoAPromoverBranco = true;
                        promocao = true;
                    }
                    if (destinoX == 0 && !pecaXadrez.isPecaBranca()) {
                        //peao preto será promovido
                        peaoAPromoverXY[0] = destinoX;
                        peaoAPromoverXY[1] = destinoY;
                        peaoAPromoverBranco = false;
                        promocao = true;
                    }
                }

                //Log.d("cyrus", "alterou o tabuleiro");
                //muda a vez de quem joga
                brancasJogam = !brancasJogam;

                desativaEnPassantExceto(destinoX, destinoY);
                return true;
            }
            //peça cravada

            else return false;

            //movimento ilegal da peça
        } else {
            //Log.d("cyrus", "erro da classe da peça");
            return false;
        }

    }

    public boolean isPeaoAPromoverBranco() {
        return peaoAPromoverBranco;
    }

    public boolean isPromocao() {
        return promocao;
    }

    public void promovePeao(int posX, int posY, ImageView imageView, int index) {
        if (promocao) {
            if (posX == peaoAPromoverXY[0] && posY == peaoAPromoverXY[1]) {
                //Tudo certinho, partiu promover
                PecaXadrez pecaXadrez;
                switch (index) {
                    case 0:
                        pecaXadrez = new Dama(isPeaoAPromoverBranco(), imageView);
                        break;
                    case 1:
                        pecaXadrez = new Torre(isPeaoAPromoverBranco(), imageView);
                        break;
                    case 2:
                        pecaXadrez = new Bispo(isPeaoAPromoverBranco(), imageView);
                        break;
                    default:
                        pecaXadrez = new Cavalo(isPeaoAPromoverBranco(), imageView);
                        break;

                }
                pecas.add(pecaXadrez);
                jogo[posX][posY] = pecas.size() - 1;
                promocao = false;
            }

        }
    }

    private void desativaEnPassantExceto(int posX, int posY) {
        Peao peao;
        int pecaInt = jogo[posX][posY];

        for (int i = 0; i < 32; i++) {
            if (pecas.get(i).toString().equals("Peao")) {

                //Não desativa o enpassant do peao que acabou de se mover
                if (i != pecaInt) {
                    peao = (Peao) pecas.get(i);
                    peao.setEnPassantActive(false);
                }
            }

        }
    }


    private void atualizaPosicaoNoTabuleiro(int inicioX, int inicioY, int destinoX, int destinoY) {
        pecas.get(jogo[inicioX][inicioY]).setHasNotMoved(false);
        jogo[destinoX][destinoY] = jogo[inicioX][inicioY];
        jogo[inicioX][inicioY] = -1;
    }

    private void arrumarPecas() {
        int i, j;
        jogo = new int[8][8];

        //posicionei fo tudo logo
        jogo[0][0] = TORRE_BRANCO1;
        jogo[0][1] = CAVALO_BRANCO1;
        jogo[0][2] = BISPO_BRANCO1;
        jogo[0][3] = DAMA_BRANCO;
        jogo[0][4] = REI_BRANCO;
        jogo[0][5] = BISPO_BRANCO2;
        jogo[0][6] = CAVALO_BRANCO2;
        jogo[0][7] = TORRE_BRANCO2;
        jogo[1][0] = PEAO_BRANCO1;
        jogo[1][1] = PEAO_BRANCO2;
        jogo[1][2] = PEAO_BRANCO3;
        jogo[1][3] = PEAO_BRANCO4;
        jogo[1][4] = PEAO_BRANCO5;
        jogo[1][5] = PEAO_BRANCO6;
        jogo[1][6] = PEAO_BRANCO7;
        jogo[1][7] = PEAO_BRANCO8;
        jogo[7][0] = TORRE_PRETO1;
        jogo[7][1] = CAVALO_PRETO1;
        jogo[7][2] = BISPO_PRETO1;
        jogo[7][3] = DAMA_PRETO;
        jogo[7][4] = REI_PRETO;
        jogo[7][5] = BISPO_PRETO2;
        jogo[7][6] = CAVALO_PRETO2;
        jogo[7][7] = TORRE_PRETO2;
        jogo[6][0] = PEAO_PRETO1;
        jogo[6][1] = PEAO_PRETO2;
        jogo[6][2] = PEAO_PRETO3;
        jogo[6][3] = PEAO_PRETO4;
        jogo[6][4] = PEAO_PRETO5;
        jogo[6][5] = PEAO_PRETO6;
        jogo[6][6] = PEAO_PRETO7;
        jogo[6][7] = PEAO_PRETO8;


        //completa com -1 os espaços vazios
        for (i = 2; i < 6; i++)
            for (j = 0; j < 8; j++)
                jogo[i][j] = -1;
    }

    private void criaListaDePecas() {
        //Cria e organiza as peças da classe PecaXadrez

        Torre torreB1 = new Torre(true, imageViewList.get(0));
        Cavalo cavaloB1 = new Cavalo(true, imageViewList.get(1));
        Bispo bispoB1 = new Bispo(true, imageViewList.get(2));
        Dama damaB = new Dama(true, imageViewList.get(3));
        Rei reiB = new Rei(true, imageViewList.get(4));
        Bispo bispoB2 = new Bispo(true, imageViewList.get(5));
        Cavalo cavaloB2 = new Cavalo(true, imageViewList.get(6));
        Torre torreB2 = new Torre(true, imageViewList.get(7));
        Peao peaoB1 = new Peao(true, imageViewList.get(8));
        Peao peaoB2 = new Peao(true, imageViewList.get(9));
        Peao peaoB3 = new Peao(true, imageViewList.get(10));
        Peao peaoB4 = new Peao(true, imageViewList.get(11));
        Peao peaoB5 = new Peao(true, imageViewList.get(12));
        Peao peaoB6 = new Peao(true, imageViewList.get(13));
        Peao peaoB7 = new Peao(true, imageViewList.get(14));
        Peao peaoB8 = new Peao(true, imageViewList.get(15));

        Torre torreP1 = new Torre(false, imageViewList.get(16));
        Cavalo cavaloP1 = new Cavalo(false, imageViewList.get(17));
        Bispo bispoP1 = new Bispo(false, imageViewList.get(18));
        Dama damaP = new Dama(false, imageViewList.get(19));
        Rei reiP = new Rei(false, imageViewList.get(20));
        Bispo bispoP2 = new Bispo(false, imageViewList.get(21));
        Cavalo cavaloP2 = new Cavalo(false, imageViewList.get(22));
        Torre torreP2 = new Torre(false, imageViewList.get(23));
        Peao peaoP1 = new Peao(false, imageViewList.get(24));
        Peao peaoP2 = new Peao(false, imageViewList.get(25));
        Peao peaoP3 = new Peao(false, imageViewList.get(26));
        Peao peaoP4 = new Peao(false, imageViewList.get(27));
        Peao peaoP5 = new Peao(false, imageViewList.get(28));
        Peao peaoP6 = new Peao(false, imageViewList.get(29));
        Peao peaoP7 = new Peao(false, imageViewList.get(30));
        Peao peaoP8 = new Peao(false, imageViewList.get(31));


        pecas.add(torreB1);
        pecas.add(cavaloB1);
        pecas.add(bispoB1);
        pecas.add(damaB);
        pecas.add(reiB);
        pecas.add(bispoB2);
        pecas.add(cavaloB2);
        pecas.add(torreB2);
        pecas.add(peaoB1);
        pecas.add(peaoB2);
        pecas.add(peaoB3);
        pecas.add(peaoB4);
        pecas.add(peaoB5);
        pecas.add(peaoB6);
        pecas.add(peaoB7);
        pecas.add(peaoB8);

        pecas.add(torreP1);
        pecas.add(cavaloP1);
        pecas.add(bispoP1);
        pecas.add(damaP);
        pecas.add(reiP);
        pecas.add(bispoP2);
        pecas.add(cavaloP2);
        pecas.add(torreP2);
        pecas.add(peaoP1);
        pecas.add(peaoP2);
        pecas.add(peaoP3);
        pecas.add(peaoP4);
        pecas.add(peaoP5);
        pecas.add(peaoP6);
        pecas.add(peaoP7);
        pecas.add(peaoP8);
    }

    public void imprimePecas() {
        String info, cor;
        for (int i = 0; i < 32; i++) {
            info = "";
            PecaXadrez peca = pecas.get(i);
            if (peca.isPecaBranca())
                cor = " branco";
            else cor = " preto";
            info += peca.toString() + cor;
            //Log.d("cyrus", info);
        }
    }

    public int[][] getTabuleiro() {
        int i, j, auxi, auxj, valor;
        int tabuleiro[][] = new int[8][8];
        auxi = 0;
        for (i = 7; i >= 0; i--, auxi++) {
            for (j = 0, auxj = 0; j < 8; j++, auxj++) {
                valor = (jogo[i][j] == -1) ? 0 : 1;
                tabuleiro[auxi][auxj] = valor;
            }
        }
        return tabuleiro;
    }

    @Override
    public String toString() {
        int i, j;
        String tabuleiro = "";
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++)
                tabuleiro += ", " + jogo[i][j];
            tabuleiro += "\n";
        }
        return tabuleiro;

    }
}
