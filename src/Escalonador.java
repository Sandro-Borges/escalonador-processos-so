import java.util.ArrayList;

public class Escalonador {

    //estrutura do processo (PCB)
    static class Processo {
        int pid, ppid, tempoRestante, tempoTotal, cpuUsada, tipoIo, tempoIo, momentoIo, prioridade, fim;
        String status;
        boolean ioFeito;

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

    static class IoItem {
        Processo p;
        int espera;
        public IoItem(Processo p, int espera) { this.p = p; this.espera = espera; }
    }

    static String[] nomesIo = {"NENHUM", "DISCO", "FITA", "IMPRESSORA"};

    //passa 1 unidade de tempo na fila de I/O e devolve quem terminou pra fila certa
    static void atualizarIo(ArrayList<IoItem> ioLista, ArrayList<Processo> alta, ArrayList<Processo> baixa, int tempo) {
        ArrayList<IoItem> concluidos = new ArrayList<>();
        for (IoItem item : ioLista) {
            item.espera--;
            if (item.espera <= 0) concluidos.add(item);
        }

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
        int quantum = 3;
        int total = 5;

        System.out.printf("[config] quantum=%d | processos=%d | valores fixos\n", quantum, total);

        ArrayList<Processo> alta = new ArrayList<>();
        ArrayList<Processo> baixa = new ArrayList<>();
        ArrayList<IoItem> ioLista = new ArrayList<>();

        //criar os 5 processos (todos filhos do processo inicial, ppid 0)
        alta.add(new Processo(1, 0, 6, 0, 0, -1));
        alta.add(new Processo(2, 0, 5, 1, 4, 2)); //disco
        alta.add(new Processo(3, 0, 4, 2, 3, 1)); //fita
        alta.add(new Processo(4, 0, 3, 3, 5, 1)); //impressora
        alta.add(new Processo(5, 0, 2, 0, 0, -1));

        ArrayList<Processo> todos = new ArrayList<>(alta); //guarda todos pro resumo final

        int tempo = 0, finalizados = 0;
        int preempcoes = 0, ociosa = 0;
        int[] qtdIo = new int[4];

        for (Processo p : alta) {
            p.status = "PRONTO";
            System.out.printf("[t=%03d] P%d criado -> fila ALTA\n", tempo, p.pid);
        }

        //loop principal
        while (finalizados < total) {

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

            int exec = Math.min(quantum, atual.tempoRestante);
            boolean pediuIo = false;

            //ve se o I/O acontece antes de acabar o quantum
            if (!atual.ioFeito && atual.tipoIo > 0 && atual.momentoIo - atual.cpuUsada < exec) {
                exec = atual.momentoIo - atual.cpuUsada;
                pediuIo = true;
            }

            atual.status = "EXECUTANDO";
            System.out.printf("[t=%03d] CPU executa P%d por %d unidades\n", tempo, atual.pid, exec);

            //anda de 1 em 1 pro I/O dos outros ir passando junto
            for (int i = 0; i < exec; i++) {
                tempo++;
                atualizarIo(ioLista, alta, baixa, tempo);
            }
            atual.tempoRestante -= exec;
            atual.cpuUsada += exec;

            if (pediuIo) {
                atual.ioFeito = true;
                atual.status = "BLOQUEADO";
                ioLista.add(new IoItem(atual, atual.tempoIo));
                qtdIo[atual.tipoIo]++;
                System.out.printf("[t=%03d] P%d solicitou I/O %s por %d unidades\n", tempo, atual.pid, nomesIo[atual.tipoIo], atual.tempoIo);
            } else if (atual.tempoRestante == 0) {
                finalizados++;
                atual.status = "FINALIZADO";
                atual.fim = tempo;
                System.out.printf("[t=%03d] P%d finalizado\n", tempo, atual.pid);
            } else {
                atual.prioridade = 2;
                atual.status = "PRONTO";
                baixa.add(atual);
                preempcoes++;
                System.out.printf("[t=%03d] P%d sofreu preempcao -> fila BAIXA\n", tempo, atual.pid);
            }
        }

        System.out.printf("[fim] Processos finalizados: %d/%d | tempo total: %d\n", finalizados, total, tempo);

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
