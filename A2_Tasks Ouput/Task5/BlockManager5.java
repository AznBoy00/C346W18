// Import (aka include) some stuff.

import common.*;

/**
 * Class BlockManager Implements character block "manager" and does twists with
 * threads.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca; Inspired by previous code
 * by Prof. D. Probst
 *
 * $Revision: 1.5 $ $Last Revision Date: 2018/02/04 $
 *
 */
public class BlockManager {

    /**
     * The stack itself
     */
    private static BlockStack soStack = new BlockStack();

    /**
     * Number of threads dumping stack
     */
    private static final int NUM_PROBERS = 4;

    /**
     * Number of steps they take
     */
    private static int siThreadSteps = 5;

    /**
     * For atomicity
     */
    private static Semaphore mutex = new Semaphore (1);

    /*
	 * For synchronization
     */
    private static Semaphore s1 = new Semaphore(0);
    /**
     * s1 is to make sure phase I for all is done before any phase II begins
     */
    /**
     * s2 is for use in conjunction with Thread.turnTestAndSet() for phase II
     * proceed in the thread creation order
     */
    private static Semaphore s2 = new Semaphore(1); //TASK5 s2=1 => at most one thread can reach the CS
	//the counter to be used with the semaphores
    private static int counter = 0; 
    // The main()
    public static void main(String[] argv) {
        try {
            // Some initial stats...
            System.out.println("Main thread starts executing.");
            System.out.println("Initial value of top = " + soStack.getTop() + ".");
            System.out.println("Initial value of stack top = " + soStack.pick() + ".");
            System.out.println("Main thread will now fork several threads.");

            /*
			 * The birth of threads
             */
            AcquireBlock ab1 = new AcquireBlock();
            AcquireBlock ab2 = new AcquireBlock();
            AcquireBlock ab3 = new AcquireBlock();

            System.out.println("main(): Three AcquireBlock threads have been created.");

            ReleaseBlock rb1 = new ReleaseBlock();
            ReleaseBlock rb2 = new ReleaseBlock();
            ReleaseBlock rb3 = new ReleaseBlock();

            System.out.println("main(): Three ReleaseBlock threads have been created.");

            // Create an array object first
            CharStackProber aStackProbers[] = new CharStackProber[NUM_PROBERS];

            // Then the CharStackProber objects
            for (int i = 0; i < NUM_PROBERS; i++) {
                aStackProbers[i] = new CharStackProber();
            }

            System.out.println("main(): CharStackProber threads have been created: " + NUM_PROBERS);

            /*
			 * Twist 'em all
             */
            ab1.start();
            aStackProbers[0].start();
            rb1.start();
            aStackProbers[1].start();
            ab2.start();
            aStackProbers[2].start();
            rb2.start();
            ab3.start();
            aStackProbers[3].start();
            rb3.start();

            System.out.println("main(): All the threads are ready.");

            /*
			 * Wait by here for all forked threads to die
             */
            ab1.join();
            ab2.join();
            ab3.join();

            rb1.join();
            rb2.join();
            rb3.join();

            for (int i = 0; i < NUM_PROBERS; i++) {
                aStackProbers[i].join();
            }

            // Some final stats after all the child threads terminated...
            System.out.println("System terminates normally.");
            System.out.println("Final value of top = " + soStack.getTop() + ".");
            System.out.println("Final value of stack top = " + soStack.pick() + ".");
            System.out.println("Final value of stack top-1 = " + soStack.getAt(soStack.getTop() - 1) + ".");
            System.out.println("Stack access count = " + soStack.getAccessCounter());

            System.exit(0);
        } catch (InterruptedException e) {
            System.err.println("Caught InterruptedException (internal error): " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            reportException(e);
        } finally {
            System.exit(1);
        }
    } // main()

    /**
     * Inner AcquireBlock thread class.
     */
    static class AcquireBlock extends BaseThread {

        /**
         * A copy of a block returned by pop().
         *
         * @see BlocStack#pop()
         */
        private char cCopy;

        public void run() {
            System.out.println("AcquireBlock thread [TID=" + this.iTID + "] starts executing.");
            mutex.P(); //protecting the critical section (preventing other threads from interrupt) 
            phase1();
            counter++;
			if (counter==10){//only signals as soon as counter gets 10 phase1 completed, essentially all of them
				s1.V();
				}   
			mutex.V();
            try {
                System.out.println("AcquireBlock thread [TID=" + this.iTID + "] requests Ms block.");

                this.cCopy = soStack.pop();

                System.out.println(
                        "AcquireBlock thread [TID=" + this.iTID + "] has obtained Ms block " + this.cCopy
                        + " from position " + (soStack.getTop() + 1) + "."
                );

                System.out.println(
                        "Acq[TID=" + this.iTID + "]: Current value of top = "
                        + soStack.getTop() + "."
                );

                System.out.println(
                        "Acq[TID=" + this.iTID + "]: Current value of stack top = "
                        + soStack.pick() + "."
                );
            } catch (Exception e) {
                reportException(e);
                System.exit(1);
            }
            s1.P(); //can only go after counter has signaled (pass s1 if all phase 1 complete)
            s1.V();
            while(true){ //loop until thread's turn to go
				//avoid deadlock by inserting lock inside loop
				s2.P();
				//return true when thread's turn to go
				if (turnTestAndSet()){ //waits until this thread's turn
					phase2();
					System.out.println("AcquireBlock thread [TID=" + this.iTID + "] terminates.");
					break;
				}
				//signals to prevent deadlock
				s2.V();
            }
            s2.V();
        }
    } // class AcquireBlock

    /**
     * Inner class ReleaseBlock.
     */
    static class ReleaseBlock extends BaseThread {

        /**
         * Block to be returned. Default is 'a' if the stack is empty.
         */
        private char cBlock = 'a';

        public void run() {
        	System.out.println("ReleaseBlock thread [TID=" + this.iTID + "] starts executing.");
        	
        	mutex.P(); //protecting the critical section (preventing other threads from interrupt) 
        	phase1();
        	counter++;
            if (counter==10){//only signals as soon as counter gets 10 phase1 completed, essentially all of them
            	s1.V();
            	}
			mutex.V();

            try {
                if (soStack.isEmpty() == false) {
                    this.cBlock = (char) (soStack.pick() + 1);
                }

                System.out.println(
                        "ReleaseBlock thread [TID=" + this.iTID + "] returns Ms block " + this.cBlock
                        + " to position " + (soStack.getTop() + 1) + "."
                );

                soStack.push(this.cBlock);

                System.out.println(
                        "Rel[TID=" + this.iTID + "]: Current value of top = "
                        + soStack.getTop() + "."
                );

                System.out.println(
                        "Rel[TID=" + this.iTID + "]: Current value of stack top = "
                        + soStack.pick() + "."
                );
            } catch (Exception e) {
                reportException(e);
                System.exit(1);
            }
            
            s1.P(); //can only go after counter has signaled (pass s1 if all phase 1 complete)
            s1.V();
            while(true){//loop until thread's turn to go
				//avoid deadlock by inserting lock inside loop
				s2.P();
				//return true when thread's turn to go
				if (turnTestAndSet()){ ////waits until this thread's turn
					phase2();
					System.out.println("ReleaseBlock thread [TID=" + this.iTID + "] terminates.");
					break;
				}
				//signals to prevent deadlock
				s2.V();
            }
            s2.V();
        }
    } // class ReleaseBlock

    /**
     * Inner class CharStackProber to dump stack contents.
     */
    static class CharStackProber extends BaseThread {

        public void run() {
        	mutex.P(); //protecting the critical section (preventing other threads from interrupt) 
        	phase1();
        	counter++;
            if (counter==10){//only signals as soon as counter gets 10 phase1 completed, essentially all of them
            	s1.V();
            	}
			mutex.V();

            try {
                for (int i = 0; i < siThreadSteps; i++) {
                    System.out.print("Stack Prober [TID=" + this.iTID + "]: Stack state: ");

                    // [s] - means ordinay slot of a stack
                    // (s) - current top of the stack
                    for (int s = 0; s < soStack.getSize(); s++) {
                        System.out.print(
                                (s == BlockManager.soStack.getTop() ? "(" : "[")
                                + BlockManager.soStack.getAt(s)
                                + (s == BlockManager.soStack.getTop() ? ")" : "]")
                        );
                    }

                    System.out.println(".");

                }
            } catch (Exception e) {
                reportException(e);
                System.exit(1);
            }

            s1.P(); //can only go after counter has signaled (pass s1 if all phase 1 complete)
            s1.V();
            while(true){//loop until thread's turn to go
				//avoid deadlock by inserting lock inside loop
				s2.P();
				//return true when thread's turn to go
				if (turnTestAndSet()){ ////waits until this thread's turn
					phase2();
					break;
				}
				//signals to prevent deadlock
				s2.V();
			}
			s2.V();
        }
    } // class CharStackProber

    /**
     * Outputs exception information to STDERR
     *
     * @param poException Exception object to dump to STDERR
     */
    private static void reportException(Exception poException) {
        System.err.println("Caught exception : " + poException.getClass().getName());
        System.err.println("Message          : " + poException.getMessage());
        System.err.println("Stack Trace      : ");
        poException.printStackTrace(System.err);
    }
} // class BlockManager

// EOF
