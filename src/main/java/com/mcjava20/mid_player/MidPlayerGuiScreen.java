package com.mcjava20.mid_player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MidPlayerGuiScreen extends Screen {
    private List<File> midFiles;
    private MIDList list;
    private Process fluidsynthProcess;
    private MidiPlayerManager manager;
    private AbstractButton pauseBtn;
    private AbstractButton loopBtn;

    public MidPlayerGuiScreen() {
        super(Component.translatable("midplayer.gui.title"));
        manager = MidiPlayerManager.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        
        midFiles = new ArrayList<>(manager.getMidFiles());
        
        int listWidth = width - 40;
        int listHeight = height - 115;
        int listX = 20;
        int listY = 30;
        
        list = new MIDList(listX, listY, listWidth, listHeight);
        this.addRenderableWidget(list);

        int btnWidth = 80;
        int btnHeight = 20;
        int firstRowY = height - 80;
        int secondRowY = height - 55;
        int thirdRowY = height - 30;

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.add_file"), btn -> addMidFile())
                        .bounds(20, firstRowY, btnWidth + 20, btnHeight)
                        .build()
        );

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.play"), btn -> playSelected())
                        .bounds(width / 2 - btnWidth / 2, firstRowY, btnWidth, btnHeight)
                        .build()
        );

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.delete"), btn -> deleteSelected())
                        .bounds(width - btnWidth - 20, firstRowY, btnWidth, btnHeight)
                        .build()
        );

        int ctrlBtnWidth = 60;
        int ctrlStartX = width / 2 - ctrlBtnWidth * 2 - 15;
        
        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.previous"), btn -> playPrevious())
                        .bounds(ctrlStartX, secondRowY, ctrlBtnWidth, btnHeight)
                        .build()
        );

        AbstractButton tempPauseBtn = ExtendedButton.builder(Component.translatable(manager.isPaused() ? "midplayer.gui.resume" : "midplayer.gui.pause"), btn -> togglePause())
                .bounds(ctrlStartX + ctrlBtnWidth + 10, secondRowY, ctrlBtnWidth, btnHeight)
                .build();
        pauseBtn = tempPauseBtn;
        this.addRenderableWidget(pauseBtn);

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.stop"), btn -> stopPlayback())
                        .bounds(ctrlStartX + ctrlBtnWidth * 2 + 20, secondRowY, ctrlBtnWidth, btnHeight)
                        .build()
        );

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.next"), btn -> playNext())
                        .bounds(ctrlStartX + ctrlBtnWidth * 3 + 30, secondRowY, ctrlBtnWidth, btnHeight)
                        .build()
        );

        AbstractButton tempLoopBtn = ExtendedButton.builder(Component.translatable(manager.isLooping() ? "midplayer.gui.loop_on" : "midplayer.gui.loop_off"), btn -> toggleLoop())
                .bounds(width / 2 - 40, thirdRowY, 80, btnHeight)
                .build();
        loopBtn = tempLoopBtn;
        this.addRenderableWidget(loopBtn);

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.gui.close"), btn -> this.onClose())
                        .bounds(width - btnWidth - 20, thirdRowY, btnWidth, btnHeight)
                        .build()
        );
    }

    private void addMidFile() {
        if (minecraft != null) {
            minecraft.setScreen(new FileChooserScreen(this, new FileChooserScreen.FileChooserCallback() {
                @Override
                public void onFileSelected(File file) {
                    if (!midFiles.contains(file)) {
                        midFiles.add(file);
                        manager.getMidFiles().add(file);
                        list.addMidEntry(new MIDListEntry(file));
                    }
                    if (minecraft != null) {
                        minecraft.setScreen(MidPlayerGuiScreen.this);
                    }
                }

                @Override
                public void onCancel() {
                    if (minecraft != null) {
                        minecraft.setScreen(MidPlayerGuiScreen.this);
                    }
                }
            }));
        }
    }

    private void deleteSelected() {
        MIDListEntry selected = list.getSelected();
        if (selected != null) {
            int idx = midFiles.indexOf(selected.getFile());
            midFiles.remove(selected.getFile());
            manager.getMidFiles().remove(selected.getFile());
            list.removeMidEntry(selected);
            list.setSelected(null);
            if (manager.getCurrentIndex() >= midFiles.size()) {
                manager.setCurrentIndex(midFiles.size() - 1);
            }
            if (manager.getCurrentIndex() == idx && manager.isPlaying()) {
                stopPlayback();
            }
        }
    }

    private void playPrevious() {
        if (midFiles.isEmpty()) {
            showMessage("midplayer.message.empty_playlist", "midplayer.message.title.tip");
            return;
        }
        
        int currentIndex = manager.getCurrentIndex();
        if (currentIndex <= 0) {
            currentIndex = midFiles.size() - 1;
        } else {
            currentIndex--;
        }
        
        manager.setCurrentIndex(currentIndex);
        playFile(midFiles.get(currentIndex));
    }

    private void playNext() {
        if (midFiles.isEmpty()) {
            showMessage("midplayer.message.empty_playlist", "midplayer.message.title.tip");
            return;
        }
        
        int currentIndex = manager.getCurrentIndex();
        if (currentIndex >= midFiles.size() - 1) {
            if (manager.isLooping()) {
                currentIndex = 0;
                manager.setCurrentIndex(currentIndex);
                playFile(midFiles.get(currentIndex));
            } else {
                showMessage("midplayer.message.playlist_ended", "midplayer.message.title.tip");
                stopPlayback();
            }
        } else {
            currentIndex++;
            manager.setCurrentIndex(currentIndex);
            playFile(midFiles.get(currentIndex));
        }
    }

    private void togglePause() {
        if (!manager.isPlaying()) {
            showMessage("midplayer.message.no_music_playing", "midplayer.message.title.tip");
            return;
        }
        
        if (manager.isPaused()) {
            manager.setPaused(false);
            showMessage("midplayer.message.resumed", "midplayer.message.title.tip");
            if (pauseBtn != null) {
                pauseBtn.setMessage(Component.translatable("midplayer.gui.pause"));
            }
        } else {
            manager.setPaused(true);
            showMessage("midplayer.message.paused", "midplayer.message.title.tip");
            if (pauseBtn != null) {
                pauseBtn.setMessage(Component.translatable("midplayer.gui.resume"));
            }
        }
    }

    private void stopPlayback() {
        manager.setStopRequested(true);
        manager.setPlaying(false);
        manager.setPaused(false);
        
        if (manager.getAudioLine() != null) {
            try {
                manager.getAudioLine().stop();
                manager.getAudioLine().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            manager.setAudioLine(null);
        }
        
        if (manager.getAudioStream() != null) {
            try {
                manager.getAudioStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            manager.setAudioStream(null);
        }

        manager.setCurrentMillis(0);
        manager.setTotalMillis(0);
        
        if (pauseBtn != null) {
            pauseBtn.setMessage(Component.translatable("midplayer.gui.pause"));
        }
        
        fluidsynthProcess = null;
    }

    private void toggleLoop() {
        manager.setLooping(!manager.isLooping());
        showMessage(manager.isLooping() ? "midplayer.gui.loop_on" : "midplayer.gui.loop_off", "midplayer.message.title.tip");
        if (loopBtn != null) {
            loopBtn.setMessage(Component.translatable(manager.isLooping() ? "midplayer.gui.loop_on" : "midplayer.gui.loop_off"));
        }
    }

    private void playSelected() {
        MIDListEntry selected = list.getSelected();
        if (selected == null) {
            showMessage("midplayer.message.select_midi_first", "midplayer.message.title.tip");
            return;
        }
        
        int index = midFiles.indexOf(selected.getFile());
        manager.setCurrentIndex(index);
        playFile(selected.getFile());
    }

    private void playFile(File midFile) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path packDir = gameDir.resolve("midplayer_pack");
        
        if (!Files.exists(packDir)) {
            try {
                Files.createDirectories(packDir);
                showMessage("midplayer.message.created_pack_dir", "midplayer.message.title.tip");
            } catch (IOException e) {
                showMessage("midplayer.message.create_pack_dir_failed", "midplayer.message.title.error", e.getMessage());
                return;
            }
        }

        File sf2File = null;
        try (Stream<Path> walk = Files.walk(packDir)) {
            sf2File = walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".sf2"))
                    .findFirst()
                    .map(Path::toFile)
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File fluidsynthFile = new File(packDir.toFile(), "fluidsynth.exe");
        if (!fluidsynthFile.exists()) {
            fluidsynthFile = new File("fluidsynth.exe");
        }

        if (!fluidsynthFile.exists() || sf2File == null) {
            if (!fluidsynthFile.exists()) {
                showMessage("midplayer.message.fluidsynth_not_found", "midplayer.message.title.error");
            }
            if (sf2File == null) {
                showMessage("midplayer.message.no_sf2", "midplayer.message.title.tip");
            }
            openDownloadUrls();
            return;
        }

        stopPlayback();

        try {
            File tempWavFile = File.createTempFile("midplayer_", ".wav");
            tempWavFile.deleteOnExit();
            
            List<String> command = new ArrayList<>();
            command.add(fluidsynthFile.getAbsolutePath());
            command.add("-i");
            command.add("-F");
            command.add(tempWavFile.getAbsolutePath());
            command.add("-r");
            command.add("44100");
            
            if (sf2File != null) {
                command.add(sf2File.getAbsolutePath());
            }
            
            command.add(midFile.getAbsolutePath());
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            
            fluidsynthProcess = pb.start();
            fluidsynthProcess.waitFor();
            
            if (fluidsynthProcess.exitValue() == 0) {
                manager.setPlaying(true);
                manager.setPaused(false);
                manager.setStopRequested(false);
                playWavFile(tempWavFile);
            } else {
                showMessage("midplayer.message.convert_failed", "midplayer.message.title.error");
            }
            
        } catch (IOException | InterruptedException e) {
            showMessage("midplayer.message.start_fluidsynth_failed", "midplayer.message.title.error", e.getMessage());
        }
    }

    private void showMessage(String messageKey, String titleKey) {
        minecraft.execute(() -> {
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("[")
                        .append(Component.translatable(titleKey))
                        .append("] ")
                        .append(Component.translatable(messageKey)));
            }
        });
    }

    private void showMessage(String messageKey, String titleKey, Object... args) {
        minecraft.execute(() -> {
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal("[")
                        .append(Component.translatable(titleKey))
                        .append("] ")
                        .append(Component.translatable(messageKey, args)));
            }
        });
    }

    private void openDownloadUrls() {
        String[] urls = {
            "https://github.com/mcjava20/midplayer_pack",
            "https://gitee.com/mcjava20/midplayer_pack"
        };
        for (String url : urls) {
            try {
                openInBrowser(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showMessage("midplayer.message.opening_browser", "midplayer.message.title.tip");
    }

    private void openInBrowser(String url) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;
        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "start", "", url);
        } else if (os.contains("mac")) {
            pb = new ProcessBuilder("open", url);
        } else {
            pb = new ProcessBuilder("xdg-open", url);
        }
        pb.redirectErrorStream(true);
        pb.start();
    }

    private void playWavFile(File wavFile) {
        new Thread(() -> {
            try {
                javax.sound.sampled.AudioInputStream stream = javax.sound.sampled.AudioSystem.getAudioInputStream(wavFile);
                javax.sound.sampled.AudioFormat format = stream.getFormat();
                javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, format);
                javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
                
                manager.setAudioStream(stream);
                manager.setAudioLine(line);
                manager.setCurrentWavFile(wavFile);

                try {
                    long frameLength = stream.getFrameLength();
                    float frameRate = format.getFrameRate();
                    if (frameLength > 0 && frameRate > 0) {
                        long totalMillis = (long) ((frameLength / frameRate) * 1000L);
                        manager.setTotalMillis(totalMillis);
                    } else {
                        manager.setTotalMillis(0);
                    }
                } catch (Exception e) {
                    manager.setTotalMillis(0);
                }
                
                line.open(format);
                line.start();
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                long framesWritten = 0;
                int frameSize = format.getFrameSize();
                float frameRate = format.getFrameRate();
                
                while (!manager.isStopRequested()) {
                    while (manager.isPaused() && !manager.isStopRequested()) {
                        Thread.sleep(100);
                    }
                    
                    bytesRead = stream.read(buffer);
                    if (bytesRead == -1) {
                        if (manager.isLooping()) {
                            stream.close();
                            stream = javax.sound.sampled.AudioSystem.getAudioInputStream(wavFile);
                            manager.setAudioStream(stream);
                            framesWritten = 0;
                            manager.setCurrentMillis(0);
                            continue;
                        } else {
                            break;
                        }
                    }
                    line.write(buffer, 0, bytesRead);

                    try {
                        if (frameSize > 0 && frameRate > 0) {
                            framesWritten += bytesRead / (long) frameSize;
                            long currentMillis = (long) ((framesWritten / frameRate) * 1000L);
                            manager.setCurrentMillis(currentMillis);
                        }
                    } catch (Exception ignored) {}
                }
                
                line.drain();
                line.stop();
                line.close();
                stream.close();
                
                manager.setAudioLine(null);
                manager.setAudioStream(null);

                // ensure current position is set to total when finished
                try {
                    if (manager.getTotalMillis() > 0) {
                        manager.setCurrentMillis(manager.getTotalMillis());
                    } else {
                        manager.setCurrentMillis(0);
                    }
                } catch (Exception ignored) {}
                
                if (!manager.isStopRequested() && manager.isPlaying() && !manager.isPaused()) {
                    playNext();
                }
                
                if (!manager.isStopRequested() && !manager.isPaused()) {
                    manager.setPlaying(false);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("midplayer.message.play_audio_failed", "midplayer.message.title.error", e.getMessage());
                manager.setPlaying(false);
            }
        }).start();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, this.getTitle(), width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private class MIDList extends ObjectSelectionList<MIDListEntry> {
        private final int leftPos;

        public MIDList(int x, int y, int width, int height) {
            super(MidPlayerGuiScreen.this.minecraft, width, height, y, 24);
            this.leftPos = x;
            for (File file : midFiles) {
                this.addEntry(new MIDListEntry(file));
            }
        }

        public void addMidEntry(MIDListEntry entry) {
            this.addEntry(entry);
        }

        public void removeMidEntry(MIDListEntry entry) {
            List<MIDListEntry> entries = new ArrayList<>(this.children());
            entries.remove(entry);
            this.replaceEntries(entries);
        }

        @Override
        public int getX() {
            return this.leftPos;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.leftPos + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }
    }

    private class MIDListEntry extends ObjectSelectionList.Entry<MIDListEntry> {
        private final File file;

        public MIDListEntry(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            String displayName = file.getName();
            if (displayName.length() > 40) {
                displayName = displayName.substring(0, 37) + "...";
            }
            int textY = top + (height - MidPlayerGuiScreen.this.font.lineHeight) / 2;
            guiGraphics.drawString(MidPlayerGuiScreen.this.font, displayName, left + 5, textY, 0xFFFFFFFF);

            // draw current/total time on the right for the currently playing item
            if (manager.getCurrentIndex() == index && manager.isPlaying()) {
                long current = manager.getCurrentMillis();
                long total = manager.getTotalMillis();
                String timeText = formatTime(current) + "/" + formatTime(total);
                int timeWidth = MidPlayerGuiScreen.this.font.width(timeText);
                int xPos = left + width - timeWidth - 8; // padding
                // ensure time does not overlap with filename
                int nameEnd = left + 5 + MidPlayerGuiScreen.this.font.width(displayName) + 8;
                if (xPos > nameEnd) {
                    guiGraphics.drawString(MidPlayerGuiScreen.this.font, timeText, xPos, textY, 0xFFFFFFFF);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MidPlayerGuiScreen.this.list.setSelected(this);
            return true;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(file.getName());
        }
    }

    private String formatTime(long millis) {
        if (millis <= 0) return "0:00";
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}