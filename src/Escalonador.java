import java.util.ArrayList;

public class Escalonador {

    //estrutura do processo
    static class Processo {
        int id, tempoRestante, tipoIo, tempoIo, momentoIo, prioridade;
        boolean ioFeito;

        public Processo(int id, int tempo, int tipoIo, int tempoIo, int momentoIo) {
            this.id = id;
            this.tempoRestante = tempo;
            this.tipoIo = tipoIo; // 0=nenhum, 1=disco, 2=fita, 3=impressora
            this.tempoIo = tempoIo;
            this.momentoIo = momentoIo;
            this.prioridade = 1; //fila alta
            this.ioFeito = false;
        }
    }

    static class IoItem {
        Processo p;
        int espera;
        public IoItem(Processo p, int espera) { this.p = p; this.espera = espera; }
    }

    public static void main(String[] args) {
        int quantum = 3;
        int total = 5;

        System.out.printf("[config] quantum=%d | processos=%d | valores fixos\n", quantum, total);

        ArrayList<Processo> alta = new ArrayList<>();
        ArrayList<Processo> baixa = new ArrayList<>();
        ArrayList<IoItem> ioLista = new ArrayList<>();

        //criar os 5 processos
        alta.add(new Processo(1, 6, 0, 0, -1));
        alta.add(new Processo(2, 5, 1, 4, 2)); //disco
        alta.add(new Processo(3, 4, 2, 3, 1)); //fita
        alta.add(new Processo(4, 3, 3, 5, 1)); //impressora
        alta.add(new Processo(5, 2, 0, 0, -1));

        int tempo = 0, finalizados = 0;
        for (Processo p : alta) {
            System.out.printf("[t=%03d] P%d criado -> fila ALTA\n", tempo, p.id);
        }

        //loop principal
        while (finalizados < total) {
            // atualizar entrada/saida
            ArrayList<IoItem> concluidos = new ArrayList<>();
            for (IoItem item : ioLista) {
                item.espera--;
                if (item.espera <= 0) concluidos.add(item);
            }

            for (IoItem item : concluidos) {
                ioLista.remove(item);
                Processo p = item.p;
                // disco volta pra baixa/ fita e impressora vão pra alta
                if (p.tipoIo == 1) {
                    p.prioridade = 2;
                    baixa.add(p);
                    System.out.printf("[t=%03d] P%d retornou do DISCO -> fila BAIXA\n", tempo, p.id);
                } else {
                    p.prioridade = 1;
                    alta.add(p);
                    String nome = (p.tipoIo == 2) ? "FITA" : "IMPRESSORA";
                    System.out.printf("[t=%03d] P%d retornou da %s -> fila ALTA\n", tempo, p.id, nome);
                }
            }

            //selecionador do processo com alta na prioridade
            Processo atual;
            boolean veioAlta = false;
            if (!alta.isEmpty()) {
                atual = alta.remove(0);
                veioAlta = true;
            } else if (!baixa.isEmpty()) {
                atual = baixa.remove(0);
            } else {
                tempo++;
                continue;
            }

            int exec = Math.min(quantum, atual.tempoRestante);
            boolean pediuIo = false;

            if (!atual.ioFeito && atual.tipoIo > 0 && atual.momentoIo < exec) {
                exec = atual.momentoIo;
                pediuIo = true;
            }

            System.out.printf("[t=%03d] CPU executa P%d por %d unidades\n", tempo, atual.id, exec);
            tempo += exec;
            atual.tempoRestante -= exec;

            if (pediuIo) {
                atual.ioFeito = true;
                ioLista.add(new IoItem(atual, atual.tempoIo));
                String nomeIo = (atual.tipoIo == 1) ? "DISCO" : (atual.tipoIo == 2) ? "FITA" : "IMPRESSORA";
                System.out.printf("[t=%03d] P%d solicitou I/O %s\n", tempo, atual.id, nomeIo);
            } else if (atual.tempoRestante == 0) {
                finalizados++;
                System.out.printf("[t=%03d] P%d finalizado\n", tempo, atual.id);
            } else {
                if (veioAlta) atual.prioridade = 2;
                baixa.add(atual);
                System.out.printf("[t=%03d] P%d sofreu preempção -> fila BAIXA\n", tempo, atual.id);
            }
        }

        System.out.printf("[fim] Processos finalizados: %d/%d | tempo total: %d\n", finalizados, total, tempo);
    }
}