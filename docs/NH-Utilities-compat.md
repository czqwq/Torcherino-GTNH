# Torcherino × NH-Utilities 共存分析 / Coexistence analysis

> 结论 / TL;DR: 两个 mod 的 mixin **不会互相崩溃**，可以同时安装运行。双方在 5 个目标类上有重复成员，
> Mixin 的裁决规则是「先应用者胜、后者被跳过并打一条 WARN」，接口则无条件合并，所以双方的功能都还能用。
> 唯一需要人工干预的地方是 EnderIO 的 `TileEntityEnder`（谁胜出取决于 late 配置的注册顺序），
> 已通过给 Torcherino 的 mixin 显式 `priority = 1100` 固定为 Torcherino 胜出。
>
> Both mods can run together: Mixin resolves the duplicated members by "first applied wins, the loser is
> skipped with a warning", and interfaces are merged unconditionally, so neither side loses functionality.
> The only arbitrary outcome (EnderIO `TileEntityEnder`) is now pinned via an explicit mixin priority.

- 对手仓库 / counterpart: `tmp/NH-Utilities-master`（Keriils/NH-Utilities，modId `NHUtilities`）
- 本方 mixin 清单 / ours: `src/main/resources/mixins.Torcherino.json`（early，随 jar manifest 在启动时注册）、
  `src/main/resources/mixins.Torcherino.late.json`（late，由 `LateMixinPlugin` 在 `LoaderState.CONSTRUCTING` 注册）
- 对手 mixin 清单 / theirs: `mixins.NHUtilities.json`(空) / `mixins.NHUtilities.early.json` / `mixins.NHUtilities.late.json`，
  实际列表由 `com.xir.NHUtilities.mixins.Mixins` 枚举 + `LateMixinPlugin` 动态生成

## 1. 目标类对照 / Target overlap

| 目标类 | NH-Utilities mixin | Torcherino mixin | 结论 |
| --- | --- | --- | --- |
| `gregtech.api.metatileentity.BaseMetaTileEntity` | `late/GregTech/BaseMetaTileEntityAcceleration_Mixin` | `early/GregTech/BaseMetaTileEntityAcceleration_Mixin`、`early/GregTech/BaseMetaTileEntityDeceleration_Mixin` | 重复成员：`tickAcceleration(I)Z` → **Torcherino 胜出（确定性）**；减速 mixin 无对手 |
| `ggfab.mte.MTEAdvAssLine` | `late/GregTech/MTEAdvAssLineAcceleration_Mixin` | `late/GregTech/MTEAdvAssLineAcceleration_Mixin` | 重复成员：`@Unique isAccelerationState` 字段 + 两个接口方法 + 同一处 `@Inject` → **字段合并为一个、行为一致** |
| `tectech.thing.metaTileEntity.multi.MTEResearchStation` | `late/TecTech/ResearchStationAcceleration_Mixin` | `late/TecTech/ResearchStationAcceleration_Mixin` | 重复成员：`tickAcceleration(I)Z`（两边代码逐字相同）→ 谁胜出都一样 |
| `com.enderio.core.common.TileEntityEnder` | `late/EnderIO/AccelerateTileEntity_Mixin` | `late/EnderIO/AccelerateTileEntity_Mixin` | 重复成员：`tickAcceleration(I)Z` + `getTickAcceleratedRate()I` → 已用 `priority` 固定 Torcherino 胜出 |
| `crazypants.enderio.machine.AbstractPoweredMachineEntity` | `late/EnderIO/AccelerateEnergyReceive_Mixin` | `late/EnderIO/AccelerateEnergyReceive_Mixin` | 同一个 `ICapacitor.getMaxEnergyReceived()` 调用点被两边 `@Redirect` → 先注入者胜出，二者等价 |
| `net.minecraft.world.World` | `early/MineCraft/WeatherCycRule_Mixin`（`updateWeather` HEAD） | `early/WorldTileEntityDeceleration_Mixin`（`updateEntities` 内 `TileEntity.updateEntity()` 重定向） | 方法不相交，**不冲突** |
| 其余 NHU mixin（DraconicEvolution、StructureLib、JABBA、ExU、WorldEdit、AE2、TecTech 无线配方/材质、GT NEI/配置/cover、`Entity`、`GameRules`） | — | — | 目标类与 Torcherino 完全不重叠，**不冲突** |

（其余 Torcherino mixin：CropsNH、ForestryMC、OmniOcular、`MTEEyeOfHarmony`/`MTETranscendentPlasmaMixer` 无线 EU、
`CommonMetaTileEntity` 左键 DataStick —— NHU 没有对应目标。）

## 2. Mixin 的裁决规则（实测于运行时 jar，非推测）

