package Task3;

import java.util.Scanner;

/**
 * Class DiningPhilosophers The main starter.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class DiningPhilosophers {

    /*
	 * ------------
	 * Data members
	 * ------------
     */
    /**
     * This default may be overridden from the command line
     */
    public static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 4;

    /**
     * Dining "iterations" per philosopher thread while they are socializing
     * there
     */
    public static final int DINING_STEPS = 10;

    /**
     * Our shared monitor for the philosphers to consult
     */
    public static Monitor soMonitor = null;

    /*
	 * -------
	 * Methods
	 * -------
     */
    /**
     * Main system starts up right here
     */
    public static void main(String[] argv) {

        try {
            /*
			 * TODO:
			 * Should be settable from the command line
			 * or the default if no arguments supplied.
             */
            int iPhilosophers = newPhilosopherNumber();

            // Make the monitor aware of how many philosophers there are
            soMonitor = new Monitor(iPhilosophers);

            // Space for all the philosophers
            Philosopher aoPhilosophers[] = new Philosopher[iPhilosophers];

            // Let 'em sit down
            for (int j = 0; j < iPhilosophers; j++) {
                aoPhilosophers[j] = new Philosopher();
                aoPhilosophers[j].start();
            }

            System.out.println(
                    iPhilosophers
                    + " philosopher(s) came in for a dinner."
            );

            // Main waits for all its children to die...
            // I mean, philosophers to finish their dinner.
            for (int j = 0; j < iPhilosophers; j++) {
                aoPhilosophers[j].join();
            }

            System.out.println("All philosophers have left. System terminates normally.");
        } catch (InterruptedException e) {
            System.err.println("main():");
            reportException(e);
            System.exit(1);
        }
    } // main()

    /**
     * Outputs exception information to STDERR
     *
     * @param poException Exception object to dump to STDERR
     */
    public static void reportException(Exception poException) {
        System.err.println("Caught exception : " + poException.getClass().getName());
        System.err.println("Message          : " + poException.getMessage());
        System.err.println("Stack Trace      : ");
        poException.printStackTrace(System.err);
    }

    /**
     * Asks for the user to input the newPhilosopherNumber
     *
     * @return integer representing the new number of philosopher the program
     * will spawn.
     */
    public static int newPhilosopherNumber() {
        try {
            Scanner input = new Scanner(System.in);
            System.out.println("Please input a positive number of philosopher (greater than 1) you want to spawn. (Default value is 4)");
            String parseString = input.nextLine();
            input.close();
            int newPhilosopherNumber = Integer.parseInt(parseString);

            if (newPhilosopherNumber < 2) {
                System.out.println(
                        '"' + parseString + '"' + " is not a positive decimal integer"
                        + "\nUsage: java DiningPhilosophers " + DEFAULT_NUMBER_OF_PHILOSOPHERS
                );
                //return DEFAULT_NUMBER_OF_PHILOSOPHERS;
                System.exit(1);
            } else {
                return newPhilosopherNumber;
            }
        } catch (NumberFormatException e) {
            System.out.println("Input is not a valid number. Exiting...");
            System.exit(1);
        }
        return DEFAULT_NUMBER_OF_PHILOSOPHERS;
    }
}

// EOF
