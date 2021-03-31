package ru.svetlov.chatClient.io;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class FileLogger implements Logger{
    private List<String> innerList;
    private File logfile;

    @Override
    public void log(String entry) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Collection<String> getEntries() {
        return null;
    }
}
