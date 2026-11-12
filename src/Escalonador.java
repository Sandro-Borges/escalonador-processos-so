import java.util.ArrayList;

/*
 * =====================================================================
 *  SIMULADOR DE ESCALONAMENTO - ROUND ROBIN COM FEEDBACK
 * =====================================================================
 *
 *  O programa "finge" ser o escalonador de um sistema operacional:
 *  cria 5 processos, decide quem usa a CPU e imprime cada evento
 *  numa linha do tempo (t=000, t=001, ...).
 *
 *  AS 3 FILAS
 *    alta    -> prontos com prioridade ALTA  (sempre atendida primeiro)
 *    baixa   -> prontos com prioridade BAIXA (so roda se a alta estiver vazia)
 *    ioLista -> processos BLOQUEADOS esperando o I/O terminar
 *
 *  REGRAS DE FEEDBACK (para qual fila o processo vai)
 *    processo novo .......................... fila ALTA
 *    usou o quantum inteiro (preempcao) ..... fila BAIXA
 *    voltou do DISCO ........................ fila BAIXA
 *    voltou da FITA ou da IMPRESSORA ........ fila ALTA
 *
 *  CODIGOS USADOS NO PROGRAMA
 *    prioridade: 1 = ALTA, 2 = BAIXA
 *    tipoIo:     0 = nenhum, 1 = disco, 2 = fita, 3 = impressora
 *
 *  ONDE ESTA CADA PARTE DA APRESENTACAO (procure por "PARTE X")
 *    PARTE 2 - PCB ........................ classe Processo
 *    PARTE 3 - filas e inicializacao ...... inicio do main
 *    PARTE 4 - ciclo principal e CPU ...... while do main
 *    PARTE 5 - preempcao e feedback ....... ultimo else do while
 *    PARTE 6 - I/O ........................ metodo atualizarIo + bloco do pediuIo
 *    EXTRA   - resumo e metricas .......... final do main
 * =====================================================================
 */
public class Escalonador {

    // =================================================================
    // PARTE 2 - PCB (Process Control Block): a "ficha" de cada processo
    // Funciona como uma struct de C: so guarda dados (sem heranca, sem OO).
    // =================================================================
    static class Processo {
        int pid,           // numero do processo (1, 2, 3...)
            ppid,          // numero do processo pai (0 = processo inicial)
            tempoRestante, // quanto de CPU ainda falta (quando chega a 0, finaliza)
            tempoTotal,    // quanto de CPU o processo precisa no total
            cpuUsada,      // quanto de CPU ja usou
            tipoIo,        // 0=nenhum, 1=disco, 2=fita, 3=impressora
            tempoIo,       // quanto tempo o I/O demora
            momentoIo,     // depois de quantas unidades de CPU ele pede o I/O
            prioridade,    // 1=ALTA, 2=BAIXA
            fim;           // instante em que terminou (usado no resumo)
        String status;     // NOVO, PRONTO, EXECUTANDO, BLOQUEADO ou FINALIZADO
        boolean ioFeito;   // true quando o I/O ja aconteceu (cada processo faz no maximo 1)

        public Processo(int pid, int ppid, int tempo, int tipoIo, int tempoIo, int momentoIo) {
            this.pid = pid;
            this.ppid = ppid; //0 = processo inicial
            this.tempoRestante = tempo;
            this.tempoTotal = tempo;
            this.cpuUsada = 0;
            this.tipoIo = tipoIo; // 0=nenhum, 1=disco, 2=fita, 3=impressora
            this.tempoIo = tempoIo;
            this.momentoIo = momentoIo; //depois de quantas unidades de cpu pede o I/O
            this.prioridade = 1; //fila alta
            this.status = "NOVO";
            this.ioFeito = false;
        }
    }

    //item da fila de I/O: o processo bloqueado + quanto falta pro I/O acabar
    static class IoItem {
        Processo p;
        int espera; //diminui 1 a cada unidade de tempo; chegou a 0 = I/O terminou
        public IoItem(Processo p, int espera) { this.p = p; this.espera = espera; }
    }

    //nome de cada tipo de I/O, so pra imprimir (nomesIo[1] = "DISCO")
    static String[] nomesIo = {"NENHUM", "DISCO", "FITA", "IMPRESSORA"};

