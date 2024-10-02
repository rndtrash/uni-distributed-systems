package org.example;

import mpi.MPI;
import mpi.Request;

public class Task2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(), size = MPI.COMM_WORLD.Size();
        System.out.println("Ранг: " + rank + ", Размер: " + size);

        int nextRank = (rank + 1) % size, prevRank = (size + rank - 1) % size;

        // Блокирующая отправка
        int[] rankBuffer = new int[]{0};
        if (rank == 0) {
            // Нулевой ранг отправляет первым, учитывается случай, когда процесс всего один
            MPI.COMM_WORLD.Send(rankBuffer, 0, 1, MPI.INT, nextRank, 0);
        } else {
            // Получить ранг с предыдущего процесса (с учётом нулевого)
            MPI.COMM_WORLD.Recv(rankBuffer, 0, 1, MPI.INT, prevRank, 0);
        }
        System.out.println("Блокирующая отправка от " + rank);

        if (rank != 0) {
            rankBuffer[0] += rank;
            // Отправляем следующему
            MPI.COMM_WORLD.Send(rankBuffer, 0, 1, MPI.INT, nextRank, 0);
        } else {
            // Выводим финальный результат, получаемый от последнего процесса
            MPI.COMM_WORLD.Recv(rankBuffer, 0, 1, MPI.INT, prevRank, 0);
            System.out.println("Финальная сумма: " + rankBuffer[0]);
        }

        MPI.COMM_WORLD.Barrier();

        // Неблокирующая отправка
        Request rq;
        rankBuffer[0] = 0;
        if (rank == 0) {
            rq = MPI.COMM_WORLD.Isend(rankBuffer, 0, 1, MPI.INT, nextRank, 0);
        } else {
            rq = MPI.COMM_WORLD.Irecv(rankBuffer, 0, 1, MPI.INT, prevRank, 0);
        }
        System.out.println("Неблокирующая отправка от " + rank);
        rq.Wait();
        System.out.println("Процесс " + rank + " дождался отправки");

        if (rank != 0) {
            rankBuffer[0] += rank;
            rq = MPI.COMM_WORLD.Isend(rankBuffer, 0, 1, MPI.INT, nextRank, 0);
        } else {
            rq = MPI.COMM_WORLD.Irecv(rankBuffer, 0, 1, MPI.INT, prevRank, 0);
        }
        rq.Wait();
        if (rank == 0) {
            System.out.println("Финальная сумма: " + rankBuffer[0]);
        }

        MPI.COMM_WORLD.Barrier();

        // Одновременная отправка и получение
        rankBuffer[0] = 0;
        System.out.println("Одновременная отправка и получение от " + rank);
        if (rank == 0) {
            int[] rankBuffer1 = new int[] {0};
            MPI.COMM_WORLD.Sendrecv(rankBuffer, 0, 1, MPI.INT, nextRank, 0, rankBuffer1, 0, 1, MPI.INT, prevRank, 0);
            System.out.println("Финальная сумма: " + rankBuffer1[0]);
        } else {
            MPI.COMM_WORLD.Recv(rankBuffer, 0, 1, MPI.INT, prevRank, 0);
            rankBuffer[0] += rank;
            MPI.COMM_WORLD.Send(rankBuffer, 0, 1, MPI.INT, nextRank, 0);
        }

        MPI.Finalize();
    }
}
