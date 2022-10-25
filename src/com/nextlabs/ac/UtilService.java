package com.nextlabs.ac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.pf.domain.destiny.serviceprovider.IFunctionServiceProvider;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderException;

public class UtilService implements IFunctionServiceProvider {

	private static IEvalValue nullResult = EvalValue.NULL;
	private static IEvalValue emptyString = EvalValue.build("");

	private static final Log LOG = LogFactory.getLog(UtilService.class);
	private String custom_logging_path;
	private String env_reqid = null;
	private static final int STARTS_WITH = 1;
	private static final int CONTAINS = 2;
	private static final int ENDS_WITH = 3;
	private static final int NONE = 0;

	/*
	 * 
	 * global default values which has to be assigned during the initiation of
	 * 
	 * the service
	 */

	@Override
	public void init() throws Exception {

		LOG.info("UtilService init() started.");

		LOG.info("UtilService init() completed.");

	}

	/*
	 * 
	 * This is a main call function called by the policy controller through
	 * 
	 * advanced condition.It checks for the valid methods and pass the control
	 * 
	 * flow to the correct methods.
	 */

	@Override
	public IEvalValue callFunction(String functionName, IEvalValue[] args)

			throws ServiceProviderException {

		LOG.info("UtilService callfunction() started, with function: "

				+ functionName);

		IEvalValue result = nullResult;

		ArrayList<ArrayList<String>> inputList;

		try {

			long lCurrentTime = System.nanoTime();

			inputList = processValues(args);

			if (inputList == null) {

				LOG.warn("Error UnexectedArguments:Expected arguments is a list of multivalues");

				return nullResult;

			}

			if ("toList".equalsIgnoreCase(functionName)) {

				if (inputList.size() < 2) {

					LOG.warn("Error:Incorrect no of arguments");

					return nullResult;

				} else {

					result = toList(inputList);
				}

			} else if ("subList".equalsIgnoreCase(functionName)) {
				if (inputList.size() < 3) {

					LOG.warn("Error:Incorrect no of arguments");

					return nullResult;

				} else {
					result = subList(inputList);
				}
			} else {
				LOG.warn("Function name is not recognized");
				return nullResult;
			}
			LOG.info("UtilService callfunction() completed.  Time spent: "

					+ ((System.nanoTime() - lCurrentTime) / 1000000.00) + "ms");

			try {
				IMultivalue value = (IMultivalue) result.getValue();
				Iterator<IEvalValue> ievIter = value.iterator();

				if (!ievIter.hasNext()) {
					LOG.warn("Return list is empty");
				}

				while (ievIter.hasNext()) {

					IEvalValue iev = ievIter.next();

					if (iev != null) {

						if (!iev.getValue().toString().isEmpty()) {

							LOG.debug("Result:" + iev.getValue().toString());

						}

					}

				}
			} catch (Exception e) {
				LOG.warn("Cannot print out result list ", e);
			}

		} catch (Exception e) {

			LOG.error("UtilService callfunction() error: ", e);

		}

		return result;

	}

	/**
	 * This method receives a list of string and return its sublist that fulfill
	 * a condition
	 * 
	 * @param inputList
	 * @return
	 */
	private IEvalValue subList(ArrayList<ArrayList<String>> inputList) {
		LOG.info("UtilService toList entered ");

		IEvalValue evalValue = IEvalValue.EMPTY;

		List<String> list = null;
		String condition = null;
		int conditionInt = 0;
		String pattern = null;

		if (inputList.get(0) != null) {

			list = inputList.get(0);

		}

		if (inputList.get(1) != null && inputList.get(1).get(0) != null) {

			condition = inputList.get(1).get(0);

			if (condition.equalsIgnoreCase("STARTSWITH")) {
				conditionInt = 1;
			} else if (condition.equalsIgnoreCase("CONTAINS")) {
				conditionInt = 2;
			} else if (condition.equalsIgnoreCase("ENDSWITH")) {
				conditionInt = 3;
			} else {
				conditionInt = 0;
			}

		}

		if (inputList.get(2) != null && inputList.get(2).get(0) != null) {

			pattern = inputList.get(2).get(0);

		}

		LOG.debug("Condition is " + condition);
		LOG.debug("Pattern is " + pattern);

		if (list != null) {

			List<String> results = new ArrayList<String>();

			if (pattern != null) {

				for (String value : list) {
					switch (conditionInt) {
					case STARTS_WITH:
						if (pattern != null) {
							if (value.startsWith(pattern)) {
								results.add(value);
								LOG.debug(value + " starts with " + pattern);
							}
						}
						break;
					case ENDS_WITH:
						if (pattern != null) {
							if (value.endsWith(pattern)) {
								results.add(value);
								LOG.debug(value + " ends with " + pattern);
							}
						}
						break;
					case CONTAINS:
						if (pattern != null) {
							if (value.contains(pattern)) {
								results.add(value);
								LOG.debug(value + " contains " + pattern);
							}
						}
						break;
					default:
						break;
					}
				}

				IMultivalue imv = Multivalue.create(results, ValueType.STRING);

				evalValue = EvalValue.build(imv);
			}

		} else {
			LOG.debug("UtilService subList input list is null");
		}

		return evalValue;
	}

