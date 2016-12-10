package server.library.statemachine;

import java.util.ArrayList;
import java.util.List;

public class StateMachine implements IService {

	List<Operation> operations;

	public StateMachine() {
		
		operations = new ArrayList<>();
	}

	@Override
	public String execute(String op) {
		
		return produceResult(op);
	}

	private String produceResult(String entry) {
		
		entry = entry.trim().replaceAll(" +", " ");

		if (entry.equalsIgnoreCase("list")) {
			return list().toString();
		}

		String parts[] = entry.split(" ");
		String command = parts[0].toLowerCase();

		if (command.equals("put")) {
			return put(parts[1]);
		}

		if (command.equals("get")) {
			return get(parts[1]);
		}

		if (command.equals("del")) {
			return del(parts[1]);
		}

		if (command.equals("cas")) {
			return cas(parts[1], parts[2], parts[3]);
		}

		// else
		return command;
	}

	private StringBuilder list() {
		
		StringBuilder contents = new StringBuilder();

		if (!operations.isEmpty()) {
			contents.append("--- List ---\n");

			for (int i = 0; i < operations.size(); i++) {
				contents.append(operations.get(i) + (i + 1 < operations.size() ? "\n" : ""));
			}
		} else {
			contents.append("No contents to list.");
		}
		
		return contents;
	}

	private String put(String op) {
		
		Operation operation = new Operation(op);
		operations.add(operation);
		
		return "'" + op + "' successfully inserted!";
	}

	private String get(String arg) {
		
		int index = -1;
		try {
			index = Integer.parseInt(arg);
		} catch (NumberFormatException e) { }

		if (index > -1) {
			if (index >= operations.size()) {
				return "There is no item at " + index;
			}
			return operations.get(index).getOp();

		} else {
			String result = "Item doesn't exist";
			for (Operation op: operations) {
				if (op.getOp().equals(arg)) {
					result = op.getOp();
					break;
				}
			}
			return result;
		}
	}

	private String del(String arg) {
		
		int index = -1;
		try {
			index = Integer.parseInt(arg);
		} catch (NumberFormatException e) { }

		String result = "";

		if (index > -1) {
			if (index >= operations.size()) {
				return "There is no item at " + index;
			}
			result = operations.get(index).getOp();

			operations.remove(index);

		} else {
			for (int i = 0; i < operations.size(); i++) {
				if (operations.get(i).getOp().equals(arg)) {
					result = operations.get(i).getOp();
					operations.remove(i);
					break;
				}
			}
		}

		for (int i = 0; i < operations.size(); i++) {
			operations.get(i).setIndex(i);
		}
		
		return "'" + result + "' successfully deleted!";
	}

	private String cas(String pos, String arg1, String arg2) {
		
		int index = -1;
		try {
			index = Integer.parseInt(pos);
		} catch (NumberFormatException e) { }

		String result = "";

		if (index > -1) {
			if (index >= operations.size()) {
				return "There is no item at " + index;
			}
			result = operations.get(index).getOp();

			if (result.equals(arg1)) {
				operations.get(index).setOp(arg2);
				return "'" + arg1 + "' was replaced by " + arg2;
			} else {
				return "'" + index + ": " + result + "' is different than " + arg1;
			}
		}
		
		return "'" + arg1 + "' was't found at " + pos;
	}

	private class Operation {
		
		private int index;
		private String op;

		private Operation(String s) {
			
			index = operations.size();
			op = s;
		}

		private String getOp() {
			
			return op;
		}
		
		private void setOp(String op) {
			
			this.op = op;
		}

		private void setIndex(int i) {
			
			this.index= i;
		}

		@Override
		public String toString() {
			
			return index + ": " + op;
		}
	}
}
