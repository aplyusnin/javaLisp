package ru.nsu.fit.javalisp;

import java.util.LinkedList;
import java.util.List;


public class Token {
	private StringBuilder value;
	private String result;
	private List<Token> subTokens;
	public enum Type {
		VARIABLE,
		STRING,
		INT,
		FLOAT,
		COMPLEX
	}

	private Type type;
	private boolean processed = false;

	public Token(){
		value = new StringBuilder();
		subTokens = new LinkedList<>();
	}

	public void process(){
		result = value.toString();
		processed = true;
	}

	public void addToken(Token token){subTokens.add(token);}


	public List<Token> getSubTokens() {
		return subTokens;
	}

	public void addChar(char ch){
		value.append(ch);
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getResult() {
		if (!processed) return "";
		return result;
	}
}
