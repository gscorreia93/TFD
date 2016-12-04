package server.library.statemachine;

import java.util.ArrayList;
import java.util.List;

public class StateMachine implements IService {

	List<Operation> operations = new ArrayList<>();

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
			return get(Integer.parseInt(parts[1]));
		}
		
		if (command.equals("del")) {
			return del(Integer.parseInt(parts[1]));
		}

		if (command.equals("cas")) {
			System.err.println("not implemented");
		}
		return new String();
	}

	private StringBuilder list() {
		StringBuilder contents = new StringBuilder();
		
		if (!operations.isEmpty()) {
			contents.append("--- List ---");
			
			for (Operation op: operations) {
				contents.append(op);
			}
		} else {
			contents.append("No contents to list.");
		}
		return contents;
	}
	
	private String put(String op) {
		Operation operation = new Operation(op);
		operations.add(operation);
		return op + " successfully inserted!";
	}
	
	private String get(int index) {
		if (index >= operations.size()) {
			return "Item doesn't exist";
		}
		return operations.get(index).getOp();
	}
	
	private String del(int index) {
		if (index >= operations.size()) {
			return "Item doesn't exist";
		}
		String op = operations.get(index).getOp();
		
		operations.remove(index);
		
		for (int i = 0; i < operations.size(); i++) {
			operations.get(i).setIndex(i);
		}
		return op + " successfully deleted!";
	}

	private class Operation {
		int index;
		String op;

		Operation(String s) {
			index = operations.size();
			op = s;
		}

		String getOp() {
			return op;
		}
		
		void setIndex(int i) {
			this.index= i;
		}

		public String toString() {
			return index + ": " + op;
		}
	}
}
