package ru.nsu.fit.javalisp.translator;

import java.util.HashMap;

/**
 * Context class for variables
 */
public class Context {
	private HashMap<String, TranslationEntry> args;
	private HashMap<TranslationEntry, String> inverse;

	public Context(){
		args = new HashMap<>();
		inverse = new HashMap<>();
	}

	/**
	 * Add variable to context
	 * @param argName in-source name
	 * @param localName translation-entry
	 */
	public void add(String argName, TranslationEntry localName){
		args.put(argName, localName);
		inverse.put(localName, argName);
	}

	/**
	 * Get entry by name
 	 * @param arg in-source name
	 * @return translation-entry
	 */
	public TranslationEntry getVar(String arg){
		if (args.containsKey(arg)) return args.get(arg);
		return null;
	}

	/**
	 * Check if variable exists
	 * @param arg - variable name
	 * @return true, if variable exists
	 */
	public boolean containsVar(String arg){
		return args.containsKey(arg);
	}

	/**
	 * Get number of variables in context
	 * @return number of variables in context
	 */
	public int size(){
		return args.size();
	}

	public String getInverse(String name){
		return inverse.get(name);
	}
	public HashMap<String, TranslationEntry> getArgs() {
		return args;
	}

	@SuppressWarnings("unchecked")
	public Context clone() throws CloneNotSupportedException {
		Context clone = new Context();
		clone.args = (HashMap<String, TranslationEntry>)this.args.clone();
		clone.inverse = (HashMap<TranslationEntry, String>)this.inverse.clone();
		return clone;
	}
}