    // =================================================================
    // PARTE 6 - I/O: retorno dos processos que terminaram o I/O
    // Este metodo e chamado a CADA unidade de tempo (ate enquanto outro
    // processo esta na CPU), entao o I/O "anda em paralelo" com a CPU.
    // =================================================================
    //passa 1 unidade de tempo na fila de I/O e devolve quem terminou pra fila certa
    static void atualizarIo(ArrayList<IoItem> ioLista, ArrayList<Processo> alta, ArrayList<Processo> baixa, int tempo) {
        //1) desconta 1 de cada I/O em andamento e separa quem terminou
        ArrayList<IoItem> concluidos = new ArrayList<>();
        for (IoItem item : ioLista) {
            item.espera--;
            if (item.espera <= 0) concluidos.add(item);
        }

        //2) devolve quem terminou pra fila certa
        for (IoItem item : concluidos) {
            ioLista.remove(item);
            Processo p = item.p;
            p.status = "PRONTO";
            // disco volta pra baixa/ fita e impressora vão pra alta
            if (p.tipoIo == 1) {
                p.prioridade = 2;
                baixa.add(p);
                System.out.printf("[t=%03d] P%d retornou do DISCO -> fila BAIXA\n", tempo, p.pid);
            } else {
                p.prioridade = 1;
                alta.add(p);
                System.out.printf("[t=%03d] P%d retornou da %s -> fila ALTA\n", tempo, p.pid, nomesIo[p.tipoIo]);
            }
        }
    }