	/**
	 * 
	 * This method takes a string with separator and returns a list of strings
	 * 
	 * @param inputList
	 * @return
	 */
	private IEvalValue toList(ArrayList<ArrayList<String>> inputList) {

		String toList = null, token = null;

		LOG.info("UtilService toList entered ");

		IEvalValue evalValue = IEvalValue.EMPTY;

		if (inputList.get(0) != null && inputList.get(0).get(0) != null) {

			toList = inputList.get(0).get(0);

		}

		if (inputList.get(1) != null && inputList.get(1).get(0) != null) {

			token = inputList.get(1).get(0);

		}

		LOG.debug("String:" + toList + "  separtor:" + token);

		if (toList != null && token != null) {

			ArrayList<String> evList = new ArrayList<String>();

			StringTokenizer tokens = new StringTokenizer(toList, token);

			while (tokens.hasMoreTokens()) {
				String value = tokens.nextToken();
				evList.add(value);
			}

			IMultivalue imv = Multivalue.create(evList, ValueType.STRING);

			evalValue = EvalValue.build(imv);

		}

		return evalValue;

	}

	/**
	 * Process the input arguments and return an array of arrays of strings
	 * 
	 * @param args
	 * @return
	 * @throws Exception
	 */
	private ArrayList<ArrayList<String>> processValues(IEvalValue[] args)

			throws Exception {

		LOG.info("UtilService processValues entered ");

		ArrayList<ArrayList<String>> sOutData = new ArrayList<ArrayList<String>>();

		for (IEvalValue ieValue : args) {
			LOG.info("ieValue " + ieValue.toString());
			if (null != ieValue) {

				if (ieValue.getType() == ValueType.MULTIVAL) {

					ArrayList<String> list = new ArrayList<String>();

					IMultivalue value = (IMultivalue) ieValue.getValue();

					Iterator<IEvalValue> ievIter = value.iterator();

					while (ievIter.hasNext()) {

						IEvalValue iev = ievIter.next();

						if (iev != null) {

							if (!iev.getValue().toString().isEmpty()) {

								list.add(iev.getValue().toString());

								LOG.debug("Processed value:" + iev.getValue().toString());

							}

						}

					}

					sOutData.add(list);

				} else if (ieValue.getType() == ValueType.STRING

						&& !ieValue.getValue().toString().isEmpty()) {

					ArrayList<String> list = new ArrayList<String>();

					list.add(ieValue.getValue().toString());

					sOutData.add(list);

				}
			}

		}
		LOG.info("Input Data: " + sOutData);
		return sOutData;

	}

	// For testing purpose

	public static void main(String args[]) throws Exception {

		UtilService plugin = new UtilService();

		plugin.init();

		IEvalValue[] sDataArr = new IEvalValue[3];
		ArrayList<String> evs = new ArrayList<String>();
		evs.add("sap://d10-ci/abc");
		evs.add("sap://d10-ci/d10/900/ECC/MM03/ITAR_DIR_1/abc/pqr");
		evs.add("sap://d10-ci/d10/900/ECC/");
		evs.add("sap://d10-ci/d10/900/ECC/MM03/ITAR_DIR_1");
		evs.add("sap://d10-ci/");
		evs.add("sap://d10-ci/d10/900/ECC/MM03/ITAR_DIR_1/");
		/*
		 * sDataArr[0] = EvalValue.build("K123#K234#j3464#lk345"); sDataArr[1] =
		 * EvalValue.build("#");
		 */

		/*
		 * System.out.println(plugin.callFunction("toList", sDataArr));
		 */
		IMultivalue imv1 = Multivalue.create(evs, ValueType.STRING);

		IEvalValue[] sDataArrTo = new IEvalValue[2];
		sDataArrTo[0] = EvalValue.build("K123#K234#j3464#lk345#lk123#2342343");
		sDataArrTo[1] = EvalValue.build("#");

		/* sDataArr[0] = EvalValue.build(imv1); */
		sDataArr[0] = EvalValue.build((IMultivalue) plugin.callFunction("toList", sDataArrTo).getValue());
		sDataArr[1] = EvalValue.build("CONTAINS");
		sDataArr[2] = EvalValue.build("K1");

		IMultivalue value = (IMultivalue) plugin.callFunction("subList", sDataArr).getValue();

		Iterator<IEvalValue> ievIter = value.iterator();

		while (ievIter.hasNext()) {

			IEvalValue iev = ievIter.next();

			if (iev != null) {

				if (!iev.getValue().toString().isEmpty()) {

					System.out.println("Result:" + iev.getValue().toString());

				}

			}

		}

		/* ArrayList<IEvalValue> evs = new ArrayList<IEvalValue>(); */

		/*
		 * 
		 * evs.add(EvalValue.build("1")); evs.add(EvalValue.build("2"));
		 * 
		 * evs.add(EvalValue.build("3")); evs.add(EvalValue.build("4"));
		 * 
		 * evs.add(EvalValue.build("5"));
		 * 
		 * 
		 * 
		 * IMultivalue imv = Multivalue.create(evs, ValueType.STRING);
		 * 
		 * sDataArr[0] = EvalValue.build(imv); evs = new
		 * 
		 * ArrayList<IEvalValue>();
		 * 
		 * 
		 * 
		 * evs.add(EvalValue.build("1")); evs.add(EvalValue.build("2"));
		 * 
		 * evs.add(EvalValue.build("3")); evs.add(EvalValue.build("5"));
		 * 
		 * evs.add(EvalValue.build("4"));
		 */

		// IMultivalue imv1 = Multivalue.create(evs, ValueType.STRING);
		/*
		 * sDataArr[0] = EvalValue.build("123;345;678;34;567;879");
		 * 
		 * sDataArr[1] = EvalValue.build(";");
		 * 
		 * sDataArr[0] = EvalValue.build("S-1-1-11-3436"); UserService us = new
		 * UserService(); us.init(); sDataArr[0] =
		 * us.callFunction("getLicenses", sDataArr);
		 * System.out.println(plugin.callFunction("isEmptySet", sDataArr));
		 */
		/*
		 * ArrayList list = new ArrayList(); list.add("1"); list.add("5");
		 * list.add("7"); System.out.println(plugin.join(list, "','"));
		 */

	}

}
