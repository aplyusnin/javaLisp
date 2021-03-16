package ru.nsu.fit.javalisp.parser;

import ru.nsu.fit.javalisp.Token;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;


public class LispParser extends Parser {

	private Stack<Token> stack;

	private int nextChar;
	private LinkedList<Token> tokens;

	public LispParser(String filename) throws Exception{
		super();
		File file = new File(filename);
		if (!file.canRead()) throw new Exception("Can't open script");
		reader = new FileReader(file);
		stack= new Stack<>();
		machine.createState("Empty");
		machine.createState("Word");
		machine.createState("StringBody");
		machine.createState("StringEnd");
		machine.createState("Integer");
		machine.createState("Float");

		for (int i = 0; i <= 32; i++){
			machine.addTransition((char)i, "StringEnd", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
			machine.addTransition((char)i, "Word", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
			machine.addTransition((char)i, "Float", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
			machine.addTransition((char)i, "Integer", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
			machine.addTransition((char)i, "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)i, "Empty", "Empty");
		}
		for (int i = 0; i < 26; i++){
			machine.addTransition((char)(i + 'a'), "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + 'A'), "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + 'a'), "Empty", "Word", this, LispParser.class.getDeclaredMethod("newWordToken"));
			machine.addTransition((char)(i + 'A'), "Empty", "Word", this, LispParser.class.getDeclaredMethod("newWordToken"));
			machine.addTransition((char)(i + 'a'), "Word", "Word", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + 'A'), "Word", "Word", this, LispParser.class.getDeclaredMethod("addChar"));
		}
		for (int i = 0; i < 10; i++){
			machine.addTransition((char)(i + '0'), "Empty", "Integer", this, LispParser.class.getDeclaredMethod("newIntToken"));
			machine.addTransition((char)(i + '0'), "Integer", "Integer", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + '0'), "Float", "Float", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + '0'), "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
			machine.addTransition((char)(i + '0'), "Word", "Word", this, LispParser.class.getDeclaredMethod("addChar"));
		}
		machine.addTransitions("[(", "Empty", "Empty", this, LispParser.class.getDeclaredMethod("newLevel"));
		machine.addTransitions("[(", "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));

		machine.addTransitions(")]", "Empty", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
		machine.addTransitions(")]", "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
		machine.addTransitions(")]", "Word", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
		machine.addTransitions(")]", "Integer", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
		machine.addTransitions(")]", "StringEnd", "Empty", this, LispParser.class.getDeclaredMethod("complete"));
		machine.addTransitions(")]", "Float", "Empty", this, LispParser.class.getDeclaredMethod("complete"));

		machine.addTransitions(".,", "Integer", "Float", this, LispParser.class.getDeclaredMethod("toFloat"));
		machine.addTransitions(".,", "Empty", "Float", this, LispParser.class.getDeclaredMethod("newFloatToken"));
		machine.addTransitions(".,", "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));

		machine.addTransitions("\"", "Empty", "StringBody", this, LispParser.class.getDeclaredMethod("newString"));
		machine.addTransitions("\"", "StringBody", "StringEnd");
		String aux = "!#$%^&*-+=_<>=?@|~`";
		machine.addTransitions(aux, "Empty", "Word", this, LispParser.class.getDeclaredMethod("newWordToken"));
		machine.addTransitions(aux, "StringBody", "StringBody", this, LispParser.class.getDeclaredMethod("addChar"));
		machine.setErrorTransitionFunction(this, LispParser.class.getDeclaredMethod("error"));
		machine.setStartState("Empty");
	}

	private void newIntToken(){
		Token token = new Token();
		token.setType(Token.Type.INT);
		stack.push(token);
		token.addChar((char)nextChar);
	}

	private void newFloatToken(){
		Token token = new Token();
		token.setType(Token.Type.FLOAT);
		stack.push(token);
		token.addChar((char)nextChar);
	}

	private void addChar(){
		stack.peek().addChar((char)nextChar);
	}

	private void error() throws Exception{
		throw  new Exception("Found invalid symbol at line: " + line + ", position: " + position);
	}

	private void newWordToken(){
		Token token = new Token();
		token.setType(Token.Type.VARIABLE);
		token.addChar((char)nextChar);
		stack.push(token);
	}

	private void complete() throws Exception{
		if (nextChar == ')' || nextChar == ']'){
			level --;
			Token token = stack.pop();
			//close brackets
			if (token.getType() == Token.Type.COMPLEX){
				if (stack.size() > 0) {
					stack.peek().addToken(token);
				}
				else{
					tokens.add(token);
				}
			}
			else{
				token.process();
				if (stack.size() > 0)
				{
					Token token1 = stack.pop();
					token1.addToken(token);
					if (stack.size() == 0){
						tokens.add(token1);
					}
					else{
						stack.peek().addToken(token1);
					}
				}
				else{
					throw new Exception("Error, there is no brackets");
				}
			}
		}
		else{
			Token token = stack.pop();
			token.process();
			if (stack.size() > 0){
				stack.peek().addToken(token);
			}
			else{
				throw new Exception("Error, there is no brackets");
			}
		}
	}

	private void toFloat(){
		stack.peek().setType(Token.Type.FLOAT);
		stack.peek().addChar((char)nextChar);
	}

	private void newLevel(){
		level++;
		Token token = new Token();
		token.setType(Token.Type.COMPLEX);
		stack.push(token);
	}

	private void newString(){
		Token token = new Token();
		token.setType(Token.Type.STRING);
		stack.push(token);
	}

	public List<Token> parse() throws Exception{
		tokens = new LinkedList<>();

		while ((nextChar = nextChar()) != -1){
			machine.feedChar((char)nextChar);
		}
		if (level != 0) throw new Exception("Incorrect source");
		return tokens;
	}

}
