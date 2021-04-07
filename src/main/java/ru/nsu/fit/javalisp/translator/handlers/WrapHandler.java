package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;
import java.util.List;

/**
 * Handler that unwraps nodes. ((x)) -> (x)
 */
public class WrapHandler extends BasicHandler{

	/**
	 * Create handler
	 * @param nameToDesc - defined functions
	 * @param nameToDummy - declared functions
	 */
	public WrapHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
	}
	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX) return TranslationResult.FAIL;
		if (node.getSubNodes().size() != 1) return TranslationResult.FAIL;
		var t = startingHandler.evalNode(currentContext, node.getSubNodes().get(0), created, resVar);
		if (!t.isSuccess()) return TranslationResult.FAIL;
		if (t.getValue() == TranslationResult.Value.SOURCE) return t;
		if (t.getParamsNumber() == 0){
			StringBuilder builder = new StringBuilder();
			builder.append(resVar).append(" = ").append(t.getFuncName()).append("();\n");
			return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
					.setValue(TranslationResult.Value.SOURCE).setUsedVars(0).build();
		}
		else {
			return t;
		}
	}

}
