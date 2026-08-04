package com.mcjava20.mid_player;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.loading.FMLPaths;

public class MidiPlayerManager {
    private static MidiPlayerManager instance;
    
    private List<File> midFiles = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean isLooping = false;
    private SourceDataLine audioLine;
    private AudioInputStream audioStream;
    private boolean stopRequested = false;
    private File currentWavFile = null;
    private long currentMillis = 0;
    private long totalMillis = 0;
    
    private MidiPlayerManager() {}
    
    public static MidiPlayerManager getInstance() {
        if (instance == null) {
            instance = new MidiPlayerManager();
        }
        return instance;
    }
    
    public List<File> getMidFiles() {
        return midFiles;
    }
    
    public void setMidFiles(List<File> files) {
        this.midFiles = files;
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
    
    public boolean isLooping() {
        return isLooping;
    }
    
    public void setLooping(boolean looping) {
        this.isLooping = looping;
    }
    
    public SourceDataLine getAudioLine() {
        return audioLine;
    }
    
    public void setAudioLine(SourceDataLine line) {
        this.audioLine = line;
    }
    
    public AudioInputStream getAudioStream() {
        return audioStream;
    }
    
    public void setAudioStream(AudioInputStream stream) {
        this.audioStream = stream;
    }
    
    public boolean isStopRequested() {
        return stopRequested;
    }
    
    public void setStopRequested(boolean requested) {
        this.stopRequested = requested;
    }
    
    public File getCurrentWavFile() {
        return currentWavFile;
    }
    
    public void setCurrentWavFile(File file) {
        this.currentWavFile = file;
    }

    public long getCurrentMillis() {
        return currentMillis;
    }

    public void setCurrentMillis(long currentMillis) {
        this.currentMillis = currentMillis;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public void setTotalMillis(long totalMillis) {
        this.totalMillis = totalMillis;
    }
    
    public void showMessage(String messageKey, String titleKey) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("[")
                        .append(Component.translatable(titleKey))
                        .append("] ")
                        .append(Component.translatable(messageKey)));
            }
        });
    }
    
    public void savePlaylist() {
        try {
            Path packDir = FMLPaths.GAMEDIR.get().resolve("midplayer_pack");
            if (!Files.exists(packDir)) {
                Files.createDirectories(packDir);
            }
            Path playlistFile = packDir.resolve("playlist.txt");
            List<String> lines = new ArrayList<>();
            for (File file : midFiles) {
                lines.add(file.getAbsolutePath());
            }
            Files.write(playlistFile, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadPlaylist() {
        try {
            Path packDir = FMLPaths.GAMEDIR.get().resolve("midplayer_pack");
            Path playlistFile = packDir.resolve("playlist.txt");
            if (!Files.exists(playlistFile)) {
                return;
            }
            List<String> lines = Files.readAllLines(playlistFile);
            midFiles.clear();
            for (String line : lines) {
                File file = new File(line);
                if (file.exists() && file.isFile()) {
                    midFiles.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}