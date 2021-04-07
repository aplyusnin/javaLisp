package ru.nsu.fit.javalisp.translator;

import java.util.ArrayList;
import java.util.List;

public class TranslationEntry {

	public enum Type {
		VARIABLE,
		FUNCTION
	}

	public TranslationEntry(){}

	private Type type;
	private String name;

	private List<String> filledArgs;
	private int arity;

	public static class Builder{
		private Type type = Type.VARIABLE;
		private int arity = 0;
		private String name = "";
		private List<String> filledArgs;
		public Builder(){
			filledArgs = new ArrayList<>();
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setArity(int arity){
			this.arity = arity;
			return this;
		}

		public Builder setType(Type type){
			this.type = type;
			return this;
		}

		public Builder addArg(String varName){
			filledArgs.add(varName);
			return this;
		}

		public TranslationEntry build(){
			TranslationEntry entry = new TranslationEntry();
			entry.arity = arity;
			entry.name = name;
			entry.type = type;
			entry.filledArgs = filledArgs;
			return entry;
		}
	}


	public List<String> getFilledArgs() {
		return filledArgs;
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public Type getType() {
		return type;
	}

}
