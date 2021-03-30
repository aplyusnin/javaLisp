package ru.nsu.fit.javalisp.translator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Class with standard functions
 */
public class BaseSource {

	private Scanner sc;

	public BaseSource(){
		sc = new Scanner(System.in);
	}

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

	private Object isLess(Double a, Double b){
		return a < b;
	}
	private Object isLess(Integer a, Double b){
		return a.doubleValue() < b;
	}
	private Object isLess(Double a, Integer b){
		return a < b.doubleValue();
	}
	private Object isLess(Integer a, Integer b){
		return a < b;
	}
	protected Object isLess(Object a, Object b) {
		try{
			return isLess((Integer)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isLess((Double)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isLess((Integer)a, (Double)b);
		}
		catch (Exception ignored){}
		return isLess((Double)a, (Double)b);
	}

	private Object isLeq(Double a, Double b){
		return a <= b;
	}
	private Object isLeq(Integer a, Double b){
		return a.doubleValue() <= b;
	}
	private Object isLeq(Double a, Integer b){
		return a <= b.doubleValue();
	}
	private Object isLeq(Integer a, Integer b){
		return a <= b;
	}
	protected Object isLeq(Object a, Object b) {
		try{
			return isLeq((Integer)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isLeq((Double)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isLeq((Integer)a, (Double)b);
		}
		catch (Exception ignored){}
		return isLeq((Double)a, (Double)b);
	}

	private Object isGt(Double a, Double b){
		return a > b;
	}
	private Object isGt(Integer a, Double b){
		return a.doubleValue() > b;
	}
	private Object isGt(Double a, Integer b){
		return a > b.doubleValue();
	}
	private Object isGt(Integer a, Integer b){
		return a > b;
	}
	protected Object isGt(Object a, Object b) {
		try{
			return isGt((Integer)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isGt((Double)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isGt((Integer)a, (Double)b);
		}
		catch (Exception ignored){}
		return isGt((Double)a, (Double)b);
	}
	private Object isGte(Double a, Double b){
		return a >= b;
	}
	private Object isGte(Integer a, Double b){
		return a.doubleValue() >= b;
	}
	private Object isGte(Double a, Integer b){
		return a >= b.doubleValue();
	}
	private Object isGte(Integer a, Integer b){
		return a >= b;
	}
	protected Object isGte(Object a, Object b) {
		try{
			return isGte((Integer)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isGte((Double)a, (Integer)b);
		}
		catch (Exception ignored){}
		try{
			return isGte((Integer)a, (Double)b);
		}
		catch (Exception ignored){}
		return isGte((Double)a, (Double)b);
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


	protected Object read(){
		return sc.next();
	}

	protected Object print(Object a){
		System.out.print(a);
		return null;
	}

	protected Object println(Object a){
		System.out.println(a);
		return null;
	}

	protected Object list(Object ... objects){
		return new LinkedList<>(Arrays.asList(objects));
	}


	public void evaluate(){

	}
}
