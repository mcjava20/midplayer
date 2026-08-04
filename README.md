# MID PLAYER

一款内置 MIDI 播放器功能的 Minecraft NeoForge 客户端辅助模组。你可以将 MIDI 文件导入游戏，在游戏内交互播放音乐。

A Minecraft NeoForge client-side helper mod with a built-in MIDI player. Import MIDI files and play music interactively in-game.

---

## 环境要求 / Requirements

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.215+ |
| Java | 21 |

---

## 安装 / Installation

1. 将构建好的 `midplayer-1.0.0.jar` 放入 `.minecraft/mods` 文件夹。
2. 启动游戏，模组会自动创建 `midplayer_pack` 目录。
3. 将 `fluidsynth.exe` 和 `.sf2` 音色文件放入 `midplayer_pack` 目录（详见下方[资源准备](#资源准备--resources)）。

---

## 资源准备 / Resources

本模组依赖 [FluidSynth](http://www.fluidsynth.org/) 将 MIDI 转换为 WAV 进行播放，同时需要 SF2 音色文件。

下载地址：
- GitHub: <https://github.com/mcjava20/midplayer_pack>
- Gitee: <https://gitee.com/mcjava20/midplayer_pack>

### 目录结构

```
.minecraft/
├── mods/
│   └── midplayer-1.0.0.jar
└── midplayer_pack/
    ├── fluidsynth.exe      # FluidSynth 可执行文件
    ├── *.sf2               # SF2 音色文件
    └── playlist.txt        # 播放列表（自动生成）
```

> **提示：** 如果 `midplayer_pack` 目录或其中资源缺失，点击播放时会自动打开浏览器跳转到下载页面。

---

## 使用方法 / Usage

### 打开播放器

在游戏中按 **F8** 键打开/关闭播放器界面。

- 播放器界面关闭后，音乐会在后台继续播放。
- 联机时服务端无需安装此模组，不会产生存档改动。

### 播放器界面

```
┌─────────────────────────────────────────┐
│           MID 播放器界面                 │
│─────────────────────────────────────────│
│  播放列表                                │
│  ├─ song1.mid           1:23/3:45       │
│  ├─ song2.mid                            │
│  └─ song3.mid                            │
│─────────────────────────────────────────│
│ [添加播放文件]      [播放]      [删除]   │
│ [上一首] [暂停] [停止] [下一首]          │
│         [循环: 关]      [关闭]           │
└─────────────────────────────────────────┘
```

### 功能按钮

| 按钮 | 功能 |
|------|------|
| 添加播放文件 | 打开文件选择器添加 MIDI 文件 |
| 播放 | 播放选中的 MIDI 文件 |
| 删除 | 从播放列表移除选中的文件 |
| 上一首 | 播放列表中的上一首 |
| 暂停 / 继续 | 暂停或继续播放（按钮文字会切换） |
| 停止 | 停止当前播放 |
| 下一首 | 播放列表中的下一首 |
| 循环: 开/关 | 切换单曲循环模式 |
| 关闭 | 关闭播放器界面（音乐继续后台播放） |

### 文件选择器

文件选择器支持跨盘符导航：

- **切换驱动器**：点击左上角"切换驱动器"按钮，显示所有可用盘符按钮。
- **盘符按钮**：点击盘符按钮直接切换到该驱动器根目录。
- **返回目录**：在驱动器视图点击"返回目录"回到文件浏览视图。
- **筛选**：仅显示 `.mid` / `.midi` 文件。

---

## 功能特性 / Features

- 🎵 **MIDI 播放**：通过 FluidSynth 将 MIDI 转换为 WAV 实时播放。
- 🎹 **SF2 音色支持**：支持自定义 SF2 音色文件，未提供时使用默认音色。
- 📋 **播放列表管理**：添加、删除、切换曲目。
- 🔁 **单曲循环**：支持单曲循环播放。
- ⏯ **暂停/继续**：从暂停位置继续播放。
- ⏱ **时间显示**：播放列表中显示当前曲目的播放时间/总时长。
- 💾 **歌单持久化**：游戏退出时自动保存歌单，下次进入自动加载（自动跳过已不存在的文件）。
- 🌐 **多语言支持**：支持简体中文、繁体中文、英语、日语、韩语。
- 🖥 **客户端运行**：仅客户端运行，服务端无需安装。
- 🔗 **资源自动引导**：资源缺失时自动打开浏览器跳转下载页面。

---

## 快捷键 / Keybindings

| 快捷键 | 功能 | 分类 |
|--------|------|------|
| F8 | 打开/关闭播放器界面 | MID Player |

> 可在 `选项 → 控制 → 按键绑定` 中自定义。

---

## 多语言 / Localization

| 语言 | 代码 | 文件 |
|------|------|------|
| 简体中文 | `zh_cn` | `assets/midplayer/lang/zh_cn.json` |
| 繁体中文 | `zh_tw` | `assets/midplayer/lang/zh_tw.json` |
| English | `en_us` | `assets/midplayer/lang/en_us.json` |
| 日本語 | `ja_jp` | `assets/midplayer/lang/ja_jp.json` |
| 한국어 | `ko_kr` | `assets/midplayer/lang/ko_kr.json` |

游戏会根据 Minecraft 客户端的语言设置自动切换。如需添加新语言，可参考 `en_us.json` 创建对应语言文件。

---

## 构建说明 / Build

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

构建产物位于 `build/libs/midplayer-1.0.0.jar`。

---

## 项目结构 / Project Structure

```
src/main/java/com/mcjava20/mid_player/
├── MIDPLAYER.java              # 模组主类，注册按键和事件
├── MidPlayerGuiScreen.java     # 播放器主界面
├── FileChooserScreen.java      # 文件选择器界面
└── MidiPlayerManager.java      # 播放状态管理（单例）

src/main/resources/assets/midplayer/
└── lang/
    ├── en_us.json              # 英语
    ├── zh_cn.json              # 简体中文
    ├── zh_tw.json              # 繁体中文
    ├── ja_jp.json              # 日语
    └── ko_kr.json              # 韩语
```

---

## 技术实现 / Technical Details

- **MIDI 转换**：调用 `fluidsynth.exe -i -F <output.wav> -r 44100 [<sf2>] <input.mid>` 将 MIDI 转换为 WAV。
- **音频播放**：使用 Java Sound API（`SourceDataLine` + `AudioInputStream`）播放 WAV 文件。
- **暂停/继续**：播放线程在暂停时通过 `Thread.sleep` 循环等待，恢复后从暂停位置继续。
- **歌单持久化**：游戏退出时触发 `GameShuttingDownEvent`，将歌单保存到 `midplayer_pack/playlist.txt`。
- **状态管理**：`MidiPlayerManager` 单例保存播放状态，GUI 关闭重开不影响播放。

---

## 许可证 / License

MIT License

## 作者 / Author

mcjava20

## 反馈 / Feedback

- GitHub: <https://github.com/mcjava20/midplayer>
