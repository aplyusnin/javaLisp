package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.Pair;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handler that applies defined or declared function to arguments
 */
public class ApplyFHandler extends BasicHandler {

	/**
	 * Create handler
	 * @param nameToDesc - defined functions
	 * @param nameToDummy - declared functions
	 */
	public ApplyFHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy){
		super(nameToDesc, nameToDummy);
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX){
			return TranslationResult.FAIL;
		}
		Node s = node.getSubNodes().get(0);
		if (s.getType() != Node.Type.VARIABLE) {
			return TranslationResult.FAIL;
		}

		FunctionDescriptor func;

		if (nameToDesc.containsKey(s.getResult()))
			func = nameToDesc.get(s.getResult());
		else if (nameToDummy.containsKey(s.getResult()))
			func = nameToDummy.get(s.getResult());
		else
			return TranslationResult.FAIL;

		StringBuilder builder = new StringBuilder();

		int cnt = 0;

		List<String> vars = new ArrayList<>();
		List<String> src = new ArrayList<>();
		for (int i = 1; i < node.getSubNodes().size(); i++) {
			try {
				String name = "_LOCAL_VAR_" + (created + cnt);
				vars.add(name);
				cnt++;
				var t = startingHandler.evalNode(currentContext, node.getSubNodes().get(i), created + cnt, name);
				if (!t.isSuccess() || t.getValue() != TranslationResult.Value.SOURCE) return TranslationResult.FAIL;
				cnt += t.getUsedVars();
				src.add(t.getSrc());
			}
			catch (Exception e){
				return TranslationResult.FAIL;
			}
		}

		if (func.getArgsCount() == -1) return TranslationResult.FAIL;

		if (vars.size() != func.getArgsCount()) {
			return TranslationResult.FAIL;
		}

		for (int i = 0; i < src.size(); i++) {
			builder.append(src.get(i));
		}

		builder.append(resVar).append(" = ").append(func.getName()).append("(");
		for (int i = 0; i < vars.size(); i++) {
			builder.append(vars.get(i));
			if (i + 1 < vars.size()) builder.append(", ");
		}
		builder.append(");\n");

		return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
				.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
	}
}
