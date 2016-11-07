package edu.cmu.cs.vbc.prog.elevator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public  class  PL_Interface_impl  {

	// to trigger the <init> of FeatureSwitches
	private FeatureSwitches fs = new FeatureSwitches();

//	@FilterField
	//@Symbolic("false")
	public static boolean executedUnimplementedAction = false;

	
//	@FilterField
	//@Symbolic("false")
	public static List<String> actionHistory = new ArrayList<String>();

	
	//@Symbolic("false")
	private static int cleanupTimeShifts = 24;

	
	private static final int NUM_FLOORS = 5;

	
//	@FilterField	
	//@Symbolic("false")
	private static boolean verbose = false;

	
//	@FilterField
	//@Symbolic("false")
	private static boolean isAbortedRun = false;

	

	public static void main(String[] args) {
		try {
			PL_Interface_impl impl = new PL_Interface_impl();
			
			verbose = true;
//			int specification = Integer.parseInt(args[0]);
//			int variation = Integer.parseInt(args[1]);
			impl.start(1, -1);
			System.out.println("no Exception");
		} catch (Throwable e) {
			System.out.println("Caught Exception: " + e.getClass() + " "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	

	public void start(int specification, int variation) throws Throwable {
	try {
		if (verbose) 
			System.out.println("Started Elevator PL with Specification " + String.valueOf(specification) +  ", Variation: " +String.valueOf(variation));
		test(specification, variation);
	} catch (Throwable e) {
		throw e;
	}finally {
		/*System.out.println("Penalty");
		if (!isAbortedRun) {
			int x = 1;
			for (int i = 0; i < 6000000; i++) {
				x = i / x + 10;
			}
		}*/
	}
	}


	public List<String> getExecutedActions() {
		return actionHistory;
	}

	
	
	public boolean isAbortedRun() {
		return isAbortedRun;
	}

	

	// this method is used as hook for the liveness properties.
	public void test(int specification, int variation) {
		if (!FeatureSwitches.valid_product()) return;
		if (variation==-1) {
//            Specification1();
//			Specification2();
//			Specification3();
//			Specification9();
//			Specification13();
			Specification14();
//			switch (specification) {
//			case 1: Specification1();break;
//			case 2: Specification2();break;
//			case 3: Specification3();break;
//			case 8: /*Specification8();*/isAbortedRun=true;break;
//			case 9: Specification9();break;
//			case 10: /*Specification10();*/isAbortedRun=true;break;
//			case 11: /*Specification11();*/isAbortedRun=true;break;
//			case 13: Specification13();break;
//			case 14: Specification14();break;
//			}
		} else {
			
			randomSequenceOfActions(variation);
		}
	}

	

	public static void randomSequenceOfActions(int maxLength) {
		cleanupTimeShifts=6 * maxLength; // tests have shown that this is enough
		Environment env = new Environment(NUM_FLOORS);
		Elevator e;
		if (getBoolean()) {
			// elevator from 0 going up
			e = new Elevator(env, verbose);
			actionHistory.add("StartFromBottom");
		} else {
			// elevator from 4 going down
			e = new Elevator(env, verbose, 4, false);
			actionHistory.add("StartFromTop");
		}
		Actions a = new Actions(env, e);

		int counter = 0;
		while (counter < maxLength) {
			counter++;
			int action = getIntegerMinMax(0, 7); 
			String actionName = "";
            if (action == 0) {
				a.bobCall(); actionName = "bobCall";
			} else if (action == 1) {
				a.aliceCall(); actionName = "aliceCall";
			} else if (action == 2) {
				a.angelinaCall(); actionName = "angelinaCall";
			} else if (action == 3) {
				a.chuckCall(); actionName = "chuckCall";
			} else if (action == 4) {
				a.monicaCall(); actionName = "monicaCall";
			} else if (action == 5) {
				a.bigMacCall(); actionName = "bigMacCall";
			} else if (action == 6) {
				e.timeShift(); actionName = "1TS";
			} else if (action == 7) {
				actionName = "3TS";
				for (int i = 0; i < 3; i++) {
					e.timeShift();
				}
			} else {
				throw new InternalError("getIntegerMinMax produced illegal value:" + action);
			}
			actionHistory.add(actionName);
			//System.out.println(listToString(actionHistory));
			if (e.isBlocked()) {
				//System.out.println("Stopping Simulation path because Elevator is stuck (overloaded)");
				return;
			}
		}
		for (counter = 0; counter < cleanupTimeShifts && ! e.isBlocked(); counter++) {
			if (e.isIdle())
				return;
			e.timeShift();
		}
	}

	
	
	
	
	
	
	

	public static void modifiedrandomSequenceOfActions(int maxLength) {
		Environment env = new Environment(NUM_FLOORS);
		Elevator e;
		if (getBoolean()) {
			// elevator from 0 going up
			e = new Elevator(env, verbose);
			actionHistory.add("StartFromBottom");
		} else {
			// elevator from 4 going down
			e = new Elevator(env, verbose, 4, false);
			actionHistory.add("StartFromTop");
		}
		Actions a = new Actions(env, e);

		int counter = 0;
		while (counter < maxLength) {
			counter++;
			int action = getIntegerMinMax(0, 7); 
			String actionName = "";
			boolean action0 = getBoolean();
			boolean action1 = getBoolean();
			boolean action2 = getBoolean();
			boolean action3 = getBoolean();
			boolean action4 = getBoolean();
			boolean action5 = getBoolean();
			boolean action6 = getBoolean();
			boolean action7 = action6 ? false : getBoolean();
			
			
			if (counter < maxLength && action0) {a.bobCall(); actionHistory.add("bobCall"); counter++; }
			if (counter < maxLength && action1) {a.aliceCall(); actionHistory.add("aliceCall"); counter++; }
			if (counter < maxLength && action2) {a.angelinaCall(); actionHistory.add("angelinaCall"); counter++; }
			if (counter < maxLength && action3) {a.chuckCall(); actionHistory.add("chuckCall"); counter++; }
			if (counter < maxLength && action4) {a.monicaCall();	actionHistory.add("monicaCall"); counter++; }
			if (counter < maxLength && action5) {a.bigMacCall();	actionHistory.add("bigMacCall"); counter++; }
			if (counter < maxLength && action6) {e.timeShift(); actionHistory.add("1TS"); counter++; } // execute one timestep
			if (counter < maxLength && action7) {
				actionHistory.add("3TS");// execute three timesteps
				for (int i = 0; i < 3; i++) {
					e.timeShift();
				}
			}
			if (e.isBlocked()) {
				//System.out.println("Stopping Simulation path because Elevator is stuck (overloaded)");
				return;
			}
		}
		for (counter = 0; counter < cleanupTimeShifts && ! e.isBlocked(); counter++) {
			if (e.isIdle())
				return;
			e.timeShift();
		}
	}

	
	
	
	public void Specification1() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, false);
		Actions a = new Actions(env, e);

		a.bigMacCall();
		a.angelinaCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++)
			e.timeShift();
	}

	
	public void Specification2() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, false);
		Actions a = new Actions(env, e);

		a.bigMacCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++) {
			e.timeShift();
		}
	}

	
	public void Specification3() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, false, 4, false);
		Actions a = new Actions(env, e);

		Person bob = a.bobCall();
		while (env.getFloor(bob.getOrigin()).hasCall())
			e.timeShift();
		// bob has been picked up in executive Suite
		
		e.timeShift();
		// executive suite calls again
		// (lift should reverse directions although in-lift button for bob's
		// destination is still pressed)
		// direction, is active)
		a.bobCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++)
			e.timeShift();
	}

	
	public void Specification9() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, false);
		Actions a = new Actions(env, e);

		a.bigMacCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++)
			e.timeShift();
	}

	
	public void Specification13() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, false);
		Actions a = new Actions(env, e);

		a.aliceCall();
		Person angelina = a.angelinaCall();
		while (env.getFloor(angelina.getOrigin()).hasCall()) {
			e.timeShift();
		}
		a.bobCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++)
			e.timeShift();
	}

	
	public void Specification14() {
		Environment env = new Environment(5);
		Elevator e = new Elevator(env, true);
		Actions a = new Actions(env, e);

		Person bm = a.bigMacCall();

		while (env.getFloor(bm.getOrigin()).hasCall()) {
			e.timeShift();
		}
		a.bobCall();
		for (int i = 0; i < cleanupTimeShifts && !e.isBlocked(); i++)
			e.timeShift();
	}

	
	public static int getIntegerMinMax(int min, int max) {
		return min + new Random().nextInt(1 + (max - min));
	}

	
	public static boolean getBoolean() {
		return true;//Verify.getBoolean();// verify true first
	}

	
	
	static String listToString(List<String> list) {
		String ret = "";
		for (String s : list) {
			ret = ret + " " + s;
		}
		return ret;
	}
}