规则取自本机实际加载的运行时实现
`io.github.legacymoddingmc:unimixins:0.3.1` 内嵌的 Sponge Mixin 0.8.5-GTNH
（`tmp/_inspect/unimixins-0.3.1-dev`，用 `javap` 逐条核对字节码）：

1. **重复方法（非 `@Overwrite`）→ 先应用者胜。**
   `MixinApplicatorStandard.mergeMethod` → `isAlreadyMerged`：若目标里已存在的方法带有
   `@MixinMerged(priority = P)` 且 `P >= 当前 mixin 的 priority`，则跳过当前方法并打 WARN：
   `Method overwrite conflict for <name> in <target>, previously written by <mixin>. Skipping method.`
   否则把已有方法移除、换成自己的（即「后应用的 priority 更高者覆盖」）。
2. **重复 `@Unique` 字段 → 只保留第一个，两边共用同一个字段。**
   所有 mixin 的 `attach`（含 `@Unique` 改名判定）都在任何成员合并**之前**完成，所以互不可见、谁都不改名；
   之后 `mergeNewFields` 用 `findTargetField`（扫描目标的**实时**字段表）按 name+desc 查到已存在的同名字段，
   于是后者的字段被直接丢弃。`MTEAdvAssLine.isAccelerationState` 正属此列：两边的读取/写入落到同一个 boolean。
3. **同一指令上的两个 `@Redirect` → 先注入者胜，后者被跳过。**
   `RedirectInjector.preInject` 发现该指令已被别的 redirector 装饰（`redirector` decoration 的 owner 不是自己）时打 WARN：
   `@Redirect conflict. Skipping <info> with priority 1000, already redirected by <name> with priority 1000`，
   直接 `return false`（不抛异常）。目标查找发生在更早的 pass（`INJECT_PREPARE` 早于 `INJECT_APPLY`），
   所以败者并不是「找不到目标」；且 `InjectionInfo.requiredCallbackCount` 默认取
   `MixinConfig.InjectorOptions.defaultRequireValue`，该值默认 **0**（两个 mod 的 json 都没配 `injection.defaultRequire`），
   因此被跳过的注入不会触发 `postInject` 的 `Critical injection failure`。
4. **同一注入点上的两个 `@Inject` → 都生效**（handler 会被重命名为 `handler$<hash>$<原方法名>`）。
5. **接口无条件合并。** `applyInterfaces` 属于 MAIN pass，与成员去重无关：
   即使某一边的同名方法被跳过，两边的接口仍会加到目标类上。
   ⇒ NHU 的物品/时间加速器调用 **它自己的** `ITileEntityTickAcceleration.tickAcceleration(...)` 时，
   实际执行的是胜出方（Torcherino）的方法体；反之亦然。双方的 `instanceof` 判断也都成立。
6. **应用顺序** = `@Mixin(priority)`（默认 1000）升序，其次 `MixinInfo.order`（每解析一个 mixin 全局自增）。
   `mixins.Torcherino.json` 来自 jar manifest，在启动阶段（RFB-Main）就被解析；NHU 的 GT/EnderIO 加速 mixin 在
   `CONSTRUCTING` 阶段才注册。⇒ **early 的 Torcherino mixin 一定先应用，`BaseMetaTileEntity` 上的
   `tickAcceleration` 一定归 Torcherino。** 两个 late 配置之间（NHU vs Torcherino）的顺序由
   `LateMixinOrchestrationMixin` 遍历 ASMDataTable 的顺序决定，实际上是不可预期的。

### 运行时证据 / Evidence

`run/client/logs/fml-client-latest.log`（本仓库真实跑过的客户端）：

```
183:  [RFB-Main/DEBUG] Registering mixin config: mixins.Torcherino.json source=ContainerHandleURI(file:.../Torcherino-...-dev.jar)
1481: [RFB-Main/DEBUG] Preparing mixins.Torcherino.json (4)              # early 配置, 启动期
4688: [Client thread/INFO] [GTNHMixins/] Loading [mixins.Torcherino.late.json] TecTech.ResearchStationAcceleration_Mixin
5161: [Client thread/DEBUG] Mixing GregTech.BaseMetaTileEntityAcceleration_Mixin from mixins.Torcherino.json
                            into gregtech.api.metatileentity.BaseMetaTileEntity
5195: [Client thread/DEBUG] Mixing GregTech.BaseMetaTileEntityDeceleration_Mixin from mixins.Torcherino.json
                            into gregtech.api.metatileentity.BaseMetaTileEntity
```

`run/client/.mixin.out/java/` 导出的变换后源码也确认了合并结果：
`BaseMetaTileEntity implements ... ITileEntityTickAcceleration`（`tickAcceleration` 来自 Torcherino），
`MTEAdvAssLine implements ... IAccelerationState, IAdvAssLineInfo`，字段 `isAccelerationState` 只有一个，
`@Inject` handler 被重命名为 `handler$bhb000$Torcherino$modifyDrainEnergy`。

