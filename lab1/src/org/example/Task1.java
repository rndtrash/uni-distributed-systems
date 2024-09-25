package org.example;

import mpi.MPI;

import java.nio.charset.StandardCharsets;

public class Task1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(), size = MPI.COMM_WORLD.Size();
        System.out.println("Ранг: " + rank + ", Размер: " + size);
        if (rank % 2 == 0) {
            if (rank < size - 1) {
                String message = "Текст " + rank;
                byte[] messageBuff = message.getBytes();
                int[] messageLength = new int[]{messageBuff.length};
                MPI.COMM_WORLD.Send(messageLength, 0, 1, MPI.INT, rank + 1, 0);
                MPI.COMM_WORLD.Send(messageBuff, 0, messageBuff.length, MPI.BYTE, rank + 1, 0);
            }
        } else {
            if (rank > 0) {
                int[] bufferSize = new int[]{0};
                MPI.COMM_WORLD.Recv(bufferSize, 0, 1, MPI.INT, rank - 1, 0);
                byte[] buff = new byte[bufferSize[0]];
                MPI.COMM_WORLD.Recv(buff, 0, buff.length, MPI.BYTE, rank - 1, 0);
                System.out.println(new String(buff, StandardCharsets.UTF_8));
            }
        }

        MPI.Finalize();
    }
}
