package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.HashMap;
import java.util.List;

/**
 * Handler that evaluates if-then and if-then-else constructions
 */
public class IfHandler extends BasicHandler{

	/**
	 * Create handler
	 * @param nameToDesc - defined functions
	 * @param nameToDummy - declared functions
	 */

	public IfHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
		keyWord = "if";
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX){
			return TranslationResult.FAIL;
		}
		Node node1 = node.getSubNodes().get(0);
		if (node1.getType() != Node.Type.VARIABLE) return TranslationResult.FAIL;
		if (!node1.getResult().equals(keyWord)) return TranslationResult.FAIL;

		if (node.getSubNodes().size() < 3 || node.getSubNodes().size() > 4) {
			return TranslationResult.FAIL;
		}

		int cnt = 0;
		String condVar = "_LOCAL_VAR_" + created;
		cnt++;

		StringBuilder builder = new StringBuilder();

		var t = evalNode(currentContext, node.getSubNodes().get(1), created + cnt, condVar);
		if (!t.isSuccess() || t.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;

		builder.append(t.getSrc());

		builder.append("if (((Boolean)").append(condVar).append(").booleanValue()){\n");

		cnt += t.getUsedVars();

		var t1 = startingHandler.evalNode(currentContext, node.getSubNodes().get(2),created + cnt, resVar);
		if (!t1.isSuccess() || t1.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;
		cnt += t1.getUsedVars();
		builder.append(t1.getSrc());
		builder.append("}\nelse {\n");
		if (node.getSubNodes().size() == 4) {
			var t2 = startingHandler.evalNode(currentContext, node.getSubNodes().get(3), created + cnt, resVar);
			if (!t2.isSuccess() || t2.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;

			builder.append(t2.getSrc());
			cnt += t2.getUsedVars();
		}
		else {
			builder.append(resVar).append(" = null;\n");
		}
		builder.append("}\n");
		return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
				.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
	}

}