## 3. 每个冲突点的实际运行表现 / Runtime behaviour

### 3.1 GT 机器：`BaseMetaTileEntity`
- Torcherino 的 `tickAcceleration` 胜出（确定性）；NHU 的同名方法被跳过，启动日志出现一条
  `Method overwrite conflict for tickAcceleration ... Skipping method.`。
- NHU 的接口仍会挂到该类上，所以 **NHU 的加速火把 / 时间加速器照常工作**，只是执行 Torcherino 的语义。
- 因此以下差异是「预期」而不是 bug：
  - NHU 的 `accelerateGregTechMachineDiscount` 配置失效，改用 Torcherino 的
    `Config.accelerateGregTechMachineDiscount`（两边默认都是 0.8）；
  - Torcherino 的黑名单生效：GT 世界加速器 `MTEWorldAccelerator` 不再能被任何一方的加速器加速；
  - Torcherino 对 `MTEAdvAssLine` 的「不重复加进度」修正同样作用于 NHU 的加速器。
- NHU 配置 `enableAccelerateGregTechMachine = false` 时它根本不会加载该 mixin，不存在任何重复。

### 3.2 `MTEAdvAssLine`
- `isAccelerationState` 只有一份（后者的字段被丢弃），两边的 `@Inject` 处理器都保留，且读写的都是这一份；
  `getMachineAccelerationState()`/`setAccelerationState(boolean)` 两边实现逐字等价。
- ⇒ 无论哪一边的方法体胜出，「加速时取消 `baseEUt` 扣电」都成立，不会出现掉电/跳电。
- 不要为了让谁胜出去 `excludedMods`（见 §4）。

### 3.3 `MTEResearchStation`
- 两边 `tickAcceleration` 逐字相同（`computationRemaining -= rate * eAvailableData`），谁胜出都一样。
- 关键前提：**Torcherino 的这个 mixin 必须保持启用** —— 它是把
  `com.czqwq.Torcherino.api.interfaces.ITileEntityTickAcceleration` 挂到研究站上的唯一来源，
  而 Torcherino 胜出的 `BaseMetaTileEntity#tickAcceleration` 正是用这个接口判断「是否研究站」。

### 3.4 EnderIO `TileEntityEnder`
- 两边各加一对 `tickAcceleration`/`getTickAcceleratedRate`（字段名不同：`Torcherino$...` / `NHUtilities$...`）。
- 若 NHU 的简单实现胜出：EnderIO 机器仍会被加速，但 Torcherino 的**导管网络加速**
  （`AbstractConduitNetwork#doNetworkTick` 每个网络每 tick 只跑一次）会静默失效。
- 已用 `@Mixin(priority = 1100)`（Torcherino 更大 ⇒ 后应用 ⇒ 覆盖）固定为 Torcherino 胜出。

### 3.5 EnderIO `AbstractPoweredMachineEntity`
- 两个 `@Redirect` 指向同一条 `ICapacitor.getMaxEnergyReceived()` 调用，先注入者胜、后者被跳过并打 WARN。
- 两个 handler 逻辑等价，且都调用虚方法 `getTickAcceleratedRate()`：机器实体继承自 `TileEntityEnder`，
  该方法由 §3.4 中胜出的 Torcherino 实现提供，与 Torcherino 的 `tickAcceleration` 写入的字段一致。
  ⇒ 无论哪一个 redirect 存活，能量倍率都与加速倍率一致（不会出现「加速了但供不上电」）。

## 4. 不应该做的事 / What must NOT be done

**不要**因为检测到 NH-Utilities 就用 `Mixins.excludedMods` 把 Torcherino 的 GT/TecTech 辅助 mixin 关掉
（`dev` 分支上曾有一版这种做法：`TargetMod.NHUtilities` + `addExcludedMod`）：

1. 关掉 `MTEAdvAssLineAcceleration_Mixin` / `ResearchStationAcceleration_Mixin` 后，这两个类上就没有
   Torcherino 的 `IAccelerationState` / `ITileEntityTickAcceleration` 接口了；而
   `BaseMetaTileEntityAcceleration_Mixin` 是 early mixin、**总是**应用且总是胜出，它的分支判断依赖这些接口，
   于是会掉进兜底的 `multiBlockBase.mProgresstime = min(max, cur + rate*discount)` 分支 ——
   对 `MTEAdvAssLine`/研究站这类自己管理进度的机器就是「假进度」，
   正是 `dev` 分支 `fe4cae3 fix: stop double-applying acceleration to the Advanced Assembly Line` 修掉的问题。
