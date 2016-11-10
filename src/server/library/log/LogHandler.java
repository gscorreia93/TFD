package server.library.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import server.library.Entry;

/**
 * Class that handles the logs.
 */
public class LogHandler {

	private RandomAccessFile logFile;

	public LogHandler(String filename) {
		try {
			openFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openFile(String filename) throws IOException {
		File tempFile = new File(filename);
		if (!tempFile.exists()) {
			tempFile.createNewFile();
		}

		logFile = new RandomAccessFile(tempFile, "rw");
	}

	/**
	 * Writes a log entry
	 * @return the log entry index
	 */
	public int writeLogEntry(Entry[] entries, int logTerm) {
		int currentLogIndex = 0;
		try {
			currentLogIndex = getCurrentLogIndex() - 1;
			setPointerAtIndex(currentLogIndex);

			for (int i = 0; i < entries.length; i++) {
				currentLogIndex++;
				logFile.write(new LogEntry(currentLogIndex, logTerm, entries[0].getEntry(), entries[0].getClientID()).writeln());
			}
		} catch (IOException e) {}
		return currentLogIndex;
	}

	/**
	 * Commits a log entry
	 */
	public String commitLogEntry(int commitEntryIndex) {
		String commitedLog = null;

		try {
			setPointerAtIndex(commitEntryIndex - 1);

			LogEntry logEntry = new LogEntry(logFile.readLine());
			logEntry.setCommited(true);

			setPointerAtIndex(commitEntryIndex - 1);
			logFile.write(logEntry.writeln());

			commitedLog = logEntry.getLog();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return commitedLog;
	}

	public boolean containsLogRecord(int logIndex, int logTerm) {
		//		if (logs.isEmpty() && logIndex == 0)
		//			return true;
		//
		//		for (int i = logs.size() - 1; i >= 0; i--) {
		//			if (logs.get(i).getLogIndex() == logIndex && logs.get(i).getLogTerm() == logTerm) {
		//				return true;
		//			}
		//		}

		return true;
	}

	public void deleteConflitingLogs(int logIndex, int logTerm) {
		//		if (logs.size() >= logIndex && logs.get(logIndex - 1).getLogTerm() != logTerm) {
		//			logs.remove(logIndex - 1);
		//			deleteConflitingLogs(logIndex - 1, logTerm);
		//		}
	}

	public LogEntry getLastLog() {
		String lastLog = null, pointerLog = null;

		try {
			logFile.seek(0);

			pointerLog = logFile.readLine();
			do { // Reads until it reaches the last line
				lastLog = pointerLog;
				pointerLog = logFile.readLine();
			} while (pointerLog != null && !pointerLog.isEmpty());
		} catch (IOException e) {}

		return lastLog != null ? new LogEntry(lastLog) : new LogEntry();
	}

	public LogEntry getLogAtIndex(int logIndex) {
		//		if (!logs.isEmpty()) {
		//			return logs.get(logIndex);
		//		}
		return new LogEntry();
	}

	/**
	 * Gets the logs since a given index to replicate
	 * to follower servers that are not up to date.
	 * @return
	 */
	public Entry[] getLogsSinceIndex(int logIndex) {
		//		List<LogEntry> tempLogs = new ArrayList<>();
		//
		//		for (int i = 0; i < logs.size(); i++) {
		//			if (i == logIndex) {
		//				tempLogs.add(logs.get(i));
		//			}
		//		}
		//
		//		Entry[] logs2Return = new Entry[tempLogs.size()];
		//		for (int i = 0; i < tempLogs.size(); i++) {
		//			logs2Return[i] = new Entry(tempLogs.get(i).getClientID(), null, tempLogs.get(i).getLog());
		//		}
		//		return logs2Return;
		return null;
	}

	public int getLastCommitedLogIndex() {
		//		for (int i = logs.size() -1; i >= 0; i--) {
		//			if (logs.get(i).isCommited()) {
		//				return logs.get(i).getLogIndex();
		//			}
		//		}
		return 0;
	}

	private int getCurrentLogIndex() throws IOException {
		// NOT RETURNING THE NUMBER OF FILLED LINES
		// return (int) logFile.length();

		// So a workaround is needed
		logFile.seek(0);

		int i = 0;
		String pointerLog = logFile.readLine();

		do { // Reads until it reaches the last line
			i++;
			pointerLog = logFile.readLine(); // Reads until it reaches the last line
		} while (pointerLog != null && !pointerLog.isEmpty());

		// When the file is empty the pointerLog is null
		// But has made the same .readLine() as if had 1 line
		return pointerLog == null ? i : i + 1;
	}

	private void setPointerAtIndex(int index) throws IOException {
		// NOT WORKING HOW IT WAS SUPPOSED
		// logFile.seek(commitEntryIndex - 1);

		// So a workaround is needed
		logFile.seek(0);

		int i = 0;
		for (i = 0; i < index; i++) { // Reads until it reaches the line we want
			logFile.readLine();
		}
	}
}
