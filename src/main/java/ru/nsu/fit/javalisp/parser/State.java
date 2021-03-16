package ru.nsu.fit.javalisp.parser;

import ru.nsu.fit.javalisp.Pair;

import java.lang.reflect.Method;
import java.util.HashMap;

public class State {

	private static class transitionFunction{
		private Object o;
		private Method method;
		public transitionFunction(Object o, Method m){
			this.o = o;
			this.method = m;
		}

		public void invoke() throws Exception {
			if (o != null && method != null)
				method.invoke(o);
		}
	}

	private HashMap<Character, Pair<State, transitionFunction>> transitions;

	private State errorState;
	private transitionFunction errorTransition;

	private String name;
	public State(String name){
		transitions = new HashMap<>();
		this.name = name;
	}

	public void setErrorTransition(Object o, Method m){
		if (m != null) m.setAccessible(true);
		errorTransition = new transitionFunction(o, m);
	}

	public void addTransition(Character ch, State state){
		addTransition(ch, state, null, null);
	}
	public void addTransition(Character ch, State state, Object o, Method m){
		if (m != null) m.setAccessible(true);
		transitionFunction function = new transitionFunction(o, m);
		transitions.put(ch, new Pair<>(state, function));
	}

	public void setErrorState(State error){
		errorState = error;
	}

	public State makeTransition(Character ch) throws Exception {
		if (transitions.containsKey(ch)) {
			State next = transitions.get(ch).first;
			transitions.get(ch).second.invoke();
			return next;
		}
		else{
			if (errorTransition != null) errorTransition.invoke();
			return errorState;
		}
	}

	public String getName(){
		return name;
	}

}