    public static void main(String[] args) {

        // =============================================================
        // PARTE 3 - FILAS E INICIALIZACAO
        // =============================================================
        int quantum = 3; //fatia maxima de CPU por vez
        int total = 5;   //quantidade de processos

        System.out.printf("[config] quantum=%d | processos=%d | valores fixos\n", quantum, total);

        //as 3 filas do escalonador
        ArrayList<Processo> alta = new ArrayList<>();
        ArrayList<Processo> baixa = new ArrayList<>();
        ArrayList<IoItem> ioLista = new ArrayList<>();

        //criar os 5 processos (todos filhos do processo inicial, ppid 0)
        //                      pid ppid cpu tipoIo tempoIo momentoIo
        alta.add(new Processo(1, 0, 6, 0, 0, -1));          //so CPU, longo
        alta.add(new Processo(2, 0, 5, 1, 4, 2)); //disco   (pede depois de 2 de CPU)
        alta.add(new Processo(3, 0, 4, 2, 3, 1)); //fita    (pede depois de 1 de CPU)
        alta.add(new Processo(4, 0, 3, 3, 5, 1)); //impressora (pede depois de 1 de CPU)
        alta.add(new Processo(5, 0, 2, 0, 0, -1));          //so CPU, curto

        ArrayList<Processo> todos = new ArrayList<>(alta); //guarda todos pro resumo final

        int tempo = 0, finalizados = 0;      //relogio da simulacao e contador de finalizados
        int preempcoes = 0, ociosa = 0;      //contadores pras metricas
        int[] qtdIo = new int[4];            //quantos I/O de cada tipo (qtdIo[1] = disco...)

        //todo processo novo entra PRONTO na fila ALTA
        for (Processo p : alta) {
            p.status = "PRONTO";
            System.out.printf("[t=%03d] P%d criado -> fila ALTA\n", tempo, p.pid);
        }

        // =============================================================
        // PARTE 4 - CICLO PRINCIPAL, SELECAO E CPU
        // Repete ate todos os processos terminarem.
        // =============================================================
        //loop principal
        while (finalizados < total) {

            //PASSO 1 - SELECAO: a fila ALTA tem preferencia; a BAIXA so se a ALTA estiver vazia.
            //dentro de cada fila sai o primeiro que entrou (remove(0) = FIFO)
            //selecionador do processo com alta na prioridade
            Processo atual;
            if (!alta.isEmpty()) {
                atual = alta.remove(0);
            } else if (!baixa.isEmpty()) {
                atual = baixa.remove(0);
            } else {
                //ninguem pronto, cpu fica parada
                System.out.printf("[t=%03d] CPU ociosa\n", tempo);
                ociosa++;
                tempo++;
                atualizarIo(ioLista, alta, baixa, tempo);
                continue;
            }

            //PASSO 2 - QUANTO TEMPO VAI EXECUTAR: no maximo 1 quantum
            //(ou menos, se faltar pouco pra ele terminar)
            int exec = Math.min(quantum, atual.tempoRestante);
            boolean pediuIo = false;

            //ve se o I/O acontece antes de acabar o quantum
            //se sim, ele roda so ate o momento de pedir o I/O
            if (!atual.ioFeito && atual.tipoIo > 0 && atual.momentoIo - atual.cpuUsada < exec) {
                exec = atual.momentoIo - atual.cpuUsada;
                pediuIo = true;
            }

            //PASSO 3 - EXECUCAO
            atual.status = "EXECUTANDO";
            System.out.printf("[t=%03d] CPU executa P%d por %d unidades\n", tempo, atual.pid, exec);

            //anda de 1 em 1 pro I/O dos outros ir passando junto
            for (int i = 0; i < exec; i++) {
                tempo++;
                atualizarIo(ioLista, alta, baixa, tempo);
            }
            atual.tempoRestante -= exec;
            atual.cpuUsada += exec;

            //PASSO 4 - PRA ONDE O PROCESSO VAI DEPOIS DE SAIR DA CPU?
            if (pediuIo) {
                // PARTE 6 - BLOQUEIO POR I/O: sai da CPU e vai pra fila de I/O
                atual.ioFeito = true;
                atual.status = "BLOQUEADO";
                ioLista.add(new IoItem(atual, atual.tempoIo));
                qtdIo[atual.tipoIo]++;
                System.out.printf("[t=%03d] P%d solicitou I/O %s por %d unidades\n", tempo, atual.pid, nomesIo[atual.tipoIo], atual.tempoIo);
            } else if (atual.tempoRestante == 0) {
                //FINALIZACAO: o tempo de CPU restante chegou a zero
                finalizados++;
                atual.status = "FINALIZADO";
                atual.fim = tempo;
                System.out.printf("[t=%03d] P%d finalizado\n", tempo, atual.pid);
            } else {
                // =====================================================
                // PARTE 5 - PREEMPCAO E FEEDBACK
                // Preempcao: usou o quantum inteiro e NAO terminou,
                // entao o SO tira ele da CPU a forca.
                // Feedback: ele e rebaixado pra fila BAIXA (prioridade 2).
                // Quem usa muita CPU perde prioridade; os curtos passam na frente.
                // =====================================================
                atual.prioridade = 2;
                atual.status = "PRONTO";
                baixa.add(atual);
                preempcoes++;
                System.out.printf("[t=%03d] P%d sofreu preempcao -> fila BAIXA\n", tempo, atual.pid);
            }
        }

        System.out.printf("[fim] Processos finalizados: %d/%d | tempo total: %d\n", finalizados, total, tempo);

        // =============================================================
        // EXTRA - RESUMO FINAL E METRICAS
        // turnaround = instante em que terminou - instante em que chegou (todos chegam em t=0)
        // espera     = turnaround - tempo de CPU - tempo em I/O (tempo parado nas filas)
        // =============================================================
        //resumo final
        System.out.println("\nPID | PPID | CPU | I/O        | Fim | Turnaround | Espera | Status");
        double somaTurn = 0, somaEspera = 0;
        for (Processo p : todos) {
            int turnaround = p.fim; //todos chegam em t=0
            int espera = turnaround - p.tempoTotal - (p.ioFeito ? p.tempoIo : 0); //tempo parado nas filas
            somaTurn += turnaround;
            somaEspera += espera;
            System.out.printf("P%-2d | %4d | %3d | %-10s | %3d | %10d | %6d | %s\n",
                    p.pid, p.ppid, p.tempoTotal, nomesIo[p.tipoIo], p.fim, turnaround, espera, p.status);
        }
        System.out.printf("Preempcoes: %d\n", preempcoes);
        System.out.printf("I/O -> DISCO=%d | FITA=%d | IMPRESSORA=%d\n", qtdIo[1], qtdIo[2], qtdIo[3]);
        System.out.printf("Espera media: %.2f | Turnaround medio: %.2f\n", somaEspera / total, somaTurn / total);
        System.out.printf("CPU ociosa: %d unidades (%.1f%%)\n", ociosa, 100.0 * ociosa / tempo);
    }
}
