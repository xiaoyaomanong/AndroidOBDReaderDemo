package com.example.obdreader.config;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;
import com.github.pires.obd.commands.control.DtcNumberCommand;
import com.github.pires.obd.commands.control.EquivalentRatioCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.control.VinCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.OilTempCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.fuel.FuelTrimCommand;
import com.github.pires.obd.commands.fuel.WidebandAirFuelRatioCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.FuelTrim;

import java.util.ArrayList;

/**
 * TODO put description
 */
public final class ObdConfig {

    public static ArrayList<ObdCommand> getCommands() {
        ArrayList<ObdCommand> cmds = new ArrayList<>();

        // 操纵装置
        cmds.add(new ModuleVoltageCommand());//模块电压指令
        cmds.add(new EquivalentRatioCommand()); //当量比命令
        cmds.add(new DistanceMILOnCommand());//距离MILAN命令
        cmds.add(new DtcNumberCommand());//DTC号码命令
        cmds.add(new TimingAdvanceCommand());//定时提前命令
        cmds.add(new TroubleCodesCommand());//故障代码命令
        cmds.add(new VinCommand());//车辆识别码命令

        // 引擎
        cmds.add(new LoadCommand());//CD加载命令
        cmds.add(new RPMCommand());//RPM命令
        cmds.add(new RuntimeCommand());//运行命令
        cmds.add(new MassAirFlowCommand());//空气质量流量指令
        cmds.add(new ThrottlePositionCommand());//油门位置命令

        // 油箱
        cmds.add(new FindFuelTypeCommand());//查找燃料类型命令
        cmds.add(new ConsumptionRateCommand());//消耗率指令
        // cmds.add(new AverageFuelEconomyObdCommand());
        //cmds.add(new FuelEconomyCommand());
        cmds.add(new FuelLevelCommand());//油位指令
        //cmds.add(new FuelEconomyMAPObdCommand());
        // cmds.add(new FuelEconomyCommandedMAPObdCommand());
        cmds.add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_1));//燃油调节命令 长期燃油调节库1
        cmds.add(new FuelTrimCommand(FuelTrim.LONG_TERM_BANK_2));//燃油调节命令  长期燃油调节库2
        cmds.add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_1));//燃油调节命令 短期燃油调节库1
        cmds.add(new FuelTrimCommand(FuelTrim.SHORT_TERM_BANK_2));//燃油调节命令  短期燃油调节库2
        cmds.add(new AirFuelRatioCommand());//空燃比指令
        cmds.add(new WidebandAirFuelRatioCommand());//宽带空燃比命令
        cmds.add(new OilTempCommand());//油温指令
        // 压力
        cmds.add(new BarometricPressureCommand());//气压指令
        cmds.add(new FuelPressureCommand());//燃油压力指令
        cmds.add(new FuelRailPressureCommand());//燃油导轨压力指令
        cmds.add(new IntakeManifoldPressureCommand());//进气歧管压力指令
        // 温度
        cmds.add(new AirIntakeTemperatureCommand());//进气温度指令
        cmds.add(new AmbientAirTemperatureCommand());//环境温度指令
        cmds.add(new EngineCoolantTemperatureCommand());//发动机冷却液温度指令
        // Misc
        cmds.add(new SpeedCommand());//速度指令
        return cmds;
    }
}
