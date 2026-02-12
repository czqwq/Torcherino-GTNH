# Torcherino-GTNH

> 专为 GregTech: New Horizons (GTNH) 整合包设计的时间加速模组

## 📖 简介

Torcherino 是一个能够加速周围方块运行速度的魔法火把模组。本版本针对 GTNH 整合包进行了特别优化，完美支持 GregTech 机器的加速，让你的自动化工厂运行得更快！

### ✨ 核心特性

- **时间加速**：放置 Torcherino 火把可以加速周围区域内的机器和方块
- **GregTech 优化**：特别支持 GregTech 机器加速，不会导致能量消耗异常或跳电问题
- **多方块结构支持**：完美支持 GregTech 的多方块结构和单方块机器
- **可调节范围**：通过 GUI 界面自由调整加速范围和速度
- **红石控制**：支持红石信号控制开关（Classic 版本）

## 🔥 方块类型

模组提供了多种不同强度的 Torcherino 火把：

### 标准版本（带 GUI）

1. **Torcherino（普通火把）**
   - 基础版本的时间加速火把
   - 可通过 GUI 调节加速倍率（0% ~ 400%）
   - 可自定义 X、Y、Z 三个方向的加速范围

2. **Compressed Torcherino（压缩火把）**
   - 中级版本，加速效果更强
   - 可调节加速倍率（0% ~ 900%）
   - 范围和控制方式与普通版本相同

3. **Double Compressed Torcherino（双重压缩火把）**
   - 高级版本，最强的加速效果
   - 可调节加速倍率（0% ~ 8100%）
   - 适合需要极速生产的场景

### Classic 版本（传统模式）

每种火把都有对应的 Classic 版本（Torcherino Classic、Compressed Torcherino Classic、Double Compressed Torcherino Classic）：
- 使用传统的模式切换方式（右键点击切换）
- 支持红石信号控制（接收到红石信号时停止工作）
- 预设多个固定的加速倍率档位

## 🎮 使用方法

### 标准版本

1. 放置 Torcherino 火把在需要加速的区域附近
2. 右键点击火把打开 GUI 界面
3. 使用滑动条调整：
   - **Speed（速度）**：调整加速倍率
   - **X/Y/Z Radius（范围）**：调整三个方向的加速半径
4. 加速范围内的机器和方块会按照设定的倍率加快运行

### Classic 版本

1. 放置 Classic 版本的 Torcherino 火把
2. 右键点击切换加速模式和速度档位
3. Shift + 右键可以切换不同的速度设置
4. 使用红石信号可以远程控制火把的开关状态

## ⚙️ 配置选项

模组提供了配置文件（位于 `config/Torcherino.cfg`）供高级用户调整：

- **enableAccelerateGregTechMachine**：是否启用 GregTech 机器的高级加速（默认：开启）
- **accelerateGregTechMachineDiscount**：GregTech 机器加速的折扣系数（默认：0.8，范围：0.0-1.0）
  - 该参数用于平衡性能和游戏体验，防止过度加速导致服务器卡顿

## 🔧 技术特性

### GregTech 特别优化

本模组使用了先进的 Mixin 技术来优化 GregTech 机器的加速：
- **防止能量异常**：加速期间不会额外消耗能量或导致跳电
- **智能进度管理**：正确处理机器的工作进度，避免逻辑错误
- **性能限制**：内置性能保护，单次加速不超过 1ms，防止服务器卡顿
- **多方块支持**：特别支持 GregTech 的大型多方块结构（如高级装配线）

### 适配的特殊机器

- GregTech 基础机器和多方块结构
- TecTech 研究站（Research Station）
- 其他实现了加速接口的 Tile Entity

## 📦 依赖要求

- Minecraft 1.7.10
- Minecraft Forge 10.13.4.1614 或更高版本
- GregTech 5（GTNH 版本）推荐但不强制

## 🙏 鸣谢

- 感谢 [MockTurtle7/Torcherino](https://github.com/MockTurtle7/Torcherino) 提供的原始代码
- 感谢 NH-Utilities 提供的加速优化方案

## 📝 开源协议

本项目遵循原项目的开源协议。详见 [LICENSE](LICENSE) 文件。

## 🔗 相关链接

- 项目地址：https://github.com/czqwq/Torcherino-GTNH
- 问题反馈：https://github.com/czqwq/Torcherino-GTNH/issues

---

**提示**：使用时请注意合理设置加速倍率，过高的加速可能会影响服务器性能。建议在单人游戏中测试后再在服务器上使用。
