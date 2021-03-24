package ru.nsu.fit.javalisp.translator;

/**
 * Class with standard functions
 */
public class BaseSource {

	private Double add(Double a, Double b){
		return a + b;
	}

	private Double add(Integer a, Double b){
		return a + b;
	}

	private Double add(Double a, Integer b){
		return a + b;
	}

	private Integer add(Integer a, Integer b){
		return a + b;
	}

	protected Object add(Object a, Object b) throws ClassCastException {
		try	{
			return add((Integer)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return add((Double)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return add((Integer)a, (Double)b);
		}
		catch (Exception e){}
		try	{
			return add((Double)a, (Double)b);
		}
		catch (Exception e){}
		throw new ClassCastException();
	}

	private Double sub(Double a, Double b){
		return a - b;
	}

	private Double sub(Integer a, Double b){
		return a - b;
	}

	private Double sub(Double a, Integer b){
		return a - b;
	}

	private Integer sub(Integer a, Integer b){
		return a - b;
	}

	protected Object sub(Object a, Object b) throws ClassCastException {
		try	{
			return sub((Integer)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return sub((Double)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return sub((Integer)a, (Double)b);
		}
		catch (Exception e){}
		try	{
			return sub((Double)a, (Double)b);
		}
		catch (Exception e){}
		throw new ClassCastException();
	}

	protected Double castD(Object o) throws Exception {
		if (String.class.isInstance(o)){
			return Double.valueOf((String)o);
		}
		else if (Number.class.isInstance(o)){
			return ((Number)o).doubleValue();
		}
		throw new Exception("Cant cast to float");
	}

	protected Integer castI(Object o) throws Exception {
		if (String.class.isInstance(o)){
			return Integer.valueOf((String)o);
		}
		else if (Number.class.isInstance(o)){
			return ((Number)o).intValue();
		}
		throw new Exception("Cant cast to float");
	}

	private Double mul(Double a, Double b){
		return a * b;
	}

	private Double mul(Integer a, Double b){
		return a * b;
	}

	private Double mul(Double a, Integer b){
		return a * b;
	}

	private Integer mul(Integer a, Integer b){
		return a * b;
	}

	protected Object mul(Object a, Object b) throws ClassCastException {
		try	{
			return mul((Integer)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return mul((Double)a, (Integer)b);
		}
		catch (Exception e){}
		try	{
			return mul((Integer)a, (Double)b);
		}
		catch (Exception e){}
		try	{
			return mul((Double)a, (Double)b);
		}
		catch (Exception e){}
		throw new ClassCastException();
	}

	protected Object div(Object a, Object b) throws ClassCastException {
		try	{
			return (Double)a / (Double)b;
		}
		catch (Exception e){}
		throw new ClassCastException();
	}

	protected Object isEqual(Object a, Object b) {
		return a.equals(b);
	}

	protected Object not(Object a) throws ClassCastException{
		return !((Boolean)a);
	}

	protected Object and(Object a, Object b) throws ClassCastException{
		return (Boolean)a && (Boolean)b;
	}
	protected Object or(Object a, Object b) throws ClassCastException{
		return (Boolean)a || (Boolean)b;
	}

	public void evaluate(){

	}
}