2. `BaseMetaTileEntityAcceleration_Mixin`/`BaseMetaTileEntityDeceleration_Mixin` 在
   `mixins.Torcherino.json`（early，硬编码列表 + jar manifest）里，**在启动阶段就注册**，
   那一刻 `ILateMixinLoader.getMixins(Set<String> loadedMods)` 还没被调用，`excludedMods` 这类基于
   「已加载 mod 集合」的门控根本管不到 early 配置 —— 想排除也排除不掉。
3. 重复成员的代价只是一条 WARN；上面的「排除法」代价是功能静默损坏。收益为负。

## 5. 已落地的兼容改动 / Changes applied

| 文件 | 改动 |
| --- | --- |
| `mixins/late/EnderIO/AccelerateTileEntity_Mixin.java` | `@Mixin(..., priority = 1100)`：确定性赢得与 NHU 的重复成员，保住导管网络加速；javadoc 说明原因 |
| `mixins/late/EnderIO/AccelerateEnergyReceive_Mixin.java` | javadoc：说明与 NHU 的 `@Redirect` 冲突是安全的（等价 handler + 虚方法解析到 Torcherino 的 getter） |
| `mixins/early/GregTech/BaseMetaTileEntityAcceleration_Mixin.java` | javadoc：说明 early 配置必胜、NHU 侧只会被跳过一条 WARN、辅助 mixin 不得被排除 |
| `mixins/late/GregTech/MTEAdvAssLineAcceleration_Mixin.java` | javadoc：说明重复 `@Unique` 字段会被合并成同一份、双 `@Inject` 无害 |
| `mixins/late/TecTech/ResearchStationAcceleration_Mixin.java` | javadoc：说明两边方法体等价、该 mixin 必须保持启用 |
| `Torcherino.java` | 新增 `hasNHUtilities`（`Loader.isModLoaded("NHUtilities")`）并在检测到时打一条 INFO，向玩家/整合包作者解释启动日志里的跳过告警 |

未改动任何 mixin 的注册/排除逻辑，新增的只有「谁胜出」的确定性（priority）与文档。

## 6. 如何验证 / Verification checklist

1. 同时安装 Torcherino 与 NH Utilities，启动游戏，日志中应出现：
   - `Method overwrite conflict for tickAcceleration in ...previously written by com.xir.NHUtilities...Skipping method.`
     （GT `BaseMetaTileEntity`，以及 late 顺序落在后面的那一方对应的 ResearchStation/AdvAssLine/TileEntityEnder）
   - 可能的 `@Redirect conflict. Skipping ... already redirected by ...`（EnderIO 能量倍率）
   - 由本 mod 打的 `NH-Utilities detected: ...` INFO
   - **不应**出现 `Critical injection failure`、`ClassFormatError`、`Duplicate field name`、
     `Duplicate method name`、`AbstractMethodError`、`NoSuchMethodError`
2. 游戏内验证：
   - 用 Torcherino 火把加速 GT 单方块/多方块（含 `MTEAdvAssLine`、TecTech 研究站）——进度增长、EU/t 不翻倍；
   - 用 NHU 的时间加速器加速同一批机器——同样应工作（走 Torcherino 的方法体）；
   - EnderIO 机器与导管：物品/流体传输被加速，机器不掉电；
   - 减速火把（`Decelerate`）区域内的 GT 机器仍然「进度变慢但耗电正常」。
3. 复现 Mixin 裁决规则（无需游戏）：见 §2 提到的 `tmp/_inspect`（可用
   `%USERPROFILE%\.gradle\caches\modules-2\files-2.1\io.github.legacymoddingmc\unimixins\0.3.1\*\unimixins-0.3.1-dev.jar`
   重新解包 + `javap`）。

## 7. 参考 / References

- NH-Utilities 源码：`tmp/NH-Utilities-master/src/main/java/com/xir/NHUtilities/mixins/`
  （`Mixins.java`、`LateMixinPlugin.java`、`late/GregTech/BaseMetaTileEntityAcceleration_Mixin.java`、
  `late/GregTech/MTEAdvAssLineAcceleration_Mixin.java`、`late/TecTech/ResearchStationAcceleration_Mixin.java`、
  `late/EnderIO/AccelerateTileEntity_Mixin.java`、`late/EnderIO/AccelerateEnergyReceive_Mixin.java`）
- 上游仓库：<https://github.com/Keriils/NH-Utilities>
- Torcherino mixin：`src/main/java/com/czqwq/Torcherino/mixins/`、`src/main/resources/mixins.Torcherino*.json`
- Mixin 运行时：`io.github.legacymoddingmc:unimixins:0.3.1`（Sponge Mixin 0.8.5-GTNH）
