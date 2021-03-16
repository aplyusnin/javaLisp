package ru.nsu.fit.javalisp.parser;

import java.lang.reflect.Method;
import java.util.HashMap;

public class StateMachine {
	private HashMap<String, State> states;

	private State current;
	private State error;

	public StateMachine(){
		states = new HashMap<>();
		error = new State("Error");
		states.put("Error", error);
	}

	public void setErrorTransitionFunction(Object o, Method m){
		for (var state : states.entrySet()){
			state.getValue().setErrorTransition(o, m);
		}
	}

	public void setStartState(String name) throws Exception{
		if (states.containsKey(name)) current = states.get(name);
		else throw new Exception("No such state");
	}

	public void createState(String name) throws Exception {
		if (states.containsKey(name)) throw new Exception("State already exists");
		State state = new State(name);
		states.put(name, state);
		state.setErrorState(error);
	}

	private void addTransition(Character ch, State from, State to, Object o, Method m){
		from.addTransition(ch, to, o, m);
	}

	public void addTransition(Character ch, String from, String to, Object o, Method m) throws Exception {
		if (!states.containsKey(from)) throw new Exception("There is no state: " + from);
		if (!states.containsKey(to)) throw new Exception("There is no state: " + to);
		addTransition(ch, states.get(from), states.get(to), o, m);
	}

	public void addTransition(Character ch, String from, String to) throws Exception {
		if (!states.containsKey(from)) throw new Exception("There is no state: " + from);
		if (!states.containsKey(to)) throw new Exception("There is no state: " + to);
		addTransition(ch, states.get(from), states.get(to), null, null);
	}

	public void addTransitions(String s, String from, String to) throws Exception {
		if (!states.containsKey(from)) throw new Exception("There is no state: " + from);
		if (!states.containsKey(to)) throw new Exception("There is no state: " + to);
		State _from = states.get(from);
		State _to = states.get(to);
		for (int i = 0; i < s.length(); i++){
			addTransition(s.charAt(i), _from, _to, null, null);
		}
	}
	public void addTransitions(String s, String from, String to, Object o, Method m) throws Exception {
		if (!states.containsKey(from)) throw new Exception("There is no state: " + from);
		if (!states.containsKey(to)) throw new Exception("There is no state: " + to);
		State _from = states.get(from);
		State _to = states.get(to);
		for (int i = 0; i < s.length(); i++){
			addTransition(s.charAt(i), _from, _to, o, m);
		}
	}


	public State feedChar(char ch) throws Exception {
		return current = current.makeTransition(ch);
	}

	public String getCurrentStateName(){
		return current.getName();
	}
}
