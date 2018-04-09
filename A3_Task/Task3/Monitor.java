package Task3;

import java.util.*;
import java.lang.*;
/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	 //TASK2
	 private enum State{
		 THINKING, EATING, TALKING, REQUEST_TALK, REQUEST_EAT;
	 };
	 private boolean[] chopsticks;
	 private State[] states;
	 private Queue<Integer> eatPriority;
	 private Queue<Integer> talkPriority;
	 private int numOfPhil;
	 private boolean someoneTalking;
	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		// TODO: set appropriate number of chopsticks based on the # of philosophers
		chopsticks = new boolean [piNumberOfPhilosophers];
		states = new State [piNumberOfPhilosophers];
		for(State state : states)
				state = State.THINKING;
		eatPriority = new LinkedList();
		talkPriority = new LinkedList();
		numOfPhil = piNumberOfPhilosophers;
		someoneTalking = false;
	}
	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID) throws InterruptedException
	{
		int Tid = piTID -1;
		states[Tid] = State.REQUEST_EAT;
		eatPriority.add(Tid);
		while(states[getLeft(Tid)]==State.EATING || states[getRight(Tid)] == State.EATING || getLeft(Tid) == eatPriority.peek() || getRight(Tid) == eatPriority.peek()){
			this.wait();
		}
		states[Tid] = State.EATING;
		takeChopsticks(Tid);
		eatPriority.remove(Tid);
		testFree(); // call method to check..
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		int Tid = piTID - 1;
		returnChopsticks(Tid);
		states[Tid] = State.THINKING;
		this.notifyAll();
		testFree(); // call method to check..
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk(final int piTID) throws InterruptedException
	{
		int Tid = piTID - 1;
		states[Tid] = State.REQUEST_TALK;
		talkPriority.add(Tid);
		while(someoneTalking
			|| getLeft(Tid) == talkPriority.peek()
			|| getRight(Tid) == talkPriority.peek()){
				this.wait();
		}
		someoneTalking = true;
		states[Tid] = State.TALKING;
		talkPriority.remove(Tid);
		testFree();
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk(final int piTID)
	{
		int Tid = piTID - 1;
		states[Tid] = State.THINKING;
		someoneTalking = false;
		testFree();
	}
	private synchronized int getLeft(int i){
		return Math.floorMod((i - 1), (numOfPhil));
	}

	// TASK2
	private synchronized int getRight(int i){
		return Math.floorMod((i + 1), (numOfPhil));
	}

	// TASK2
	private synchronized void takeChopsticks(int i){
		chopsticks[i] = false;
		chopsticks[getRight(i)] = false;
	}

	// TASK2
	private synchronized void returnChopsticks(int i){
		chopsticks[i] = true;
		chopsticks[getRight(i)] = true;
	}

	private synchronized void testFree(){
		//check for starvation & deadlock 
	}
}

// EOF
