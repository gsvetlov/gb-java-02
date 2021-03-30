package ru.svetlov.chatClient.io;

import java.util.Collection;

public interface Logger {
    void log(String entry); // logs entry
    void clear(); //clears log
    Collection<String> getEntries(); //get entries from the log
}
