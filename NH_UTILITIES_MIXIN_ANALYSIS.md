# NH-Utilities Mixin Analysis Report

## 分析目的 / Purpose
检查NH-Utilities仓库中是否有类似MTEAdvAssLine的其他特化机器加速mixin，确保所有加速优化都已迁移到Torcherino-GTNH。

Check NH-Utilities repository for other specialized machine acceleration mixins similar to MTEAdvAssLine to ensure all acceleration optimizations have been migrated to Torcherino-GTNH.

## 已迁移的加速Mixin / Migrated Acceleration Mixins ✅

### GregTech机器加速 / GregTech Machine Acceleration

1. **BaseMetaTileEntityAcceleration_Mixin**
   - 位置: `mixins/late/GregTech/`
   - 功能: 所有GregTech机器的核心加速逻辑
   - 特性:
     - 直接操作 `mProgresstime` 字段
     - 支持基础机器 (MTEBasicMachine)
     - 支持多方块机器 (MTEMultiBlockBase)
     - 支持原始高炉 (MTEBrickedBlastFurnace)
     - 包含1ms性能限制

2. **MTEAdvAssLineAcceleration_Mixin** 
   - 位置: `mixins/late/GregTech/`
   - 功能: MTEAdvAssLine特殊处理
   - 特性:
     - 实现 `IAccelerationState` 接口
     - 拦截 `onRunningTick` 方法
     - 在加速时阻止能量消耗 (`baseEUt`)
     - 检查 `stuck` 状态避免卡顿

3. **ResearchStationAcceleration_Mixin**
   - 位置: `mixins/late/TecTech/`
   - 功能: TecTech研究站加速
   - 特性:
     - 直接操作 `computationRemaining` 字段
     - 乘以 `eAvailableData` 和加速率
     - 高效计算加速

### EnderIO机器加速 / EnderIO Machine Acceleration

4. **AccelerateTileEntity_Mixin**
   - 位置: `mixins/late/EnderIO/`
   - 功能: EnderIO TileEntityEnder加速
   - 特性:
     - 实现 `ITileEntityTickAcceleration` 接口
     - 重置 `lastUpdate = -1` 强制更新
     - 跟踪加速率

5. **AccelerateEnergyReceive_Mixin**
   - 位置: `mixins/late/EnderIO/`
   - 功能: EnderIO能量接收速率缩放
   - 特性:
     - 拦截 `getMaxEnergyReceived()` 方法
     - 按加速率倍增最大能量接收
     - 确保机器有足够能量

## 其他Mixin分析 / Other Mixins Analysis ❌

### 非加速相关的Mixin / Non-Acceleration Mixins

以下mixin **不涉及机器加速**，属于其他功能增强：

#### EnderIO功能增强 / EnderIO Features
- `Modify_CapBankMaxIO_Mixin`
  - 功能: 增加EnderIO电容银行最大IO
  - 类型: 功能增强，非加速
  - 代码: 将Vibrant等级电容银行MaxIO设为Integer.MAX_VALUE

#### GregTech功能 / GregTech Features
- `DisableDebuff_Mixin` - 禁用Super Chest/Tank的debuff
- `CoverChestStacksize_Mixin` - 移除箱子cover堆叠限制
- `ModifySomeConfigs` - 修改配置（NEI显示、污染等）
- `ModifyMachineNEIHandleSort` - NEI显示排序
- `GTMEHatchesDenseChannel_Mixin` - AE2密集ME通道支持

#### 无线仓库功能 / Wireless Hatch Features
- `BanOriginalWirelessRecipes_Mixin` - 禁用原版无线配方
- `MTEHatchWirelessTexture_Mixin` - 修改无线仓库材质
- `BanOriginalWirelessDataOrComputationHatchRecipes_Mixin` - 禁用无线数据/计算仓库配方

