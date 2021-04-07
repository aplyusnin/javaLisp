package ru.nsu.fit.javalisp.translator;

import java.util.ArrayList;
import java.util.List;

public class TranslationResult {
	public enum Value {
		SOURCE,
		FUNCTIONAL
	}
	private Value value;
	private boolean success;
	private String var;
	private String src;
	private String funcName;
	private int paramsNumber;
	private List<String> params;
	private int usedVars;

	public static TranslationResult FAIL = new TranslationResult();

	private TranslationResult(){
		this.success = false;
	}


	public static class Builder{
		private Value value;
		private boolean success = false;
		private String var = "";
		private String src = "";
		private String funcName = "";
		private int paramsNumber = 0;
		private List<String> params;
		private int usedVars = 0;


		public Builder(){
			params = new ArrayList<>();
		}
		public Builder setVar(String var){
			this.var = var;
			return this;
		}
		public Builder setSuccess(boolean success){
			this.success = success;
			return this;
		}

		public Builder setSrc(String src){
			this.src = src;
			return this;
		}

		public Builder setName(String name){
			this.funcName = name;
			return this;
		}

		public Builder setNumber(int number){
			this.paramsNumber = number;
			return this;
		}

		public Builder addParam(String param){
			this.params.add(param);
			return this;
		}

		public Builder setValue(Value value){
			this.value = value;
			return this;
		}

		public Builder setUsedVars(int usedVars) {
			this.usedVars = usedVars;
			return this;
		}

		public TranslationResult build(){
			TranslationResult result = new TranslationResult();
			result.value = value;
			result.success = success;
			result.var = var;
			result.src = src;
			result.funcName = funcName;
			result.paramsNumber = paramsNumber;
			result.params = params;
			result.usedVars = usedVars;
			return result;
		}
	}

	public int getParamsNumber() {
		return paramsNumber;
	}

	public List<String> getParams() {
		return params;
	}

	public String getFuncName() {
		return funcName;
	}

	public String getSrc() {
		return src;
	}

	public String getVar() {
		return var;
	}

	public Value getValue() {
		return value;
	}

	public int getUsedVars() {
		return usedVars;
	}

	public boolean isSuccess() {
		return success;
	}


}
