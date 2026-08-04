package com.mcjava20.mid_player;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileChooserScreen extends Screen {
    private final Screen parent;
    private final FileChooserCallback callback;
    private File currentDir;
    private FileList fileList;
    private AbstractButton confirmBtn;
    private AbstractButton driveBtn;
    private boolean showDrives = false;

    public interface FileChooserCallback {
        void onFileSelected(File file);
        void onCancel();
    }

    public FileChooserScreen(Screen parent, FileChooserCallback callback) {
        super(Component.translatable("midplayer.file_chooser.title"));
        this.parent = parent;
        this.callback = callback;
        this.currentDir = new File(System.getProperty("user.home"));
        if (!this.currentDir.exists()) {
            this.currentDir = new File("C:\\");
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int btnWidth = 80;
        int btnHeight = 20;

        if (showDrives) {
            int btnY = 60;
            int cols = 4;
            int driveBtnWidth = (width - 60) / cols;
            
            List<File> drives = new ArrayList<>();
            File[] roots = File.listRoots();
            boolean foundRoots = false;
            if (roots != null && roots.length > 0) {
                for (File root : roots) {
                    if (root.exists()) {
                        drives.add(root);
                        foundRoots = true;
                    }
                }
            }
            if (!foundRoots) {
                for (char c = 'C'; c <= 'Z'; c++) {
                    File drive = new File(c + ":\\");
                    if (drive.exists()) {
                        drives.add(drive);
                    }
                }
            }

            for (int i = 0; i < drives.size(); i++) {
                File drive = drives.get(i);
                int row = i / cols;
                int col = i % cols;
                int x = 20 + col * driveBtnWidth;
                int y = btnY + row * (btnHeight + 5);
                
                if (y > height - 60) break;
                
                addRenderableWidget(
                        ExtendedButton.builder(Component.literal(drive.getAbsolutePath()), btn -> selectDrive(drive))
                                .bounds(x, y, driveBtnWidth - 5, btnHeight)
                                .build()
                );
            }
        } else {
            int listWidth = width - 40;
            int listHeight = height - 120;
            int listX = 20;
            int listY = 60;
            
            fileList = new FileList(listX, listY, listWidth, listHeight);
            this.addRenderableWidget(fileList);
        }

        int btnY = height - 30;

        addRenderableWidget(
                ExtendedButton.builder(Component.translatable("midplayer.file_chooser.cancel"), btn -> callback.onCancel())
                        .bounds(width / 2 - btnWidth - 10, btnY, btnWidth, btnHeight)
                        .build()
        );

        AbstractButton tempBtn = ExtendedButton.builder(Component.translatable("midplayer.file_chooser.confirm"), btn -> confirmSelection())
                .bounds(width / 2 + 10, btnY, btnWidth, btnHeight)
                .build();
        confirmBtn = tempBtn;
        confirmBtn.active = !showDrives;
        this.addRenderableWidget(confirmBtn);

        AbstractButton tempDriveBtn = ExtendedButton.builder(Component.translatable(showDrives ? "midplayer.file_chooser.back_to_dir" : "midplayer.file_chooser.switch_drive"), btn -> toggleDriveView())
                .bounds(20, 35, 100, btnHeight)
                .build();
        driveBtn = tempDriveBtn;
        this.addRenderableWidget(driveBtn);
    }

    private void confirmSelection() {
        if (showDrives) return;
        FileListEntry selected = fileList.getSelected();
        if (selected != null && selected.isMidiFile()) {
            callback.onFileSelected(selected.getFile());
        }
    }

    private void toggleDriveView() {
        showDrives = !showDrives;
        minecraft.setScreen(this);
    }

    private void selectDrive(File drive) {
        currentDir = drive;
        showDrives = false;
        minecraft.setScreen(this);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, this.getTitle(), width / 2, 10, 0xFFFFFFFF);
        guiGraphics.drawString(font, currentDir.getAbsolutePath(), 20, 25, 0x888888);
    }

    private class FileList extends ObjectSelectionList<FileListEntry> {
        private final int leftPos;

        public FileList(int x, int y, int width, int height) {
            super(FileChooserScreen.this.minecraft, width, height, y, 24);
            this.leftPos = x;
            refresh();
        }

        public void refresh() {
            this.clearEntries();
            
            List<File> files = new ArrayList<>();
            
            if (currentDir.getParentFile() != null) {
                files.add(currentDir.getParentFile());
            }
            
            try {
                File[] dirFiles = currentDir.listFiles();
                if (dirFiles != null) {
                    List<File> dirList = new ArrayList<>(Arrays.asList(dirFiles));
                    dirList.sort((a, b) -> {
                        if (a.isDirectory() && !b.isDirectory()) return -1;
                        if (!a.isDirectory() && b.isDirectory()) return 1;
                        return a.getName().compareToIgnoreCase(b.getName());
                    });
                    files.addAll(dirList);
                }
            } catch (SecurityException e) {
                files.add(new File("无法访问此目录"));
            }
            
            for (File file : files) {
                this.addEntry(new FileListEntry(file));
            }
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

    private class FileListEntry extends ObjectSelectionList.Entry<FileListEntry> {
        private final File file;

        public FileListEntry(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public boolean isMidiFile() {
            if (!file.isFile()) return false;
            String name = file.getName().toLowerCase();
            return name.endsWith(".mid") || name.endsWith(".midi");
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            String displayName = file.getName();
            
            if (file.equals(currentDir.getParentFile())) {
                displayName = "..";
            }
            
            int color = 0xFFFFFFFF;
            if (file.equals(currentDir.getParentFile())) {
                color = 0x55FF55;
            } else if (file.isDirectory()) {
                color = 0x5555FF;
            } else if (!isMidiFile()) {
                color = 0x666666;
            }
            
            if (displayName.length() > 50) {
                displayName = displayName.substring(0, 47) + "...";
            }
            
            guiGraphics.drawString(FileChooserScreen.this.font, displayName, left + 5, top + (height - FileChooserScreen.this.font.lineHeight) / 2, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            FileChooserScreen.this.fileList.setSelected(this);
            
            if (file.isDirectory()) {
                currentDir = file;
                fileList.refresh();
            } else if (isMidiFile()) {
                confirmBtn.active = true;
            }
            
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(file.getName());
        }
    }
}