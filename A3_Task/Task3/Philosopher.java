package Task3;

import Task2.common.BaseThread;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Philosopher. Outlines main subrutines of our virtual philosopher.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Philosopher extends BaseThread {

    /**
     * Max time an action can take (in milliseconds)
     */
    public static final long TIME_TO_WASTE = 1000;

    /**
     * The act of eating. - Print the fact that a given phil (their TID) has
     * started eating. - yield - Then sleep() for a random interval. - yield -
     * The print that they are done eating.
     */
    public void eat() //TASK 1 EAT METHOD IMPLEMENTATION
    {
        try {
            // ...
            System.out.println("Philosopher " + this.iTID + " has started EATING.");
            Thread.yield();
            sleep((long) (Math.random() * TIME_TO_WASTE));
            Thread.yield();
            System.out.println("Philosopher " + this.iTID + " is done eating.");
            // ...
        } catch (InterruptedException e) {
            System.err.println("Philosopher.eat():");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
    }

    /**
     * The act of thinking. - Print the fact that a given phil (their TID) has
     * started thinking. - yield - Then sleep() for a random interval. - yield -
     * The print that they are done thinking.
     */
    public void think() //TASK 1 THINK METHOD IMPLEMENTATION
    {
        try {
            System.out.println("Philosopher " + this.iTID + " started THINKING.");
            Thread.yield();
            sleep((long) (Math.random() * TIME_TO_WASTE));
            Thread.yield();
            System.out.println("Philosopher " + this.iTID + " is done THINKING.");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException caught. Exiting...");
            System.exit(1);
        }

    }

    /**
     * The act of talking. - Print the fact that a given phil (their TID) has
     * started talking. - yield - Say something brilliant at random - yield -
     * The print that they are done talking.
     */
    public void talk() //TASK 1 TALK METHOD IMPLEMENTATION
    {
        // ...
        System.out.println("Philosopher " + this.iTID + " started TALKING");
        Thread.yield();
        saySomething();
        Thread.yield();
        System.out.println("Philosopher " + this.iTID + " is done TALKING");

        // ...
    }

    /**
     * No, this is not the act of running, just the overridden Thread.run()
     */
    public void run() {
        for (int i = 0; i < DiningPhilosophers.DINING_STEPS; i++) {
            try {
                DiningPhilosophers.soMonitor.pickUp(getTID());

                eat();

                DiningPhilosophers.soMonitor.putDown(getTID());

                think();

                /*
                * TODO:
                * A decision is made at random whether this particular
                * philosopher is about to say something terribly useful.
                 */
                if (Math.random() > 0.65);      //TASK1 (probability a philosopher would like to say something)
                {
                    DiningPhilosophers.soMonitor.requestTalk(getTID());
                    talk();
                    DiningPhilosophers.soMonitor.endTalk(getTID());
                }

                yield();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException caught. Exiting...");
                System.exit(1);
            }
        }
    } // run()

    /**
     * Prints out a phrase from the array of phrases at random. Feel free to add
     * your own phrases.
     */
    public void saySomething() {
        String[] astrPhrases
                = {
                    "Eh, it's not easy to be a philosopher: eat, think, talk, eat...",
                    "You know, true is false and false is true if you think of it",
                    "2 + 2 = 5 for extremely large values of 2...",
                    "If thee cannot speak, thee must be silent",
                    "My number is " + getTID() + ""
                };

        System.out.println(
                "Philosopher " + getTID() + " says: "
                + astrPhrases[(int) (Math.random() * astrPhrases.length)]
        );
    }
}

// EOF
