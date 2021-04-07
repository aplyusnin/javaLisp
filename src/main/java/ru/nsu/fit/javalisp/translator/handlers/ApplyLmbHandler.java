package ru.nsu.fit.javalisp.translator.handlers;

import ru.nsu.fit.javalisp.Node;
import ru.nsu.fit.javalisp.translator.Context;
import ru.nsu.fit.javalisp.translator.FunctionDescriptor;
import ru.nsu.fit.javalisp.translator.TranslationEntry;
import ru.nsu.fit.javalisp.translator.TranslationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplyLmbHandler extends BasicHandler {

	public ApplyLmbHandler(HashMap<String, FunctionDescriptor> nameToDesc, HashMap<String, FunctionDescriptor> nameToDummy) {
		super(nameToDesc, nameToDummy);
	}

	@Override
	protected TranslationResult generateSource(Context currentContext, Node node, int created, String resVar) throws Exception
	{
		if (node.getType() != Node.Type.COMPLEX){
			return TranslationResult.FAIL;
		}
		Node s = node.getSubNodes().get(0);
		if (s.getType() != Node.Type.COMPLEX) {
			return TranslationResult.FAIL;
		}

		var t1 = startingHandler.evalNode(currentContext, s, 0, resVar);

		if (!t1.isSuccess() || t1.getValue() != TranslationResult.Value.FUNCTIONAL) return TranslationResult.FAIL;

		if (node.getSubNodes().size() - 1 + t1.getParams().size() != t1.getParamsNumber()) return TranslationResult.FAIL;

		StringBuilder builder = new StringBuilder();

		List<String> vars = new ArrayList<>();
		List<String> src = new ArrayList<>();
		int cnt = 0;
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

		for (var x : t1.getParams()){
			vars.add(x);
		}
		for (int i = 0; i < src.size(); i++) {
			builder.append(src.get(i));
		}

		builder.append(resVar).append(" = ").append(t1.getFuncName()).append("(");
		for (int i = 0; i < vars.size(); i++) {
			builder.append(vars.get(i));
			if (i + 1 < vars.size()) builder.append(", ");
		}
		builder.append(");\n");

		return new TranslationResult.Builder().setSuccess(true).setSrc(builder.toString())
				.setValue(TranslationResult.Value.SOURCE).setUsedVars(cnt).build();
		/*FunctionDescriptor func;

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
			if (node.getSubNodes().get(i).getType() == Node.Type.VARIABLE){
				String name;
				if (nameToDesc.containsKey(node.getSubNodes().get(i).getResult())){
					name = nameToDesc.get(node.getSubNodes().get(i).getResult()).getName();
					vars.add("(\"" + name + "\")");
					continue;
				}
				else if (nameToDummy.containsKey(node.getSubNodes().get(i).getResult())){
					name = nameToDummy.get(node.getSubNodes().get(i).getResult()).getName();
					vars.add("(\"" + name + "\")");
					continue;
				}
			}

			String name = "_LOCAL_VAR_" + (created + cnt);
			vars.add(name);
			cnt++;
			var t = startingHandler.evalNode(node.getSubNodes().get(i), created + cnt, name);
			cnt += t.second;
			src.add(t.first);
		}

		if (func.getArgsCount() == -1) return TranslationResult.FAIL;

		if (vars.size() != func.getArgsCount()) throw new Exception("Waited for: " + func.getArgsCount() + "arguments, found: " + vars.size() + " arguments");

		for (int i = 0; i < src.size(); i++) {
			builder.append(src.get(i));
		}

		builder.append("").append(resVar).append(" = ").append(func.getName()).append("(");
		for (int i = 0; i < vars.size(); i++) {
			builder.append(vars.get(i));
			if (i + 1 < vars.size()) builder.append(", ");
		}
		builder.append(");\n");

		return new Pair<>(Boolean.TRUE, new Pair<>(builder.toString(), cnt));*/
	}

}