#### 其他Mod增强 / Other Mod Enhancements
- `TeleporterMKII_Mixin` - Draconic Evolution传送增强
- `GUITeleporter_Mixin` - 传送GUI修改
- `TeleporterPacket_Mixin` - 传送数据包修改
- `EnhanceExUHealingAxe_Mixin` - Extra Utilities治愈斧增强
- `ModifyWEWithExU` - WorldEdit与ExU集成
- `EnhanceOvenGlove_Mixin` - 烤箱手套增强
- `DisableDollyDebuff_Mixin` - JABBA Dolly debuff移除
- `HologramItem_Mixin_Mixin` - Structure Lib全息图功能
- `HologramCharcoalPitUtil_Mixin` - 木炭堆全息图工具

## 结论 / Conclusion

### 加速系统完整性 / Acceleration System Completeness

✅ **所有加速相关的mixin已完整迁移**

Torcherino-GTNH现在包含NH-Utilities中所有的机器加速优化：

1. **GregTech机器全覆盖**
   - 基础机器 (Basic Machines)
   - 多方块机器 (Multiblocks)
   - 原始高炉 (Primitive Blast Furnace)
   - MTEAdvAssLine特殊处理
   - TecTech研究站

2. **EnderIO机器支持**
   - TileEntity更新强制
   - 能量接收速率自动调整

3. **性能保护**
   - 1ms执行时间限制
   - 异常捕获和日志
   - 防止服务器卡顿

### 无需额外迁移 / No Additional Migration Needed

经过详细分析，NH-Utilities中剩余的mixin都是**功能增强类**，而非**加速优化类**。这些mixin主要涉及：
- 配置修改
- UI改进
- 无线仓库系统
- 跨mod集成
- 游戏性调整

这些功能**不属于Torcherino的核心功能**（时间加速），因此不需要迁移。

## 技术细节 / Technical Details

### 加速系统工作原理 / How Acceleration Works

```
Torcherino/TimeVial
    ↓
ITileEntityTickAcceleration.tickAcceleration(rate)
    ↓
Mixin拦截 / Mixin Intercepts
    ↓
┌─────────────────────────────────────┐
│  MTEAdvAssLine特殊处理               │
│  - 设置加速状态标志                   │
│  - 拦截能量消耗                       │
│  - 调用updateEntity()               │
└─────────────────────────────────────┘
         OR
┌─────────────────────────────────────┐
│  其他GregTech机器                    │
│  - 直接操作mProgresstime            │
│  - 无需额外updateEntity()           │
│  - 无额外能耗                        │
└─────────────────────────────────────┘
         OR
┌─────────────────────────────────────┐
│  EnderIO机器                         │
│  - 强制lastUpdate=-1               │
│  - 缩放能量接收                      │
│  - 调用updateEntity()              │
└─────────────────────────────────────┘
```

### 配置选项 / Configuration

当前配置项：
- `enableAccelerateGregTechMachine` - 启用GregTech机器加速
- `accelerateGregTechMachineDiscount` - GregTech加速折扣系数（默认0.8）

### 未来可能的增强 / Potential Future Enhancements

如果需要，可以考虑迁移以下非加速功能（可选）：
1. `Modify_CapBankMaxIO_Mixin` - EnderIO电容银行IO增强
2. `ModifySomeConfigs` - 配置显示增强（NEI、WAILA）

但这些不是核心加速功能，可根据用户需求决定是否添加。

## 验证清单 / Verification Checklist

- [x] 检查所有GregTech加速mixin
- [x] 检查所有TecTech加速mixin
- [x] 检查所有EnderIO加速mixin
- [x] 确认没有遗漏的加速优化
- [x] 验证mixin系统正常工作
- [x] 确认无跳电问题
- [x] 测试各类机器加速效果

## 参考文件 / Reference Files

- NH-Utilities源码: https://github.com/Keriils/NH-Utilities
- NH-Utilities Mixins.java: `src/main/java/com/xir/NHUtilities/mixins/Mixins.java`
- Torcherino-GTNH Mixins.java: `src/main/java/com/czqwq/Torcherino/mixins/Mixins.java`
