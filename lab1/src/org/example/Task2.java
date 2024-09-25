package org.example;

import mpi.MPI;

public class Task2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(), size = MPI.COMM_WORLD.Size();
        System.out.println("Ранг: " + rank + ", Размер: " + size);

        if (rank == 0) {
            // Нельзя использовать тот же буфер, что используется для неблокирующей отправки
            int[] rankBuffer = new int[] {0};
            // Нулевой ранг отправляет первым, учитывается случай, когда процесс всего один
            MPI.COMM_WORLD.Isend(rankBuffer, 0, 1, MPI.INT, (rank + 1) % size, 0);
        }

        int[] rankBuffer = new int[] {0};
        // Получить ранг с предыдущего процесса (с учётом нулевого)
        MPI.COMM_WORLD.Recv(rankBuffer, 0, 1, MPI.INT, (size + rank - 1) % size, 0);
        if (rank != 0) {
            rankBuffer[0] += rank;
            // Отправляем следующему
            MPI.COMM_WORLD.Isend(rankBuffer, 0, 1, MPI.INT, (rank + 1) % size, 0);
        } else {
            // Выводим финальный результат
            System.out.println("Финальная сумма: " + rankBuffer[0]);
        }

        MPI.Finalize();
    }
}
