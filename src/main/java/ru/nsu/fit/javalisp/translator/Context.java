package ru.nsu.fit.javalisp.translator;

import java.util.HashMap;

public class Context {
	private HashMap<String, Integer> args;

	public Context(){
		args = new HashMap<>();
	}

	public void add(String argName, Integer index){
		args.put(argName, index);
	}

	public String getVar(String arg){
		if (args.containsKey(arg)) return "$" + args.get(arg);
		return arg;
	}

	public boolean containsVar(String arg){
		return args.containsKey(arg);
	}
}
