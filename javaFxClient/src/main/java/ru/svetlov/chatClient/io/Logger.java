package ru.svetlov.chatClient.io;

import java.util.Collection;

public interface Logger {
    void log(LogMessage entry); // logs entry
    Collection<String> getEntries(String nickname); //get entries from the log
}
