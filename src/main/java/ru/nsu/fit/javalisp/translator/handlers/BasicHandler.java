package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;
import java.util.List;

/**
 * Class basis for handler for chain of handlers;
 */
public abstract class BasicHandler {

	protected HashMap<String, FunctionDescriptor> nameToDesc;
	protected HashMap<String, FunctionDescriptor> nameToDummy;
	protected BasicHandler nextHandler;
	protected BasicHandler startingHandler;
	protected String keyWord;

	/**
	 * Constructor
	 * @param nameToDesc - Table of defined methods. Being looked up after contexts
	 * @param nameToDummy - Table of declared methods, Being looked up after declared methods
	 */
	public BasicHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		this.nameToDesc = nameToDesc;
		this.nameToDummy = nameToDummy;
	}

	/**
	 * Generate source evaluating node
	 * @param node node to generate source for
	 * @param created index of first local variable to declare
	 * @param resVar - variable name to store computation result
	 * @return Pair of generated source and number of used variables
	 * @throws Exception - unable to create source
	 */
	public TranslationResult evalNode(Context currentContext, Node node, int created, String resVar) throws Exception {
		var t = generateSource(currentContext, node, created, resVar);
		if (t.isSuccess()) return t;
		try {
			return nextHandler.evalNode(currentContext, node, created, resVar);
		}
		catch (Exception e){
			String res = node.getInfo();
			throw new Exception("Cannot compile: " + res);
		}
	}

	/**
	 * Try to generate source for given type of node
	 * @param node node to generate source for
	 * @param created index of first local variable to declare
	 * @param resVar - variable name to store computation result
	 * @return Pair of boolean and pair of generated source and number of used variables.
	 * If generate source will return (true, (source, cnt)), otherwise (false, null)
	 * @throws Exception - unable to build source
	 */
	protected abstract TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception;

	/**
	 * Set next handler in chain
	 * @param nextHandler - next handler
	 */
	public void setNextHandler(BasicHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	/**
	 * Set starting handler in chain to evaluate sub-nodes
	 * @param startingHandler - starting handler
	 */
	public void setStartingHandler(BasicHandler startingHandler) {
		this.startingHandler = startingHandler;
	}


}
