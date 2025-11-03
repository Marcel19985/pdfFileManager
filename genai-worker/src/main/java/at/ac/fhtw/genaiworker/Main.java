package at.ac.fhtw.genaiworker;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting GenAI Worker...");
        RabbitConsumer consumer = new RabbitConsumer();
        consumer.start();
    }
}