package ru.svetlov.chatClient.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileLogger implements Logger {
    private final File logFile;
    private final BlockingQueue<LogMessage> queue;
    private boolean append;
    private Collection<LogMessage> oldMessages;

    public FileLogger(String filename, boolean append) {
        this.append = append;
        logFile = new File(filename);
        try {
            if (logFile.exists()) {
                getOldMessages();
                logFile.delete();
            } else
                oldMessages = new ArrayList<>();
            logFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        queue = new LinkedBlockingQueue<>();
        Thread listener = new Thread(() -> {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(logFile))) {
                if (append)
                    for (LogMessage lm : oldMessages)
                        out.writeObject(lm);
                while (true) {
                    LogMessage lm = queue.take();
                    out.writeObject(lm);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    @Override
    public void log(LogMessage entry) {
        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<String> getEntries(String nickname) {
        Collection<String> list = new ArrayList<>();
        for (LogMessage lm : oldMessages)
            if (lm.getNickname().equals(nickname))
                list.add(lm.getMessage());
        return list;
    }

    private void getOldMessages() {
        oldMessages = new ArrayList<>();
        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(logFile))) {
            while (true)
                oldMessages.add((LogMessage) stream.readObject());
        } catch (EOFException ignored) {
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
