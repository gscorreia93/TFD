package server.library.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import server.library.Entry;

/**
 * Class that handles the logs.
 */
public class LogHandler {

	private String filename;
	private RandomAccessFile logFile;

	public LogHandler(String filename) {
		try {
			openFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openFile(String filename) throws IOException {
		this.filename = filename;

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
			currentLogIndex = getCurrentLogIndex();
			setPointerAtIndex(currentLogIndex);

			for (int i = 0; i < entries.length; i++) {
				currentLogIndex++;
				// When the write entry comes from the client it doesn't bring the term
				int term = entries[i].getTerm() > 0 ? entries[0].getTerm() : logTerm;

				logFile.write(new LogEntry(currentLogIndex, term, entries[i].getEntry(), entries[i].getClientID()).writeln());
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
		try {
			if (fileIsEmpty() && logIndex == 0)
				return true;

			logFile.seek(0);

			LogEntry logEntry;
			String pointerLog = logFile.readLine();

			while (pointerLog != null && !pointerLog.isEmpty()) { // Reads until it reaches the last line
				logEntry = new LogEntry(pointerLog);

				if (logEntry.getLogIndex() == logIndex && logEntry.getLogTerm() == logTerm) {
					return true;
				}
				pointerLog = logFile.readLine(); // Reads until it reaches the last line
			}
		} catch (IOException e) {}
		return false;
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

	/**
	 * Deletes all logs from the logIndex, until the end of the file.
	 */
	public void deleteConflitingLogs(int logIndex) {
		try {
			int currentIndex = getCurrentLogIndex();

			// Since we can't directly delete the lines from a file
			// We need to create a new file with the lines to keep
			// And then delete the old file

			if (currentIndex >= logIndex) { // We check if there are lines to delete above our index
				List<LogEntry> logs = getLogsSinceIndex(0);
				List<LogEntry> logs2Keep = new ArrayList<>();

				// First we get the lines to keep
				for (int i = 1; i < logIndex; i++) {
					logs2Keep.add(logs.get(i - 1));
				}

				if (!logs2Keep.isEmpty()) {
					logFile.close(); // Then we close the current log file

					// We create a temporary file to store the entries to keep
					String tempFilename = filename + "_temp";
					File tempFile = new File(tempFilename);
					if (!tempFile.exists()) {
						tempFile.createNewFile();
					}

					RandomAccessFile tempLogFile = new RandomAccessFile(tempFile, "rw");
					// We store the entries to keep in the new file
					for (int i = 0; i < logs2Keep.size(); i++) {
						tempLogFile.write(logs2Keep.get(i).writeln());
					}
					tempLogFile.close();
					
					// And then we replace the old file with the new
					tempFile.renameTo(new File(filename));
				}
			}
		} catch (IOException e) {}
	}

	/**
	 * Gets the logs since a given index to replicate
	 * to follower servers that are not up to date in
	 * the client's format.
	 */
	public Entry[] getEntriesSinceIndex(int logIndex) {
		List<LogEntry> tempLogs = getLogsSinceIndex(logIndex);

		Entry[] logs2Return = new Entry[tempLogs.size()];
		for (int i = 0; i < tempLogs.size(); i++) {
			logs2Return[i] = new Entry(tempLogs.get(i).getClientID(), null,
					tempLogs.get(i).getLog(), tempLogs.get(i).getLogTerm(), tempLogs.get(i).isCommited());
		}
		return logs2Return;
	}

	public int getLastCommitedLogIndex() {
		List<LogEntry> logs = getLogsSinceIndex(0);
		for (int i = logs.size() -1; i >= 0; i--) {
			if (logs.get(i).isCommited()) {
				return logs.get(i).getLogIndex();
			}
		}
		return 0;
	}

	private int getCurrentLogIndex() throws IOException {
		// NOT RETURNING THE NUMBER OF FILLED LINES
		// return (int) logFile.length();
		// So a workaround is needed
		logFile.seek(0);

		int i = 0;
		String pointerLog = logFile.readLine();

		while (pointerLog != null && !pointerLog.isEmpty()) { // Reads until it reaches the last line
			i++;
			pointerLog = logFile.readLine(); // Reads until it reaches the last line
		}

		// When the file is empty the pointerLog is null
		// But has made the same .readLine() as if had 1 line
		return i;
	}

	private void setPointerAtIndex(int index) throws IOException {
		// NOT WORKING HOW IT WAS SUPPOSED
		// logFile.seek(commitEntryIndex - 1);
		// So a workaround is needed
		logFile.seek(0);

		for (int i = 0; i < index; i++) { // Reads until it reaches the line we want
			logFile.readLine();
		}
	}

	private boolean fileIsEmpty() throws IOException {
		// NOT RETURNING THE NUMBER OF FILLED LINES
		// return (int) logFile.length();
		// So a workaround is needed
		logFile.seek(0);

		int i = 0;
		String pointerLog = logFile.readLine();

		while (pointerLog != null && !pointerLog.isEmpty()) { // Reads until it reaches the last line
			i++;
			pointerLog = logFile.readLine(); // Reads until it reaches the last line
		}
		return i == 0;
	}

	/**
	 * Gets the logs since a given index to replicate
	 * to follower servers that are not up to date.
	 * @return
	 */
	private List<LogEntry> getLogsSinceIndex(int logIndex) {
		List<LogEntry> tempLogs = new ArrayList<>();
		try {
			setPointerAtIndex(logIndex);

			String pointerLog = logFile.readLine();
			while (pointerLog != null && !pointerLog.isEmpty()) { // Reads until it reaches the last line
				tempLogs.add(new LogEntry(pointerLog));
				pointerLog = logFile.readLine();
			}

		} catch (IOException e) {}
		return tempLogs;
	}
}
